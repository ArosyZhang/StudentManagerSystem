@echo off
chcp 65001 > nul
cd /d "%~dp0"

echo 正在编译...
javac -d bin -encoding UTF-8 src\StudentManagerSystem\*.java
if errorlevel 1 (
    echo.
    echo 编译失败，请检查上面的错误信息。
    pause
    exit /b 1
)

echo 正在打包...
jar -cvfm StudentManagerSystem-1.0.jar manifest.txt -C bin . > nul
if errorlevel 1 (
    echo.
    echo 打包失败，请检查上面的错误信息。
    pause
    exit /b 1
)

echo.
echo 编译打包完成：StudentManagerSystem-1.0.jar
echo 运行方式：java -jar StudentManagerSystem-1.0.jar
pause