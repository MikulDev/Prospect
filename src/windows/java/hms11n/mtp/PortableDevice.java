package hms11n.mtp;

import hms11n.mtp.content.PortableDeviceObject;

import java.util.Map;

public interface PortableDevice {
    String getFriendlyName();

    String getDescription();

    String getManufacture();

    Map<String, DeviceProperties.PropertyValue> getProperties();

    String getFirmwareVersion();

    String getSerialNumber();

    String getProtocol();

    String getSyncPartner();

    int getPowerLevel();

    boolean IsNonConsumableSupported();

    PowerSource getPowerSource();

    void reloadProperties();

    void open();

    void close();

    PortableDeviceObject[] getRootObjects();

    public static enum PowerSource {
        BATTERY,
        EXTERNAL,
        UNKNOWN;
    }

    public static enum PortableDeviceType {
        GENERIC,
        CAMERA,
        MEDIA_PLAYER,
        PHONE,
        VIDEO,
        PERSONAL_INFORMATION_MANAGER,
        AUDIO_RECORDER;
    }
}
