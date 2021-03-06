package com.ys.temperaturelib.heatmap;

import android.graphics.Bitmap;
import android.os.HandlerThread;
import android.util.Log;

import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.util.List;

public class DefaultHeatMap {
    private HeatMap heatMap;
    private int viewWidth;
    private int viewHeight;
    int radius;

    public DefaultHeatMap(int viewWidth, int viewHeight, int radius) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.radius = radius;
        HandlerThread handlerThread = new HandlerThread("hotmap_draw");
        handlerThread.start();
    }

    public Bitmap drawHeatMap(TemperatureEntity temperature, int matrixXCount, int matrixYCount) {
        if(viewWidth <= 0 || viewHeight<= 0) return null;
        List<WeightedLatLng> latLngs = DataFormatUtil.getHeatMapData(temperature,
                viewWidth, viewHeight, matrixXCount, matrixYCount);
        if (latLngs == null || latLngs.isEmpty()) return null;
        if (heatMap == null)
            heatMap = new HeatMap.Builder().weightedData(latLngs).radius(radius).
                    width(viewWidth).height(viewHeight).build();
        else
            heatMap.setWeightedData(latLngs);
        return heatMap.generateMap();
    }
}
