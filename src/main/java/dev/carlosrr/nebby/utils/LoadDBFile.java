package dev.carlosrr.nebby.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.Vector;

public class LoadDBFile {

    /**
     * Opens a file chooser dialog to select an SQLite database file,
     * then loads the "valid" table from that database.
     * 
     * @param parent The parent component for the file chooser dialog
     * @return An Object array containing the selected File and DefaultTableModel with data from the "valid" table
     */
    public static Object[] loadDatabaseFile(Component parent) {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select SQLite Database File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(parent);

        // If the user selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            DefaultTableModel tableModel = loadDataFromFile(selectedFile);
            if (tableModel != null) {
                return new Object[] { selectedFile, tableModel };
            }
        }

        return null;
    }

    /**
     * Loads a specific database file without showing a file chooser dialog.
     * 
     * @param dbFile The database file to load
     * @return An Object array containing the File and DefaultTableModel with data from the "valid" table, or null if loading fails
     */
    public static Object[] loadSpecificDatabaseFile(File dbFile) {
        DefaultTableModel tableModel = loadDataFromFile(dbFile);
        if (tableModel != null) {
            return new Object[] { dbFile, tableModel };
        }
        return null;
    }

    /**
     * Loads data from the "valid" table in the specified SQLite database file.
     * 
     * @param dbFile The SQLite database file
     * @return A DefaultTableModel containing the data from the "valid" table (excluding the id column)
     */
    private static DefaultTableModel loadDataFromFile(File dbFile) {
        DefaultTableModel tableModel = null;

        try {
            // Connect to the database
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            Connection connection = DriverManager.getConnection(url);

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute a query to get data from the "valid" table
            ResultSet resultSet = statement.executeQuery("SELECT * FROM valid");

            // Get metadata about the result set
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create vectors for column names and data
            Vector<String> columnNames = new Vector<>();

            // Add column names (excluding the id column which is the first column)
            for (int i = 2; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            // Create a vector for the data
            Vector<Vector<Object>> data = new Vector<>();

            // Add data rows
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();

                // Add each column value (excluding the id column)
                for (int i = 2; i <= columnCount; i++) {
                    row.add(resultSet.getObject(i));
                }

                data.add(row);
            }

            // Create the table model
            tableModel = new DefaultTableModel(data, columnNames);

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error loading database: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return tableModel;
    }

    /**
     * Counts the number of records in the "invalid" table in the specified SQLite database file.
     * 
     * @param dbFile The SQLite database file
     * @return The number of records in the "invalid" table, or 0 if an error occurs
     */
    public static int countInvalidRecords(File dbFile) {
        int count = 0;

        try {
            // Connect to the database
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            Connection connection = DriverManager.getConnection(url);

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute a query to count records in the "invalid" table
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM invalid");

            // Get the count
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            // Just return 0 if there's an error
            e.printStackTrace();
        }

        return count;
    }
}
