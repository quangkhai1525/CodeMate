@echo off
title CodeMate Pro - Khoi dong ung dung
cd /d "%~dp0"

echo ==========================================================
echo           KHOI DONG UNG DUNG CODEMATE PRO
echo ==========================================================
echo.
echo Dang khoi chay ung dung JavaFX...
echo.

if not defined CODEMATE_DB_USER set CODEMATE_DB_USER=root
if not defined CODEMATE_DB_PASSWORD set CODEMATE_DB_PASSWORD=

call .\mvnw.cmd javafx:run
if %errorlevel% neq 0 (
    echo.
    echo [LUU Y] Neu gap loi ket noi, vui long kiem tra MySQL trong XAMPP da BAT chua nhe!
    pause
)
