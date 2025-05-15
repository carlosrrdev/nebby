package dev.carlosrr.nebby.dialogs;

import dev.carlosrr.nebby.methods.IndexDBFromDirectory;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.File;

public class CreateNewDBIndex extends JDialog {

    private String selectedDirectoryPath;
    private JTextField directoryPathField;
    private JTextField splitterInputField;
    private JTextField columnsInputField;
    private JLabel errorLabel;
    private JRadioButton createTableRadio;
    private JRadioButton skipRadio;
    private JProgressBar progressBar;
    private IndexDBFromDirectory indexer;
    private JButton startButton;

    public CreateNewDBIndex(JFrame parent) {
        super(parent, "Create New DB Index", true);

        setSize(800, 300);
        setResizable(false);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JPanel middlePanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        configureTopPanel(topPanel);

        configureMiddlePanel(middlePanel);

        configureBottomPanel(bottomPanel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void configureTopPanel(JPanel topPanel) {
        topPanel.setLayout(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel directoryLabel = new JLabel("Select directory");

        directoryPathField = new JTextField();
        directoryPathField.setEditable(false);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> selectDirectory());

        topPanel.add(directoryLabel, BorderLayout.WEST);
        topPanel.add(directoryPathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);
    }

    private void configureMiddlePanel(JPanel middlePanel) {
        middlePanel.setBorder(new CompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder("Options")
        ));
        middlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel splitterInputLabel = new JLabel("Split filename by");
        splitterInputField = new JTextField("_");
        splitterInputField.setPreferredSize(new Dimension(30, 25));

        PlainDocument doc = (PlainDocument) splitterInputField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String resultText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                if (resultText.length() <= 1) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (currentText.length() + string.length() <= 1) {
                    super.insertString(fb, offset, string, attr);
                }
            }
        });

        splitterInputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateColumnsFieldState();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateColumnsFieldState();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateColumnsFieldState();
            }

            private void updateColumnsFieldState() {
                String text = splitterInputField.getText();
                columnsInputField.setEnabled(!text.isEmpty());
                validateStartButton();
            }
        });

        middlePanel.add(splitterInputLabel);
        middlePanel.add(splitterInputField);

        JLabel columnsInputLabel = new JLabel("Enter column names separated by comma");
        columnsInputField = new JTextField();
        columnsInputField.setPreferredSize(new Dimension(325, 25));

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
        errorLabel = new JLabel("Spaces are not allowed in column names");
        errorLabel.setForeground(new Color(246, 110, 135));
        errorLabel.setVisible(false);

        JLabel infoLabel = new JLabel("If no splitter is provided, the table columns will be 'filename','full_path'");
        labelsPanel.add(errorLabel);
        labelsPanel.add(infoLabel);



        columnsInputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                checkForSpaces();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                checkForSpaces();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                checkForSpaces();
            }

            private void checkForSpaces() {
                String text = columnsInputField.getText();
                errorLabel.setVisible(text.contains(" "));
                validateStartButton();
            }
        });

        // Create a panel for radio buttons
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
        radioPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel radioLabel = new JLabel("Choose what to do with files that do not meet the requirements: ");
        radioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioPanel.add(radioLabel);

        JPanel radioButtonsPanel = new JPanel();
        radioButtonsPanel.setLayout(new BoxLayout(radioButtonsPanel, BoxLayout.X_AXIS));
        ButtonGroup buttonGroup = new ButtonGroup();

        createTableRadio = new JRadioButton("Create separate table", true);
        skipRadio = new JRadioButton("Skip and ignore");

        buttonGroup.add(createTableRadio);
        buttonGroup.add(skipRadio);

        radioButtonsPanel.add(createTableRadio);
        radioButtonsPanel.add(skipRadio);

        radioPanel.add(radioButtonsPanel);

        middlePanel.add(columnsInputLabel);
        middlePanel.add(columnsInputField);
        middlePanel.add(labelsPanel);
        middlePanel.add(radioPanel);
    }

    private void selectDirectory() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Select a directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedDirectoryPath = selectedFile.getAbsolutePath();
            directoryPathField.setText(selectedDirectoryPath);
        }
    }

    public String getSelectedDirectoryPath() {
        return selectedDirectoryPath;
    }

    public String getSplitterInput() {
        return splitterInputField.getText();
    }

    public String getColumnsInput() {
        return columnsInputField.getText();
    }

    public boolean isCreateSeparateTableSelected() {
        return createTableRadio.isSelected();
    }

    private void validateStartButton() {
        if (startButton != null) {
            String splitterText = splitterInputField.getText();
            String columnsText = columnsInputField.getText();

            // Disable startButton if splitterInputField is NOT empty AND columnsInputField IS empty
            boolean isValid = !(splitterText != null && !splitterText.isEmpty() && 
                              (columnsText == null || columnsText.isEmpty()));

            startButton.setEnabled(isValid);
        }
    }

    private void configureBottomPanel(JPanel bottomPanel) {
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.setLayout(new BorderLayout(10, 0));
//        bottomPanel.setPreferredSize(new Dimension(800, 40));

        // Create progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Create Start indexing button
        startButton = new JButton("Start indexing");
        startButton.addActionListener(e -> {
            if (selectedDirectoryPath == null || selectedDirectoryPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create indexer and start indexing
            indexer = new IndexDBFromDirectory(
                selectedDirectoryPath, 
                progressBar, 
                getSplitterInput(), 
                getColumnsInput(), 
                isCreateSeparateTableSelected()
            );

            // Set callback to re-enable the start button when indexing is completed
            indexer.setOnCompletionCallback(() -> {
                SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
            });

            // Disable start button during indexing
            startButton.setEnabled(false);

            // Start indexing
            indexer.startIndexing();
        });

        // Create Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            if (indexer != null) {
                indexer.cancel();
                startButton.setEnabled(true);
            }
        });

        // Add buttons to the panel
        buttonsPanel.add(startButton);
        buttonsPanel.add(cancelButton);

        // Add components to the bottom panel
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.NORTH);

        // Set initial validation state
        validateStartButton();
    }
}
