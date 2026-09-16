@echo off
cd /d "%~dp0"
javac -d bin -encoding UTF-8 src\StudentManagerSystem\*.java
jar -cvfm 学生管理系统1.0.jar manifest.txt -C bin .
echo 编译打包完成！
pause