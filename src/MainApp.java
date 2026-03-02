import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainApp extends JFrame {

    private static final Color HEADER_BG   = new Color(25, 90, 200);
    private static final Color HEADER_SUB  = new Color(170, 205, 255);

    public MainApp() {
        setTitle("Mumbai House Price Analyzer");
        setSize(1080, 700);
        setMinimumSize(new Dimension(860, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(230, 238, 255));
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createTabs(),   BorderLayout.CENTER);
        root.add(createStatus(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER_BG);
        h.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("  Mumbai House Price Analyzer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("ML Model: Gradient Boosting  |  71,938 Properties  ");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        sub.setForeground(HEADER_SUB);
        h.add(sub, BorderLayout.EAST);

        return h;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabs.setBackground(new Color(230, 238, 255));
        tabs.addTab("  Price Predictor  ", new PredictionPanel());
        tabs.addTab("  Charts & Graphs  ", new ChartPanel());
        tabs.addTab("  Data Explorer    ", new DataPanel());
        return tabs;
    }

    private JLabel createStatus() {
        JLabel s = new JLabel("  Project: Mumbai House Price Predictor  |  Dataset: 71,938 entries  |  Model: Gradient Boosting");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s.setForeground(new Color(90, 110, 155));
        s.setOpaque(true);
        s.setBackground(new Color(215, 227, 250));
        s.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return s;
    }

    public static void main(String[] args) {
        if (!new File("models/model.pkl").exists()) {
            int c = JOptionPane.showConfirmDialog(null,
                "Model file not found!\nRun: python python_scripts/train_mumbai_model.py\n\nContinue anyway?",
                "Model Missing", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) System.exit(0);
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainApp();
        });
    }
}
