@echo off
echo Deleting temporary files...

rmdir /S /Q ..\out
cd ../src/pt/isec/brago/eventsManager/datafiles
for %%i in (*) do if exist "%%i" del /Q "%%i"
for /D %%i in (*) do if exist "%%i" rmdir /S /Q "%%i"