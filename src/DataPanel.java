import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataPanel extends BasePanel {
    private final DefaultTableModel model = new DefaultTableModel() { 
        @Override public boolean isCellEditable(int r, int c) { return false; } 
    };
    private final JTable table = new JTable(model);
    private final List<String[]> rows = new ArrayList<>();
    private final JLabel status = makeLabel("Loading...", 12, false);

    public DataPanel() {
        super("Data Explorer");
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField search = new JTextField(20);
        top.add(makeLabel("Search: ", 13, false)); 
        top.add(search); 
        top.add(status);
        add(top, BorderLayout.NORTH);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(new JScrollPane(table), BorderLayout.CENTER);

        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filter(search.getText().toLowerCase()); }
        });

        loadData();
    }

    @Override public void loadData() {
        new SwingWorker<Void, Void>() {
            String[] headers;
            @Override protected Void doInBackground() throws Exception {
                try (BufferedReader br = new BufferedReader(new FileReader("mumbai-house-price-data-cleaned.csv"))) {
                    String head = br.readLine();
                    if (head == null) return null;
                    headers = head.split(",");
                    String line;
                    while ((line = br.readLine()) != null) rows.add(line.split(",", -1));
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }
            @Override protected void done() {
                if (headers != null) {
                    model.setColumnIdentifiers(headers);
                    filter("");
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        table.getColumnModel().getColumn(i).setPreferredWidth(100);
                    }
                } else {
                    status.setText("Error loading data");
                }
            }
        }.execute();
    }

    private void filter(String q) {
        model.setRowCount(0);
        int c = 0;
        for (String[] r : rows) {
            if (q.isEmpty() || String.join(" ", r).toLowerCase().contains(q)) {
                model.addRow(r);
                if (++c >= 2000) break;
            }
        }
        status.setText(String.format("Showing %d / %d records", c, rows.size()));
    }
}
