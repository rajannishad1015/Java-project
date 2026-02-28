# Mumbai House Price Predictor

A student-style Java application with a Python Machine Learning backend to predict house prices in Mumbai based on user-provided features like area, locality, bedrooms, etc.

## Prerequisites

Before running the project, please ensure you have the following installed:

1. **Java JDK 8 or higher**: Required to compile and run the Java GUI application.
2. **Python 3.x**: Required to run the machine learning model.
3. **Python Libraries**:
   Install the necessary libraries using pip:
   ```bash
   pip install pandas numpy scikit-learn
   ```

## Project Structure

- `src/`: Contains all the Java source code for the GUI application (`MainApp.java`, `BasePanel.java`, `ChartPanel.java`, `DataPanel.java`, `PredictionPanel.java`).
- `python_scripts/`: Contains Python scripts for training the model (`train_mumbai_model.py`) and predicting prices (`predict_server.py`).
- `models/`: Stores the trained Machine Learning Model (`model.pkl`) and metadata (`meta.json`).
- `lib/`: Contains external libraries like `json-simple-1.1.1.jar` and Apache Commons CSV.
- `mumbai-house-price-data-cleaned.csv`: The dataset used to train the machine learning model and display statistics in the application.

## How to Run the Application

### Option 1: Using the provided batch script (Windows)

Simply double-click on `run.bat` or run it from the command line:

```cmd
run.bat
```

This batch script will automatically compile the Java code and run the main application.

### Option 2: Running manually

1. **Train the Model (If not already trained or if you want to rebuild it):**
   Navigate to the project root directory and run the training script:

   ```bash
   python python_scripts\train_mumbai_model.py
   ```

   This will train the model and save `model.pkl` and `meta.json` inside the `models/` directory.

2. **Compile the Java Code:**
   Open a terminal/command prompt in the project root directory and run:

   ```cmd
   javac -cp "lib/*" -d out src/*.java
   ```

3. **Run the Java Application:**
   After compiling, run the compiled Java application:
   ```cmd
   java -cp "out;lib/*" MainApp
   ```

## Features

1. **Price Predictor**: Predicts house prices based on area, number of bedrooms, bathrooms, locality, etc. It communicates locally with a Python predict script.
2. **Data Explorer**: View the raw house listing dataset.
3. **Charts & Graphs**: Visualizes average house prices per locality and generates trend graphs using a Python backend.

## Object-Oriented Programming (OOP) Concepts Used

The Java code incorporates OOP principles like:

- **Inheritance**: `PredictionPanel`, `DataPanel`, and `ChartPanel` extend `BasePanel`.
- **Interfaces**: Contains the `Predictable` interface.
- **Encapsulation**: Using private variables with public getter/setter methods.
- **Polymorphism**: Overriding methods like `loadData()`.

## Notes

- Ensure that the path to `models/meta.json` and the `.csv` file is correct, as the application relies on these for data.
- The Python script is called via a process spawned from the Java application, so Python must be added to your system's PATH.
