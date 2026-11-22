package uz.kuvonch.istigfor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Istig‘for sanagichi ilovasi.
 * Sayt (assets/index.html) telefonning ichida ochiladi, shuning uchun internetsiz ham ishlaydi.
 * Hisob telefon xotirasida (localStorage) saqlanadi va ilova yopilganda yo‘qolmaydi.
 */
public class MainActivity extends Activity {

    private WebView sahifa;

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
    protected void onDestroy() {
        if (sahifa != null) sahifa.destroy();
        super.onDestroy();
    }

    /** Sahifadagi JavaScript chaqira oladigan funksiyalar */
    public class Ilova {
        /** "Ekran o‘chmasin" sozlamasi: true bo‘lsa ekran o‘chmaydi */
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
    }
}
