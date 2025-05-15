package dev.carlosrr.nebby.panels;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {

    private final JLabel dbFileLabel;
    private final JLabel totalRowsLabel;
    private final JLabel visibleRowsLabel;
    private final JLabel invalidRecordsLabel;

    public StatusPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(52, 54, 56)),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        dbFileLabel = new JLabel("DB File: None");
        totalRowsLabel = new JLabel("Total Rows: 0");
        visibleRowsLabel = new JLabel("Visible Rows: 0");
        invalidRecordsLabel = new JLabel("Invalid Records: 0");

        add(dbFileLabel);
        add(totalRowsLabel);
        add(visibleRowsLabel);
        add(invalidRecordsLabel);
    }

    public void updateDbFile(String fileName) {
        dbFileLabel.setText("DB File: " + (fileName != null ? fileName : "None"));
    }

    public void updateTotalRows(int count) {
        totalRowsLabel.setText("Total Rows: " + count);
    }

    public void updateVisibleRows(int count) {
        visibleRowsLabel.setText("Visible Rows: " + count);
    }

    public void updateInvalidRecords(int count) {
        invalidRecordsLabel.setText("Invalid Records: " + count);
    }
}
