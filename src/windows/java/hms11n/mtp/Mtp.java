package hms11n.mtp;

public class Mtp {
    public static void register() {
        System.loadLibrary("mtpjava");
        registerJNI();
    }

    public static native void registerJNI();
}
