package hms11n.mtp.content;

import hms11n.mtp.DeviceProperties;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

public interface PortableDeviceObject {
    String getId();

    String getName();

    String getOriginalFileName();

    boolean canDelete();

    boolean isHidden();

    boolean isSystemObject();

    boolean isDrmProtected();

    default boolean isContainer() {
        return false;
    }

    Date getDateModified();

    Date getDateCreated();

    Date getDateAuthored();

    PortableDeviceContainerObject getParent();

    BigInteger getSize();

    String getPersistentUniqueIdentifier();

    String getSyncID();

    String getFormat();

    String getContentType();

    Map<String, DeviceProperties.PropertyValue> getProperties();

    void reloadProperties();

    void delete();

    void copy(String var1);

    void rename(String var1);

    InputStream getInputStream();
}
