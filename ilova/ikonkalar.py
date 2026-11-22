# Ilova va sayt belgilarini (ikonkalarini) chizadi:
# yashil fonda oq sakkiz qirrali yulduz (saytdagi belgi bilan bir xil).
# Ishlatish:  python3 ikonkalar.py   (ilova papkasining ichida)
import math
from pathlib import Path
from PIL import Image, ImageDraw

YASHIL = (0, 122, 94, 255)   # #007a5e — saytning asosiy rangi
OQ = (255, 255, 255, 255)


def belgi(olcham, toliq_fon=False):
    """olcham — piksel; toliq_fon=True bo‘lsa burchaklar yumaloqlanmaydi (maskable ikonka uchun)"""
    S = 1024                                   # avval katta chizib, keyin kichraytiramiz (silliq chiqadi)
    rasm = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    q = ImageDraw.Draw(rasm)
    if toliq_fon:
        q.rectangle([0, 0, S, S], fill=YASHIL)
    else:
        q.rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=YASHIL)

    markaz = S / 2
    yarim_tomon = S * (0.20 if toliq_fon else 0.25)   # maskable'da yulduz kichikroq (xavfsiz hudud)
    qalinlik = int(S * 0.045)
    for burilish in (0, 45):                           # ikki kvadrat: oddiy va 45° burilgan
        uchlar = []
        for i in range(4):
            a = math.radians(burilish + 45 + 90 * i)
            r = yarim_tomon * math.sqrt(2)
            uchlar.append((markaz + r * math.cos(a), markaz + r * math.sin(a)))
        q.line(uchlar + [uchlar[0]], fill=OQ, width=qalinlik, joint="curve")
    nuqta = S * 0.055
    q.ellipse([markaz - nuqta, markaz - nuqta, markaz + nuqta, markaz + nuqta], fill=OQ)
    return rasm.resize((olcham, olcham), Image.LANCZOS)


papka = Path(__file__).resolve().parent

# Android ilova belgilari (har xil ekran zichligi uchun)
for nom, olcham in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
    joy = papka / "res" / f"mipmap-{nom}"
    joy.mkdir(parents=True, exist_ok=True)
    belgi(olcham).save(joy / "ic_launcher.png")

# Sayt belgilari (telefonga o‘rnatilganda ko‘rinadi)
belgi(192).save(papka.parent / "ikonka-192.png")
belgi(512).save(papka.parent / "ikonka-512.png")
belgi(512, toliq_fon=True).save(papka.parent / "ikonka-maskable-512.png")
print("Belgilar tayyor")
