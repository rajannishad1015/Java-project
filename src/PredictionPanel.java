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

    private static final Color BLUE       = new Color(25, 90, 200);
    private static final Color LIGHT_BLUE = new Color(235, 243, 255);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color FIELD_BG   = new Color(248, 250, 255);

    public PredictionPanel() {
        super("Price Predictor");
        setBackground(new Color(230, 238, 255));
        setLayout(new BorderLayout(0, 0));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ---- Top heading ----
        JLabel heading = new JLabel("Mumbai House Price Predictor", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(BLUE);
        heading.setBorder(new EmptyBorder(22, 0, 16, 0));
        add(heading, BorderLayout.NORTH);

        // ---- Center card ----
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new EmptyBorder(0, 60, 0, 60),
            new CompoundBorder(
                new LineBorder(new Color(200, 215, 245), 1, true),
                new EmptyBorder(20, 30, 20, 30)
            )
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 10, 8, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        locCb   = styleCombo(new JComboBox<>(new String[]{"Andheri","Bandra","Thane","Other"}));
        propCb  = styleCombo(new JComboBox<>(new String[]{"Apartment","Villa","Studio"}));
        furnCb  = styleCombo(new JComboBox<>(new String[]{"Furnished","Semi-Furnished","Unfurnished"}));
        areaSp  = styleSpin(new JSpinner(new SpinnerNumberModel(800, 100, 50000, 50)));
        bedSp   = styleSpin(new JSpinner(new SpinnerNumberModel(2, 1, 10, 1)));
        bathSp  = styleSpin(new JSpinner(new SpinnerNumberModel(2, 1, 10, 1)));
        balcSp  = styleSpin(new JSpinner(new SpinnerNumberModel(1, 0, 5, 1)));
        floorSp = styleSpin(new JSpinner(new SpinnerNumberModel(10, 1, 60, 1)));

        // Two-column layout: labels 35%, fields 65%
        String[][] rows = {
            {"Locality",       ""}, {"Property Type",  ""},
            {"Furnishing",     ""}, {"Area  (sq ft)",  ""},
            {"Bedrooms",       ""}, {"Bathrooms",      ""},
            {"Balconies",      ""}, {"Total Floors",   ""}
        };
        JComponent[] fields = { locCb, propCb, furnCb, areaSp, bedSp, bathSp, balcSp, floorSp };

        for (int i = 0; i < fields.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(new Color(55, 65, 95));
            card.add(lbl, gc);

            gc.gridx = 1; gc.weightx = 0.65;
            card.add(fields[i], gc);
        }

        add(card, BorderLayout.CENTER);

        // ---- Bottom panel ----
        JPanel bot = new JPanel();
        bot.setLayout(new BoxLayout(bot, BoxLayout.Y_AXIS));
        bot.setBackground(new Color(230, 238, 255));
        bot.setBorder(new EmptyBorder(16, 60, 24, 60));

        accLbl = new JLabel("Accuracy: loading...", SwingConstants.CENTER);
        accLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accLbl.setForeground(new Color(100, 120, 160));
        accLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        bot.add(accLbl);
        bot.add(Box.createVerticalStrut(12));

        predictBtn = buildPredictButton();
        predictBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bot.add(predictBtn);
        bot.add(Box.createVerticalStrut(14));

        resultLbl = new JLabel("Enter details and click Predict", SwingConstants.CENTER);
        resultLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
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
                g2.setColor(getModel().isRollover() ? new Color(10, 70, 180) : BLUE);
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

    private JComboBox<String> styleCombo(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(FIELD_BG);
        cb.setBorder(new LineBorder(new Color(200, 215, 245), 1));
        return cb;
    }

    private JSpinner styleSpin(JSpinner sp) {
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setBackground(FIELD_BG);
        return sp;
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
                ((Number)m.get("mae_inr")).doubleValue() / 1e5));
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
