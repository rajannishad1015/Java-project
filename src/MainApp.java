// MainApp.java
// This is the main class for our Mumbai House Price Prediction project
// I used JFrame and JTabbedPane to create a window with multiple tabs
// This project uses OOP: abstract class (BasePanel), interface (Predictable), inheritance
// Each panel class extends BasePanel which is the parent class

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

// MainApp is the starting point of the whole application
public class MainApp extends JFrame {

    // window title
    private static final String APP_TITLE = "Mumbai House Price Analyzer";

    // base directory where project files are
    private String basePath;

    // constructor - builds the main window
    public MainApp() {
        // get the current directory (project root)
        basePath = Paths.get("").toAbsolutePath().toString();

        // set JFrame properties
        setTitle(APP_TITLE);
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close when X is clicked
        setLocationRelativeTo(null); // center window on screen

        // build the UI
        buildUI();

        // make window visible last (after adding everything)
        setVisible(true);
    }

    // creates and adds all UI components to the frame
    private void buildUI() {
        // content pane background color
        getContentPane().setBackground(new Color(235, 240, 255));

        // main panel to hold everything
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(new Color(235, 240, 255));
        setContentPane(mainPanel);

        // --- Top Header ---
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Tabbed Pane in Center ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.setBackground(new Color(220, 230, 255));

        // create all three panels (these are subclasses of BasePanel - OOP polymorphism)
        PredictionPanel predictTab = new PredictionPanel(); // extends BasePanel, implements Predictable
        ChartPanel      chartTab   = new ChartPanel();      // extends BasePanel
        DataPanel       dataTab    = new DataPanel();       // extends BasePanel

        // add tabs with icons-as-text and panel objects
        tabs.addTab("  Price Predictor  ", predictTab);
        tabs.addTab("  Charts & Graphs  ", chartTab);
        tabs.addTab("  Data Explorer    ", dataTab);

        mainPanel.add(tabs, BorderLayout.CENTER);

        // --- Bottom Status Bar ---
        JLabel statusBar = new JLabel("  Project: Mumbai House Price Predictor  |  Dataset: 71,938 entries  |  Model: Gradient Boosting");
        statusBar.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusBar.setForeground(new Color(100, 100, 140));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(220, 228, 250));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }

    // creates the top header panel with title
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 80, 180));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // left side - app title
        JLabel titleLabel = new JLabel("Mumbai House Price Analyzer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        // right side - subtitle
        JLabel subLabel = new JLabel("ML Model: Gradient Boosting  |  71,938 Properties");
        subLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subLabel.setForeground(new Color(180, 210, 255));
        header.add(subLabel, BorderLayout.EAST);

        return header;
    }

    // ---- main method - program starts here ----
    public static void main(String[] args) {

        // check if model.pkl file exists, if not warn the user
        checkModelFile();

        // SwingUtilities.invokeLater ensures UI runs on the Event Dispatch Thread
        // this is important for thread safety in Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // try to set system look and feel for native OS look
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e) {
                    System.out.println("Look and feel class not found: " + e.getMessage());
                } catch (InstantiationException e) {
                    System.out.println("Could not instantiate look and feel: " + e.getMessage());
                } catch (IllegalAccessException e) {
                    System.out.println("Illegal access to look and feel: " + e.getMessage());
                } catch (UnsupportedLookAndFeelException e) {
                    System.out.println("Unsupported look and feel: " + e.getMessage());
                }

                // create the main window
                new MainApp();
            }
        });
    }

    // checks if model file exists and warns user if not
    private static void checkModelFile() {
        String modelPath = Paths.get("").toAbsolutePath().toString()
            + File.separator + "models"
            + File.separator + "model.pkl";

        File modelFile = new File(modelPath);

        if (!modelFile.exists()) {
            // model not trained - warn the user
            int choice = JOptionPane.showConfirmDialog(
                null,
                "Model file not found!\n\n" +
                "Please run this command first:\n" +
                "   python python_scripts/train_mumbai_model.py\n\n" +
                "Continue anyway? (Price Predictor tab won't work)",
                "Model Not Found",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            // if user clicks No, exit the application
            if (choice == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
    }
}
