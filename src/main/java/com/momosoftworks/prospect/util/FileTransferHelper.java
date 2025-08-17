package com.momosoftworks.prospect.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.momosoftworks.prospect.ProspectApplication;
import com.momosoftworks.prospect.file_transfer.MTPHandler;
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

public class FileTransferHelper
{
    public enum ImportType
    {   REPORT, TEMPLATE
    }

    public enum TransferDirection
    {   FROM_DEVICE, TO_DEVICE
    }

    private final MTPHandler mtpHandler;

    public FileTransferHelper()
    {   this.mtpHandler = ProspectApplication.getMtpHandler();
    }

    /**
     * Get list of connected devices that have the Prospect app
     */
    public List<String> getConnectedDevicesWithProspectApp()
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot get connected devices.");
            return List.of();
        }
        List<String> allDevices = mtpHandler.getConnectedDevices();
        List<String> prospectDevices = new ArrayList<>();

        for (String device : allDevices)
        {
            if (hasProspectAppOnDevice(device))
            {   prospectDevices.add(device);
            }
        }

        return prospectDevices;
    }

    /**
     * Check if a device has the Prospect app installed with data
     */
    private boolean hasProspectAppOnDevice(String deviceName)
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot check for Prospect app.");
            return false;
        }
        try
        {   // Try to get files from the prospect app directory
            List<File> prospectFiles = mtpHandler.getFiles(deviceName, getProspectAppPath());
            return prospectFiles != null && !prospectFiles.isEmpty();
        }
        catch (Exception e)
        {   return false;
        }
    }

    /**
     * Get the Prospect app directory path on mobile devices
     */
    private String getProspectAppPath()
    {   return "Android/data/com.momosoftworks.prospect/files";
    }

    /**
     * Get list of files of specified type from a connected device
     */
    public List<DeviceFile> getDeviceFiles(String deviceName, ImportType type)
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot get device files.");
            return List.of();
        }
        String subPath = type == ImportType.REPORT ? "reports" : "templates";
        String fullPath = getProspectAppPath() + "/" + subPath;

        try
        {   List<File> files = mtpHandler.getFiles(deviceName, fullPath);
            return files.stream()
                    .filter(file -> file.getName().toLowerCase().endsWith(".json"))
                    .map(file -> new DeviceFile(file.getName(), fullPath + "/" + file.getName(), file.length()))
                    .collect(Collectors.toList());
        }
        catch (Exception e)
        {   throw new FileTransferException("Failed to get files from device: " + e.getMessage(), e);
        }
    }

    /**
     * Import a file from local filesystem
     */
    public void importLocalFile(File sourceFile, ImportType type) throws IOException
    {
        Path targetPath = getTargetPath(type);
        File targetFile = targetPath.resolve(sourceFile.getName()).toFile();
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Import a file from a connected device
     */
    public void importFileFromDevice(String deviceName, String deviceFilePath, ImportType type)
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot import file from device.");
            return;
        }
        try
        {
            // Create a temporary file to receive the data
            Path targetPath = getTargetPath(type);
            String fileName = deviceFilePath.substring(deviceFilePath.lastIndexOf("/") + 1);
            File targetFile = targetPath.resolve(fileName).toFile();

            // Get the file from device and copy it to our local storage
            List<File> deviceFiles = mtpHandler.getFiles(deviceName, deviceFilePath.substring(0, deviceFilePath.lastIndexOf("/")));
            File sourceFile = deviceFiles.stream()
                    .filter(f -> f.getName().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new FileTransferException("File not found on device: " + fileName));

            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        }
        catch (Exception e)
        {   throw new FileTransferException("Failed to import file from device: " + e.getMessage(), e);
        }
    }

    /**
     * Export a report to a connected device
     */
    public void exportReportToDevice(String deviceName, Report report)
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot export report to device.");
            return;
        }
        try
        {
            // Serialize the report to a temporary file
            File tempFile = createTempReportFile(report);
            // Send to device
            String devicePath = getProspectAppPath() + "/reports";
            mtpHandler.writeFile(deviceName, devicePath, tempFile);
            // Clean up temp file
            tempFile.delete();
        }
        catch (Exception e)
        {   throw new FileTransferException("Failed to export report to device: " + e.getMessage(), e);
        }
    }

    /**
     * Export a template to a connected device
     */
    public void exportTemplateToDevice(String deviceName, Template template)
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot export template to device.");
            return;
        }
        try
        {
            // Serialize the template to a temporary file
            File tempFile = createTempTemplateFile(template);
            // Send to device
            String devicePath = getProspectAppPath() + "/templates";
            mtpHandler.writeFile(deviceName, devicePath, tempFile);
            // Clean up temp file
            tempFile.delete();
        }
        catch (Exception e)
        {   throw new FileTransferException("Failed to export template to device: " + e.getMessage(), e);
        }
    }

    /**
     * Export multiple reports to a connected device
     */
    public void exportReportsToDevice(String deviceName, List<Report> reports)
    {
        for (Report report : reports)
        {   exportReportToDevice(deviceName, report);
        }
    }

    /**
     * Export multiple templates to a connected device
     */
    public void exportTemplatesToDevice(String deviceName, List<Template> templates)
    {
        for (Template template : templates)
        {   exportTemplateToDevice(deviceName, template);
        }
    }

    /**
     * Create a temporary file for a report
     */
    private File createTempReportFile(Report report) throws IOException
    {
        File tempFile = File.createTempFile("prospect_report", ".json");

        // Serialize the report to JSON and write to temp file
        ObjectNode reportJson = report.serialize();
        if (!Serialization.writeJsonFile(reportJson, tempFile.toPath()))
        {   throw new IOException("Failed to serialize report to temporary file");
        }
        return tempFile;
    }

    /**
     * Create a temporary file for a template
     */
    private File createTempTemplateFile(Template template) throws IOException
    {
        File tempFile = File.createTempFile("prospect_template", ".json");

        // Serialize the template to JSON and write to temp file
        ObjectNode templateJson = template.serialize();
        if (!Serialization.writeJsonFile(templateJson, tempFile.toPath()))
        {   throw new IOException("Failed to serialize template to temporary file");
        }
        return tempFile;
    }

    /**
     * Get the target path for imports based on type
     */
    private Path getTargetPath(ImportType type)
    {
        return type == ImportType.REPORT ?
               ProspectApplication.getReportPath() :
               ProspectApplication.getTemplatePath();
    }

    /**
     * Refresh the MTP handler to detect new devices
     */
    public void refreshDevices()
    {
        if (mtpHandler == null)
        {   ProspectApplication.LOGGER.log(Level.WARNING, "MTP handler is not initialized. Cannot refresh devices.");
            return;
        }
        mtpHandler.refresh();
    }

    /**
     * Represents a file on a connected device
     */
    public record DeviceFile(String name, String path, long size)
    {
        @Override
        public String toString()
        {   return name + " (" + formatFileSize(size) + ")";
        }

        private String formatFileSize(long bytes)
        {
            if (bytes < 1024) return bytes + " B";
            else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            else return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Exception for file transfer operations
     */
    public static class FileTransferException extends RuntimeException
    {
        public FileTransferException(String message)
        {   super(message);
        }

        public FileTransferException(String message, Throwable cause)
        {   super(message, cause);
        }
    }
}
