package br.com.vrsoftware.desktop_gui;

import br.com.vrsoftware.desktop_gui.ui.OrderFrame;

import javax.swing.SwingUtilities;

public class DesktopGuiApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrderFrame frame = new OrderFrame();
            frame.setVisible(true);
        });
    }
}
