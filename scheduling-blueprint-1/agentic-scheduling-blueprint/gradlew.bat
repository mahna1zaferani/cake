@echo off
where gradle >NUL 2>&1 || goto no-gradle
cd /d "%~dp0"
gradle %*
goto :eof
:no-gradle
echo Gradle is not on PATH.
echo Open the project in Android Studio to build and run the app.
exit /b 1
