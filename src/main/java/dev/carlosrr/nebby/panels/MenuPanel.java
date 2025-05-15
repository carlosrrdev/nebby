package dev.carlosrr.nebby.panels;

import javax.swing.*;
import java.awt.*;
import dev.carlosrr.nebby.menus.AppMenuBar;

public class MenuPanel extends JPanel {

    private final AppMenuBar menuBar;

    public MenuPanel(ContentPanel contentPanel) {
        setLayout(new BorderLayout());

        menuBar = new AppMenuBar(contentPanel);
        add(menuBar, BorderLayout.NORTH);
    }

    public AppMenuBar getMenuBar() {
        return menuBar;
    }
}
