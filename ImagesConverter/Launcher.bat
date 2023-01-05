echo off
Title Launcher
cls

mkdir InputFile
mkdir OutputFile
rem just ignore the output of these two commands
cls

Echo Place the files you want to convert in "InputFile"
Echo press anykey when ready to convert..
Pause>nul
cls
java -jar compiled.jar
echo.
echo.
echo Results are in "OutputFile"
Pause>nul

