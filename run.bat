@echo off
echo ===================================================
echo   Compiling Ninja Sky Jump (Java Swing)
echo ===================================================
if not exist bin mkdir bin

javac -d bin src/game/*.java

if %errorlevel% neq 0 (
    echo [ERROR] Kompilasi gagal!
    echo Pastikan JDK (Java Development Kit) sudah terinstal dan terdaftar di PATH environment variable.
    pause
    exit /b %errorlevel%
)

echo ===================================================
echo   Running Ninja Sky Jump...
echo ===================================================
java -cp bin game.Main
pause
