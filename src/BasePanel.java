// This is the base class for all panels in our project
// I made this abstract class so that every panel must implement the loadData() method
// Teacher told us to use abstract classes for common behavior - Rajan

import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel {

    // every panel has a title - encapsulation using protected
    protected String panelTitle;

    // constructor to set the title
    public BasePanel(String title) {
        this.panelTitle = title;
        setBackground(new Color(240, 245, 255)); // light blue background
        setLayout(new BorderLayout());
    }

    // abstract method - every subclass MUST implement this
    // this is the OOP concept of abstraction
    public abstract void loadData();

    // common method - all panels can use this to show error popups
    // try-catch se already handle hota hai, but ye dialog bhi dikhata hai
    public void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // helper to create a styled label - reuse karo mat baar baar likhna
    public JLabel makeLabel(String text, int fontSize, boolean bold) {
        JLabel lbl = new JLabel(text);
        int style = bold ? Font.BOLD : Font.PLAIN;
        lbl.setFont(new Font("Arial", style, fontSize));
        return lbl;
    }

    // getter for title (encapsulation)
    public String getPanelTitle() {
        return panelTitle;
    }
}
