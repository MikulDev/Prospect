package hms11n.mtp.win32;

import hms11n.mtp.PortableDevice;
import hms11n.mtp.PortableDeviceManager;

class PortableDeviceManagerWin32 implements PortableDeviceManager
{
    PortableDeviceManagerWin32() {
        this.getDeviceCount();
    }

    public PortableDevice[] getDevices() {
        String[] iDs = this.getDeviceID();
        PortableDevice[] devices = new PortableDevice[iDs.length];
        int i = 0;

        for(String deviceID : iDs) {
            devices[i] = new PortableDeviceWin32(deviceID);
            ++i;
        }

        return devices;
    }

    public native int getDeviceCount();

    public native String[] getDeviceID();
}
