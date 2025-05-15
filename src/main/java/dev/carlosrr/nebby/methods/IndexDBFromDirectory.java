package dev.carlosrr.nebby.methods;

import dev.carlosrr.nebby.utils.CreateDBFile;
import javax.swing.*;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexDBFromDirectory {

    private final String directoryPath;
    private final JProgressBar progressBar;
    private final AtomicBoolean isCancelled;
    private ExecutorService executorService;
    private List<DirectoryInfo> directoryInfoList;
    private final String splitterInput;
    private final String columnsInput;
    private final boolean createSeparateTable;

    public IndexDBFromDirectory(String directoryPath, JProgressBar progressBar, String splitterInput, String columnsInput, boolean createSeparateTable) {
        this.directoryPath = directoryPath;
        this.progressBar = progressBar;
        this.isCancelled = new AtomicBoolean(false);
        this.directoryInfoList = new ArrayList<>();
        this.splitterInput = splitterInput;
        this.columnsInput = columnsInput;
        this.createSeparateTable = createSeparateTable;
    }

    private Runnable onCompletionCallback;

    public void startIndexing() {
        if (directoryPath == null || directoryPath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please select a directory first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Reset progress and cancel flag
        progressBar.setValue(0);
        isCancelled.set(false);
        directoryInfoList.clear();

        // Create a thread pool
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Start the indexing process in a separate thread to not block the UI
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                File rootDir = new File(directoryPath);

                // First, count total number of subdirectories for progress tracking
                AtomicInteger totalDirs = new AtomicInteger(0);
                countSubdirectories(rootDir, totalDirs);

                if (totalDirs.get() == 0) {
                    return null;
                }

                // Now process the directories
                AtomicInteger processedDirs = new AtomicInteger(0);
                processDirectory(rootDir, processedDirs, totalDirs);

                // Shutdown the executor service
                executorService.shutdown();
                return null;
            }

            @Override
            protected void done() {
                if (isCancelled.get()) {
                    progressBar.setValue(0);
                    System.out.println("Indexing cancelled");
                } else {
                    progressBar.setValue(100);
                    System.out.println("Indexing completed");

                    // Create SQLite database with the collected directory information
                    if (!directoryInfoList.isEmpty()) {
                        // Get the parent component from the progress bar
                        Component parent = progressBar.getParent();
                        CreateDBFile.createDatabase(directoryInfoList, parent, splitterInput, columnsInput, createSeparateTable);
                    } else {
                        JOptionPane.showMessageDialog(
                            progressBar.getParent(),
                            "No directories found to index.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                }

                // Call the completion callback if set
                if (onCompletionCallback != null) {
                    onCompletionCallback.run();
                }
            }
        };

        worker.execute();
    }

    private void countSubdirectories(File directory, AtomicInteger count) {
        if (isCancelled.get()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count.incrementAndGet();
                    countSubdirectories(file, count);
                }
            }
        }
    }

    private void processDirectory(File directory, AtomicInteger processedDirs, AtomicInteger totalDirs) {
        if (isCancelled.get()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (isCancelled.get()) {
                    return;
                }

                if (file.isDirectory()) {
                    // Add this directory to our list
                    DirectoryInfo dirInfo = new DirectoryInfo(file.getName(), file.getAbsolutePath());
                    synchronized (directoryInfoList) {
                        directoryInfoList.add(dirInfo);
                    }

                    // Update progress
                    int processed = processedDirs.incrementAndGet();
                    int progress = (int) ((processed / (double) totalDirs.get()) * 100);
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));

                    // Process subdirectories in parallel
                    final File currentDir = file;
                    executorService.submit(() -> processDirectory(currentDir, processedDirs, totalDirs));
                }
            }
        }
    }

    public void cancel() {
        isCancelled.set(true);
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public List<DirectoryInfo> getDirectoryInfoList() {
        return directoryInfoList;
    }

    public void setOnCompletionCallback(Runnable callback) {
        this.onCompletionCallback = callback;
    }

    // Inner class to store directory information
    public static class DirectoryInfo {
        private final String filename;
        private final String fullPath;

        public DirectoryInfo(String filename, String fullPath) {
            this.filename = filename;
            this.fullPath = fullPath;
        }

        public String getFilename() {
            return filename;
        }

        public String getFullPath() {
            return fullPath;
        }
    }
}
