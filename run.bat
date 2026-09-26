@echo off
cd /d "%~dp0"

rem ASCII-only on purpose: a .bat containing Chinese text would need "chcp 65001",
rem and running chcp breaks the console input that the Java program below waits for.

echo Compiling...
javac -d bin -encoding UTF-8 src\StudentManagerSystem\*.java
if errorlevel 1 (
    echo.
    echo Compile failed. Please check the error messages above.
    pause
    exit /b 1
)

echo.
java -cp bin StudentManagerSystem.StudentManagerSystem

echo.
pause
