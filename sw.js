/* Service worker: saytni telefonga o‘rnatilgan ilova kabi internetsiz ham ochadi.
   Har safar avval internetdan yangi nusxa olinadi va xotiraga (keshga) yoziladi;
   internet bo‘lmasa, xotiradagi nusxa ko‘rsatiladi. */
const KESH = "istigfor-v1";
const ASOSIY_FAYLLAR = ["./", "index.html", "manifest.webmanifest", "ikonka-192.png", "ikonka-512.png"];

// O‘rnatilganda: asosiy fayllarni oldindan saqlab qo‘yish
self.addEventListener("install", (hodisa) => {
  hodisa.waitUntil(caches.open(KESH).then((kesh) => kesh.addAll(ASOSIY_FAYLLAR)));
  self.skipWaiting();
});

// Yangi versiya ishga tushganda: eski keshlarni o‘chirish
self.addEventListener("activate", (hodisa) => {
  hodisa.waitUntil(
    caches.keys().then((nomlar) => Promise.all(nomlar.filter((n) => n !== KESH).map((n) => caches.delete(n))))
  );
  self.clients.claim();
});

// Har bir so‘rov: avval internet, bo‘lmasa xotira
self.addEventListener("fetch", (hodisa) => {
  if (hodisa.request.method !== "GET") return;
  hodisa.respondWith(
    fetch(hodisa.request)
      .then((javob) => {
        const nusxa = javob.clone();
        caches.open(KESH).then((kesh) => kesh.put(hodisa.request, nusxa));
        return javob;
      })
      .catch(() => caches.match(hodisa.request).then((saqlangan) => saqlangan || caches.match("index.html")))
  );
});
