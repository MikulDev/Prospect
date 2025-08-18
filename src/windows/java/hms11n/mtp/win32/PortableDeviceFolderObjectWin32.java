package hms11n.mtp.win32;

import hms11n.mtp.content.PortableDeviceFolderObject;

class PortableDeviceFolderObjectWin32 extends PortableDeviceContainerObjectWin32 implements PortableDeviceFolderObject
{
    protected PortableDeviceFolderObjectWin32(String id, PortableDeviceContentWin32 content) {
        super(id, content);
    }
}
