package hms11n.mtp;

import hms11n.mtp.win32.MtpWin32;

import java.math.BigInteger;

public class DeviceProperties {
    public static String MANUFACTURE = MtpWin32.getGuid("WPD_DEVICE_MANUFACTURER");
    public static String SERIAL_NUMBER = MtpWin32.getGuid("WPD_DEVICE_SERIAL_NUMBER");
    public static String POWER_SOURCE = MtpWin32.getGuid("WPD_DEVICE_POWER_SOURCE");
    public static String FIRMWARE_VERSION = MtpWin32.getGuid("WPD_DEVICE_FIRMWARE_VERSION");
    public static String POWER_LEVEL = MtpWin32.getGuid("WPD_DEVICE_POWER_LEVEL");
    public static String PROTOCOL = MtpWin32.getGuid("WPD_DEVICE_PROTOCOL");
    public static String SUPPORTS_NON_CONSUMABLE = MtpWin32.getGuid("SUPPORTS_NON_CONSUMABLE");
    public static String SYNC_PARTNER = MtpWin32.getGuid("WPD_DEVICE_SYNC_PARTNER");
    public static String OBJECT_ID = MtpWin32.getGuid("WPD_OBJECT_ID");
    public static String OBJECT_CONTAINER_FUNCTIONAL_OBJECT_ID = MtpWin32.getGuid("WPD_OBJECT_CONTAINER_FUNCTIONAL_OBJECT_ID");
    public static String OBJECT_PERSISTENT_UNIQUE_ID = MtpWin32.getGuid("WPD_OBJECT_PERSISTENT_UNIQUE_ID");
    public static String OBJECT_NAME = MtpWin32.getGuid("WPD_OBJECT_NAME");
    public static String OBJECT_CAN_DELETE = MtpWin32.getGuid("WPD_OBJECT_CAN_DELETE");
    public static String OBJECT_CONTENT_TYPE = MtpWin32.getGuid("WPD_OBJECT_CONTENT_TYPE");

    public static class PropertyValue {
        private Object value;
        private final String key;
        private final Class<?> type;

        public PropertyValue(Class<?> type, String key, Object value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public Class<?> getType() {
            return this.type;
        }

        public String toString() {
            return this.value.toString();
        }

        public String getStringValue() {
            return this.value.toString();
        }

        public <T> T getValue() {
            return (T)this.value;
        }

        public <T> T getValue(T type) {
            return (T)this.value;
        }

        public <T> T[] getArrayValue(T type) {
            return (T[])((Object[])this.value);
        }

        public <T> T[] getArrayValue() {
            return (T[])((Object[])this.value);
        }

        public boolean getBooleanValue() {
            return (Boolean)this.value;
        }

        public BigInteger getBigIntegerValue() {
            return (BigInteger)this.value;
        }
    }
}
