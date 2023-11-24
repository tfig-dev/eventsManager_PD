@echo off

echo Deleting compiled files
start cmd /c deleteFiles.bat
timeout /t 2 /nobreak >nul
taskkill /F /IM cmd.exe /FI "WINDOWTITLE eq deleteFiles.bat"

echo Compiling...
start cmd /c compile.bat
timeout /t 2 /nobreak >nul
taskkill /F /IM cmd.exe /FI "WINDOWTITLE eq compile.bat"

echo Running Server
start cmd /c initializeSERVER.bat
timeout /t 1 /nobreak >nul

echo Running Observer
start cmd /c initializeOBSERVER.bat
timeout /t 1 /nobreak >nul

echo Running Observer_2
start cmd /c initializeOBSERVER_2.bat
timeout /t 1 /nobreak >nul

echo Running Client
start cmd /c initializeCLIENT.bat
timeout /t 1 /nobreak >nul
