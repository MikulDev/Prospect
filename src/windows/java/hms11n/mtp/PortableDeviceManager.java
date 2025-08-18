package hms11n.mtp;

import hms11n.mtp.win32.MtpWin32;

public interface PortableDeviceManager {
    PortableDevice[] getDevices();

    int getDeviceCount();

    String[] getDeviceID();

    static PortableDeviceManager getDeviceManager() {
        return MtpWin32.getWin32Manager();
    }
}
