package dev.carlosrr.nebby;

import javax.swing.*;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme;
import dev.carlosrr.nebby.panels.AppPanel;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatGitHubDarkIJTheme());
        } catch (UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        JFrame mainFrame = new JFrame("Nebby");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(750, 550);
        mainFrame.setLocationRelativeTo(null);
//        mainFrame.setResizable(false);

        AppPanel appPanel = new AppPanel();
        mainFrame.add(appPanel);

        mainFrame.setVisible(true);
    }
}
