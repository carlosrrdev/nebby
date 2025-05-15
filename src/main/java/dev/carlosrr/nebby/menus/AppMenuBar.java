package dev.carlosrr.nebby.menus;

import dev.carlosrr.nebby.panels.ContentPanel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class AppMenuBar extends JMenuBar {

    private ContentPanel contentPanel;

    public AppMenuBar(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;

        JMenu menu = new JMenu("Actions");

        JMenuItem createNewDBIndex = new JMenuItem("Create new DB Index");
        createNewDBIndex.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new dev.carlosrr.nebby.dialogs.CreateNewDBIndex(parentFrame).setVisible(true);
        });

        JMenuItem loadDBFromFile = new JMenuItem("Load DB from file");
        loadDBFromFile.addActionListener(e -> contentPanel.loadDatabaseFile());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        menu.add(createNewDBIndex);
        menu.add(loadDBFromFile);
        menu.addSeparator();
        menu.add(exit);

        setBorder(new MatteBorder(0, 0, 1, 0, new Color(52, 54, 56)));
        add(menu);
    }
}
