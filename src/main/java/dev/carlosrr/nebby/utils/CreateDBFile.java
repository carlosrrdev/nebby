package dev.carlosrr.nebby.utils;

import dev.carlosrr.nebby.methods.IndexDBFromDirectory.DirectoryInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.File;
import java.sql.*;
import java.util.List;

public class CreateDBFile {

    /**
     * Creates a SQLite database file with the directory information
     * 
     * @param directoryInfoList List of directory information to add to the database
     * @param parent Parent component for dialog boxes
     * @param splitterInput String to split filename by
     * @param columnsInput Comma-separated column names
     * @param createSeparateTable Whether to create a separate table for invalid entries
     * @return true if the database was created successfully, false otherwise
     */
    public static boolean createDatabase(List<DirectoryInfo> directoryInfoList, Component parent, 
                                        String splitterInput, String columnsInput, boolean createSeparateTable) {
        // Let the user choose where to save the file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save SQLite Database");
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQLite Database (*.db)", "db"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return false; // User cancelled
        }

        // Get the selected file path and ensure it has .db extension
        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".db")) {
            filePath += ".db";
        }

        Connection connection = null;

        try {
            // Create a connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);

            // Create the tables
            createValidTable(connection, columnsInput);

            // Create invalid table if needed
            if (createSeparateTable) {
                createInvalidTable(connection);
            }

            // Insert directory information
            insertDirectoryInfo(connection, directoryInfoList, splitterInput, columnsInput, createSeparateTable);

            JOptionPane.showMessageDialog(parent, 
                "Database created successfully at:\n" + filePath, 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);

            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, 
                "Error creating database: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        } finally {
            // Close the connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates the valid table in the database with dynamic columns based on user input
     * 
     * @param connection Database connection
     * @param columnsInput Comma-separated column names
     * @throws SQLException if an error occurs
     */
    private static void createValidTable(Connection connection, String columnsInput) throws SQLException {
        Statement statement = connection.createStatement();

        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS valid (id INTEGER PRIMARY KEY AUTOINCREMENT");

        // Add dynamic columns if provided
        if (columnsInput != null && !columnsInput.isEmpty()) {
            String[] columns = columnsInput.split(",");
            for (String column : columns) {
                if (!column.trim().isEmpty()) {
                    createTableSQL.append(", ").append(column.trim()).append(" TEXT");
                }
            }
        }

        // Add path column (always present)
        createTableSQL.append(", path TEXT NOT NULL)");

        statement.execute(createTableSQL.toString());
        statement.close();
    }

    /**
     * Creates the invalid table in the database for entries that don't match column count
     * 
     * @param connection Database connection
     * @throws SQLException if an error occurs
     */
    private static void createInvalidTable(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        // Create the invalid table with id, filename, and path columns
        String createTableSQL = "CREATE TABLE IF NOT EXISTS invalid (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "filename TEXT NOT NULL, " +
                "path TEXT NOT NULL)";

        statement.execute(createTableSQL);
        statement.close();
    }

    /**
     * Inserts directory information into the appropriate tables based on user options
     * 
     * @param connection Database connection
     * @param directoryInfoList List of directory information to insert
     * @param splitterInput String to split filename by
     * @param columnsInput Comma-separated column names
     * @param createSeparateTable Whether to create a separate table for invalid entries
     * @throws SQLException if an error occurs
     */
    private static void insertDirectoryInfo(Connection connection, List<DirectoryInfo> directoryInfoList, 
                                           String splitterInput, String columnsInput, boolean createSeparateTable) throws SQLException {
        // Set auto-commit to false for batch operations
        connection.setAutoCommit(false);

        // Prepare statements for valid and invalid tables
        PreparedStatement validStatement = null;
        PreparedStatement invalidStatement = null;

        try {
            // Get column names from columnsInput
            String[] columnNames = (columnsInput != null && !columnsInput.isEmpty()) 
                                  ? columnsInput.split(",") 
                                  : new String[0];

            // Build the SQL for the valid table
            StringBuilder validInsertSQL = new StringBuilder("INSERT INTO valid (");
            StringBuilder validPlaceholders = new StringBuilder();

            // Always start with id (auto-increment)
            validInsertSQL.append("id, ");
            validPlaceholders.append("NULL, ");

            // Add dynamic columns if provided
            for (String column : columnNames) {
                if (!column.trim().isEmpty()) {
                    validInsertSQL.append(column.trim()).append(", ");
                    validPlaceholders.append("?, ");
                }
            }

            // Add path column (always present)
            validInsertSQL.append("path) VALUES (");
            validPlaceholders.append("?)");

            // Combine SQL parts
            String validSQL = validInsertSQL.toString() + validPlaceholders.toString();
            validStatement = connection.prepareStatement(validSQL);

            // Prepare invalid statement if needed
            if (createSeparateTable) {
                invalidStatement = connection.prepareStatement("INSERT INTO invalid (id, filename, path) VALUES (NULL, ?, ?)");
            }

            // Process each directory info
            for (DirectoryInfo info : directoryInfoList) {
                String filename = info.getFilename();
                String path = info.getFullPath();

                // If splitter is empty, just use the original logic
                if (splitterInput == null || splitterInput.isEmpty() || columnNames.length == 0) {
                    // No splitting needed, just insert with path
                    validStatement.setString(1, path);
                    validStatement.addBatch();
                } else {
                    // Split the filename
                    String[] parts = filename.split(splitterInput);

                    // Check if parts match column count
                    if (parts.length == columnNames.length) {
                        // Valid entry - add to valid table
                        for (int i = 0; i < parts.length; i++) {
                            validStatement.setString(i + 1, parts[i]);
                        }
                        validStatement.setString(parts.length + 1, path);
                        validStatement.addBatch();
                    } else if (createSeparateTable) {
                        // Invalid entry - add to invalid table
                        invalidStatement.setString(1, filename);
                        invalidStatement.setString(2, path);
                        invalidStatement.addBatch();
                    }
                    // If not createSeparateTable and parts don't match, skip this entry
                }
            }

            // Execute batches
            validStatement.executeBatch();
            if (createSeparateTable && invalidStatement != null) {
                invalidStatement.executeBatch();
            }

            // Commit the transaction
            connection.commit();
        } finally {
            // Close statements
            if (validStatement != null) {
                validStatement.close();
            }
            if (invalidStatement != null) {
                invalidStatement.close();
            }
        }
    }
}
