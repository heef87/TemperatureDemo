package com.ys.temperaturelib.device.i2cmatrix;

import com.ys.libhtpa3232.htpa3232;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class IHTPA_32x32 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "HTPA_32x32(矩阵)";
    public static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 32; //温度矩阵横坐标总数量
    htpa3232 mHtpa3232;

    public IHTPA_32x32() {
        mHtpa3232 = new htpa3232();
        setParser(this);
        setMeasureParm(new MeasureParm(MODE_NAME, 50, 100, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[8];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(0.3f);
        entities[0] = entity1;
        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(20);
        entity2.setTakeTemperature(0.9f);
        entities[1] = entity2;
        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(30);
        entity3.setTakeTemperature(1.5f);
        entities[2] = entity3;
        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(40);
        entity4.setTakeTemperature(1.9f);
        entities[3] = entity4;
        TakeTempEntity entity5 = new TakeTempEntity();
        entity5.setDistances(50);
        entity5.setTakeTemperature(2.1f);
        entities[4] = entity5;
        TakeTempEntity entity6 = new TakeTempEntity();
        entity6.setDistances(60);
        entity6.setTakeTemperature(2.4f);
        entities[5] = entity6;
        TakeTempEntity entity7 = new TakeTempEntity();
        entity7.setDistances(70);
        entity7.setTakeTemperature(2.7f);
        entities[6] = entity7;
        TakeTempEntity entity8 = new TakeTempEntity();
        entity8.setDistances(100);
        entity8.setTakeTemperature(3f);
        entities[7] = entity8;
        return entities;
    }

    int count = 0;
    List<Float> mFloats = new ArrayList<>();
    float lastTemp = 0;
    int tempCount = 0;

    @Override
    public float check(float value, TemperatureEntity entity) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        return value + takeTempEntity.getTakeTemperature();
//        count++;
//        mFloats.add(value);
//        if (mFloats.size() == 6) {
//            tempCount = 5;
//        } else if (mFloats.size() > 6) {
//            List<Float> floats = mFloats.subList(tempCount - 3, tempCount - 3 + 5);
//            float sum = 0;
//            float max = floats.get(0);
//            float min = floats.get(0);
//
//            for (int i = 0; i < floats.size(); i++) {
//                sum += floats.get(i);
//                if (floats.get(i) > max) max = floats.get(i);
//                if (floats.get(i) < min) min = floats.get(i);
//            }
//
//            float tt = sum / 5f + takeTempEntity.getTakeTemperature();
//            if (tt >= 34f && tt < 36f) {
//                int tt1 = (int) (tt * 100);
//                tt = Float.parseFloat("36." + String.valueOf(tt1).substring(2, 4));
//            } else if (tt >= 37.2f && tt <= 37.5f) {
//                tt += 0.3f;
//            }
////            getStorager().add(tempCount + ":" + floats + " t:" + tt);
//            lastTemp = tt;
//            tempCount++;
//            return tt;
//        }
//        return lastTemp;
    }

    @Override
    protected float[] read() {
        if (mHtpa3232 == null) return null;
        int[] read = mHtpa3232.read();
        if (read == null) return null;
        float[] temps = new float[read.length];
        for (int i = 0; i < read.length; i++) {
            temps[i] = read[i] / 10f;
        }
        return temps;
    }

    @Override
    protected void release() {
        if (mHtpa3232 == null) return;
        mHtpa3232.release();
        mHtpa3232 = null;
    }

    @Override
    protected boolean init() {
        if (mHtpa3232 != null) {
            int init = mHtpa3232.init();
            return init > 0;
        }
        return false;
    }

    @Override
    public void order(byte[] data) {

    }

    @Override
    public float[] oneFrame(float[] data) {
        return data;
    }

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.ta = data[1024];
            entity.min = data[1025];
            entity.max = data[1026];
            for (int i = 0; i < 1024; i++) {
                float temp = check(data[i], entity);
                temps.add(temp);
            }
            entity.tempList = temps;
            entity.temperatue = check(data[1027],entity);
            return entity;
        }
        return null;
    }
}
