@echo off
setlocal

set JDK9=C:\Program Files\Java\jdk-9.0.4\bin
set OUT=out
set RESOURCES=src\main\resources
set MAIN=com.grincare.MainApp

if not exist "%OUT%\com\grincare\MainApp.class" (
    echo Belum dikompilasi. Jalankan build.bat terlebih dahulu.
    pause
    exit /b 1
)

echo Menjalankan GrinCare...
"%JDK9%\java" --add-modules javafx.controls,javafx.fxml -cp "%OUT%;%RESOURCES%" %MAIN%
