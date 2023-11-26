@echo off

echo Deleting compiled files...
start /wait cmd /c deleteFiles.bat
taskkill /F /IM cmd.exe /FI "WINDOWTITLE eq deleteFiles.bat"

echo Compiling...
start /wait cmd /c compile.bat
taskkill /F /IM cmd.exe /FI "WINDOWTITLE eq compile.bat"

echo Running Server
start cmd /c initializeSERVER.bat
timeout /t 2 /nobreak >nul

echo Running Observer
start cmd /c initializeOBSERVER.bat

echo Running Observer_2
start cmd /c initializeOBSERVER_2.bat

echo Running Client
start cmd /c initializeCLIENT.bat