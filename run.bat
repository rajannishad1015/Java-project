@echo off
REM ============================================================
REM  Mumbai House Price Analyzer - Build and Run Script
REM ============================================================
echo.
echo [1/2] Compiling Java source files...

javac -encoding UTF-8 -cp "lib\json-simple-1.1.1.jar" -d "out" ^
    "src\BasePanel.java" ^
    "src\Predictable.java" ^
    "src\MainApp.java" ^
    "src\PredictionPanel.java" ^
    "src\DataPanel.java" ^
    "src\ChartPanel.java"

if ERRORLEVEL 1 (
    echo.
    echo ERROR: Compilation failed! Make sure Java JDK is installed.
    echo Run: javac -version to check.
    pause
    exit /b 1
)

echo      Done! All files compiled successfully.
echo.
echo [2/2] Launching application...
java -cp "out;lib\json-simple-1.1.1.jar" MainApp
pause
