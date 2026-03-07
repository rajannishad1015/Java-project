import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

public class PredictionPanel extends BasePanel implements Predictable {

    private JComboBox<String> locCb, propCb, furnCb;
    private JSpinner areaSp, bedSp, bathSp, balcSp, floorSp;
    private JLabel resultLbl, accLbl;
    private JButton predictBtn;


    public PredictionPanel() {
        super("Price Predictor");
        buildUI();
        loadData();
    }

    private void buildUI() {
        add(createTitle("Mumbai House Price Predictor"), BorderLayout.NORTH);

        JPanel card = createCard();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 10, 8, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        // Initialize fields
        locCb   = new JComboBox<>(new String[]{"Andheri","Bandra","Thane","Other"});
        propCb  = new JComboBox<>(new String[]{"Apartment","Villa","Studio"});
        furnCb  = new JComboBox<>(new String[]{"Furnished","Semi-Furnished","Unfurnished"});
        areaSp  = new JSpinner(new SpinnerNumberModel(800, 100, 50000, 50));
        bedSp   = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        bathSp  = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        balcSp  = new JSpinner(new SpinnerNumberModel(1, 0, 5, 1));
        floorSp = new JSpinner(new SpinnerNumberModel(10, 1, 60, 1));

        String[] labels = {"Locality", "Property Type", "Furnishing", "Area (sq ft)", "Bedrooms", "Bathrooms", "Balconies", "Total Floors"};
        JComponent[] fields = {locCb, propCb, furnCb, areaSp, bedSp, bathSp, balcSp, floorSp};

        for (int i = 0; i < fields.length; i++) {
            styleField(fields[i]);
            gc.gridy = i;
            
            gc.gridx = 0; gc.weightx = 0.35;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(FONT_TEXT);
            lbl.setForeground(TEXT_DARK);
            card.add(lbl, gc);

            gc.gridx = 1; gc.weightx = 0.65;
            card.add(fields[i], gc);
        }

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 80, 0, 80));
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // ---- Bottom panel ----
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        bot.setOpaque(false);
        bot.setBorder(new EmptyBorder(16, 60, 24, 60));

        accLbl = new JLabel("Accuracy: loading...", SwingConstants.CENTER);
        accLbl.setFont(FONT_SMALL);
        accLbl.setForeground(new Color(100, 120, 160));
        accLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        bot.add(accLbl);
        bot.add(Box.createVerticalStrut(12));

        predictBtn = buildPredictButton();
        predictBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bot.add(predictBtn);
        bot.add(Box.createVerticalStrut(14));

        resultLbl = new JLabel("Enter details and click Predict", SwingConstants.CENTER);
        resultLbl.setFont(FONT_BOLD.deriveFont(20f));
        resultLbl.setForeground(new Color(20, 148, 80));
        resultLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        bot.add(resultLbl);

        add(bot, BorderLayout.SOUTH);
    }

    private JButton buildPredictButton() {
        JButton btn = new JButton("  Predict Price  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(10, 70, 180) : THEME_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(260, 48));
        btn.setPreferredSize(new Dimension(260, 48));
        btn.addActionListener(e -> predict());
        return btn;
    }


    @Override public void loadData() {
        try {
            String json = Files.readString(Paths.get("models/meta.json"));
            JSONObject obj = (JSONObject) new JSONParser().parse(json);
            updateCb(locCb,   (Iterable<?>) obj.get("localities"));
            updateCb(propCb,  (Iterable<?>) obj.get("property_types"));
            updateCb(furnCb,  (Iterable<?>) obj.get("furnishings"));
            JSONObject m = (JSONObject) obj.get("metrics");
            if (m != null) accLbl.setText(String.format(
                "Accuracy: %.1f%%  |  Avg Error: ₹%.1f Lakhs",
                ((Number)m.get("r2")).doubleValue() * 100,
                ((Number)m.get("mae")).doubleValue() / 1e5));
        } catch (Exception ex) { System.err.println("Meta: " + ex.getMessage()); }
    }

    private void updateCb(JComboBox<String> cb, Iterable<?> items) {
        if (items == null || cb == null) return;
        cb.removeAllItems();
        items.forEach(i -> cb.addItem(i.toString()));
    }

    @Override @SuppressWarnings("unchecked")
    public void predict() {
        predictBtn.setEnabled(false);
        resultLbl.setForeground(new Color(200, 130, 20));
        resultLbl.setText("Calculating... please wait");

        JSONObject req = new JSONObject();
        req.put("area",          areaSp.getValue());
        req.put("bedroom_num",   bedSp.getValue());
        req.put("bathroom_num",  bathSp.getValue());
        req.put("balcony_num",   balcSp.getValue());
        req.put("total_floors",  floorSp.getValue());
        req.put("locality",      locCb.getSelectedItem());
        req.put("property_type", propCb.getSelectedItem());
        req.put("furnished",     furnCb.getSelectedItem());

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                Process p = new ProcessBuilder("python", "python_scripts/predict_server.py").start();
                try (PrintWriter w = new PrintWriter(p.getOutputStream())) { w.println(req.toJSONString()); }
                try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    return r.lines().reduce((a,b)->b).orElse("{}");
                }
            }
            @Override protected void done() {
                predictBtn.setEnabled(true);
                try {
                    JSONObject res = (JSONObject) new JSONParser().parse(get());
                    if ("ok".equals(res.get("status"))) {
                        resultLbl.setForeground(new Color(20, 148, 80));
                        resultLbl.setText("Predicted Price:  " + res.get("price_str"));
                    } else {
                        resultLbl.setForeground(Color.RED);
                        resultLbl.setText("Error: " + res.get("message"));
                    }
                } catch (Exception e) {
                    resultLbl.setForeground(Color.RED);
                    resultLbl.setText("Failed: " + e.getMessage());
                }
            }
        }.execute();
    }
}
