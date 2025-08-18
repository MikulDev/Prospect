package hms11n.mtp;

public class DeviceClosedException extends RuntimeException {
    DeviceClosedException(String str) {
        super(str);
    }
}
