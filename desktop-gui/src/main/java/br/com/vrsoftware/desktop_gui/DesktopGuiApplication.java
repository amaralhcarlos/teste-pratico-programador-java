package br.com.vrsoftware.desktop_gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class DesktopGuiApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DesktopGuiApplication::createAndShow);
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Sistema de Pedidos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.add(new JLabel("Cliente desktop do sistema de pedidos", JLabel.CENTER), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
