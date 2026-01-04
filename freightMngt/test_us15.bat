@echo off
cd /d "%~dp0"
call mvn compile -q && java -cp "target/classes" isep.ipp.pt.g322.Main 2>&1 | findstr /C:"Source:" /C:"Destination:" /C:"Total Cost" /C:"START:" /C:"Station" /C:"Path Summary" /C:"Complexidade"
