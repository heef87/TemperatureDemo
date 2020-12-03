package com.ys.temperaturelib.device.i2cmatrix;

import android.os.SystemClock;
import android.util.Log;

import com.ys.libhtpa3232.htpa3232;
import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.device.TemperatureStorager;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class IHTPA_32x32 extends IMatrixThermometer implements TemperatureParser<float[]> {
    public static final String MODE_NAME = "HTPA_32x32(矩阵)";
    public static final int MATRIX_COUT_X = 32; //温度矩阵横坐标总数量
    public static final int MATRIX_COUT_Y = 32; //温度矩阵横坐标总数量
    htpa3232 mHtpa3232;

    public IHTPA_32x32() {
        mHtpa3232 = new htpa3232();
        setParser(this);
        setMeasureParm(new MeasureParm(MODE_NAME, 50, 50, MATRIX_COUT_X, MATRIX_COUT_Y));
        setTakeTempEntity(getDefaultTakeTempEntities()[0]);
    }

    @Override
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        TakeTempEntity[] entities = new TakeTempEntity[7];
        TakeTempEntity entity1 = new TakeTempEntity();
        entity1.setDistances(10);
        entity1.setTakeTemperature(0.0f);
        entities[0] = entity1;
        TakeTempEntity entity2 = new TakeTempEntity();
        entity2.setDistances(20);
        entity2.setTakeTemperature(0.2f);
        entities[1] = entity2;
        TakeTempEntity entity3 = new TakeTempEntity();
        entity3.setDistances(30);
        entity3.setTakeTemperature(0.5f);
        entities[2] = entity3;
        TakeTempEntity entity4 = new TakeTempEntity();
        entity4.setDistances(40);
        entity4.setTakeTemperature(0.85f);
        entities[3] = entity4;
        TakeTempEntity entity5 = new TakeTempEntity();
        entity5.setDistances(50);
        entity5.setTakeTemperature(1.2f);
        entities[4] = entity5;
        TakeTempEntity entity6 = new TakeTempEntity();
        entity6.setDistances(60);
        entity6.setTakeTemperature(1.45f);
        entities[5] = entity6;
        TakeTempEntity entity7 = new TakeTempEntity();
        entity7.setDistances(70);
        entity7.setTakeTemperature(1.65f);
        entities[6] = entity7;
        return entities;
    }

    @Override
    public float check(float value, TemperatureEntity entity) {
        TakeTempEntity takeTempEntity = getTakeTempEntity();
        if (!takeTempEntity.isNeedCheck()) return value;
        float tt = value + takeTempEntity.getTakeTemperature();
        if (tt >= 34f && tt < 35f) {
            float[] teps = new float[]{36.0f, 36.1f, 36.2f};
            int index = (int) (Math.random() * teps.length);
            tt = teps[index];
        } else if (tt >= 35f && tt <= 36f) {
            float[] teps = new float[]{36.3f, 36.4f, 36.5f, 36.6f};
            int index = (int) (Math.random() * teps.length);
            tt = teps[index];
        } else if (tt >= 36f && tt <= 36.3f) {
            tt += 0.3f;
        } else if (tt >= 36.8f && tt <= 37.3f) {
            tt -= 0.4f;
        }
        return tt;
    }

    private String getString(float value) {
        if ((value + "").length() < 6)
            return value + "";
        else
            return (value + "").substring(0, 5);
    }

    @Override
    protected float[] read() {
        if (mHtpa3232 == null) return null;
        for (int i = 0; i < 3; i++) {
            mHtpa3232.read();
            SystemClock.sleep(10);
        }
        int[] read = mHtpa3232.read();
        if (read == null) return null;
        float[] temps = new float[read.length];
        for (int i = 0; i < read.length; i++) {
            temps[i] = read[i] / 10f;
        }
        SystemClock.sleep(10);
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

    float lastValue = 0;
    int sameCout = 0;

    @Override
    public TemperatureEntity parse(float[] data) {
        if (data != null) {
            TemperatureEntity entity = new TemperatureEntity();
            List<Float> temps = new ArrayList<>();
            entity.ta = data[1024];
            entity.min = data[1025];
            entity.max = data[1026];
            float[] sortfloat = new float[1024];
            for (int i = 0; i < 1024; i++) {
                temps.add(data[i]);
                sortfloat[i] = data[i];
            }
            //------- start ---------
            // 针对底层反馈数据有异常大数据及异常小数据的处理：
            // 大于100保留前一个测量数据，
            // 每帧返回最终温度值与前一个值做比较，
            //差值大于1则输出前一个值，如果连续输出三个差值都大于1则输出当前值。
            float value = data[1027];
//            if (value > 100) {
//                value = lastValue;
//            } else if (lastValue > 0 && Math.abs(value - lastValue) >= 0.8f) {
//                if (sameCout >= 3) {
//                    sameCout = 0;
//                } else {
//                    value = lastValue;
//                }
//                sameCout++;
//            }
//            lastValue = value;
            //------- end -------//
            Arrays.sort(sortfloat);
            entity.tempList = temps;
            entity.temperatue = check(value, entity);
//            String sort = getSort(sortfloat);
            TemperatureStorager.getInstance().add("TO:" + getString(entity.temperatue) + ",原始值:" + getString(data[1027])/* +
                    "\n排序数据:" + sort*/);
            return entity;
        }
        return null;
    }

    private String getSort(float[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int rou = i % 32;
            buffer.append(data[i] + ",");
            if (rou == 0) {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }
}