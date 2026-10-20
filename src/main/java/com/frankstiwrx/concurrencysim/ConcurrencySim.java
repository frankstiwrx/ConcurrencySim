package com.frankstiwrx.concurrencysim;

import com.frankstiwrx.concurrencysim.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ConcurrencySim {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}