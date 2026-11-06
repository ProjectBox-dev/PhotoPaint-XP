@echo off
cd /d "%~dp0"
if exist build\classes (
    java -cp build\classes photopaint.Main
) else (
    echo Run build.ps1 first.
    pause
)
