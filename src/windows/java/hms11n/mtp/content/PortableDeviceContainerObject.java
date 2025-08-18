package hms11n.mtp.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface PortableDeviceContainerObject extends PortableDeviceObject {
    PortableDeviceObject[] getChildObjects();

    PortableDeviceObject addFileObject(File var1) throws IOException;

    PortableDeviceObject addFileObject(InputStream var1, String var2);

    PortableDeviceObject addFileObject(InputStream var1, String var2, String var3);

    PortableDeviceFolderObject createFolderObject(String var1);

    default boolean isContainer() {
        return true;
    }
}
