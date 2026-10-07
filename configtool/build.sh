#!/bin/bash
# =========================================================
#  MuleXML Tool — Build Script (Linux/macOS)
#  Requisito: JDK 11+ installato e nel PATH
# =========================================================

set -e

echo "[1/3] Pulizia cartella out..."
rm -rf out && mkdir out

echo "[2/3] Compilazione sorgenti..."
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm sources.txt

echo "[3/3] Creazione JAR..."
echo "Main-Class: com.muletool.Main" > manifest.txt
jar cfm mule-xml-tool.jar manifest.txt -C out .
rm manifest.txt

echo ""
echo "========================================"
echo " Build completata: mule-xml-tool.jar"
echo " Esegui con:  java -jar mule-xml-tool.jar"
echo "========================================"
