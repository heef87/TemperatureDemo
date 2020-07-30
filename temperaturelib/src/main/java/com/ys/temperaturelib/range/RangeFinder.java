package com.ys.temperaturelib.range;

public class RangeFinder {
    Ranger mRange;

    public RangeFinder(Ranger range) {
        this.mRange = range;
    }

    public void create() {
        if (mRange != null) {
            mRange.create();
        }
    }
    public void release(){
        if (mRange != null) {
            mRange.release();
        }
    }

    public void onMesure(MesureResult result){
        if (mRange != null) {
            mRange.onMesure(result);
        }
    }
}
