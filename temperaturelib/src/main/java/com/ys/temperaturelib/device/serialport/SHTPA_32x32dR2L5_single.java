package com.ys.temperaturelib.device.serialport;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SHTPA_32x32dR2L5_single extends ProductImp implements TemperatureParser<byte[]> {
    public static final String DEFAULT_MODE_NAME = "HTPA_32x32dR2L5(单点)"; //型号
    static final String DEFAULT_DEVICE = "/dev/ttyS3"; //设备号
    static final int DEFAULT_RATE = 115200; //波特率

    static final int MATRIX_COUT_X = 0; //温度矩阵横坐标总数量
    static final int MATRIX_COUT_Y = 0; //温度矩阵纵坐标总数量

    static final byte[] ORDER_DATA_OUTPUT_AUTO = new byte[]{(byte) 0xA5, 0x05, 0x01, (byte) 0xBF}; //自动测量  A50601BF
    static final byte[] ORDER_DATA_OUTPUT_MANUAL = new byte[]{(byte) 0xA5, 0x05, 0x00, (byte) 0xBF}; //手动测量  A50600BF

    public SHTPA_32x32dR2L5_single() {
        super(DEFAULT_DEVICE, DEFAULT_RATE,
                new MeasureParm(DEFAULT_MODE_NAME, 50, 100, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTemperatureParser(this);
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
        setWriteInThread(true);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[8];
        TakeTempEntity entity0 = new TakeTempEntity();
        entity0.setDistances(10);
        entity0.setTakeTemperature(1.15f);//2.3  0  2 -0.8  -0.25
        entities[0] = entity0;

        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(20);
        entity1.setTakeTemperature(1.40f);//3.15  -0.05  1.5 -0.8  1.15
        entities[1] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(1.85f);//3.7 -0.15  1.2  -0.8  0.3
        entities[2] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(40);
        entity3.setTakeTemperature(2.00f);//4.25  -0.05  1 -0.6  0.1
        entities[3] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(50);
        entity4.setTakeTemperature(2.15f);//4.6  -0.2  0.7  -0.6  -0.6
        entities[4] = entity4;

        TakeTempEntity entity5 = new TakeTempEntity();
        entity5.setDistances(60);
        entity5.setTakeTemperature(2.40f);//4.6  -0.2  0.7  -0.6  -0.6
        entities[5] = entity5;

        TakeTempEntity entity6 = new TakeTempEntity();
        entity6.setDistances(70);
        entity6.setTakeTemperature(2.60f);//4.6  -0.2  0.7  -0.6  -0.6
        entities[6] = entity6;

        TakeTempEntity entity7 = new TakeTempEntity();
        entity7.setDistances(100);
        entity7.setTakeTemperature(2.95f);//4.6  -0.2  0.7  -0.6  -0.6
        entities[7] = entity7;

        return entities;
    }

    @Override
    public byte[] getOrderDataOutputType(boolean isAuto) {
        return isAuto ? ORDER_DATA_OUTPUT_AUTO : ORDER_DATA_OUTPUT_MANUAL;
    }

    @Override
    public byte[] getOrderDataOutputQuery() {
        return ORDER_DATA_OUTPUT_AUTO;
    }

    @Override
    public boolean isPoint() {
        return true;
    }

    float[] teps = new float[]{36.0f, 36.1f, 36.2f, 36.3f};

    @Override
    public float check(float value, TemperatureEntity entity) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        value = value + takeTempEntity.getTakeTemperature();
        if (value >= 35f && value <= 36f) {
            int index = (int) (Math.random() * teps.length);
            value = teps[index];
        } else if (value >= 36f && value <= 36.3f) {
            value += 0.3f;
        }
        return value;
    }

    @Override
    public byte[] oneFrame(byte[] data) {
        return data;
    }

    @Override
    public TemperatureEntity parse(byte[] data) {
        if (data == null || data.length < 12) return null;
        TemperatureEntity entity = new TemperatureEntity();
        entity.ta = ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
        entity.ta /= 10f;
        entity.max = ((data[6] & 0xFF) << 8) | (data[7] & 0xFF);
        entity.max /= 10f;
        entity.min = ((data[8] & 0xFF) << 8) | (data[9] & 0xFF);
        entity.min /= 10f;
        entity.temperatue = ((data[10] & 0xFF) << 8) | (data[11] & 0xFF);
        entity.temperatue /= 10f;
        entity.temperatue = check(entity.temperatue, entity);
        return entity;
    }
}
