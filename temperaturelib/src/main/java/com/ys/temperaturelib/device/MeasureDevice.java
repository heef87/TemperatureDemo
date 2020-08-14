package com.ys.temperaturelib.device;

import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TakeTempEntity;
import com.ys.temperaturelib.temperature.TemperatureEntity;

/**
 * 测量设备类，主要有初始化、启动、下发指令、销毁等方法。
 * 用于设备数据读取、指令下发、参数设置。
 */
public abstract class MeasureDevice {

    TakeTempEntity mTakeTempEntity; //温度补偿方案
    MeasureParm mMeasureParm; //测量温度热力图参数

    /**
     * 获取多个补偿方案
     * 子类继承实现方法
     *
     * @return
     */
    public TakeTempEntity[] getDefaultTakeTempEntities() {
        return null;
    }

    /**
     * 获取当前补偿方案
     *
     * @return
     */
    public TakeTempEntity getTakeTempEntity() {
        return mTakeTempEntity;
    }

    /**
     * 设置当前补偿方案
     *
     * @param takeTempEntity
     */
    public void setTakeTempEntity(TakeTempEntity takeTempEntity) {
        mTakeTempEntity = takeTempEntity;
    }

    /**
     * 设置热力图参数
     * @param parm
     */
    public void setMeasureParm(MeasureParm parm) {
        mMeasureParm = parm;
    }

    /**
     * 获取热力图参数
     * @return
     */
    public MeasureParm getMeasureParm() {
        return mMeasureParm;
    }

    /**
     * 初始化设备
     *
     * @return 成功返回true 失败返回false
     */
    protected abstract boolean init();


    /**
     * 启动设别
     *
     * @param result 结果回调
     * @param period 间隔 millis
     */
    public abstract void startUp(MeasureResult result, long period);


    /**
     * 下发设备指令
     *
     * @param data
     */
    public abstract void order(byte[] data);

    /**
     * 销毁设备
     */
    public abstract void destroy();

    /**
     * 校验温度值
     *
     * @param value 温度值
     * @param entity 温度实体类
     * @return  添加补偿后的温度值
     */
    public abstract float check(float value, TemperatureEntity entity);

    /**
     * 是否为单点输出
     *
     * @return
     */
    public abstract boolean isPoint();
}
