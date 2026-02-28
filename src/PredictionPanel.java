// PredictionPanel.java
// This panel is used for predicting Mumbai house prices
// It extends BasePanel (OOP - inheritance) and implements Predictable (OOP - interface)
// Made by: Student Project - Mumbai House Price Predictor

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

// PredictionPanel inherits from BasePanel and also implements Predictable interface
// This shows both inheritance and interface implementation (OOP concepts)
public class PredictionPanel extends BasePanel implements Predictable {

    // --- Private variables (Encapsulation - data hiding) ---
    private JComboBox<String> localityDropdown;
    private JComboBox<String> propertyDropdown;
    private JComboBox<String> furnishedDropdown;

    private JSpinner areaField;
    private JSpinner bedroomField;
    private JSpinner bathroomField;
    private JSpinner balconyField;
    private JSpinner floorsField;

    private JLabel resultLabel;
    private JLabel accuracyLabel;
    private JButton predictButton;

    // arrays to store dropdown data loaded from meta.json
    private String[] localityList  = { "Andheri", "Bandra", "Thane", "Other" }; // default fallback
    private String[] propertyList  = { "Apartment", "Villa", "Studio" };
    private String[] furnishedList = { "Furnished", "Semi-Furnished", "Unfurnished" };

    // path to project folder - from where python script runs
    private String basePath = Paths.get("").toAbsolutePath().toString();

    // Constructor - calls parent constructor with title (inheritance)
    public PredictionPanel() {
        super("Price Predictor"); // calling BasePanel constructor

        // first load the dropdown options from trained model meta file
        loadData(); // this is abstract method from BasePanel, we MUST override it

        // then setup the UI
        setupUI();
    }

    // MUST implement this because BasePanel declared it abstract
    // Loads dropdown data from models/meta.json file
    @Override
    public void loadData() {
        // path to meta.json file
        String metaFile = basePath + File.separator + "models" + File.separator + "meta.json";

        try {
            // read the file content
            String content = new String(Files.readAllBytes(Paths.get(metaFile)), StandardCharsets.UTF_8);

            // parse json using json-simple library
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(content);

            // get the lists from json
            java.util.List<?> loc  = (java.util.List<?>) jsonObj.get("localities");
            java.util.List<?> prop = (java.util.List<?>) jsonObj.get("property_types");
            java.util.List<?> furn = (java.util.List<?>) jsonObj.get("furnishings");

            // convert List to String array
            localityList  = loc.stream().map(Object::toString).toArray(String[]::new);
            propertyList  = prop.stream().map(Object::toString).toArray(String[]::new);
            furnishedList = furn.stream().map(Object::toString).toArray(String[]::new);

            // also show model accuracy from meta file
            JSONObject metrics = (JSONObject) jsonObj.get("metrics");
            if (metrics != null) {
                double r2  = ((Number) metrics.get("r2")).doubleValue();
                double mae = ((Number) metrics.get("mae_inr")).doubleValue();
                // update label on screen once UI is built
                SwingUtilities.invokeLater(() -> {
                    if (accuracyLabel != null) {
                        accuracyLabel.setText(
                            "Model Accuracy (R2): " + String.format("%.2f", r2 * 100) + "%  |  Avg Error: Rs." +
                            String.format("%.0f", mae / 100000) + " Lakhs"
                        );
                    }
                });
            }

        } catch (FileNotFoundException e) {
            // model not trained yet - show warning to user
            JOptionPane.showMessageDialog(this,
                "models/meta.json not found!\nPlease run train_mumbai_model.py first.",
                "Model Missing", JOptionPane.WARNING_MESSAGE);

        } catch (Exception e) {
            // some other error while reading file
            JOptionPane.showMessageDialog(this,
                "Error loading model info: " + e.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Setup all the Swing UI components
    private void setupUI() {
        setBackground(new Color(245, 248, 255));

        // top heading label using parent class helper method
        JLabel heading = makeLabel("Mumbai House Price Predictor", 20, true);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setForeground(new Color(30, 80, 160));
        heading.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        // center panel with form fields using GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255));
        formPanel.setBorder(new CompoundBorder(
            new EmptyBorder(10, 50, 10, 50),
            BorderFactory.createLineBorder(new Color(180, 200, 240), 1)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 10, 7, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // add all form rows
        int row = 0;
        row = addFormRow(formPanel, gbc, row, "Locality:", localityDropdown = new JComboBox<>(localityList));
        row = addFormRow(formPanel, gbc, row, "Property Type:", propertyDropdown = new JComboBox<>(propertyList));
        row = addFormRow(formPanel, gbc, row, "Furnished Status:", furnishedDropdown = new JComboBox<>(furnishedList));
        row = addFormRow(formPanel, gbc, row, "Area (sq ft):",    areaField    = new JSpinner(new SpinnerNumberModel(800, 100, 50000, 50)));
        row = addFormRow(formPanel, gbc, row, "Bedrooms:",        bedroomField  = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1)));
        row = addFormRow(formPanel, gbc, row, "Bathrooms:",       bathroomField = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1)));
        row = addFormRow(formPanel, gbc, row, "Balconies:",       balconyField  = new JSpinner(new SpinnerNumberModel(1, 0, 5, 1)));
        row = addFormRow(formPanel, gbc, row, "Total Floors:",    floorsField   = new JSpinner(new SpinnerNumberModel(10, 1, 60, 1)));

        add(formPanel, BorderLayout.CENTER);

        // bottom panel - button and result
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 8));
        bottomPanel.setBackground(new Color(245, 248, 255));
        bottomPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        // accuracy label
        accuracyLabel = makeLabel("Please train model first to see accuracy.", 11, false);
        accuracyLabel.setForeground(Color.GRAY);
        accuracyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(accuracyLabel, BorderLayout.NORTH);

        // predict button
        predictButton = new JButton("Predict Price");
        predictButton.setFont(new Font("Arial", Font.BOLD, 15));
        predictButton.setBackground(new Color(30, 100, 220));
        predictButton.setForeground(Color.BLACK);
        predictButton.setFocusPainted(false);
        predictButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // button click calls predict() method from Predictable interface
        predictButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                predict(); // implements Predictable interface method
            }
        });
        bottomPanel.add(predictButton, BorderLayout.CENTER);

        // result label at bottom
        resultLabel = makeLabel("Enter details above and click Predict.", 18, true);
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setForeground(new Color(20, 140, 80));
        resultLabel.setBorder(new EmptyBorder(12, 0, 0, 0));
        bottomPanel.add(resultLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // helper method to add one row (label + component) to form
    private int addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        // label column
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = makeLabel(labelText, 13, false);
        lbl.setForeground(new Color(60, 60, 90));
        panel.add(lbl, gbc);

        // component column
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comp.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(comp, gbc);

        return row + 1; // go to next row
    }

    // ---- Implementation of Predictable interface method ----
    // This method runs the Python script and gets the predicted price
    @Override
    public void predict() {
        // disable button so user doesn't click again while loading
        predictButton.setEnabled(false);
        resultLabel.setText("Calculating... please wait.");
        resultLabel.setForeground(new Color(200, 130, 20));

        // SwingWorker runs prediction in background thread
        // (otherwise the UI would freeze)
        SwingWorker<String, Void> bgTask = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                // build the json input string manually
                String inputJson = buildInputJson();

                // path to python predict script
                String scriptPath = basePath + File.separator + "python_scripts"
                    + File.separator + "predict_server.py";

                // use ProcessBuilder to run python script
                ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
                pb.directory(new File(basePath));
                pb.redirectErrorStream(false); // keep stderr separate

                Process process = null;
                String result = "";

                try {
                    process = pb.start();

                    // write input json to python's stdin
                    PrintWriter writer = new PrintWriter(
                        new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)
                    );
                    writer.println(inputJson);
                    writer.flush();
                    writer.close();

                    // read output from python's stdout
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result = line; // keep last line (the json output)
                    }
                    reader.close();

                    // wait for process to finish
                    process.waitFor();

                } catch (IOException e) {
                    // if python not found or script not found
                    throw new Exception("Could not run Python: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new Exception("Process was interrupted.");
                } finally {
                    // always destroy process if something went wrong
                    if (process != null) {
                        process.destroy();
                    }
                }

                if (result.isEmpty()) {
                    throw new Exception("No output received from Python script.");
                }

                return result;
            }

            @Override
            protected void done() {
                predictButton.setEnabled(true); // re-enable button

                try {
                    String jsonOutput = get(); // get result from doInBackground

                    // parse json response from python
                    JSONParser parser = new JSONParser();
                    JSONObject response = (JSONObject) parser.parse(jsonOutput);

                    String status = response.get("status").toString();

                    if (status.equals("ok")) {
                        // success - show predicted price
                        String priceStr = response.get("price_str").toString();
                        double lakhs    = ((Number) response.get("price_lakhs")).doubleValue();

                        resultLabel.setText("Predicted Price: " + priceStr);
                        resultLabel.setForeground(new Color(20, 140, 80));

                    } else {
                        // python returned an error
                        String msg = response.get("message").toString();
                        resultLabel.setText("Prediction Error!");
                        resultLabel.setForeground(Color.RED);
                        showError("Python Error:\n" + msg); // using BasePanel's method

                    }

                } catch (Exception e) {
                    // if something breaks while reading result
                    resultLabel.setText("Something went wrong.");
                    resultLabel.setForeground(Color.RED);
                    showError("Error: " + e.getMessage());
                }
            }
        };

        bgTask.execute(); // start background thread
    }

    // builds the json string to send to python
    // used isValidNumber() from Predictable interface
    private String buildInputJson() {
        double area     = ((Number) areaField.getValue()).doubleValue();
        double bedrooms = ((Number) bedroomField.getValue()).doubleValue();
        double bathrooms= ((Number) bathroomField.getValue()).doubleValue();
        double balconies= ((Number) balconyField.getValue()).doubleValue();
        double floors   = ((Number) floorsField.getValue()).doubleValue();

        String locality     = localityDropdown.getSelectedItem().toString();
        String propertyType = propertyDropdown.getSelectedItem().toString();
        String furnished    = furnishedDropdown.getSelectedItem().toString();

        // create json manually (simple approach)
        JSONObject obj = new JSONObject();
        obj.put("area",          area);
        obj.put("bedroom_num",   bedrooms);
        obj.put("bathroom_num",  bathrooms);
        obj.put("balcony_num",   balconies);
        obj.put("total_floors",  floors);
        obj.put("locality",      locality);
        obj.put("property_type", propertyType);
        obj.put("furnished",     furnished);

        return obj.toJSONString();
    }
}
