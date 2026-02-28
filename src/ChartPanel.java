// ChartPanel.java
// I made this panel to show charts for the house price data
// It extends BasePanel (inheritance) and uses Java's Graphics2D for drawing
// Drawing charts manually to understand how painting works in Java AWT

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

// inherits from BasePanel - OOP inheritance
public class ChartPanel extends BasePanel {

    // --- private data (encapsulation) ---
    // stores average price for each bedroom count  e.g. {2 -> 75.5 Lakhs}
    private Map<Integer, Double> avgPriceByBedroom = new TreeMap<>();

    // stores price distribution buckets for histogram
    private int[] priceBuckets;
    private double histMinPrice, histMaxPrice, bucketSize;

    private boolean isDataLoaded = false;
    private String errorMessage  = "Loading data, please wait...";

    private String basePath = Paths.get("").toAbsolutePath().toString();

    // Constructor
    public ChartPanel() {
        super("Charts"); // parent constructor
        setBackground(new Color(245, 248, 255));
        loadData(); // abstract method from BasePanel
    }

    // MUST implement this abstract method from BasePanel
    @Override
    public void loadData() {
        String csvPath = basePath + File.separator + "mumbai-house-price-data-cleaned.csv";

        // load CSV in background thread so app doesn't freeze
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            // data structures to build while reading
            Map<Integer, java.util.List<Long>> roomWisePrices = new TreeMap<>();
            java.util.List<Double> allPricesLakhs = new ArrayList<>();

            @Override
            protected Void doInBackground() {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(csvPath));
                    String headerLine = reader.readLine();

                    if (headerLine == null) {
                        errorMessage = "CSV file is empty.";
                        return null;
                    }

                    // find index of price and bedroom columns
                    String[] headers = headerLine.split(",");
                    int priceCol   = -1;
                    int bedroomCol = -1;

                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].equalsIgnoreCase("price"))       priceCol   = i;
                        if (headers[i].equalsIgnoreCase("bedroom_num")) bedroomCol = i;
                    }

                    if (priceCol == -1) {
                        errorMessage = "Price column not found in CSV.";
                        return null;
                    }

                    // read all rows
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",", -1);
                        try {
                            long price = Long.parseLong(parts[priceCol].trim());
                            allPricesLakhs.add(price / 100000.0); // convert to lakhs

                            // bedroom-wise grouping
                            if (bedroomCol >= 0 && bedroomCol < parts.length) {
                                // some values have .0 so remove decimal part
                                String bedStr = parts[bedroomCol].trim().split("\\.")[0];
                                int beds = Integer.parseInt(bedStr);

                                if (beds >= 1 && beds <= 6) {
                                    // add to the list for that bedroom count
                                    roomWisePrices
                                        .computeIfAbsent(beds, k -> new ArrayList<>())
                                        .add(price);
                                }
                            }

                        } catch (NumberFormatException e) {
                            // skip rows with invalid numbers - they exist in real datasets
                            continue;
                        }
                    }

                    reader.close();

                } catch (FileNotFoundException e) {
                    errorMessage = "CSV not found. Put CSV in project root folder.";
                    return null;
                } catch (IOException e) {
                    errorMessage = "Error reading file: " + e.getMessage();
                    return null;
                }

                // calculate averages for bar chart
                for (Map.Entry<Integer, java.util.List<Long>> entry : roomWisePrices.entrySet()) {
                    double avg = entry.getValue().stream()
                                      .mapToLong(l -> l).average().orElse(0);
                    avgPriceByBedroom.put(entry.getKey(), avg / 100000.0); // in Lakhs
                }

                // build histogram buckets for price distribution
                if (!allPricesLakhs.isEmpty()) {
                    Collections.sort(allPricesLakhs);
                    // use 5th to 95th percentile to ignore extreme outliers
                    int lowIdx  = (int)(allPricesLakhs.size() * 0.05);
                    int highIdx = (int)(allPricesLakhs.size() * 0.95);
                    histMinPrice = allPricesLakhs.get(lowIdx);
                    histMaxPrice = allPricesLakhs.get(highIdx);

                    int BINS = 25;
                    bucketSize   = (histMaxPrice - histMinPrice) / BINS;
                    priceBuckets = new int[BINS];

                    for (double val : allPricesLakhs) {
                        int idx = (int)((val - histMinPrice) / bucketSize);
                        if (idx >= 0 && idx < BINS) {
                            priceBuckets[idx]++;
                        }
                    }
                }

                isDataLoaded = true;
                return null;
            }

            @Override
            protected void done() {
                repaint(); // redraw everything once data is loaded
            }
        };

        worker.execute();
    }

    // This is called by Java automatically to draw the component
    // We override paintComponent from JPanel (which extends Component)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // important - call parent first to clear background

        Graphics2D g2 = (Graphics2D) g; // cast to Graphics2D for better drawing

        // enable anti-aliasing to make drawing look smooth
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int W = getWidth();
        int H = getHeight();

        // fill background
        g2.setColor(new Color(245, 248, 255));
        g2.fillRect(0, 0, W, H);

        if (!isDataLoaded) {
            // data not loaded yet - show message
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, 15));
            g2.drawString(errorMessage, 50, H / 2);
            return;
        }

        // Draw two charts side by side
        // Left half: bar chart for avg price by bedrooms
        // Right half: histogram for price distribution

        int halfW  = W / 2 - 20;
        int padTop = 50;  // space for chart title
        int padBot = 40;  // space for x axis labels
        int padLeft= 60;  // space for y axis labels
        int chartH = H - padTop - padBot - 20;

        // --- Draw Left Chart: Average Price by Bedrooms ---
        drawBarChart(g2, 10, 30, halfW, padLeft, padTop, padBot, chartH);

        // --- Draw Right Chart: Price Distribution Histogram ---
        drawHistogram(g2, W / 2 + 10, 30, halfW, padLeft, padTop, padBot, chartH);
    }

    // Draws a bar chart showing average price for 1-6 BHK
    private void drawBarChart(Graphics2D g2, int x0, int y0,
                               int chartWidth, int padLeft, int padTop, int padBot, int chartH) {

        if (avgPriceByBedroom.isEmpty()) return;

        // chart title
        g2.setColor(new Color(30, 80, 160));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Avg Price by Bedrooms (Lakhs)", x0 + padLeft, y0 + 18);

        // find max value for scaling
        double maxVal = avgPriceByBedroom.values().stream().mapToDouble(d -> d).max().orElse(1);

        // draw axes
        g2.setColor(new Color(100, 100, 150));
        int axisX  = x0 + padLeft;
        int axisY  = y0 + padTop;
        int axisY2 = axisY + chartH;
        int axisX2 = x0 + chartWidth;

        g2.drawLine(axisX, axisY, axisX, axisY2);  // Y axis
        g2.drawLine(axisX, axisY2, axisX2, axisY2); // X axis

        // draw Y-axis grid lines and labels
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        for (int i = 0; i <= 5; i++) {
            int yPos = axisY2 - (int)((i / 5.0) * chartH);
            g2.setColor(new Color(200, 215, 240));
            g2.drawLine(axisX, yPos, axisX2, yPos); // horizontal grid line
            g2.setColor(new Color(80, 80, 120));
            g2.drawString(String.format("%.0f", (i / 5.0) * maxVal), x0 + 5, yPos + 4);
        }

        // draw each bar
        int numBars = avgPriceByBedroom.size();
        int barWidth = (chartWidth - padLeft - 10) / numBars - 8;

        Color[] barColors = {
            new Color(70, 130, 220),
            new Color(60, 180, 130),
            new Color(220, 150, 60),
            new Color(180, 80, 200),
            new Color(220, 80, 100),
            new Color(60, 200, 220)
        };

        int barX = axisX + 8;
        int colorIdx = 0;

        for (Map.Entry<Integer, Double> entry : avgPriceByBedroom.entrySet()) {
            double val = entry.getValue();
            int barH   = (int)(val / maxVal * chartH);
            int barY   = axisY2 - barH;

            // fill bar with color
            g2.setColor(barColors[colorIdx % barColors.length]);
            g2.fillRoundRect(barX, barY, barWidth, barH, 5, 5);

            // draw value on top of bar
            g2.setColor(new Color(40, 40, 80));
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            String valStr = String.format("%.0fL", val);
            g2.drawString(valStr, barX + 3, barY - 3);

            // x-axis label (bedroom count)
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(60, 60, 100));
            String xLabel = entry.getKey() + " BHK";
            g2.drawString(xLabel, barX + 2, axisY2 + 15);

            barX += barWidth + 8;
            colorIdx++;
        }
    }

    // Draws a histogram for price distribution
    private void drawHistogram(Graphics2D g2, int x0, int y0,
                                int chartWidth, int padLeft, int padTop, int padBot, int chartH) {

        if (priceBuckets == null) return;

        // chart title
        g2.setColor(new Color(30, 80, 160));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("Price Distribution (in Lakhs)", x0 + padLeft, y0 + 18);

        // find max bucket count for scaling Y axis
        int maxCount = 0;
        for (int count : priceBuckets) {
            if (count > maxCount) maxCount = count;
        }
        if (maxCount == 0) return;

        // axes
        g2.setColor(new Color(100, 100, 150));
        int axisX  = x0 + padLeft;
        int axisY  = y0 + padTop;
        int axisY2 = axisY + chartH;
        int axisX2 = x0 + chartWidth - 5;

        g2.drawLine(axisX, axisY, axisX, axisY2);
        g2.drawLine(axisX, axisY2, axisX2, axisY2);

        // Y axis labels
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= 4; i++) {
            int yPos = axisY2 - (int)((i / 4.0) * chartH);
            g2.setColor(new Color(200, 215, 240));
            g2.drawLine(axisX, yPos, axisX2, yPos);
            g2.setColor(new Color(80, 80, 120));
            g2.drawString(String.format("%,d", (int)((i / 4.0) * maxCount)), x0 + 3, yPos + 4);
        }

        // draw histogram bars
        int numBins  = priceBuckets.length;
        int binWidth = Math.max(1, (axisX2 - axisX) / numBins);

        for (int i = 0; i < numBins; i++) {
            int barH = (int)((1.0 * priceBuckets[i] / maxCount) * chartH);
            int barX = axisX + i * binWidth;
            int barY = axisY2 - barH;

            // color changes from blue to orange along x axis
            float t = (float) i / numBins;
            Color barColor = new Color(
                (int)(50  + t * 200),   // R increases
                (int)(120 - t * 50),    // G decreases slightly
                (int)(220 - t * 120)    // B decreases
            );
            g2.setColor(barColor);
            g2.fillRect(barX, barY, binWidth - 1, barH);
        }

        // X-axis labels (price values in lakhs) - every 5 bins
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(60, 60, 100));
        for (int i = 0; i < numBins; i += 5) {
            double labelVal = histMinPrice + i * bucketSize;
            int labelX = axisX + i * binWidth;
            g2.drawString(String.format("%.0f", labelVal), labelX, axisY2 + 14);
        }
    }
}
