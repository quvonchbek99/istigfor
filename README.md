# Istig‘for sanagichi

Istig‘for duolarini sanash uchun oddiy sayt: 33 donali tasbeh, 5 ta istig‘for duosi (arabcha matni, o‘qilishi, ma’nosi va manbasi bilan), maqsad tanlash, bugungi hisob, davralar va so‘nggi 7 kun grafigi.

**Sayt:** https://quvonchbek99.github.io/istigfor/

## Kod qanday tuzilgan

Butun sayt bitta `index.html` faylda, har bir qismiga o‘zbekcha izoh yozilgan:

- **HTML**: sahifa tuzilishi (duo tugmalari, tasbeh doirasi, hisob, sozlamalar).
- **CSS** (`<style>` ichida): ko‘rinish. Ranglar faylning boshidagi `:root` blokida.
- **JavaScript** (`<script>` ichida): mantiq.
  - `ZIKRLAR`: duolar ro‘yxati. Yangi duo qo‘shish uchun shu ro‘yxatga yangi blok qo‘shing.
  - `sanash()`: har bosishda +1, maqsadga yetganda davra yopiladi.
  - `saqlash()` / `yuklash()`: hisobni brauzer xotirasiga (localStorage) yozadi va o‘qiydi.
  - `chizish()`: ekrandagi son, donalar va grafikni yangilaydi.

Hisob foydalanuvchining o‘z brauzerida saqlanadi va hech qayerga yuborilmaydi.

## Manbalar

Qur’oni karim, Nuh surasi, 10–12-oyatlar. Hadislar: Imom Buxoriy (6306, 6307), Imom Muslim (591, 2702), Abu Dovud (1516, 1517), Termiziy (3434, 3577), Ibn Moja (3818). O‘zbekcha ma’nolar sodda tilda berilgan.
