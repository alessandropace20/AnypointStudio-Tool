@echo off
REM =========================================================
REM  MuleXML Tool — Build Script (Windows)
REM  Requisito: JDK 11+ installato e nel PATH
REM =========================================================

echo [1/3] Pulizia cartella out...
if exist out rmdir /s /q out
mkdir out

echo [2/3] Compilazione sorgenti...
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo ERRORE nella compilazione.
    del sources.txt
    exit /b 1
)
del sources.txt

echo [3/3] Creazione JAR...
echo Main-Class: com.muletool.Main > manifest.txt
jar cfm mule-xml-tool.jar manifest.txt -C out .
del manifest.txt

echo.
echo ========================================
echo  Build completata: mule-xml-tool.jar
echo  Esegui con:  java -jar mule-xml-tool.jar
echo ========================================
