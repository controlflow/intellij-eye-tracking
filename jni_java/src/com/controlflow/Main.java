package com.controlflow;

public class Main {

    public static void main(String[] args) {

        //final var eyeTrackerJni = new EyeTrackerJni();
        final var s1 = EyeTrackerJni.initializeApi();
        final var devices = EyeTrackerJni.listDevices();
        final var conn = EyeTrackerJni.connectDevice(devices[1]);

        //Double.NaN;
        //final var i = Float.floatToIntBits(Float.NaN);
        //final var lll = i | ((long) i << 32);

        for (int index = 0; index < 1000; index++)
        {
            final var l = EyeTrackerJni.receivePosition();

            final var y = Float.intBitsToFloat((int) l);
            final var x = Float.intBitsToFloat((int) (l >> 32));

            System.out.println(x);
            System.out.println(y);
        }

        EyeTrackerJni.disconnectDevice();
        EyeTrackerJni.freeApi();

        // write your code here
    }
}

class EyeTrackerJni {

    static {
        System.loadLibrary("tobii_stream_engine");
        System.loadLibrary("intellij_eye_tracking_jni");
    }

    public static native String initializeApi();
    public static native String freeApi();
    public static native String[] listDevices();
    public static native String connectDevice(String device);
    public static native String disconnectDevice();
    public static native long receivePosition();
}