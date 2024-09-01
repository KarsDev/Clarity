@echo off
:: Check if the script is running as administrator
:: If not, relaunch it with admin privileges

:: Get the current script path
set "batchFile=%~f0"

:: Check for admin privileges
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo Requesting administrative privileges...
    :: Relaunch the script with admin privileges
    powershell -Command "Start-Process cmd -ArgumentList '/c \"%batchFile%\"' -Verb RunAs"
    exit /b
)

:: Define variables
set "ext=.clr"
set "iconPath=%userprofile%\Clarity\logo.ico"
set "fileType=ClrFileType"

:: Create a registry entry for the file extension
reg add "HKEY_CLASSES_ROOT\%ext%" /v "" /t REG_SZ /d "%fileType%" /f

:: Create a registry entry for the file type
reg add "HKEY_CLASSES_ROOT\%fileType%" /v "" /t REG_SZ /d "Clarity File Type" /f

:: Set the icon for the file type
reg add "HKEY_CLASSES_ROOT\%fileType%\DefaultIcon" /v "" /t REG_SZ /d "%iconPath%" /f

:: Refresh the icon cache (Restart Explorer)
echo Restarting Explorer to apply changes...
taskkill /f /im explorer.exe
start explorer.exe

echo Done!
pause