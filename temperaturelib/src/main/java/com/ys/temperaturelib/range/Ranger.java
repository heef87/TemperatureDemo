package com.ys.temperaturelib.range;

public interface Ranger {

    void create();

    void release();

    void onMesure(MesureResult result);
}
