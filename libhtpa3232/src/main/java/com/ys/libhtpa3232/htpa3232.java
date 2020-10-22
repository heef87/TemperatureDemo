package com.ys.libhtpa3232;

public class htpa3232 {

    /**
     * 初始化htpa3232
     *
     * @return
     */
    public int init() {
        return open(1);
    }

    /**
     * 读取htpa3232数据
     *
     * @return 返回数据
     * [0,1023] 为32x32的温度数据
     * [1024] 为 Ta
     * [1025] 为 min
     * [1026] 为 max
     * [1027] 为 actu_tempvalue
     * [1028] 为 actu_tempvalue
     */
    public int[] read() {
        return readTemperature();
    }

    /**
     * 释放设备
     */
    public void release() {
        close();
    }

    public static native int open(int value);

    public static native void close();

    public static native int[] readTemperature();

    static {
        System.loadLibrary("htpa3232");
    }

}