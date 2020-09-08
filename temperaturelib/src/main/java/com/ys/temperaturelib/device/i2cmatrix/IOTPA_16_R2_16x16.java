package com.ys.temperaturelib.device.i2cmatrix;


import android.util.Log;

import com.ys.otpa16r2.Otpa16R2;
import com.ys.rtx2080ti.Rtx2080ti;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.List;

public class IOTPA_16_R2_16x16 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "OTPA-16-R2-16*16";
    public static final int MATRIX_COUT_X = 16; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 16; //温度矩阵横坐标总数量
    Otpa16R2 mOtpa16R2;

    public IOTPA_16_R2_16x16() {
        mOtpa16R2 = new Otpa16R2();
        setParser(this);
        setMeasureParm(new MeasureParm(MODE_NAME, 50, 250, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    protected float[] read() {
        if (mOtpa16R2 == null) return null;
        return mOtpa16R2.read();
    }

    @Override
    protected void release() {
        if (mOtpa16R2 == null) return;
        mOtpa16R2.release();
        mOtpa16R2 = null;
    }

    @Override
    protected boolean init() {
        if (mOtpa16R2 != null) {
            int init = mOtpa16R2.init(Rtx2080ti.RATE_4HZ);
            return init > 0;
        }
        return false;
    }

    @Override
    public void order(byte[] data) {

    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[7];
        TakeTempEntity entity0 = new TakeTempEntity();
        entity0.setDistances(10);
        entity0.setTakeTemperature(1.1f);//-1.25   -1.3
        entities[0] = entity0;

        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(20);
        entity1.setTakeTemperature(1.7f);//0.05   -0.5
        entities[1] = entity1;

        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(30);
        entity2.setTakeTemperature(2.0f);//-0.15  -0.4
        entities[2] = entity2;

        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(40);
        entity3.setTakeTemperature(2.3f);//0.6   -0.25
        entities[3] = entity3;

        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(50);
        entity4.setTakeTemperature(2.6f);//1.45  -0.4
        entities[4] = entity4;

        TakeTempEntity entity5 = new TakeTempEntity();
        entity5.setDistances(60);
        entity5.setTakeTemperature(2.9f);//1.45  -0.4
        entities[5] = entity5;

        TakeTempEntity entity6 = new TakeTempEntity();
        entity6.setDistances(70);
        entity6.setTakeTemperature(3.2f);//1.45  -0.4
        entities[6] = entity6;
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
        count++;
        mFloats.add(value);
        if (mFloats.size() == 4) {
            tempCount = 3;
        } else if (mFloats.size() > 4) {
            List<Float> floats = mFloats.subList(tempCount - 1, tempCount - 1 + 3);
            float sum = 0;
            float max = floats.get(0);
            float min = floats.get(0);

            for (int i = 0; i < floats.size(); i++) {
                sum += floats.get(i);
                if (floats.get(i) > max) max = floats.get(i);
                if (floats.get(i) < min) min = floats.get(i);
            }
            float tt = sum / 3f + takeTempEntity.getTakeTemperature();
            if (getStorager() != null) {
                getStorager().add("平均值:" + getString(sum / 3f) +
                        ", 平均值+距离补偿:" + getString(sum / 3f + takeTempEntity.getTakeTemperature()) +
                        ", to：" + getString(tt) + ", ta:" + getString(entity.ta)
                        + "\n" + floats);
            }
            lastTemp = tt;
            tempCount++;
            return tt;
        }
        return lastTemp;
    }

    private String getString(float value) {
        if ((value + "").length() < 6)
            return value + "";
        else
            return (value + "").substring(0, 5);
    }

    @Override
    public float[] oneFrame(float[] data) {
        if (data.length == 257) {
            float tmp = data[0];
            for (int i = 0; i < data.length; i++) {
                if (data[i] < 0) {
                    data[i] = tmp;
                } else {
                    tmp = data[i];
                }
            }
            return data;
        }
        return null;
    }

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.ta = data[0];
            entity.min = entity.max = data[0];
            for (int i = 0; i < data.length - 4; i++) {
                float temp = data[i];
                if (temp < entity.min) entity.min = temp;
                if (temp > entity.max) entity.max = temp;
                temps.add(temp);
            }
            entity.tempList = temps;
            entity.temperatue = check(entity.max, entity);
            return entity;
        }
        return null;
    }
}
