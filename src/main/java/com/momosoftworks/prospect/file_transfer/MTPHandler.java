package com.momosoftworks.prospect.file_transfer;

import com.momosoftworks.prospect.ProspectApplication;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public interface MTPHandler
{
    List<String> getConnectedDevices();
    List<File> getFiles(String deviceName, String path);
    void writeFile(String deviceName, String path, File file);
    void refresh();

    static MTPHandler getHandler()
    {
        scanForMTPHandlers();
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {   return constructHandler("momosoftworks.file_transfer.WindowsMTPHandler");
        }
        else if (System.getProperty("os.name").toLowerCase().contains("linux"))
        {   ProspectApplication.LOGGER.log(Level.WARNING, "Linux MTP handling is not implemented yet.");
            return null;
        }
        else throw new RuntimeException("Unsupported platform for MTP handling: " + System.getProperty("os.name"));
    }

    private static MTPHandler constructHandler(String path)
    {
        try
        {   return Class.forName(path).asSubclass(MTPHandler.class).getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {   String handlerName = path.substring(path.lastIndexOf('.') + 1);
            throw new RuntimeException("Failed to create MTP handler: " + handlerName, e);
        }
    }

    private static void scanForMTPHandlers() {
    System.out.println("Scanning for MTPHandler implementations...");

    try {
        // Get all URLs from the class loader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        System.out.println(Arrays.toString(classLoader.getDefinedPackages()));

        System.out.println("\nClasspath from system property:");
        String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            for (String path : classpath.split(File.pathSeparator)) {
                System.out.println("  - " + path);
            }
        }

        // Try to find the class as a resource
        System.out.println("\nSearching for class as resource:");
        String[] resourcePaths = {
                "file_transfer/WindowsMTPHandler.class",
                "WindowsMTPHandler.class",
                "/file_transfer/WindowsMTPHandler.class"
        };

        for (String resourcePath : resourcePaths) {
            java.net.URL resource = classLoader.getResource(resourcePath);
            InputStream stream = classLoader.getResourceAsStream(resourcePath);
            System.out.println("  " + resourcePath + " -> " +
                                       (resource != null ? "Found at: " + resource : "Not found") +
                                       (stream != null ? " (stream available)" : ""));
            if (stream != null) stream.close();
        }

        // Try alternative class names in case there's a naming issue
        String[] possibleClassNames = {
            "file_transfer.WindowsMTPHandler",
            "WindowsMTPHandler",
            "com.momosoftworks.prospect.file_transfer.WindowsMTPHandler",
            "com.momosoftworks.file_transfer.WindowsMTPHandler",
            "src.windows.java.file_transfer.WindowsMTPHandler"
        };

        for (String className : possibleClassNames) {
            try {
                Class<?> clazz = Class.forName(className);
                System.out.println("✓ Found class with name: " + className);
                System.out.println("  Actual class: " + clazz.getName());
            } catch (ClassNotFoundException e) {
                System.out.println("✗ Not found: " + className);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
