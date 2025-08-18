package hms11n.mtp.win32;

import java.io.InputStream;

class PortableDeviceInputStreamWin32 extends InputStream {
    private final byte[] bytes;
    private int pos;

    protected PortableDeviceInputStreamWin32(byte[] b) {
        this.bytes = b;
    }

    public int read() {
        return this.pos >= this.bytes.length ? -1 : this.bytes[this.pos++];
    }

    public int available() {
        return this.bytes.length - (this.pos + 1);
    }
}
