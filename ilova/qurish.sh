#!/bin/bash
# Istig‘for ilovasini (APK) yig‘ish skripti.
# Kerakli dasturlar (Ubuntu):
#   sudo apt install aapt apksigner zipalign dalvik-exchange android-sdk-platform-23 default-jdk
# Ishlatish (ilova papkasida):
#   KALIT_PAROL=parol KALIT_FAYL=/yo‘l/istigfor-kalit.jks ./qurish.sh
# Natija: repo ildizida istigfor.apk
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KALIT_FAYL="${KALIT_FAYL:-istigfor-kalit.jks}"

# 1) Saytning o‘zi ilova ichiga joylanadi (internetsiz ishlashi uchun)
mkdir -p assets
cp ../index.html assets/index.html

rm -rf build gen obj
mkdir -p build gen obj

# 2) Manifest, belgilar va matnlarni yig‘ish (resources.arsc siqilmagan holda qoladi)
aapt package -f -m -J gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/ilova.apk -0 arsc

# 3) Java kodini kompilyatsiya qilib, Android formatiga (classes.dex) o‘tkazish
javac --release 8 -encoding UTF-8 -nowarn -classpath "$JAR" -d obj $(find src gen -name '*.java')
dalvik-exchange --dex --output=build/classes.dex obj
(cd build && aapt add ilova.apk classes.dex > /dev/null)

# 4) Tekislash va imzolash. Imzo kalitini (istigfor-kalit.jks) yo‘qotmang:
#    ilovaning keyingi versiyalari faqat shu kalit bilan imzolansa, eski ilova ustiga o‘rnatiladi.
zipalign -f -p 4 build/ilova.apk build/ilova-tekis.apk
apksigner sign --ks "$KALIT_FAYL" --ks-key-alias istigfor \
  --ks-pass env:KALIT_PAROL --key-pass env:KALIT_PAROL \
  --out ../istigfor.apk build/ilova-tekis.apk
apksigner verify ../istigfor.apk
echo "Tayyor: istigfor.apk"
