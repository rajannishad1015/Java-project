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

    public ChartPanel() { super("Charts & Graphs"); loadData(); }

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
        if (statusMsg != null) { g2.drawString(statusMsg, 50, getHeight()/2); return; }

        int w = getWidth()/2 - 20, h = getHeight() - 90;
        drawBars(g2, 10, 30, w, h);
        drawHist(g2, getWidth()/2 + 10, 30, w, h);
    }

    private void drawBars(Graphics2D g2, int x, int y, int w, int h) {
        g2.drawString("Avg Price by Bedrooms (Lakhs)", x + 60, y + 18);
        double max = avgPrices.values().stream().mapToDouble(d->d).max().orElse(1);
        int ax = x + 60, ay2 = y + 50 + h;
        g2.drawLine(ax, y + 50, ax, ay2);
        g2.drawLine(ax, ay2, x + w, ay2);
        
        int bw = (w - 70) / Math.max(1, avgPrices.size()) - 8, bx = ax + 8, i = 0;
        Color[] colors = { new Color(70,130,220), new Color(60,180,130), new Color(220,150,60), new Color(180,80,200), new Color(220,80,100), new Color(60,200,220) };
        for (var e : avgPrices.entrySet()) {
            int bh = (int)(e.getValue() / max * h);
            g2.setColor(colors[i++ % colors.length]);
            g2.fillRoundRect(bx, ay2 - bh, bw, bh, 5, 5);
            g2.setColor(Color.BLACK);
            g2.drawString(String.format("%.0fL", e.getValue()), bx + 3, ay2 - bh - 3);
            g2.drawString(e.getKey() + " BHK", bx + 2, ay2 + 15);
            bx += bw + 8;
        }
    }

    private void drawHist(Graphics2D g2, int x, int y, int w, int h) {
        if (buckets == null) return;
        g2.drawString("Price Distribution (Lakhs)", x + 60, y + 18);
        int max = Arrays.stream(buckets).max().orElse(1);
        int ax = x + 60, ay2 = y + 50 + h, bx = ax, bw = Math.max(1, (w - 65) / 25);
        g2.drawLine(ax, y + 50, ax, ay2);
        g2.drawLine(ax, ay2, x + w, ay2);
        
        for (int i=0; i<25; i++) {
            int bh = (int)(1.0 * buckets[i] / max * h);
            g2.setColor(new Color(50 + i*8, 120 - i*2, 220 - i*4));
            g2.fillRect(bx, ay2 - bh, bw - 1, bh);
            if (i % 5 == 0) g2.drawString(String.format("%.0f", histMin + i * bucketSize), bx, ay2 + 15);
            bx += bw;
        }
    }
}
