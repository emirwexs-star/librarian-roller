@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo  Librarian Roller - Mod Derleme Araci
echo ========================================================
echo.

:: 1. Java Kontrolu
echo [*] Java kontrol ediliyor...
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [HATA] Sisteminizde Java bulunamadi!
    echo Lutfen su adresten Java 21 JDK indirin ve kurun:
    echo https://adoptium.net/temurin/releases/?version=21
    echo.
    pause
    exit /b 1
)

:: 2. Derleme Islemi
echo.
echo [*] Gradle ve Minecraft kutuphaneleri indiriliyor...
echo [*] Bu islem ilk seferde internet hizina bagli olarak birkac dakika surebilir.
echo.

call gradlew.bat build --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo  [TEBRIKLER] Derleme basariyla tamamlandi!
    echo  .jar dosyaniz burada hazir:
    echo  build\libs\librarian-roller-1.0.0.jar
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo  [HATA] Derleme tamamlanamadi.
    echo  Lutfen internet baglantinizi kontrol edip tekrar deneyin.
    echo ========================================================
)

pause
