@echo off
setlocal

set JDK9=C:\Program Files\Java\jdk-9.0.4\bin
set SRC=src\main\java
set OUT=out

if not exist "%OUT%" mkdir "%OUT%"

echo Mencari file sumber...
dir /s /b "%SRC%\*.java" > sources.txt 2>nul

if not exist sources.txt (
    echo Tidak ada file .java ditemukan!
    exit /b 1
)

echo Mengkompilasi...
"%JDK9%\javac" --add-modules javafx.controls,javafx.fxml -d "%OUT%" @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [GAGAL] Kompilasi error!
    del sources.txt 2>nul
    pause
    exit /b 1
)

del sources.txt 2>nul
echo.
echo [OK] Kompilasi berhasil. File class ada di folder: %OUT%
