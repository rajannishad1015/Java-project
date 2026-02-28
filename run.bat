@echo off
REM ============================================================
REM  Mumbai House Price Analyzer - Build and Run Script
REM ============================================================
echo.
echo [1/3] Checking Python dependencies...
python -c "import pandas, numpy, sklearn" 2>nul
if %errorlevel% neq 0 (
    echo    Installing missing Python packages from requirements.txt...
    pip install -r requirements.txt
    if ERRORLEVEL 1 (
        echo ERROR: Failed to install Python dependencies!
        pause
        exit /b 1
    )
) else (
    echo    Python dependencies are already installed.
)

echo.
echo [2/3] Compiling Java source files...

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
echo [3/3] Launching application...
java -cp "out;lib\json-simple-1.1.1.jar" MainApp
pause
