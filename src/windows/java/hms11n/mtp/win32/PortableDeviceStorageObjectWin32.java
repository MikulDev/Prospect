package hms11n.mtp.win32;

import hms11n.mtp.DeviceProperties;
import hms11n.mtp.content.PortableDeviceStorageObject;

import java.math.BigInteger;

public class PortableDeviceStorageObjectWin32 extends PortableDeviceContainerObjectWin32 implements PortableDeviceStorageObject
{
    protected PortableDeviceStorageObjectWin32(String id, PortableDeviceContentWin32 content) {
        super(id, content);
    }

    public String getFileSystemType() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_FILE_SYSTEM_TYPE.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public String getDescription() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_DESCRIPTION.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public String getSerialNumber() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_SERIAL_NUMBER.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public BigInteger getCapacity() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_CAPACITY.toString());
        return ret == null ? null : ret.getBigIntegerValue();
    }

    public BigInteger getCapacityInObjects() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_CAPACITY_IN_OBJECTS.toString());
        return ret == null ? null : ret.getBigIntegerValue();
    }

    public BigInteger getFreeSpace() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_FREE_SPACE_IN_BYTES.toString());
        return ret == null ? null : ret.getBigIntegerValue();
    }

    public BigInteger getFreeSpaceInObjects() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_FREE_SPACE_IN_OBJECTS.toString());
        return ret == null ? null : ret.getBigIntegerValue();
    }

    public BigInteger getMaximumObjectSize() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_STORAGE_MAX_OBJECT_SIZE.toString());
        return ret == null ? null : ret.getBigIntegerValue();
    }
}
