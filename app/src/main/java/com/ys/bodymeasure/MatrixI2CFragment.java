package com.ys.bodymeasure;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ys.temperaturelib.device.IMatrixThermometer;
import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.device.i2cmatrix.IMLX90641_16x12;
import com.ys.temperaturelib.heatmap.DefaultHeatMap;
import com.ys.temperaturelib.heatmap.maxtrix.MatrixView;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MatrixI2CFragment extends BaseFragment {
    private RadioGroup mRadioGroup;
    private TextView mTaText;
    private TextView mToText;
    private TextView mDataText;
    private ImageView mDataImageView;
    private DefaultHeatMap mHeatMap;
    private IMatrixThermometer mThermometer;
    private MatrixView mMatrixView;
    private boolean isStop = false;
    private boolean showHotMap = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_matrix_i2c_measure, container, false);
        mRadioGroup = inflate.findViewById(R.id.measure_view_select);
        mDataImageView = inflate.findViewById(R.id.measure_data_img);
        mTaText = inflate.findViewById(R.id.measure_data_ta);
        mToText = inflate.findViewById(R.id.measure_data_to);
        mDataText = inflate.findViewById(R.id.measure_data_text);
        mDataText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mMatrixView = inflate.findViewById(R.id.measure_data_view);
        mToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStop = !isStop;
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                showHotMap = id == R.id.measure_view_2;
                if (showHotMap) {
                    postDataImage();
                    mDataImageView.setVisibility(View.VISIBLE);
                    mMatrixView.setVisibility(View.GONE);
                } else {
                    mDataImageView.setVisibility(View.GONE);
                    mMatrixView.setVisibility(View.VISIBLE);
                }
            }
        });
        return inflate;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mThermometer = (IMatrixThermometer) ((I2CActivity) getActivity()).getCurProduct();
        if (mThermometer instanceof IMLX90641_16x12)
            mDataImageView.setVisibility(View.GONE);
        else
            mDataText.setVisibility(View.GONE);
        postDataImage();
        mThermometer.startUp(new MeasureResult<float[]>() {
            @Override
            public void onResult(TemperatureEntity entity, float[] oneFrame) {
                Message message = mHandler.obtainMessage();
                message.obj = entity;
                mHandler.sendMessage(message);
            }
        });
    }

    private void postDataImage() {
        mDataImageView.post(new Runnable() {
            @Override
            public void run() {
                MeasureParm parm = mThermometer.getMeasureParm();
                int w = mDataImageView.getWidth() / (parm.xCount > 32 ? 1 : 32 / parm.xCount);
                int h = mDataImageView.getHeight() / (parm.xCount > 24 ? 1 : 24 / parm.yCount);
                mHeatMap = new DefaultHeatMap(w, h, mThermometer.getMeasureParm().radio);
            }
        });
    }

    DecimalFormat fnum = new DecimalFormat("##0.0");
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            TemperatureEntity temperature = (TemperatureEntity) message.obj;
            if (mHeatMap != null && temperature != null && mThermometer != null && !isStop) {

                mToText.setText("TO:" + fnum.format(temperature.temperatue) + "°");
                mTaText.setText("TA:" + fnum.format(temperature.ta) + "°");
                mDataText.setText(temperature.sb);
                if (showHotMap) {
                    Bitmap bitmap = mHeatMap.drawHeatMap(temperature
                            , mThermometer.getMeasureParm().xCount
                            , mThermometer.getMeasureParm().yCount);
                    if (bitmap != null)
                        mDataImageView.setImageBitmap(bitmap);
                } else {
                    MeasureParm parm = mThermometer.getMeasureParm();
                    mMatrixView.setDataResource(temperature, parm.xCount, parm.yCount);
                }
            }
            return true;
        }
    });
}
