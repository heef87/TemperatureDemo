package com.ys.temperaturelib.device;

import android.os.SystemClock;

import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.temperature.TemperatureParser;

public abstract class IMatrixThermometer extends MeasureDevice {
    boolean enabled;
    DataRead mDataRead;
    TemperatureParser<float[]> mParser;

    public IMatrixThermometer() {
    }

    public void setParser(TemperatureParser<float[]> parser) {
        mParser = parser;
    }

    public void startUp(MeasureResult result) {
        startUp(result, getMeasureParm().perid);
    }

    @Override
    public void startUp(MeasureResult result, long period) {
        enabled = init();
        if (enabled) {
            if (mDataRead != null) mDataRead.interrupt();
            mDataRead = null;
            mDataRead = new DataRead(result, period);
            mDataRead.start();
        }
    }

    public boolean isPoint() {
        return false;
    }

    protected abstract float[] read();

    protected abstract void release();

    @Override
    public void destroy() {
        enabled = false;
        if (mDataRead != null) mDataRead.interrupt();
        mDataRead = null;
        TemperatureStorager.getInstance().exit();
    }

    /**
     * 数据读取线程
     */
    class DataRead extends Thread {
        MeasureResult mResult;
        long period;
        boolean isInterrupted;

        public DataRead(MeasureResult result, long period) {
            mResult = result;
            this.period = period;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            isInterrupted = true;
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                float[] read = read();
                if (read != null && mParser != null) {
                    float[] oneFrame = mParser.oneFrame(read);
                    if (oneFrame != null) {
                        TemperatureEntity entity = mParser.parse(oneFrame);
                        if (mResult != null) mResult.onResult(entity, oneFrame);
                    }
                }
                if (isInterrupted) {
                    release();
                    break;
                }
                SystemClock.sleep(period);
            }
        }
    }
}
