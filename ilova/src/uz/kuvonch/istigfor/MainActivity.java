package uz.kuvonch.istigfor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Istig‘for sanagichi ilovasi.
 * Sayt (assets/index.html) telefonning ichida ochiladi, shuning uchun internetsiz ham ishlaydi.
 * Hisob telefon xotirasida (localStorage) saqlanadi va ilova yopilganda yo‘qolmaydi.
 *
 * Ovoz bilan sanashning ikki usuli:
 *  1) «Ovoz bo‘yicha»: mikrofon ovoz balandligini o‘lchab, sahifaga yuboradi (internetsiz ishlaydi);
 *  2) «So‘z bo‘yicha»: telefonning nutqni tanish xizmati gapni matnga aylantiradi.
 */
public class MainActivity extends Activity {

    private static final int MIKROFON_SOROVI = 71;   // mikrofon ruxsati so‘rovining raqami

    private WebView sahifa;
    private final Handler kutuvchi = new Handler(Looper.getMainLooper());

    // «So‘z bo‘yicha» usuli
    private SpeechRecognizer tinglovchi;             // telefonning nutqni tanish xizmati
    private boolean tinglash = false;                // so‘z bo‘yicha tinglash yoqiqmi
    private String tinglashTili = "ar-SA";           // istig‘for arabcha aytiladi
    private int xatolarKetmaKet = 0;                 // ketma-ket xatolar (cheksiz aylanib qolmaslik uchun)

    // «Ovoz bo‘yicha» usuli
    private AudioRecord mikrofon;                    // mikrofondan ovoz yozib oluvchi
    private Thread mikIshchisi;                      // mikrofonni orqada o‘qiydigan oqim
    private volatile boolean mikIshlayapti = false;
    private boolean mikKutilmoqda = false;           // ruxsat so‘ralgan, berilsa mikrofonni yoqish kerak

    @Override
    protected void onCreate(Bundle saqlanganHolat) {
        super.onCreate(saqlanganHolat);
        sahifa = new WebView(this);

        WebSettings sozlama = sahifa.getSettings();
        sozlama.setJavaScriptEnabled(true);              // sayt JavaScript bilan ishlaydi
        sozlama.setDomStorageEnabled(true);              // localStorage: hisob saqlanishi uchun
        sozlama.setTextZoom(100);                        // telefondagi shrift o‘lchami sahifani buzmasin
        sozlama.setMediaPlaybackRequiresUserGesture(false);

        // Sahifadagi JavaScript ilovaga murojaat qila olishi uchun ko‘prik: window.Ilova
        sahifa.addJavascriptInterface(new Ilova(), "Ilova");
        sahifa.setWebChromeClient(new WebChromeClient());
        sahifa.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String manzil) {
                if (manzil.startsWith("file:///android_asset/")) return false;
                try {
                    // Tashqi havolalar telefonning oddiy brauzerida ochiladi
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(manzil)));
                } catch (Exception xato) {
                    // brauzer topilmasa, hech narsa qilmaymiz
                }
                return true;
            }
        });

        // Ekran burilganda va hokazoda sahifa holati tiklanadi, aks holda sayt ochiladi
        if (saqlanganHolat == null || sahifa.restoreState(saqlanganHolat) == null) {
            sahifa.loadUrl("file:///android_asset/index.html");
        }
        setContentView(sahifa);
    }

    @Override
    protected void onSaveInstanceState(Bundle holat) {
        super.onSaveInstanceState(holat);
        sahifa.saveState(holat);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ilova orqa fonga o‘tsa, mikrofon yopiladi va sahifaga xabar beriladi
        if (tinglash || mikIshlayapti) {
            tinglashniToxtat();
            mikniToxtat();
            sahifagaAyt("ovozToxtadi()");
        }
    }

    @Override
    protected void onDestroy() {
        tinglashniToxtat();
        mikniToxtat();
        if (sahifa != null) sahifa.destroy();
        super.onDestroy();
    }

    /** Sahifadagi JavaScript kodini bajaradi */
    private void sahifagaAyt(String kod) {
        if (sahifa != null) sahifa.evaluateJavascript(kod, null);
    }

    /** Mikrofonga ruxsat bormi; yo‘q bo‘lsa so‘raydi va false qaytaradi */
    private boolean mikrofonRuxsati() {
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIKROFON_SOROVI);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int kod, String[] ruxsatlar, int[] natijalar) {
        if (kod != MIKROFON_SOROVI) return;
        if (natijalar.length > 0 && natijalar[0] == PackageManager.PERMISSION_GRANTED) {
            if (mikKutilmoqda) mikniBoshla();
            if (tinglash) tinglashniBoshla();
        } else {
            mikKutilmoqda = false;
            tinglash = false;
            sahifagaAyt("ovozXatosi('ruxsat')");
        }
    }

    // ------------------------------------------------------------------
    // 1-USUL: OVOZ BO‘YICHA — mikrofon ovoz balandligini sahifaga yuboradi
    // ------------------------------------------------------------------

    private void mikniBoshla() {
        if (mikIshlayapti) return;
        mikKutilmoqda = true;
        if (!mikrofonRuxsati()) return;              // ruxsat berilsa, onRequestPermissionsResult qayta chaqiradi
        mikKutilmoqda = false;

        final int chastota = 16000;                  // sekundiga 16 000 o‘lchov
        int minBufer = AudioRecord.getMinBufferSize(chastota, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufer <= 0) {
            sahifagaAyt("ovozXatosi('mikYoq')");
            return;
        }
        try {
            mikrofon = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, chastota,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(minBufer, chastota));
            if (mikrofon.getState() != AudioRecord.STATE_INITIALIZED) throw new IllegalStateException("mikrofon");
            mikrofon.startRecording();
        } catch (Exception xato) {
            mikniToxtat();
            sahifagaAyt("ovozXatosi('mikYoq')");
            return;
        }

        mikIshlayapti = true;
        final AudioRecord yozgich = mikrofon;
        mikIshchisi = new Thread(new Runnable() {
            @Override
            public void run() {
                short[] kadr = new short[chastota / 40];     // 25 ms lik bo‘lak
                StringBuilder toplam = new StringBuilder();
                int soni = 0;
                while (mikIshlayapti) {
                    int oqildi = yozgich.read(kadr, 0, kadr.length);
                    if (oqildi <= 0) continue;
                    double yigindi = 0;
                    for (int i = 0; i < oqildi; i++) {
                        double q = kadr[i] / 32768.0;
                        yigindi += q * q;
                    }
                    double db = 10 * Math.log10(yigindi / oqildi + 1e-12);   // ovoz balandligi, dB
                    if (soni > 0) toplam.append(',');
                    toplam.append(Math.round(db * 10) / 10.0);
                    if (++soni >= 4) {                       // har 100 ms da sahifaga yuboramiz
                        final String royxat = "[" + toplam + "]";
                        toplam.setLength(0);
                        soni = 0;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mikIshlayapti) sahifagaAyt("window.ovozDarajasi && window.ovozDarajasi(" + royxat + ")");
                            }
                        });
                    }
                }
            }
        });
        mikIshchisi.start();
    }

    private void mikniToxtat() {
        mikIshlayapti = false;
        mikKutilmoqda = false;
        if (mikIshchisi != null) {
            try {
                mikIshchisi.join(300);
            } catch (InterruptedException xato) {
                // kutish to‘xtatildi
            }
            mikIshchisi = null;
        }
        if (mikrofon != null) {
            try {
                mikrofon.stop();
            } catch (Exception xato) {
                // allaqachon to‘xtagan
            }
            mikrofon.release();
            mikrofon = null;
        }
    }

    // ------------------------------------------------------------------
    // 2-USUL: SO‘Z BO‘YICHA — telefonning nutqni tanish xizmati
    // ------------------------------------------------------------------

    /** Tinglashni boshlaydi (kerak bo‘lsa, avval mikrofonga ruxsat so‘raydi) */
    private void tinglashniBoshla() {
        if (!tinglash) return;
        if (!mikrofonRuxsati()) return;
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            tinglash = false;
            sahifagaAyt("ovozXatosi('yoq')");
            return;
        }
        if (tinglovchi == null) {
            tinglovchi = SpeechRecognizer.createSpeechRecognizer(this);
            tinglovchi.setRecognitionListener(new Tinglovchi());
        }
        Intent niyat = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        niyat.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        niyat.putExtra(RecognizerIntent.EXTRA_LANGUAGE, tinglashTili);
        niyat.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);   // gap tugashini kutmasdan sanash
        niyat.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        niyat.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        try {
            tinglovchi.startListening(niyat);
        } catch (Exception xato) {
            qaytaBoshla(600);
        }
    }

    /** Bir oz kutib, tinglashni qayta boshlaydi (xizmat har gapdan keyin to‘xtaydi) */
    private void qaytaBoshla(long kechikish) {
        kutuvchi.postDelayed(new Runnable() {
            @Override
            public void run() {
                tinglashniBoshla();
            }
        }, kechikish);
    }

    private void tinglashniToxtat() {
        tinglash = false;
        kutuvchi.removeCallbacksAndMessages(null);
        if (tinglovchi != null) {
            try {
                tinglovchi.cancel();
                tinglovchi.destroy();
            } catch (Exception xato) {
                // allaqachon yopilgan
            }
            tinglovchi = null;
        }
    }

    /** Nutqni tanish xizmatidan keladigan natijalar */
    private class Tinglovchi implements RecognitionListener {
        @Override public void onReadyForSpeech(Bundle p) { }
        @Override public void onBeginningOfSpeech() { }
        @Override public void onRmsChanged(float d) { }
        @Override public void onBufferReceived(byte[] b) { }
        @Override public void onEndOfSpeech() { }
        @Override public void onEvent(int turi, Bundle p) { }

        @Override
        public void onPartialResults(Bundle natija) {
            sahifagaYubor(natija, false);
        }

        @Override
        public void onResults(Bundle natija) {
            xatolarKetmaKet = 0;
            sahifagaYubor(natija, true);
            if (tinglash) qaytaBoshla(100);
        }

        @Override
        public void onError(int xato) {
            if (!tinglash) return;
            if (xato == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                tinglashniToxtat();
                sahifagaAyt("ovozXatosi('ruxsat')");
            } else if (xato == SpeechRecognizer.ERROR_NETWORK || xato == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                tinglashniToxtat();
                sahifagaAyt("ovozXatosi('internet','kod " + xato + "')");
            } else if (xato == 12 || xato == 13) {      // til qo‘llanmaydi / til paketi yo‘q (Android 12+)
                tinglashniToxtat();
                sahifagaAyt("ovozXatosi('til','kod " + xato + "')");
            } else if (++xatolarKetmaKet > 12) {          // juda ko‘p ketma-ket xato: to‘xtatamiz
                tinglashniToxtat();
                sahifagaAyt("ovozXatosi('yoq','kod " + xato + "')");
            } else {
                // Jimlik yoki tanilmagan gap: tinglash davom etadi
                if (xato == SpeechRecognizer.ERROR_RECOGNIZER_BUSY && tinglovchi != null) {
                    tinglovchi.destroy();
                    tinglovchi = null;
                }
                qaytaBoshla(xato == SpeechRecognizer.ERROR_NO_MATCH || xato == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ? 150 : 600);
            }
        }

        private void sahifagaYubor(Bundle natija, boolean yakuniy) {
            ArrayList<String> matnlar = natija.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matnlar == null || matnlar.isEmpty()) return;
            if (!yakuniy) xatolarKetmaKet = 0;
            sahifagaAyt("window.ovozdanKeldi && window.ovozdanKeldi(" + JSONObject.quote(matnlar.get(0)) + "," + yakuniy + ")");
        }
    }

    /** Sahifadagi JavaScript chaqira oladigan funksiyalar (window.Ilova) */
    public class Ilova {
        /** "Ekran o‘chmasin": true bo‘lsa ekran o‘chmaydi */
        @JavascriptInterface
        public void ekranniYoqiqTut(final boolean yoqilsin) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (yoqilsin) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }

        /** «Ovoz bo‘yicha»: mikrofonni yoqadi */
        @JavascriptInterface
        public void mikniBoshla() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.mikniBoshla();
                }
            });
        }

        /** «Ovoz bo‘yicha»: mikrofonni o‘chiradi */
        @JavascriptInterface
        public void mikniToxtat() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.mikniToxtat();
                }
            });
        }

        /** «So‘z bo‘yicha»: telefonda nutqni tanish xizmati bormi */
        @JavascriptInterface
        public boolean ovozBormi() {
            return SpeechRecognizer.isRecognitionAvailable(MainActivity.this);
        }

        /** «So‘z bo‘yicha»: tinglashni boshlaydi */
        @JavascriptInterface
        public void ovozniBoshla(final String til) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (til != null && til.length() > 0) tinglashTili = til;
                    tinglash = true;
                    xatolarKetmaKet = 0;
                    tinglashniBoshla();
                }
            });
        }

        /** «So‘z bo‘yicha»: tinglashni to‘xtatadi */
        @JavascriptInterface
        public void ovozniToxtat() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tinglashniToxtat();
                }
            });
        }
    }
}
