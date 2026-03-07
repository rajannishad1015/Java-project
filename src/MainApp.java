import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MainApp extends JFrame {

    private String accuracy = "89.6%";

    public MainApp() {
        // Load accuracy from JSON if available
        try {
            String json = Files.readString(Paths.get("models/meta.json"));
            JSONObject obj = (JSONObject) new JSONParser().parse(json);
            JSONObject m = (JSONObject) obj.get("metrics");
            if (m != null) {
                accuracy = String.format("%.1f%%", ((Number)m.get("r2")).doubleValue() * 100);
            }
        } catch (Exception ignored) {}

        setTitle("Mumbai House Price Analyzer");
        setSize(1080, 700);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Root panel Setup
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BasePanel.BG_BLUE);
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createTabs(),   BorderLayout.CENTER);
        root.add(createStatus(), BorderLayout.SOUTH);

        setVisible(true);
    }


    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(BasePanel.THEME_BLUE);
        h.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("  Mumbai House Price Analyzer");
        title.setFont(BasePanel.FONT_TITLE.deriveFont(24f));
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("ML Model: AutoGluon  |  Accuracy: " + accuracy + "  |  70,873 Properties  ");
        sub.setFont(BasePanel.FONT_TEXT.deriveFont(Font.ITALIC));
        sub.setForeground(new Color(170, 205, 255));
        h.add(sub, BorderLayout.EAST);

        return h;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BasePanel.FONT_BOLD);
        tabs.setBackground(BasePanel.BG_BLUE);
        tabs.addTab("  Price Predictor  ", new PredictionPanel());
        tabs.addTab("  Charts & Graphs  ", new ChartPanel());
        tabs.addTab("  Data Explorer    ", new DataPanel());
        return tabs;
    }

    private JLabel createStatus() {
        JLabel s = new JLabel("  Project: Mumbai House Price Predictor  |  Dataset: 70,873 entries  |  Model: AutoGluon Ensemble  |  Model Accuracy: " + accuracy);
        s.setFont(BasePanel.FONT_SMALL);
        s.setForeground(new Color(90, 110, 155));
        s.setOpaque(true);
        s.setBackground(new Color(215, 227, 250));
        s.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return s;
    }

    public static void main(String[] args) {
        if (!new File("models/meta.json").exists()) {
            int c = JOptionPane.showConfirmDialog(null,
                "Model metadata not found!\nRun: python python_scripts/train_mumbai_model.py\n\nContinue anyway?",
                "Model Missing", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) System.exit(0);
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainApp();
        });
    }
}
