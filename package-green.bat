@echo off
chcp 65001 > nul
cd /d "%~dp0"

set "VERSION=1.0.0"
set "JAR=StudentManagerSystem-%VERSION%.jar"
set "DIST=StudentManagerSystem-%VERSION%-win"

rem 从 PATH 里反查 JDK 根目录：jlink 和 javac 装在同一个 bin 下
for %%i in (javac.exe) do set "JAVAC=%%~$PATH:i"
if not defined JAVAC (
    echo 找不到 javac，请确认已安装 JDK 17 并加入 PATH。
    pause
    exit /b 1
)
for %%i in ("%JAVAC%") do set "JDK=%%~dpi.."

echo [1/4] 编译源码...
if exist bin rmdir /s /q bin
javac -d bin -encoding UTF-8 src\StudentManagerSystem\*.java
if errorlevel 1 (
    echo.
    echo 编译失败，请检查上面的错误信息。
    pause
    exit /b 1
)

echo [2/4] 打包 jar...
jar -cfm "%JAR%" manifest.txt -C bin .
if errorlevel 1 (
    echo.
    echo 打包失败，请检查上面的错误信息。
    pause
    exit /b 1
)

echo [3/4] 生成精简 JRE，约 27 MB，需要十几秒...
if exist "%DIST%" rmdir /s /q "%DIST%"
"%JDK%\bin\jlink.exe" --add-modules java.base --output "%DIST%\jre" --strip-debug --no-header-files --no-man-pages --compress=2
if errorlevel 1 (
    echo.
    echo 生成 JRE 失败，请检查上面的错误信息。
    pause
    exit /b 1
)

echo [4/4] 组装文件并压缩...
copy /y "%JAR%" "%DIST%\" > nul
if exist data xcopy /e /i /y data "%DIST%\data" > nul
copy /y packaging\start.bat "%DIST%\启动.bat" > nul
copy /y packaging\使用说明.txt "%DIST%\使用说明.txt" > nul
powershell -NoProfile -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; [System.IO.Compression.ZipFile]::CreateFromDirectory('%DIST%','%DIST%.zip',[System.IO.Compression.CompressionLevel]::Optimal,$true)"
if errorlevel 1 (
    echo.
    echo 压缩失败，请检查上面的错误信息。
    pause
    exit /b 1
)

echo.
echo 完成：%DIST%.zip
echo 这就是绿色版：解压后双击「启动.bat」即可运行，不需要装 Java。
pause
