package com.momosoftworks.prospect.file_transfer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdbFileTransfer
{
    private static final Logger LOGGER = Logger.getLogger(AdbFileTransfer.class.getName());
    private final String adbPath;
    private File tempAdbDir;

    public AdbFileTransfer() {
        this.adbPath = initializeAdb();
    }

    /**
     * Initialize ADB by extracting bundled tools to temp directory
     */
    private String initializeAdb() {
        try {
            // Determine platform
            String osName = System.getProperty("os.name").toLowerCase();
            String platform;
            String adbExecutable;

            if (osName.contains("windows")) {
                platform = "windows";
                adbExecutable = "adb.exe";
            } else if (osName.contains("mac")) {
                platform = "macos";
                adbExecutable = "adb";
            } else {
                platform = "linux";
                adbExecutable = "adb";
            }

            // Create temp directory
            tempAdbDir = Files.createTempDirectory("prospect_adb").toFile();
            tempAdbDir.deleteOnExit();

            // Extract ADB executable
            String adbResourcePath = "/tools/" + platform + "/" + adbExecutable;
            File adbFile = extractResourceToFile(adbResourcePath,
                                                 new File(tempAdbDir, adbExecutable));

            // Extract additional Windows DLLs if needed
            if (osName.contains("windows")) {
                extractResourceToFile("/tools/windows/AdbWinApi.dll",
                                      new File(tempAdbDir, "AdbWinApi.dll"));
                extractResourceToFile("/tools/windows/AdbWinUsbApi.dll",
                                      new File(tempAdbDir, "AdbWinUsbApi.dll"));
            }

            // Make executable on Unix systems
            if (!osName.contains("windows")) {
                adbFile.setExecutable(true);
            }

            LOGGER.log(Level.INFO, "ADB initialized at: " + adbFile.getAbsolutePath());
            return adbFile.getAbsolutePath();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize bundled ADB, falling back to system ADB", e);
            return "adb"; // Fallback to system ADB
        }
    }

    /**
     * Extract a resource file to a target location
     */
    private File extractResourceToFile(String resourcePath, File targetFile) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }

            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            targetFile.deleteOnExit();
            return targetFile;
        }
    }

    /**
     * Check if any devices are connected
     */
    public List<String> getConnectedDevices() {
        try {
            ProcessBuilder pb = new ProcessBuilder(adbPath, "devices");
            Process process = pb.start();

            List<String> devices = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean foundDevicesHeader = false;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals("List of devices attached")) {
                        foundDevicesHeader = true;
                        continue;
                    }

                    if (foundDevicesHeader && !line.trim().isEmpty()) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2 && "device".equals(parts[1])) {
                            devices.add(parts[0]); // Device ID
                        }
                    }
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
            return devices;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get connected devices", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get files from a specific path on the device
     */
    public List<DeviceFile> getFiles(String deviceId, String remotePath) {
        try {
            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("shell");
            command.add("find");
            command.add(remotePath);
            command.add("-type");
            command.add("f");
            command.add("-name");
            command.add("*.json");

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            List<DeviceFile> files = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && line.endsWith(".json")) {
                        String fileName = Paths.get(line).getFileName().toString();
                        files.add(new DeviceFile(fileName, line, 0)); // Size not available from find
                    }
                }
            }

            process.waitFor(15, TimeUnit.SECONDS);
            return files;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get files from device", e);
            return new ArrayList<>();
        }
    }

    /**
     * Check if a specific path exists on the device
     */
    public boolean pathExists(String deviceId, String remotePath) {
        try {
            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("shell");
            command.add("test");
            command.add("-d");
            command.add(remotePath);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Push a file to the device
     */
    public boolean writeFile(String deviceId, String remotePath, File localFile) {
        try {
            // Create directory structure first
            createDirectory(deviceId, remotePath);

            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("push");
            command.add(localFile.getAbsolutePath());
            command.add(remotePath + "/" + localFile.getName());

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Capture error output
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            boolean success = process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0;

            if (!success) {
                LOGGER.log(Level.WARNING, "ADB push failed: " + errorOutput.toString());
            }

            return success;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to write file to device", e);
            return false;
        }
    }

    /**
     * Pull a file from the device
     */
    public File readFile(String deviceId, String remoteFilePath, File localDestination) {
        try {
            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("pull");
            command.add(remoteFilePath);
            command.add(localDestination.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            boolean success = process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0;

            if (success && localDestination.exists()) {
                return localDestination;
            } else {
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to read file from device", e);
            return null;
        }
    }

    /**
     * Create a directory on the device
     */
    private boolean createDirectory(String deviceId, String remotePath) {
        try {
            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("shell");
            command.add("mkdir");
            command.add("-p");
            command.add(remotePath);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to create directory on device", e);
            return false;
        }
    }

    /**
     * Refresh/restart ADB server
     */
    public void refresh() {
        try {
            // Kill server
            ProcessBuilder killPb = new ProcessBuilder(adbPath, "kill-server");
            Process killProcess = killPb.start();
            killProcess.waitFor(5, TimeUnit.SECONDS);

            // Start server
            ProcessBuilder startPb = new ProcessBuilder(adbPath, "start-server");
            Process startProcess = startPb.start();
            startProcess.waitFor(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh ADB server", e);
        }
    }

    /**
     * Get device information
     */
    public String getDeviceInfo(String deviceId) {
        try {
            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("shell");
            command.add("getprop");
            command.add("ro.product.model");

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
            return output.toString().trim();

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get device info", e);
            return "Unknown Device";
        }
    }

    /**
     * Clean up temporary files
     */
    public void cleanup() {
        if (tempAdbDir != null && tempAdbDir.exists()) {
            deleteDirectory(tempAdbDir);
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Represents a file on a connected device
     */
    public static class DeviceFile {
        private final String name;
        private final String path;
        private final long size;

        public DeviceFile(String name, String path, long size) {
            this.name = name;
            this.path = path;
            this.size = size;
        }

        public String getName() { return name; }
        public String getPath() { return path; }
        public long getSize() { return size; }

        @Override
        public String toString() {
            return path + (size > 0 ? " (" + formatFileSize(size) + ")" : "");
        }

        private String formatFileSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            else return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}