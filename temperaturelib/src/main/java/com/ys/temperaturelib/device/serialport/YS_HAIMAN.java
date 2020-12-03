package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.device.TemperatureStorager;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

public class YS_HAIMAN extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "YS-HAIMAN(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 0; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 0; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT = new byte[]{(byte) 0xA5, (byte) 0x55, 0x01, (byte) 0xFB}; //查询输出数据指令

    public YS_HAIMAN() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 24, 250, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
        setQureyInLoop(true);
        setBigData(false);
    }

    @Override
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return ORDER_DATA_OUTPUT;
    }

    @Override
    public byte[] getOrderDataOutputQuery() {
        return ORDER_DATA_OUTPUT;
    }

    @Override
    public boolean isPoint() {
        return true;
    }

    @Override
    public byte[] oneFrame(byte[] data) {
        return data;
    }

    TemperatureEntity entity = new TemperatureEntity();

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null) return null;
        if (data.length == 7) {
            entity.temperatue = ((data[2] & 0xFF) + 256 * (data[3] & 0xFF)) / 100f;
            entity.ta = 0;
            TemperatureStorager.getInstance().add("TO:" + entity.temperatue);
            return entity;
        }
        return null;
    }
}
