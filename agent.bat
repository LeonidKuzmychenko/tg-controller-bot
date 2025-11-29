@echo off
setlocal

echo ==========================================
echo      RUN SPRING BOOT WITH GRAAL AGENT
echo ==========================================

REM ==== НАСТРОЙКИ (менять здесь!) ========================

REM Путь к JAVA (можешь указать путь к GraalVM java.exe)
set "JAVA_BIN=C:\Java\graalvm-jdk-25.0.1+8.1\bin\java.exe"

REM Путь к JAR файлу твоего бота
set "APP_JAR=C:\IntelijIdeaProjects\learn-tg-bot\build\libs\tg-controller-bot-0.0.1-SNAPSHOT.jar"

REM Папка куда агент сохранит конфиги
set "OUT_DIR=C:\IntelijIdeaProjects\learn-tg-bot\src\main\resources\META-INF\native-image\lk.tech\tg-controller-bot"

REM ========================================

echo JAVA:    %JAVA_BIN%
echo APP:     %APP_JAR%
echo OUT DIR: %OUT_DIR%

if not exist "%JAVA_BIN%" (
    echo ERROR: Java not found at %JAVA_BIN%
    pause
    exit /b
)

if not exist "%APP_JAR%" (
    echo ERROR: JAR not found: %APP_JAR%
    pause
    exit /b
)

echo.
echo Running app with GraalVM agent...
echo.

"%JAVA_BIN%" ^
  -agentlib:native-image-agent=config-output-dir=%OUT_DIR% ^
  -jar "%APP_JAR%"

echo.
echo Agent finished. Configs generated in: %OUT_DIR%
pause
