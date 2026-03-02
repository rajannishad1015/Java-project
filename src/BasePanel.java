import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel {
    protected final String title;

    public BasePanel(String title) {
        this.title = title;
        setBackground(new Color(240, 245, 255));
        setLayout(new BorderLayout());
    }

    public abstract void loadData();

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JLabel makeLabel(String text, int fontSize, boolean bold) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, fontSize));
        return lbl;
    }

    public String getPanelTitle() { return title; }
}
