import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ChartPanel extends BasePanel {
    private final Map<Integer, Double> avgPrices = new TreeMap<>();
    private int[] buckets;
    private double histMin, histMax, bucketSize;
    private String statusMsg = "Loading data...";
    
    // Smooth animation/transition colors
    private static final Color BAR_GRAD_1 = new Color(50, 120, 220);
    private static final Color BAR_GRAD_2 = new Color(100, 180, 255);
    private static final Color GRID_COLOR = new Color(230, 235, 245);

    public ChartPanel() { 
        super("Charts & Graphs"); 
        loadData(); 
    }

    @Override public void loadData() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                try (BufferedReader br = new BufferedReader(new FileReader("mumbai-house-price-data-cleaned.csv"))) {
                    String[] head = br.readLine().split(",");
                    int pCol = -1, bCol = -1;
                    for (int i=0; i<head.length; i++) {
                        if (head[i].equalsIgnoreCase("price")) pCol = i;
                        if (head[i].equalsIgnoreCase("bedroom_num")) bCol = i;
                    }
                    if (pCol < 0) { statusMsg = "Price column missing."; return null; }

                    Map<Integer, List<Long>> grouped = new HashMap<>();
                    List<Double> allPrices = new ArrayList<>();
                    String line;
                    while ((line = br.readLine()) != null) {
                        try {
                            String[] p = line.split(",", -1);
                            long price = Long.parseLong(p[pCol].trim());
                            allPrices.add(price / 1e5);
                            if (bCol >= 0) {
                                int beds = (int) Double.parseDouble(p[bCol].trim());
                                if (beds > 0 && beds <= 6) grouped.computeIfAbsent(beds, k -> new ArrayList<>()).add(price);
                            }
                        } catch (Exception ignored) {}
                    }
                    grouped.forEach((k, v) -> avgPrices.put(k, v.stream().mapToLong(l->l).average().orElse(0) / 1e5));

                    if (!allPrices.isEmpty()) {
                        Collections.sort(allPrices);
                        histMin = allPrices.get((int)(allPrices.size()*.05));
                        histMax = allPrices.get((int)(allPrices.size()*.95));
                        bucketSize = (histMax - histMin) / 25;
                        buckets = new int[25];
                        for (double val : allPrices) {
                            int idx = (int)((val - histMin) / bucketSize);
                            if (idx >= 0 && idx < 25) buckets[idx]++;
                        }
                    }
                    statusMsg = null;
                } catch (Exception e) { statusMsg = "Error: " + e.getMessage(); }
                return null;
            }
            @Override protected void done() { repaint(); }
        }.execute();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (statusMsg != null) { 
            g2.setFont(FONT_BOLD);
            g2.setColor(TEXT_DARK);
            g2.drawString(statusMsg, getWidth()/2 - 50, getHeight()/2); 
            return; 
        }

        // Divide area into two cards
        int padding = 25;
        int cardW = (getWidth() - padding * 3) / 2;
        int cardH = getHeight() - padding * 2;

        drawChartCard(g2, padding, padding, cardW, cardH, "Average Price by BHK (Lakhs)", true);
        drawChartCard(g2, padding * 2 + cardW, padding, cardW, cardH, "Price Distribution (Lakhs)", false);
    }

    private void drawChartCard(Graphics2D g2, int x, int y, int w, int h, String title, boolean isBar) {
        // Draw Card Shadow & Background
        g2.setColor(new Color(0, 0, 0, 20)); // Soft shadow
        g2.fillRoundRect(x + 3, y + 3, w, h, 15, 15);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, 15, 15);
        g2.setColor(new Color(220, 225, 240));
        g2.drawRoundRect(x, y, w, h, 15, 15);

        // Draw Title
        g2.setFont(FONT_BOLD);
        g2.setColor(THEME_BLUE);
        int titleY = y + 30;
        g2.drawString(title, x + 25, titleY);

        // Chart area
        int cx = x + 50;
        int cy = titleY + 30;
        int cw = w - 80;
        int ch = h - 100;

        // Draw Grid Lines
        g2.setColor(GRID_COLOR);
        for (int i = 0; i <= 5; i++) {
            int gy = cy + ch - (i * ch / 5);
            g2.drawLine(cx, gy, cx + cw, gy);
        }

        if (isBar) drawBars(g2, cx, cy, cw, ch);
        else drawHist(g2, cx, cy, cw, ch);
    }

    private void drawBars(Graphics2D g2, int x, int y, int w, int h) {
        if (avgPrices.isEmpty()) return;
        double max = avgPrices.values().stream().mapToDouble(d->d).max().orElse(1);
        int n = avgPrices.size();
        int bw = (w / n) - 15;
        int i = 0;

        for (var e : avgPrices.entrySet()) {
            int bh = (int)(e.getValue() / max * h);
            int bx = x + i * (w / n) + 5;
            int by = y + h - bh;

            // Gradient Bar
            g2.setPaint(new GradientPaint(bx, by, BAR_GRAD_1, bx, by + bh, BAR_GRAD_2));
            g2.fillRoundRect(bx, by, bw, bh, 8, 8);

            // Labels
            g2.setColor(TEXT_DARK);
            g2.setFont(FONT_SMALL);
            g2.drawString(e.getKey() + " BHK", bx + (bw/2) - 15, y + h + 20);
            
            g2.setFont(FONT_SMALL.deriveFont(Font.BOLD));
            g2.drawString(String.format("%.0fL", e.getValue()), bx + (bw/2) - 12, by - 8);
            i++;
        }
    }

    private void drawHist(Graphics2D g2, int x, int y, int w, int h) {
        if (buckets == null) return;
        int max = Arrays.stream(buckets).max().orElse(1);
        int bw = w / buckets.length;

        for (int i = 0; i < buckets.length; i++) {
            int bh = (int)(1.0 * buckets[i] / max * h);
            int bx = x + i * bw;
            int by = y + h - bh;

            // Hist bar with color scale
            g2.setColor(new Color(50 + i * 5, 120, 220 - i * 3));
            g2.fillRect(bx, by, bw - 1, bh);

            if (i % 5 == 0) {
                g2.setColor(TEXT_DARK);
                g2.setFont(FONT_SMALL);
                g2.drawString(String.format("%.0f", histMin + i * bucketSize), bx, y + h + 20);
            }
        }
    }
}
