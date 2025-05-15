package dev.carlosrr.nebby.panels;

import dev.carlosrr.nebby.dialogs.ViewInvalidRecords;
import dev.carlosrr.nebby.utils.LoadDBFile;
import dev.carlosrr.nebby.utils.RecentDBManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class ContentPanel extends JPanel {

    private JPanel centerPanel;
    private JPanel initialPanel;
    private JScrollPane tableScrollPane;
    private StatusPanel statusPanel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public ContentPanel(StatusPanel statusPanel) {
        this.statusPanel = statusPanel;
        setLayout(new BorderLayout());

        // Create the center panel that will hold either the initial view or the table
        centerPanel = new JPanel(new BorderLayout());

        // Create the initial panel with the "No DB loaded" message and load button
        createInitialPanel();

        // Add the initial panel to the center panel
        centerPanel.add(initialPanel, BorderLayout.CENTER);

        // Add the center panel to the content panel
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the initial panel with the "No DB loaded" message and load button
     */
    private void createInitialPanel() {
        initialPanel = new JPanel();
        initialPanel.setLayout(new BoxLayout(initialPanel, BoxLayout.Y_AXIS));
        initialPanel.add(Box.createVerticalGlue());

        JLabel noDbLabel = new JLabel("No DB file has been loaded yet");
        noDbLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loadDbButton = new JButton("Load DB from file");
        loadDbButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadDbButton.addActionListener(e -> loadDatabaseFile());

        initialPanel.add(noDbLabel);
        initialPanel.add(Box.createVerticalStrut(10)); // Add some spacing
        initialPanel.add(loadDbButton);
        initialPanel.add(Box.createVerticalGlue());
    }

    /**
     * Filters the table based on the search text
     * 
     * @param searchText The text to filter by
     */
    private void filterTable(String searchText) {
        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }

        // Update the visible rows count in the status panel
        statusPanel.updateVisibleRows(table.getRowCount());
    }

    /**
     * Loads a specific database file and displays its contents in a table
     * 
     * @param dbFile The database file to load
     * @return true if the file was loaded successfully, false otherwise
     */
    public boolean loadSpecificDatabaseFile(File dbFile) {
        // Use the LoadDBFile utility to load the specific database file
        Object[] result = LoadDBFile.loadSpecificDatabaseFile(dbFile);

        // Process the result
        return processLoadResult(result);
    }

    /**
     * Loads a database file and displays its contents in a table
     */
    public void loadDatabaseFile() {
        // Use the LoadDBFile utility to load a database file
        Object[] result = LoadDBFile.loadDatabaseFile(this);

        // Process the result
        processLoadResult(result);
    }

    /**
     * Processes the result of loading a database file
     * 
     * @param result The result of loading a database file
     * @return true if the file was loaded successfully, false otherwise
     */
    private boolean processLoadResult(Object[] result) {
        // If a result was returned (user selected a file and it was loaded successfully)
        if (result != null) {
            File selectedFile = (File) result[0];
            DefaultTableModel tableModel = (DefaultTableModel) result[1];

            // Save the path to the most recently loaded database file
            RecentDBManager.saveRecentDBFilePath(selectedFile.getAbsolutePath());

            // Create a table with the model
            table = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make all cells non-editable
                }
            };

            // Set up the row sorter for filtering
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);

            // Make the table fill the viewport
            table.setFillsViewportHeight(true);

            // Auto-resize columns to fit their content
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int column = 0; column < table.getColumnCount(); column++) {
                int width = 15; // Min width

                // Get width of column header
                Object headerValue = table.getColumnModel().getColumn(column).getHeaderValue();
                TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        table, headerValue, false, false, 0, column);
                width = Math.max(width, headerComp.getPreferredSize().width);

                // Get width of each cell in the column
                for (int row = 0; row < table.getRowCount(); row++) {
                    Object value = table.getValueAt(row, column);
                    Component comp = table.getDefaultRenderer(table.getColumnClass(column))
                            .getTableCellRendererComponent(table, value, false, false, row, column);
                    width = Math.max(width, comp.getPreferredSize().width);
                }

                // Add some padding
                width += 10;

                // Set the width
                table.getColumnModel().getColumn(column).setPreferredWidth(width);
            }

            // Add double-click listener to open directory
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row != -1) {
                            // Convert row index to model index in case of sorting
                            int modelRow = table.convertRowIndexToModel(row);
                            // The path is in the last column
                            String path = tableModel.getValueAt(modelRow, tableModel.getColumnCount() - 1).toString();
                            try {
                                Desktop.getDesktop().open(new File(path));
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(ContentPanel.this,
                                    "Error opening directory: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });

            // Create a scroll pane for the table
            tableScrollPane = new JScrollPane(table);

            // Remove the initial panel
            centerPanel.removeAll();

            // Add the table scroll pane
            centerPanel.add(tableScrollPane, BorderLayout.CENTER);

            // Create a panel at the top with search field and "View invalid records" button
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            // Add search label and field
            JLabel searchLabel = new JLabel("Search:");
            JTextField searchField = new JTextField(20);
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterTable(searchField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterTable(searchField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterTable(searchField.getText());
                }
            });

            // Add "View invalid records" button
            JButton viewInvalidButton = new JButton("View invalid records");
            viewInvalidButton.addActionListener(e -> {
                // Get the parent frame
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                // Open the ViewInvalidRecords dialog
                new ViewInvalidRecords(parentFrame, selectedFile).setVisible(true);
            });

            topPanel.add(searchLabel);
            topPanel.add(searchField);
            topPanel.add(viewInvalidButton);
            centerPanel.add(topPanel, BorderLayout.NORTH);

            // Update the status panel
            statusPanel.updateDbFile(selectedFile.getName());
            statusPanel.updateTotalRows(tableModel.getRowCount());
            statusPanel.updateVisibleRows(tableModel.getRowCount());

            // Count and update invalid records
            int invalidRecordsCount = LoadDBFile.countInvalidRecords(selectedFile);
            statusPanel.updateInvalidRecords(invalidRecordsCount);

            // Refresh the UI
            centerPanel.revalidate();
            centerPanel.repaint();

            return true;
        }

        return false;
    }
}
