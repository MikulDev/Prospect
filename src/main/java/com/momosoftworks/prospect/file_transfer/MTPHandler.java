package com.momosoftworks.prospect.file_transfer;

import com.momosoftworks.prospect.ProspectApplication;

import java.io.File;
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
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {   return constructHandler("Windows");
        }
        else if (System.getProperty("os.name").toLowerCase().contains("linux"))
        {   ProspectApplication.LOGGER.log(Level.WARNING, "Linux MTP handling is not implemented yet.");
            return null;
        }
        else throw new RuntimeException("Unsupported platform for MTP handling: " + System.getProperty("os.name"));
    }

    private static MTPHandler constructHandler(String system)
    {
        try
        {   return Class.forName(String.format("com.momosoftworks.prospect.file_transfer.%sMTPHandler", system) + system).asSubclass(MTPHandler.class).getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {   throw new RuntimeException("Failed to create MTP handler: " + system, e);
        }
    }
}
