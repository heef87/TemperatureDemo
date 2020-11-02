package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

public class SMLX90641_STM32 extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "MLX90641_STM32(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 0; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 0; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT = new byte[]{(byte) 0xa5, 0x05, 0x01, (byte) 0xbf}; //查询输出数据指令

    public SMLX90641_STM32() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 24, 200, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
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
        if ((data[0] & 0xFF) == 0xA5) {
            entity.temperatue = ((data[10] & 0xFF) << 8 | (data[11] & 0xFF)) / 10f;
            entity.max = ((data[6] & 0xFF) << 8 | (data[7] & 0xFF)) / 10f;
            entity.ta = ((data[4] & 0xFF) << 8 | (data[5] & 0xFF)) / 10f;
            entity.min = ((data[8] & 0xFF) << 8 | (data[9] & 0xFF)) / 10f;
            return entity;
        }
        return null;
    }
}
