package me.kuwg.clarity.debug;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PerformanceHistogram extends JPanel {

    private final List<Map.Entry<String, Long>> sortedTimings;

    public PerformanceHistogram() {
        this.sortedTimings = MethodTimingRegistry.getSortedTimings();
        this.setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final int max = sortedTimings.stream().mapToInt(e -> e.getValue().intValue()).max().orElse(1);
        final int width = getWidth() - 100;
        final int height = getHeight() - 100;
        final int barWidth = width / sortedTimings.size();

        int x = 50;
        for (final Map.Entry<String, Long> entry : sortedTimings) {
            final int barHeight = (int) (((double) entry.getValue() / max) * height);

            final float proportion = (float) entry.getValue() / max;
            final int red = (int) (proportion * 255);
            final int green = (int) ((1 - proportion) * 255);
            g.setColor(new Color(red, green, 0));
            g.fillRect(x, height + 50 - barHeight, barWidth, barHeight);

            g.setColor(Color.BLACK);
            g.drawString(entry.getKey() + ": " + (entry.getValue() / 1E6) + "ms", x, height + 70);

            x += barWidth;
        }
    }

    public static void showHistogram() {
        final JFrame frame = new JFrame("Method Performance Histogram");
        PerformanceHistogram panel = new PerformanceHistogram();
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
