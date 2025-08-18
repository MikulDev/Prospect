package com.momosoftworks.prospect.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gluonhq.attach.util.Platform;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.file_transfer.AdbFileTransfer;
import com.momosoftworks.prospect.report.Report;
import com.momosoftworks.prospect.report.template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileTransferHelper {

    public enum ImportType {
        REPORT, TEMPLATE
    }

    private final AdbFileTransfer adbManager;

    public FileTransferHelper() {
        this.adbManager = new AdbFileTransfer();

        // Register shutdown hook to clean up temporary ADB files
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (adbManager != null) {
                adbManager.cleanup();
            }
        }));
    }

    /**
     * Get list of connected devices that have the Prospect app
     */
    public List<String> getConnectedDevicesWithProspectApp() {
        try {
            List<String> allDevices = adbManager.getConnectedDevices();
            List<String> prospectDevices = new ArrayList<>();

            for (String deviceId : allDevices) {
                if (hasProspectAppOnDevice(deviceId))
                {
                    // Get a friendly name for the device
                    String deviceInfo = adbManager.getDeviceInfo(deviceId);
                    String displayName = deviceInfo.isEmpty() ? deviceId : deviceInfo + " (" + deviceId + ")";
                    prospectDevices.add(displayName);
                }
            }

            return prospectDevices;

        } catch (Exception e) {
            ProspectApplication.LOGGER.log(Level.WARNING, "Failed to get connected devices", e);
            return List.of();
        }
    }

    /**
     * Check if a device has the Prospect app installed with data
     */
    private boolean hasProspectAppOnDevice(String deviceId) {
        try {
            // Check if the prospect app directory exists
            return adbManager.pathExists(deviceId, getProspectAppPath());
        } catch (Exception e) {
            ProspectApplication.LOGGER.log(Level.WARNING, "Failed to check for Prospect app on device: " + deviceId, e);
            return false;
        }
    }

    /**
     * Get the Prospect app directory path on mobile devices
     */
    private String getProspectAppPath()
    {   return ProspectApplication.windowsPathToAndroid(ProspectApplication.getDataPath(Platform.ANDROID));
    }

    /**
     * Extract device ID from display name
     */
    private String extractDeviceId(String displayName) {
        if (displayName.contains("(") && displayName.contains(")")) {
            int start = displayName.lastIndexOf("(") + 1;
            int end = displayName.lastIndexOf(")");
            return displayName.substring(start, end);
        }
        return displayName; // Fallback if no parentheses found
    }

    /**
     * Get list of files of specified type from a connected device
     */
    public List<DeviceFile> getDeviceFiles(String deviceDisplayName, ImportType type) {
        try {
            String deviceId = extractDeviceId(deviceDisplayName);
            String subPath = type == ImportType.REPORT ? "reports" : "templates";
            String fullPath = getProspectAppPath() + "/" + subPath;

            List<AdbFileTransfer.DeviceFile> adbFiles = adbManager.getFiles(deviceId, fullPath);

            return adbFiles.stream()
                    .filter(file -> file.getName().toLowerCase().endsWith(".json"))
                    .map(file -> new DeviceFile(file.getName(), file.getPath(), file.getSize()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new FileTransferException("Failed to get files from device: " + e.getMessage(), e);
        }
    }

    /**
     * Import a file from local filesystem
     */
    public void importLocalFile(File sourceFile, ImportType type) throws IOException {
        Path targetPath = getTargetPath(type);
        File targetFile = targetPath.resolve(sourceFile.getName()).toFile();
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Import a file from a connected device
     */
    public void importFileFromDevice(String deviceDisplayName, String deviceFilePath, ImportType type) {
        try {
            String deviceId = extractDeviceId(deviceDisplayName);
            Path targetPath = getTargetPath(type);
            String fileName = deviceFilePath.substring(deviceFilePath.lastIndexOf("/") + 1);
            File targetFile = targetPath.resolve(fileName).toFile();

            // Create a temporary file to receive the data
            File tempFile = File.createTempFile("prospect_import", ".json");

            try {
                // Pull the file from device to temp location
                File downloadedFile = adbManager.readFile(deviceId, deviceFilePath.replace("/sdcard/", ""), tempFile);

                if (downloadedFile != null && downloadedFile.exists()) {
                    // Copy from temp to final location
                    Files.copy(downloadedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    downloadedFile.renameTo(targetFile);
                } else {
                    throw new FileTransferException("Failed to download file from device");
                }

            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (Exception e) {
            throw new FileTransferException("Failed to import file from device: " + e.getMessage(), e);
        }
    }

    /**
     * Export a report to a connected device
     */
    public void exportReportToDevice(String deviceDisplayName, Report report) {
        try {
            String deviceId = extractDeviceId(deviceDisplayName);
            // Serialize the report to a temporary file
            File tempFile = createTempReportFile(report);
            tempFile.renameTo(new File(tempFile.getParent() + report.getFileName() + ".json"));

            try {
                // Send to device
                String devicePath = getProspectAppPath() + "/reports";
                boolean success = adbManager.writeFile(deviceId, devicePath, tempFile);

                if (!success) {
                    throw new FileTransferException("Failed to write report file to device");
                }

            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (Exception e) {
            throw new FileTransferException("Failed to export report to device: " + e.getMessage(), e);
        }
    }

    /**
     * Export a template to a connected device
     */
    public void exportTemplateToDevice(String deviceDisplayName, Template template) {
        try {
            String deviceId = extractDeviceId(deviceDisplayName);
            // Serialize the template to a temporary file
            File tempFile = createTempTemplateFile(template);

            try {
                // Send to device
                String devicePath = getProspectAppPath() + "/templates";
                boolean success = adbManager.writeFile(deviceId, devicePath, tempFile);

                if (!success) {
                    throw new FileTransferException("Failed to write template file to device");
                }

            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (Exception e) {
            throw new FileTransferException("Failed to export template to device: " + e.getMessage(), e);
        }
    }

    /**
     * Export multiple reports to a connected device
     */
    public void exportReportsToDevice(String deviceDisplayName, List<Report> reports) {
        for (Report report : reports) {
            exportReportToDevice(deviceDisplayName, report);
        }
    }

    /**
     * Export multiple templates to a connected device
     */
    public void exportTemplatesToDevice(String deviceDisplayName, List<Template> templates) {
        for (Template template : templates) {
            exportTemplateToDevice(deviceDisplayName, template);
        }
    }

    /**
     * Create a temporary file for a report
     */
    private File createTempReportFile(Report report) throws IOException {
        File tempFile = File.createTempFile("prospect_report", ".json");

        // Serialize the report to JSON and write to temp file
        ObjectNode reportJson = report.serialize();
        if (!Serialization.writeJsonFile(reportJson, tempFile.toPath())) {
            throw new IOException("Failed to serialize report to temporary file");
        }
        return tempFile;
    }

    /**
     * Create a temporary file for a template
     */
    private File createTempTemplateFile(Template template) throws IOException {
        File tempFile = File.createTempFile("prospect_template", ".json");

        // Serialize the template to JSON and write to temp file
        ObjectNode templateJson = template.serialize();
        if (!Serialization.writeJsonFile(templateJson, tempFile.toPath())) {
            throw new IOException("Failed to serialize template to temporary file");
        }
        return tempFile;
    }

    /**
     * Get the target path for imports based on type
     */
    private Path getTargetPath(ImportType type) {
        return type == ImportType.REPORT ?
               ProspectApplication.getReportPath() :
               ProspectApplication.getTemplatePath();
    }

    /**
     * Refresh the device connections
     */
    public void refreshDevices() {
        try {
            adbManager.refresh();
        } catch (Exception e) {
            ProspectApplication.LOGGER.log(Level.WARNING, "Failed to refresh devices", e);
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (adbManager != null) {
            adbManager.cleanup();
        }
    }

    /**
     * Represents a file on a connected device
     */
    public record DeviceFile(String name, String path, long size) {
        @Override
        public String toString() {
            return name + " (" + formatFileSize(size) + ")";
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            else return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Exception for file transfer operations
     */
    public static class FileTransferException extends RuntimeException {
        public FileTransferException(String message) {
            super(message);
        }

        public FileTransferException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}