package hms11n.mtp.win32;

class Guid {
    final String guid;
    final int pid;

    public Guid(String guid, int pid) {
        this.guid = guid;
        this.pid = pid;
    }

    public Guid(String guid) {
        this.guid = guid;
        this.pid = -1;
    }

    public String toString() {
        return this.pid == -1 ? this.guid : this.guid + this.pid;
    }
}
