package hms11n.mtp.content;

import java.math.BigInteger;

public interface PortableDeviceStorageObject extends PortableDeviceObject {
    String getFileSystemType();

    String getDescription();

    String getSerialNumber();

    BigInteger getCapacity();

    BigInteger getCapacityInObjects();

    BigInteger getFreeSpace();

    BigInteger getFreeSpaceInObjects();

    BigInteger getMaximumObjectSize();
}
