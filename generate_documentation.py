#!/usr/bin/env python3
"""
Generate DOCX documentation for Mumbai House Price Analyzer project
"""

from docx import Document
from docx.shared import Pt, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH

def create_documentation():
    # Create a new Document
    doc = Document()

    # Set default font
    style = doc.styles['Normal']
    font = style.font
    font.name = 'Calibri'
    font.size = Pt(11)

    # Title
    title = doc.add_heading('Mumbai House Price Analyzer', 0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER

    # Subtitle
    subtitle = doc.add_paragraph('Complete Project Documentation')
    subtitle.alignment = WD_ALIGN_PARAGRAPH.CENTER
    subtitle_format = subtitle.runs[0]
    subtitle_format.font.size = Pt(14)
    subtitle_format.font.italic = True

    doc.add_paragraph()

    # 1. Project Overview
    doc.add_heading('1. Project Overview (Introduction)', 1)
    doc.add_paragraph(
        'This project is a Mumbai House Price Prediction system. '
        'The main function is that users enter house details — such as area, bedrooms, locality — '
        'and the system automatically provides an estimated price.'
    )

    # Technology Stack
    doc.add_heading('Technology Stack:', 2)
    table = doc.add_table(rows=5, cols=2)
    table.style = 'Light Grid Accent 1'

    # Header row
    hdr_cells = table.rows[0].cells
    hdr_cells[0].text = 'Component'
    hdr_cells[1].text = 'Technology'

    # Data rows
    data = [
        ('Machine Learning (AI Brain)', 'Python 3 + AutoGluon'),
        ('User Interface (UI)', 'Java Swing + AWT'),
        ('Data Communication', 'Process-based JSON communication'),
        ('Dataset', 'Mumbai houses CSV (70,873 rows)')
    ]

    for i, (comp, tech) in enumerate(data, 1):
        row_cells = table.rows[i].cells
        row_cells[0].text = comp
        row_cells[1].text = tech

    doc.add_paragraph()

    # 2. Project Structure
    doc.add_heading('2. Project Structure', 1)
    doc.add_paragraph('The project is organized as follows:')

    structure = """
JavaProject/
│
├── mumbai-house-price-data-cleaned.csv   (Original dataset - 70,873 rows)
│
├── run.bat                                (One-click compile and run script)
│
├── python_scripts/
│   ├── train_mumbai_model.py              (Trains the ML model)
│   └── predict_server.py                  (Price prediction service)
│
├── models/
│   ├── model.pkl                          (Trained model file)
│   └── meta.json                          (Localities list, accuracy info)
│
├── src/                                   (Java source files)
│   ├── BasePanel.java                     (Abstract parent class)
│   ├── Predictable.java                   (Interface for prediction)
│   ├── MainApp.java                       (Main window - JFrame)
│   ├── PredictionPanel.java               (Price predictor tab)
│   ├── DataPanel.java                     (CSV data viewer tab)
│   └── ChartPanel.java                    (Charts tab)
│
├── lib/
│   └── json-simple-1.1.1.jar             (JSON library for Java)
│
└── out/                                   (Compiled .class files)
    """

    p = doc.add_paragraph(structure)
    p.style = 'Intense Quote'

    # 3. Dataset Description
    doc.add_heading('3. Dataset Description', 1)
    doc.add_paragraph(
        'File: mumbai-house-price-data-cleaned.csv\n'
        'Total Rows: 70,873 (almost 71 thousand Mumbai properties!)'
    )

    doc.add_heading('Dataset Columns:', 2)
    dataset_table = doc.add_table(rows=15, cols=3)
    dataset_table.style = 'Light Grid Accent 1'

    # Header
    hdr = dataset_table.rows[0].cells
    hdr[0].text = 'Column Name'
    hdr[1].text = 'Description'
    hdr[2].text = 'Example'

    # Data
    dataset_data = [
        ('title', 'Property name', 'Shakti Siyara Heights'),
        ('price', 'Price in Rupees', '6169841'),
        ('area', 'Area in sq ft', '601'),
        ('price_per_sqft', 'Price per sq ft', '9462.97'),
        ('locality', 'Area/Location', 'Kalyan'),
        ('city', 'City', 'Mumbai'),
        ('property_type', 'Type of property', 'Apartment'),
        ('bedroom_num', 'Number of bedrooms', '2'),
        ('bathroom_num', 'Number of bathrooms', '2'),
        ('balcony_num', 'Number of balconies', '0'),
        ('furnished', 'Furnishing status', 'Unfurnished'),
        ('age', 'Building age', '5'),
        ('total_floors', 'Total floors', '1'),
        ('latitude', 'GPS latitude', '19.247...')
    ]

    for i, (col, desc, ex) in enumerate(dataset_data, 1):
        cells = dataset_table.rows[i].cells
        cells[0].text = col
        cells[1].text = desc
        cells[2].text = ex

    doc.add_page_break()

    # 4. Machine Learning Component
    doc.add_heading('4. Machine Learning Component (Python)', 1)

    doc.add_heading('4.1 Model Training (train_mumbai_model.py)', 2)
    doc.add_paragraph('This script trains the model once and needs to be run before using the application.')

    doc.add_heading('Training Process:', 3)
    steps = [
        'Load Data: Read the CSV file using pandas',
        'Data Cleaning: Remove missing values and outliers',
        'Feature Engineering: Create new features like room_ratio and total_rooms',
        'Locality Processing: Keep top 80 most common localities, group rest as "Other"',
        'Prepare Features: Split data into input features (X) and target (y - price)',
        'Encoding: Convert text columns to numbers using One-Hot Encoding',
        'Scaling: Normalize numerical features using StandardScaler',
        'Model Training: Use AutoGluon ensemble method for best accuracy',
        'Evaluation: Calculate R² score and Mean Absolute Error (MAE)',
        'Save Model: Store trained model in models/model.pkl and metadata in models/meta.json'
    ]

    for step in steps:
        doc.add_paragraph(step, style='List Bullet')

    doc.add_heading('4.2 Prediction Service (predict_server.py)', 2)
    doc.add_paragraph(
        'This script acts as a bridge between Java and Python. '
        'It receives JSON input from Java, processes it through the ML model, '
        'and returns the predicted price in JSON format.'
    )

    doc.add_heading('Communication Flow:', 3)
    doc.add_paragraph('Java → JSON → predict_server.py → ML Model → JSON → Java')

    # 5. Java Application
    doc.add_heading('5. Java Application (User Interface)', 1)

    doc.add_heading('5.1 Object-Oriented Programming Concepts Used', 2)

    oop_table = doc.add_table(rows=6, cols=3)
    oop_table.style = 'Light Grid Accent 1'

    hdr = oop_table.rows[0].cells
    hdr[0].text = 'OOP Concept'
    hdr[1].text = 'Where Used'
    hdr[2].text = 'How'

    oop_data = [
        ('Abstract Class', 'BasePanel.java', 'abstract void loadData() - each panel must implement'),
        ('Interface', 'Predictable.java', 'predict() method contract'),
        ('Inheritance', 'All Panel classes', 'PredictionPanel, DataPanel, ChartPanel extend BasePanel'),
        ('Implementation', 'PredictionPanel', 'implements Predictable interface'),
        ('Encapsulation', 'All files', 'Private fields with public getter/setter methods')
    ]

    for i, (concept, where, how) in enumerate(oop_data, 1):
        cells = oop_table.rows[i].cells
        cells[0].text = concept
        cells[1].text = where
        cells[2].text = how

    doc.add_paragraph()

    doc.add_heading('5.2 Main Classes Description', 2)

    # BasePanel
    doc.add_heading('BasePanel.java (Abstract Parent Class)', 3)
    doc.add_paragraph(
        'This is the abstract parent class for all panels. '
        'It defines common functionality like error display and label creation. '
        'All panel classes must implement the abstract loadData() method.'
    )

    # Predictable Interface
    doc.add_heading('Predictable.java (Interface)', 3)
    doc.add_paragraph(
        'This interface defines a contract for classes that can perform predictions. '
        'It includes the abstract predict() method and a default method for number validation.'
    )

    # PredictionPanel
    doc.add_heading('PredictionPanel.java (Main Prediction UI)', 3)
    doc.add_paragraph(
        'This class extends BasePanel and implements Predictable. '
        'It provides the user interface for entering house details and displays the predicted price. '
        'Key features include:'
    )
    prediction_features = [
        'Form inputs: JComboBox for localities, JSpinner for numeric values',
        'Prediction button that calls Python script via ProcessBuilder',
        'JSON communication with Python backend',
        'Error handling for invalid inputs and Python process failures',
        'Display results in formatted currency (Lakhs and Crores)'
    ]
    for feature in prediction_features:
        doc.add_paragraph(feature, style='List Bullet')

    # DataPanel
    doc.add_heading('DataPanel.java (CSV Data Viewer)', 3)
    doc.add_paragraph(
        'This panel displays the dataset in a table format. Features include:'
    )
    data_features = [
        'JTable with DefaultTableModel for displaying CSV data',
        'SwingWorker for background loading to prevent UI freezing',
        'Real-time search functionality using KeyAdapter',
        'Displays up to 5000 rows for performance'
    ]
    for feature in data_features:
        doc.add_paragraph(feature, style='List Bullet')

    # ChartPanel
    doc.add_heading('ChartPanel.java (Visualizations)', 3)
    doc.add_paragraph(
        'This panel displays two types of charts:'
    )
    chart_features = [
        'Bar Chart: Average house prices by bedroom count (1 BHK, 2 BHK, etc.)',
        'Histogram: Price distribution showing how many houses are in each price range',
        'Custom drawing using paintComponent() method override',
        'Uses Java AWT Graphics2D for rendering'
    ]
    for feature in chart_features:
        doc.add_paragraph(feature, style='List Bullet')

    # MainApp
    doc.add_heading('MainApp.java (Main Application Window)', 3)
    doc.add_paragraph(
        'This is the main entry point of the application. It creates a JFrame with:'
    )
    main_features = [
        'Header panel showing project title and model accuracy',
        'JTabbedPane with three tabs: Price Predictor, Charts & Graphs, Data Explorer',
        'Status bar showing dataset information',
        'Model validation on startup'
    ]
    for feature in main_features:
        doc.add_paragraph(feature, style='List Bullet')

    doc.add_page_break()

    # 6. How to Run
    doc.add_heading('6. How to Run the Application', 1)

    doc.add_heading('Prerequisites:', 2)
    prereq = [
        'Java JDK 8 or higher',
        'Python 3.x',
        'Python libraries: pandas, numpy, autogluon'
    ]
    for item in prereq:
        doc.add_paragraph(item, style='List Bullet')

    doc.add_heading('Step 1: Train the Model (First Time Only)', 2)
    doc.add_paragraph('Run the following command:')
    p = doc.add_paragraph('python python_scripts/train_mumbai_model.py')
    p.style = 'Intense Quote'
    doc.add_paragraph('This creates models/model.pkl and models/meta.json files (takes 1-2 minutes)')

    doc.add_heading('Step 2: Launch the Application', 2)
    doc.add_paragraph('Option 1 - Using batch script (Windows):')
    p = doc.add_paragraph('Double-click run.bat')
    p.style = 'Intense Quote'

    doc.add_paragraph('Option 2 - Manual compilation and run:')
    p1 = doc.add_paragraph('javac -cp "lib/*" -d out src/*.java')
    p1.style = 'Intense Quote'
    p2 = doc.add_paragraph('java -cp "out;lib/*" MainApp')
    p2.style = 'Intense Quote'

    # 7. Features
    doc.add_heading('7. Application Features', 1)

    features_list = [
        ('Price Predictor', 'Enter house details and get instant price prediction using ML model'),
        ('Data Explorer', 'View and search through the complete dataset of 70,873 properties'),
        ('Charts & Graphs', 'Visual analysis of price trends by bedroom count and price distribution'),
        ('Real-time Processing', 'Seamless Java-Python integration for instant predictions'),
        ('Error Handling', 'Comprehensive error messages for missing files or invalid inputs'),
        ('Modern UI', 'Clean, professional interface using Java Swing')
    ]

    for title, desc in features_list:
        doc.add_paragraph(f'{title}: {desc}', style='List Bullet')

    # 8. Error Handling
    doc.add_heading('8. Error Handling', 1)
    doc.add_paragraph('The application includes comprehensive error handling:')

    error_table = doc.add_table(rows=8, cols=3)
    error_table.style = 'Light Grid Accent 1'

    hdr = error_table.rows[0].cells
    hdr[0].text = 'Error Situation'
    hdr[1].text = 'Location'
    hdr[2].text = 'Handling Method'

    error_data = [
        ('Model file not found', 'MainApp.java', 'Warning dialog with option to exit'),
        ('Metadata missing', 'PredictionPanel.java', 'FileNotFoundException catch with warning'),
        ('Python process fails', 'PredictionPanel.java', 'IOException catch with error message'),
        ('CSV file missing', 'DataPanel.java', 'FileNotFoundException catch with error popup'),
        ('File read error', 'DataPanel.java', 'IOException catch'),
        ('Invalid JSON', 'predict_server.py', 'JSONDecodeError catch returns error JSON'),
        ('Invalid number input', 'Predictable.java', 'NumberFormatException catch in validation')
    ]

    for i, (situation, location, handling) in enumerate(error_data, 1):
        cells = error_table.rows[i].cells
        cells[0].text = situation
        cells[1].text = location
        cells[2].text = handling

    doc.add_paragraph()

    # 9. Model Performance
    doc.add_heading('9. Model Performance', 1)
    doc.add_paragraph('The machine learning model shows strong performance:')

    perf = [
        'Model Type: AutoGluon Ensemble (combines multiple ML algorithms)',
        'Accuracy (R² Score): ~89.6% (explains 89.6% of price variation)',
        'Mean Absolute Error: Approximately 38 Lakhs average prediction error',
        'Training Dataset: 70,873 Mumbai property listings',
        'Features Used: 14 features including area, bedrooms, locality, furnishing, etc.'
    ]

    for item in perf:
        doc.add_paragraph(item, style='List Bullet')

    # 10. Technical Highlights
    doc.add_heading('10. Technical Highlights', 1)

    highlights = [
        'Object-Oriented Design: Proper use of inheritance, interfaces, and polymorphism',
        'Multi-threading: SwingWorker for background data loading',
        'Inter-Process Communication: Java-Python integration via stdin/stdout',
        'JSON Serialization: Structured data exchange between components',
        'Machine Learning Integration: Advanced ensemble model (AutoGluon)',
        'UI/UX: Clean, tabbed interface with responsive design',
        'Data Visualization: Custom charts using Java Graphics2D',
        'Exception Handling: Comprehensive error management throughout'
    ]

    for item in highlights:
        doc.add_paragraph(item, style='List Bullet')

    doc.add_page_break()

    # 11. Conclusion
    doc.add_heading('11. Conclusion', 1)
    doc.add_paragraph(
        'This Mumbai House Price Analyzer project successfully demonstrates the integration '
        'of multiple technologies to create a functional real estate price prediction system. '
        'The application combines Java\'s robust GUI capabilities with Python\'s powerful '
        'machine learning libraries to provide accurate house price predictions.'
    )

    doc.add_paragraph()
    doc.add_paragraph(
        'Key achievements of this project include:'
    )

    achievements = [
        'Successfully implemented Object-Oriented Programming concepts in Java',
        'Integrated machine learning model with 89.6% accuracy',
        'Created user-friendly interface for non-technical users',
        'Processed and analyzed dataset of 70,873+ properties',
        'Implemented real-time prediction with instant results',
        'Designed modular, maintainable code structure'
    ]

    for achievement in achievements:
        doc.add_paragraph(achievement, style='List Bullet')

    doc.add_paragraph()
    doc.add_paragraph(
        'This project serves as an excellent demonstration of practical software engineering, '
        'combining theoretical concepts with real-world application. It showcases skills in '
        'Java programming, Python machine learning, data processing, and system integration.'
    )

    # Footer
    doc.add_paragraph()
    doc.add_paragraph()
    footer = doc.add_paragraph('--- End of Documentation ---')
    footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
    footer_format = footer.runs[0]
    footer_format.font.italic = True
    footer_format.font.color.rgb = RGBColor(128, 128, 128)

    # Save document
    output_path = '/home/runner/work/Java-project/Java-project/Mumbai_House_Price_Analyzer_Documentation.docx'
    doc.save(output_path)
    print(f"Documentation created successfully: {output_path}")
    return output_path

if __name__ == "__main__":
    create_documentation()
