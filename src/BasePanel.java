import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public abstract class BasePanel extends JPanel {
    // ---- Design Tokens (Easy to change in one place) ----
    protected static final Color THEME_BLUE = new Color(25, 90, 200);
    protected static final Color BG_LIGHT    = new Color(235, 243, 255);
    protected static final Color BG_BLUE     = new Color(230, 238, 255);
    protected static final Color FIELD_BG    = new Color(248, 250, 255);
    protected static final Color TEXT_DARK   = new Color(55, 65, 95);

    protected static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    protected static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    protected static final Font FONT_TEXT  = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    protected final String title;

    public BasePanel(String title) {
        this.title = title;
        setBackground(BG_BLUE);
        setLayout(new BorderLayout());
    }

    public abstract void loadData();

    // ---- UI Helpers to make code "Easy" ----
    
    protected JLabel createTitle(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(THEME_BLUE);
        lbl.setBorder(new EmptyBorder(20, 0, 15, 0));
        return lbl;
    }

    protected JPanel createCard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 215, 245), 1, true),
            new EmptyBorder(20, 30, 20, 30)
        ));
        return p;
    }

    protected void styleField(JComponent c) {
        c.setFont(FONT_TEXT);
        c.setBackground(FIELD_BG);
        if (c instanceof JComboBox || c instanceof JTextField) {
            c.setBorder(new LineBorder(new Color(200, 215, 245), 1));
        }
        if (c instanceof JSpinner) {
            JSpinner s = (JSpinner) c;
            ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setBackground(FIELD_BG);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
