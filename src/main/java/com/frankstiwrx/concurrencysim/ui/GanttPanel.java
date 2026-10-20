package com.frankstiwrx.concurrencysim.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GanttPanel extends JPanel {
    private final List<Map<Integer, String>> coreHistory = new ArrayList<>();
    private int currentTime = 0;
    private int maxTime = 30;
    private final int cellWidth = 26;
    private final int rowHeight = 28; // Compacto para caber 1, 2 e 4 CPUs sem rolagem vertical
    private final int startX = 75;
    private final int startY = 24;

    public GanttPanel(int numCores) {
        setBackground(new Color(20, 22, 26));
        resetCores(numCores);
    }

    public final void resetCores(int numCores) {
        coreHistory.clear();
        for (int i = 0; i < numCores; i++) {
            coreHistory.add(new ConcurrentHashMap<>());
        }
        currentTime = 0;
        maxTime = 30;
        atualizarTamanho();
        repaint();
    }

    public void registrarTick(int time, int coreIndex, String taskName) {
        this.currentTime = time;
        if (coreIndex < coreHistory.size()) {
            coreHistory.get(coreIndex).put(time, taskName);
        }
        if (time >= maxTime - 3) {
            maxTime = time + 20;
            atualizarTamanho();
        }
        repaint();
    }

    private void atualizarTamanho() {
        int minWidth = Math.max(1200, startX + (maxTime + 10) * cellWidth);
        int totalHeight = startY + coreHistory.size() * rowHeight + 18;
        setPreferredSize(new Dimension(minWidth, totalHeight));
        revalidate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int totalGridCols = Math.max(maxTime + 10, (getWidth() - startX) / cellWidth + 5);

        // Linhas de CPU
        for (int c = 0; c < coreHistory.size(); c++) {
            int y = startY + c * rowHeight;
            g2.setColor(new Color(28, 32, 38));
            g2.fillRoundRect(8, y + 1, getWidth() - 16, rowHeight - 2, 6, 6);
            g2.setColor(new Color(45, 52, 62));
            g2.drawRoundRect(8, y + 1, getWidth() - 16, rowHeight - 2, 6, 6);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("CPU " + c, 16, y + 18);
        }

        // Marcadores do tempo
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        for (int t = 0; t <= totalGridCols; t++) {
            int x = startX + t * cellWidth;

            if (t % 5 == 0) {
                g2.setColor(new Color(70, 80, 95));
                g2.drawLine(x, startY - 6, x, startY + coreHistory.size() * rowHeight + 2);
                g2.setColor(new Color(180, 190, 205));
                g2.drawString(String.valueOf(t), x - 4, startY - 10);
            } else {
                g2.setColor(new Color(40, 45, 55));
                g2.drawLine(x, startY - 2, x, startY + coreHistory.size() * rowHeight);
            }
        }

        // Blocos de Tarefas
        for (int c = 0; c < coreHistory.size(); c++) {
            int y = startY + c * rowHeight;
            Map<Integer, String> history = coreHistory.get(c);

            for (Map.Entry<Integer, String> entry : history.entrySet()) {
                int time = entry.getKey();
                String taskName = entry.getValue();
                int x = startX + (time - 1) * cellWidth;

                if ("IDLE".equals(taskName) || "-".equals(taskName)) {
                    g2.setColor(new Color(38, 42, 50));
                    g2.fillRect(x + 1, y + 3, cellWidth - 2, rowHeight - 6);
                    g2.setColor(new Color(60, 68, 80));
                    g2.drawRect(x + 1, y + 3, cellWidth - 2, rowHeight - 6);
                } else {
                    // Usa long para evitar overflow ao calcular as cores de cada tarefa.
                    long hash = Integer.toUnsignedLong(taskName.hashCode());
                    Color blockColor = new Color(
                        50 + (int) ((hash * 73) % 160),
                        50 + (int) ((hash * 101) % 160),
                        60 + (int) ((hash * 137) % 160)
                    );

                    g2.setColor(blockColor);
                    g2.fillRoundRect(x + 1, y + 3, cellWidth - 2, rowHeight - 6, 4, 4);
                    g2.setColor(Color.WHITE);
                    g2.drawRoundRect(x + 1, y + 3, cellWidth - 2, rowHeight - 6, 4, 4);

                    g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                    String label = taskName.length() > 4 ? taskName.substring(0, 4) : taskName;
                    g2.drawString(label, x + 2, y + 17);
                }
            }
        }

        // Indicador Vermelho de Posição do Tempo Atual
        if (currentTime > 0) {
            int currentX = startX + currentTime * cellWidth;
            g2.setColor(new Color(255, 60, 60));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(currentX, startY - 12, currentX, startY + coreHistory.size() * rowHeight + 4);
            g2.fillOval(currentX - 3, startY - 14, 6, 6);
        }
    }
}