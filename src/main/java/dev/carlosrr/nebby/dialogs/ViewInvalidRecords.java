package dev.carlosrr.nebby.dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;

public class ViewInvalidRecords extends JDialog {

    private JTable invalidRecordsTable;

    /**
     * Creates a dialog that displays the invalid records from the database
     * 
     * @param parent The parent frame
     * @param dbFile The database file to load invalid records from
     */
    public ViewInvalidRecords(Frame parent, File dbFile) {
        super(parent, "Invalid Records", true);

        // Set up the dialog
        setSize(600, 400);
        setResizable(false);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Load the invalid records from the database
        DefaultTableModel tableModel = loadInvalidRecords(dbFile);

        if (tableModel != null) {
            // Create a table with the model
            invalidRecordsTable = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make all cells non-editable
                }
            };

            // Make the table fill the viewport
            invalidRecordsTable.setFillsViewportHeight(true);

            // Add double-click listener to open directory
            invalidRecordsTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = invalidRecordsTable.getSelectedRow();
                        if (row != -1) {
                            // Assuming the path is in the second column (index 1)
                            String path = invalidRecordsTable.getValueAt(row, 1).toString();
                            try {
                                Desktop.getDesktop().open(new File(path));
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(ViewInvalidRecords.this,
                                    "Error opening directory: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });

            // Create a scroll pane for the table
            JScrollPane scrollPane = new JScrollPane(invalidRecordsTable);
            add(scrollPane, BorderLayout.CENTER);

            // Add a close button at the bottom
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
        } else {
            // If no invalid records were found, display a message
            JLabel noRecordsLabel = new JLabel("No invalid records found in the database.");
            noRecordsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(noRecordsLabel, BorderLayout.CENTER);
        }
    }

    /**
     * Loads invalid records from the database
     * 
     * @param dbFile The database file to load invalid records from
     * @return A DefaultTableModel containing the invalid records
     */
    private DefaultTableModel loadInvalidRecords(File dbFile) {
        DefaultTableModel tableModel = null;

        try {
            // Connect to the database
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            Connection connection = DriverManager.getConnection(url);

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute a query to get data from the "invalid" table
            ResultSet resultSet = statement.executeQuery("SELECT filename, path FROM invalid");

            // Get metadata about the result set
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create vectors for column names and data
            Vector<String> columnNames = new Vector<>();

            // Add column names
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            // Create a vector for the data
            Vector<Vector<Object>> data = new Vector<>();

            // Add data rows
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();

                // Add each column value
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getObject(i));
                }

                data.add(row);
            }

            // Create the table model if there is data
            if (!data.isEmpty()) {
                tableModel = new DefaultTableModel(data, columnNames);
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading invalid records: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return tableModel;
    }
}
