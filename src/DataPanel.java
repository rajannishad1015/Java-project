// DataPanel.java
// This panel shows the CSV data in a table so user can see all Mumbai house data
// Extends BasePanel - inheritance concept
// I used JTable and JScrollPane for the table with search feature

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

// inherits from BasePanel (OOP - inheritance)
public class DataPanel extends BasePanel {

    // private variables - data hiding (encapsulation)
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JTextField searchBox;
    private JLabel statusLabel;

    // all rows stored here so we can search without re-reading file
    private java.util.List<String[]> allRows  = new ArrayList<>();
    private String[] columnNames;

    // base directory path
    private String basePath = Paths.get("").toAbsolutePath().toString();

    public DataPanel() {
        super("Data Explorer"); // parent class constructor call
        setupUI();
        loadData();            // abstract method from BasePanel
    }

    // MUST implement this - it is abstract in BasePanel
    @Override
    public void loadData() {
        String csvPath = basePath + File.separator + "mumbai-house-price-data-cleaned.csv";

        // use SwingWorker so UI does not freeze while reading big CSV file
        SwingWorker<Void, Void> loader = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(csvPath));

                    // first line is header
                    String headerLine = reader.readLine();
                    if (headerLine == null) {
                        return null; // file is empty - nothing to do
                    }

                    columnNames = headerLine.split(","); // split by comma

                    // read rest of the rows
                    String line;
                    while ((line = reader.readLine()) != null) {
                        allRows.add(line.split(",", -1)); // -1 to keep empty values
                    }

                    reader.close(); // always close the reader

                } catch (FileNotFoundException e) {
                    // csv file not found
                    showError("CSV file not found at:\n" + csvPath);
                } catch (IOException e) {
                    // some read error
                    showError("Error reading CSV: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                // called after doInBackground finishes - update the table
                try {
                    if (columnNames != null) {
                        // add column headers to table model
                        tableModel.setColumnIdentifiers(columnNames);

                        // only show first 2000 rows (too many rows = slow)
                        int showCount = Math.min(allRows.size(), 2000);
                        for (int i = 0; i < showCount; i++) {
                            tableModel.addRow(allRows.get(i));
                        }

                        // auto-resize columns based on content
                        autoResizeColumns();

                        // update status bar
                        statusLabel.setText("Showing " + showCount + " of " +
                            allRows.size() + " rows | Columns: " + columnNames.length);
                    }
                } catch (Exception e) {
                    // just in case something breaks here
                    showError("Failed to display data: " + e.getMessage());
                }
            }
        };

        loader.execute(); // run the background task
    }

    // sets up all UI components
    private void setupUI() {
        setBackground(new Color(245, 248, 255));

        // top heading
        JLabel heading = makeLabel("Data Explorer - All Mumbai House Records", 17, true);
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setForeground(new Color(30, 80, 160));
        heading.setBorder(new EmptyBorder(12, 0, 8, 0));
        add(heading, BorderLayout.NORTH);

        // search bar at top of center
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        searchPanel.setBackground(new Color(230, 238, 255));

        JLabel searchLabel = makeLabel("Search:", 13, false);
        searchPanel.add(searchLabel);

        searchBox = new JTextField(25);
        searchBox.setFont(new Font("Arial", Font.PLAIN, 13));
        searchBox.setToolTipText("Type to search in any column...");
        searchPanel.add(searchBox);

        statusLabel = makeLabel("Loading data...", 12, false);
        statusLabel.setForeground(new Color(80, 120, 80));
        searchPanel.add(statusLabel);

        // table with model
        // DefaultTableModel is used so we can add/remove rows dynamically
        tableModel = new DefaultTableModel() {
            // override this to make cells not editable
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // user should not edit data
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataTable.setRowHeight(22);
        dataTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dataTable.setGridColor(new Color(200, 210, 230));
        dataTable.setSelectionBackground(new Color(180, 210, 255));

        // style the header
        JTableHeader header = dataTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(new Color(50, 100, 200));
        header.setForeground(Color.BLACK);

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(new LineBorder(new Color(180, 200, 240)));

        // combine search panel and table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane,  BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // search functionality - filter when user types
        searchBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // every time user types a key, filter table
                filterTable(searchBox.getText().trim().toLowerCase());
            }
        });
    }

    // filters table rows based on search text
    private void filterTable(String query) {
        tableModel.setRowCount(0); // clear table first
        int count = 0;

        for (String[] row : allRows) {
            if (count >= 2000) break; // don't show more than 2000

            if (query.isEmpty()) {
                // no search - show all (up to 2000)
                tableModel.addRow(row);
                count++;
            } else {
                // check if any column in this row contains the search text
                boolean found = false;
                for (String cell : row) {
                    if (cell != null && cell.toLowerCase().contains(query)) {
                        found = true;
                        break; // no need to check other cells
                    }
                }
                if (found) {
                    tableModel.addRow(row);
                    count++;
                }
            }
        }

        // update status text
        String filterInfo = query.isEmpty() ? "none" : query;
        statusLabel.setText("Showing " + count + " of " + allRows.size() +
            " rows | Filter: " + filterInfo);
    }

    // auto resize columns so content fits
    private void autoResizeColumns() {
        for (int col = 0; col < dataTable.getColumnCount(); col++) {
            int maxWidth = 80; // minimum width

            // check first 50 rows to find max content width
            for (int row = 0; row < Math.min(dataTable.getRowCount(), 50); row++) {
                Object val = dataTable.getValueAt(row, col);
                if (val != null) {
                    int width = val.toString().length() * 8;
                    if (width > maxWidth) maxWidth = width;
                }
            }

            // cap it at 200px so columns dont get too wide
            dataTable.getColumnModel().getColumn(col).setPreferredWidth(Math.min(maxWidth, 200));
        }
    }
}
