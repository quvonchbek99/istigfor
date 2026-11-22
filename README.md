# Istig‘for sanagichi

Istig‘for duolarini sanash uchun sayt va Android ilovasi.

**Sayt:** https://quvonchbek99.github.io/istigfor/
**Android ilovasi:** [istigfor.apk](istigfor.apk) (internetsiz ishlaydi)

## Imkoniyatlar

- 33 donali tasbeh: odatiy maqsad 33, xohlasangiz 70, 100, 300, 1 000 yoki cheksiz.
- 5 ta istig‘for duosi: arabcha matni, o‘qilishi, ma’nosi va manbasi bilan.
- **Katta marra:** 40 000 (yoki 1 000, 10 000, 100 000) istig‘for sari yo‘l. Hisob kunma-kun saqlanib boradi, bajarilgan marralar ro‘yxatda qoladi.
- **Ovoz bilan sanash:** duoni aytganingizda telefon eshitib, o‘zi sanaydi. Ikki usul:
  - «Ovoz bo‘yicha» (odatiy) — ovoz balandligi bo‘yicha, internetsiz, har qanday brauzer va telefonda; har duodan keyin bir lahza to‘xtash kerak, sezgirlikni sozlash mumkin;
  - «So‘z bo‘yicha» — so‘zni tanib sanaydi (Chrome, Safari yoki telefonda Google xizmati va internet kerak).
- Bugungi hisob, davralar, ketma-ket kunlar, so‘nggi 7 kun grafigi.
- Tungi rejim, tebranish, ovoz, «Ekran o‘chmasin».

## Telefonga o‘rnatish

- **Android:** `istigfor.apk` ni yuklab oling va o‘rnating (telefon «noma’lum manbadan o‘rnatish»ga ruxsat so‘rasa, ruxsat bering).
- **iPhone:** saytni Safari’da oching va Share → Add to Home Screen ni tanlang.

## Kod qanday tuzilgan

Butun sayt bitta `index.html` faylda, har bir qismiga o‘zbekcha izoh yozilgan:

- **HTML**: sahifa tuzilishi (duo tugmalari, tasbeh doirasi, marra, hisob, sozlamalar).
- **CSS** (`<style>` ichida): ko‘rinish. Ranglar faylning boshidagi `:root` blokida.
- **JavaScript** (`<script>` ichida): mantiq.
  - `ZIKRLAR`: duolar ro‘yxati. Yangi duo qo‘shish uchun shu ro‘yxatga yangi blok qo‘shing.
  - `MARRALAR`: katta marra hajmlari (odatda 40 000).
  - `sanash()`: har bosishda +1; davra va marra to‘lishini tekshiradi.
  - `marraniTekshir()`: marra to‘lsa, uni «Bajarilgan marralar»ga yozib, yangisini boshlaydi.
  - `OVOZ_NAQSHLARI`: ovoz bilan sanashda har bir duodan nima eshitilganda sanalishi.
  - `ovozniBoshla()`: tanlangan usulda tinglashni boshlaydi.
  - `darajaKeldi()`: «Ovoz bo‘yicha» — har 25 ms da ovoz balandligini tekshirib, gap boshlanishi va tugashini aniqlaydi.
  - `eshitilganMatn()`: «So‘z bo‘yicha» — tanilgan matndagi istig‘forlarni sanaydi.
  - `saqlash()` / `yuklash()`: hisobni telefon/brauzer xotirasiga (localStorage) yozadi va o‘qiydi.
  - `chizish()`: ekrandagi son, donalar, marra va grafikni yangilaydi.

Boshqa fayllar:

- `manifest.webmanifest`, `sw.js`, `ikonka-*.png` — saytni telefonga ilova kabi o‘rnatish va internetsiz ochish uchun.
- `ilova/` — Android ilovasining kodi:
  - `AndroidManifest.xml` — ilova nomi, belgisi va ruxsatlari;
  - `src/uz/kuvonch/istigfor/MainActivity.java` — saytni telefon ichida ochuvchi oyna (WebView) va ovozni tanish;
  - `res/` — belgilar va ranglar, `ikonkalar.py` — belgilarni chizadi;
  - `qurish.sh` — APK yig‘ish skripti. Imzo kaliti (`istigfor-kalit.jks`) repoda yo‘q, u alohida saqlanadi.

Hisob foydalanuvchining o‘z telefonida saqlanadi va hech qayerga yuborilmaydi.

## Manbalar

Qur’oni karim, Nuh surasi, 10–12-oyatlar. Hadislar: Imom Buxoriy (6306, 6307), Imom Muslim (591, 2702), Abu Dovud (1516, 1517), Termiziy (3434, 3577), Ibn Moja (3818). O‘zbekcha ma’nolar sodda tilda berilgan.
