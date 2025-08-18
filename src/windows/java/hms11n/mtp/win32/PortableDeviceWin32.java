package hms11n.mtp.win32;

import hms11n.mtp.DeviceProperties;
import hms11n.mtp.PortableDevice;
import hms11n.mtp.content.PortableDeviceObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class PortableDeviceWin32 implements PortableDevice
{
    private final String deviceID;
    private boolean isOpen;
    private Map<String, Object> nativeProperties;
    private Map<String, DeviceProperties.PropertyValue> properties;
    private final PortableDeviceContentWin32 content;

    PortableDeviceWin32(String deviceID) {
        this.deviceID = deviceID;
        this.content = new PortableDeviceContentWin32(this);
    }

    private static native String getFriendlyName(String var0);

    private static native String getManufacturer(String var0);

    private static native String getDescription(String var0);

    private static native Map<String, Object> getProperties(String var0);

    public void reloadProperties() {
        this.nativeProperties = getProperties(this.deviceID);
        if (this.nativeProperties == null) {
            System.err.println("Native Properties are Null");
        }

        Map<String, DeviceProperties.PropertyValue> ret = new HashMap();

        for(int i = 0; i < this.nativeProperties.size(); ++i) {
            String key = (String)(new ArrayList(this.nativeProperties.keySet())).get(i);
            Object obj = this.nativeProperties.get(key);
            ret.put(key, new DeviceProperties.PropertyValue(obj.getClass(), key, obj));
        }

        this.properties = ret;
    }

    public Map<String, DeviceProperties.PropertyValue> getProperties() {
        if (this.nativeProperties == null) {
            this.reloadProperties();
        }

        return this.properties;
    }

    public String getFriendlyName() {
        return getFriendlyName(this.deviceID);
    }

    public String getManufacture() {
        return getManufacturer(this.deviceID);
    }

    public String getDescription() {
        return getDescription(this.deviceID);
    }

    public String getFirmwareVersion() {
        return ((DeviceProperties.PropertyValue)this.getProperties().get(PropertiesWin32.WPD_DEVICE_FIRMWARE_VERSION.toString())).getStringValue();
    }

    public String getSerialNumber() {
        return ((DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_DEVICE_SERIAL_NUMBER.toString())).getStringValue();
    }

    public String getProtocol() {
        DeviceProperties.PropertyValue protocol = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_DEVICE_PROTOCOL.toString());
        return getNoNullValue(protocol);
    }

    public String getSyncPartner() {
        DeviceProperties.PropertyValue partner = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_DEVICE_SYNC_PARTNER.toString());
        return getNoNullValue(partner);
    }

    public int getPowerLevel() {
        DeviceProperties.PropertyValue powerL = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_DEVICE_POWER_LEVEL.toString());
        return powerL == null ? -1 : (Integer)powerL.getValue(0);
    }

    public boolean IsNonConsumableSupported() {
        DeviceProperties.PropertyValue supportsNonConsumable = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_DEVICE_SUPPORTS_NON_CONSUMABLE.toString());
        return supportsNonConsumable == null ? false : (Boolean)supportsNonConsumable.getValue(true);
    }

    public void open() {
        this.openN();
        this.reloadProperties();
        this.isOpen = true;
    }

    public void close() {
        this.closeN();
        this.isOpen = false;
    }

    protected native void openN();

    protected native void closeN();

    protected native Map<String, String> getObjectsN(String var1);

    protected native String addFileObjectN(String var1, String var2, File var3, String var4, String var5);

    protected native String addFileFromInputStreamN(String var1, String var2, InputStream var3, String var4, String var5);

    protected native String addFolderObjectN(String var1, String var2);

    protected native void copyFileN(String var1, String var2);

    protected native boolean deleteFileN(String var1, int var2);

    protected native void updatePropertyN(String var1, String var2, int var3, String var4);

    protected native byte[] getBytesN(String var1);

    public PortableDeviceObject[] getRootObjects() {
        Map<String, String> objects = this.getObjectsN("DEVICE");
        PortableDeviceObject[] objs = new PortableDeviceObject[objects.size()];
        int i = 0;

        for(String id : objects.keySet()) {
            objs[i] = this.content.getObjectFromID(id, (String)objects.get(id));
            ++i;
        }

        return objs;
    }

    public PortableDevice.PowerSource getPowerSource() {
        String powerSource = ((DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_DEVICE_POWER_SOURCE.toString())).getStringValue();
        if (powerSource.equals("0")) {
            return PowerSource.BATTERY;
        } else {
            return powerSource.equals("1") ? PowerSource.EXTERNAL : PowerSource.UNKNOWN;
        }
    }

    private static String getNoNullValue(DeviceProperties.PropertyValue p) {
        return p == null ? null : p.getStringValue();
    }
}
