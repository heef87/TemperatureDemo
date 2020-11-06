package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

public class RE_XINGMA extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "RE-XINGMA(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 57600; //波特率

    static final int MATRIX_COUT_X = 0; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 0; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT = new byte[]{(byte) 0xEE, (byte) 0xE1, 0x01, 0x55, (byte) 0xFF, (byte) 0xFC}; //查询输出数据指令

    public RE_XINGMA() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 24, 250, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
        setQureyInLoop(true);
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
        if (data.length == 8) {
            entity.temperatue = ((data[3] & 0xFF) << 8 | (data[4] & 0xFF)) / 100f;
            entity.ta = ((data[5] & 0xFF) * 256 + (data[6] & 0xFF) - 27315) / 100f;
            return entity;
        }
        return null;
    }
}
