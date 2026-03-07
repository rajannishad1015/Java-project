import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.LineBorder;
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
    private final JLabel status = new JLabel("Loading...");

    public DataPanel() {
        super("Data Explorer");
        setBorder(new javax.swing.border.EmptyBorder(20, 40, 20, 40));

        JPanel mainCard = createCard();
        mainCard.setLayout(new BorderLayout(15, 15));

        // --- Search bar area ---
        JPanel top = new JPanel(new BorderLayout(15, 0));
        top.setOpaque(false);
        
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBox.setOpaque(false);
        
        JTextField search = new JTextField(25);
        styleField(search);
        search.setPreferredSize(new Dimension(300, 35));
        
        JLabel searchIcon = new JLabel("🔍 Search Properties: ");
        searchIcon.setFont(FONT_BOLD);
        searchIcon.setForeground(THEME_BLUE);
        
        searchBox.add(searchIcon);
        searchBox.add(search);
        top.add(searchBox, BorderLayout.WEST);

        status.setFont(FONT_SMALL);
        status.setForeground(new Color(100, 110, 140));
        top.add(status, BorderLayout.EAST);
        
        mainCard.add(top, BorderLayout.NORTH);

        // --- Table area ---
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFont(FONT_TEXT);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setSelectionForeground(Color.BLACK);
        
        // Custom Table Header
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(BG_BLUE);
        header.setForeground(THEME_BLUE);
        header.setPreferredSize(new Dimension(100, 40));
        header.setReorderingAllowed(false);

        // Custom Table Renderer for Alternating Colors & Padding
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!s) {
                    l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 252, 255));
                }
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(230, 235, 245), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        mainCard.add(scroll, BorderLayout.CENTER);

        add(mainCard, BorderLayout.CENTER);

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
                    if (head != null) {
                        headers = head.split(",");
                        String line;
                        while ((line = br.readLine()) != null) rows.add(line.split(",", -1));
                    }
                } catch (Exception e) { System.err.println("Data: " + e.getMessage()); }
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
        status.setText(String.format("  Showing %d / %d records  ", c, rows.size()));
    }
}
