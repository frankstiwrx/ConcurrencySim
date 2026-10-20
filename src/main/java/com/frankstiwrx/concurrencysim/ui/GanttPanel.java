package com.frankstiwrx.concurrencysim.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GanttPanel extends JPanel {
    private final List<Map<Integer, String>> coreHistory = new ArrayList<>();
    private int maxTime = 30;

    public GanttPanel(int numCores) {
        setPreferredSize(new Dimension(800, 140));
        setBackground(new Color(25, 25, 25));
        resetCores(numCores);
    }

    public final void resetCores(int numCores) {
        coreHistory.clear();
        for (int i = 0; i < numCores; i++) {
            coreHistory.add(new HashMap<>());
        }
        maxTime = 30;
        repaint();
    }

    public void registrarTick(int time, int coreIndex, String taskName) {
        if (coreIndex < coreHistory.size()) {
            coreHistory.get(coreIndex).put(time, taskName);
        }
        if (time >= maxTime - 2) {
            maxTime += 10;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellWidth = Math.max(18, (getWidth() - 100) / Math.max(1, maxTime));
        int rowHeight = 35;
        int startX = 75;
        int startY = 30;

        // Desenha eixos e grade de tempo
        g2.setColor(new Color(150, 150, 150));
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));

        for (int t = 0; t <= maxTime; t++) {
            int x = startX + t * cellWidth;
            g2.setColor(new Color(60, 60, 60));
            g2.drawLine(x, startY - 5, x, startY + coreHistory.size() * rowHeight);

            if (t % 5 == 0) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawString(String.valueOf(t), x - 4, startY - 10);
            }
        }

        // Desenha o histórico por CPU Core
        for (int c = 0; c < coreHistory.size(); c++) {
            int y = startY + c * rowHeight;
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("CPU " + c + ":", 15, y + 22);

            Map<Integer, String> history = coreHistory.get(c);
            for (Map.Entry<Integer, String> entry : history.entrySet()) {
                int time = entry.getKey();
                String taskName = entry.getValue();
                int x = startX + time * cellWidth;

                // Cor gerada a partir do nome da tarefa
                int hash = Math.abs(taskName.hashCode());
                Color blockColor = new Color((hash * 65) % 180 + 50, (hash * 110) % 180 + 50, (hash * 160) % 180 + 50);

                g2.setColor(blockColor);
                g2.fillRect(x + 1, y + 4, cellWidth - 2, rowHeight - 8);
                g2.setColor(Color.WHITE);
                g2.drawRect(x + 1, y + 4, cellWidth - 2, rowHeight - 8);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                String label = taskName.length() > 4 ? taskName.substring(0, 4) : taskName;
                g2.drawString(label, x + 2, y + 20);
            }
        }
    }
}