package hms11n.mtp.win32;

import hms11n.mtp.DeviceProperties;
import hms11n.mtp.content.PortableDeviceContainerObject;
import hms11n.mtp.content.PortableDeviceObject;

import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class PortableDeviceObjectWin32 implements PortableDeviceObject
{
    Map<String, DeviceProperties.PropertyValue> properties;
    final String id;
    final PortableDeviceContentWin32 content;
    final DateFormat dateFormat;

    protected PortableDeviceObjectWin32(String id, PortableDeviceContentWin32 content) {
        this.dateFormat = new SimpleDateFormat("yyyy/MM/dd:HH:mm:sss", Locale.ENGLISH);
        this.id = id;
        this.content = content;
        this.init(id);
        this.loadProperties();
    }

    public static native Map<String, Object> getPropertiesN(String var0);

    private void loadProperties() {
        if (this.properties == null) {
            this.reloadProperties();
        }

    }

    public void reloadProperties() {
        Map<String, Object> nativeProperties = getPropertiesN(this.id);
        Map<String, DeviceProperties.PropertyValue> ret = new HashMap();

        for(int i = 0; i < nativeProperties.size(); ++i) {
            String key = (String)(new ArrayList(nativeProperties.keySet())).get(i);
            Object obj = nativeProperties.get(key);
            ret.put(key, new DeviceProperties.PropertyValue(obj.getClass(), key, obj));
        }

        this.properties = ret;
    }

    public Map<String, DeviceProperties.PropertyValue> getProperties() {
        this.loadProperties();
        return this.properties;
    }

    public native void init(String var1);

    public String getId() {
        return this.id;
    }

    public String getName() {
        this.loadProperties();
        return ((DeviceProperties.PropertyValue)this.properties.get(DeviceProperties.OBJECT_NAME)).getStringValue();
    }

    public String getOriginalFileName() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_ORIGINAL_FILE_NAME.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public boolean canDelete() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_CAN_DELETE.toString());
        return ret == null ? false : ret.getBooleanValue();
    }

    public boolean isHidden() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_ISHIDDEN.toString());
        return ret == null ? false : ret.getBooleanValue();
    }

    public boolean isSystemObject() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_ISSYSTEM.toString());
        return ret == null ? false : ret.getBooleanValue();
    }

    public boolean isDrmProtected() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_IS_DRM_PROTECTED.toString());
        return ret == null ? false : ret.getBooleanValue();
    }

    public Date getDateModified() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_DATE_MODIFIED.toString());
        if (ret == null) {
            return null;
        } else {
            try {
                return this.dateFormat.parse(ret.getStringValue());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Date getDateCreated() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_DATE_CREATED.toString());
        if (ret == null) {
            return null;
        } else {
            try {
                return this.dateFormat.parse(ret.getStringValue());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Date getDateAuthored() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_DATE_AUTHORED.toString());
        if (ret == null) {
            return null;
        } else {
            try {
                return this.dateFormat.parse(ret.getStringValue());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public PortableDeviceContainerObject getParent() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_PARENT_ID.toString());
        return ret == null ? null : new PortableDeviceContainerObjectWin32(ret.getStringValue(), this.content);
    }

    public BigInteger getSize() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_SIZE.toString());
        return ret == null ? null : ret.getBigIntegerValue();
    }

    public String getPersistentUniqueIdentifier() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_PERSISTENT_UNIQUE_ID.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public String getSyncID() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_SYNC_ID.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public String getFormat() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_FORMAT.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public String getContentType() {
        DeviceProperties.PropertyValue ret = (DeviceProperties.PropertyValue)this.properties.get(PropertiesWin32.WPD_OBJECT_CONTENT_TYPE.toString());
        return ret == null ? null : ret.getStringValue();
    }

    public void delete() {
        this.content.delete(this.id, 0);
    }

    public void copy(String path) {
        this.content.copyFile(this.id, path);
    }

    public void rename(String newName) {
        this.content.rename(this.id, newName);
    }

    public InputStream getInputStream() {
        return new PortableDeviceInputStreamWin32(this.content.getBytes(this.id));
    }
}
