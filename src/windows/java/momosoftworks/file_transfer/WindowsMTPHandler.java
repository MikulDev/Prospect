package momosoftworks.file_transfer;

import com.momosoftworks.prospect.file_transfer.MTPHandler;

import hms11n.mtp.PortableDevice;
import hms11n.mtp.PortableDeviceManager;
import hms11n.mtp.content.PortableDeviceContainerObject;
import hms11n.mtp.content.PortableDeviceObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

public class WindowsMTPHandler implements MTPHandler
{
    PortableDeviceManager deviceManager = PortableDeviceManager.getDeviceManager();
    Map<String, PortableDevice> devices = new HashMap<>();

    @Override
    public void refresh()
    {   devices.clear();
    }

    private PortableDevice getDevice(String deviceID)
    {   return devices.get(deviceID);
    }

    @Override
    public List<String> getConnectedDevices()
    {   return Arrays.stream(deviceManager.getDevices()).map(PortableDevice::getFriendlyName).toList();
    }

    @Override
    public List<File> getFiles(String deviceID, String path)
    {
        PortableDevice device = this.getDevice(deviceID);
        if (device == null) return List.of();
        device.open();
        PortableDeviceContainerObject directory = getDirectory(device, path);
        if (directory == null) return List.of();
        return getFilesInDirectory(directory);
    }

    @Override
    public void writeFile(String deviceID, String path, File file)
    {
        PortableDevice device = this.getDevice(deviceID);
        if (device == null) return;
        device.open();
        PortableDeviceContainerObject directory = getDirectory(device, path);
        if (directory == null) return;

        try
        {   directory.addFileObject(file);
        }
        catch (Exception e)
        {   e.printStackTrace();
        }
    }

    private static PortableDeviceContainerObject getDirectory(PortableDevice device, String path)
    {
        String[] pathParts = path.split("\\\\");
        PortableDeviceContainerObject currentDir = device.getRootObjects()[0].getParent();

        for (String part : pathParts)
        {
            boolean found = false;
            for (PortableDeviceObject obj : currentDir.getChildObjects())
            {
                if (obj instanceof PortableDeviceContainerObject container && container.getName().equals(part))
                {
                    currentDir = container;
                    found = true;
                    break;
                }
            }
            if (!found)
            {   return null;
            }
        }
        return currentDir;
    }

    private static List<File> getFilesInDirectory(PortableDeviceContainerObject directory)
    {
        return Arrays.stream(directory.getChildObjects())
                .filter(obj -> !(obj instanceof PortableDeviceContainerObject))
                .map(obj ->
                {
                    File file = new File(obj.getName());
                    try (FileOutputStream writer = new FileOutputStream(file);
                         InputStream fileContents = obj.getInputStream())
                    {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileContents.read(buffer)) != -1)
                        {   writer.write(buffer, 0, bytesRead);
                        }
                        return file;
                    }
                    catch (Exception e)
                    {   e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
