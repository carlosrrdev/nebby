package dev.carlosrr.nebby.panels;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import dev.carlosrr.nebby.utils.RecentDBManager;

public class AppPanel extends JPanel {

    private MenuPanel menuPanel;
    private ContentPanel contentPanel;
    private StatusPanel statusPanel;

    public AppPanel() {
        setLayout(new BorderLayout());

        // Initialize panels - StatusPanel must be initialized before ContentPanel
        statusPanel = new StatusPanel();
        contentPanel = new ContentPanel(statusPanel);
        menuPanel = new MenuPanel(contentPanel);

        // Add panels to the layout
        add(menuPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Attempt to load the most recent database file
        SwingUtilities.invokeLater(this::loadRecentDatabaseFile);
    }

    public MenuPanel getMenuPanel() {
        return menuPanel;
    }

    public ContentPanel getContentPanel() {
        return contentPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    /**
     * Attempts to load the most recent database file if one exists.
     */
    private void loadRecentDatabaseFile() {
        String recentDBFilePath = RecentDBManager.getRecentDBFilePath();
        if (recentDBFilePath != null && !recentDBFilePath.isEmpty()) {
            File dbFile = new File(recentDBFilePath);
            if (dbFile.exists() && dbFile.isFile()) {
                // Load the specific database file
                contentPanel.loadSpecificDatabaseFile(dbFile);
            }
        }
    }
}
