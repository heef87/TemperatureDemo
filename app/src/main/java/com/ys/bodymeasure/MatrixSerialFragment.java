package com.ys.bodymeasure;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ys.temperaturelib.device.serialport.CHUWO_32x32;
import com.ys.temperaturelib.device.serialport.SHAIMAN_32x24;
import com.ys.temperaturelib.device.serialport.SM23_32x32_XM;
import com.ys.temperaturelib.device.serialport.ProductImp;
import com.ys.temperaturelib.device.serialport.SMLX90621_HH;
import com.ys.temperaturelib.device.serialport.SMLX90621_RR;
import com.ys.temperaturelib.device.serialport.SYM32A_32x32_XM;
import com.ys.temperaturelib.device.MeasureResult;
import com.ys.temperaturelib.heatmap.DefaultHeatMap;
import com.ys.temperaturelib.heatmap.maxtrix.MatrixView;
import com.ys.temperaturelib.temperature.MeasureParm;
import com.ys.temperaturelib.temperature.TemperatureEntity;
import com.ys.temperaturelib.utils.DataFormatUtil;

import java.text.DecimalFormat;


public class MatrixSerialFragment extends BaseFragment implements View.OnClickListener {
    private RadioGroup mRadioGroup;
    private TextView mDataText;
    private TextView mTaText;
    private TextView mToText;
    private Button mSendButton;
    private ImageView mDataImageView;
    private CheckBox mCheckBox;
    DefaultHeatMap mHeatMap;
    private boolean isAuto;
    ProductImp mSerialProduct;
    private MatrixView mMatrixView;
    private boolean isStop = false;
    private boolean showHotMap = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_matrix_measure, container, false);
        mMatrixView = inflate.findViewById(R.id.measure_data_view);
        mRadioGroup = inflate.findViewById(R.id.measure_view_select);
        mCheckBox = inflate.findViewById(R.id.measure_data_mode);
        mDataImageView = inflate.findViewById(R.id.measure_data_img);
        mTaText = inflate.findViewById(R.id.measure_data_ta);
        mToText = inflate.findViewById(R.id.measure_data_to);
        mDataText = inflate.findViewById(R.id.measure_data_text);
        mDataText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mSendButton = inflate.findViewById(R.id.measure_data_send);
        mSendButton.setOnClickListener(this);
        isAuto = mCheckBox.isChecked();
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isAuto = isChecked;
                checkOrder();
                SM32ASendOrder();
            }
        });
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
        mSerialProduct = ((SerialActivity) getActivity()).getCurProduct();
        postDataImage();
        measure(mSerialProduct.getDevice(), mSerialProduct.getBaudrate());
    }

    private void checkOrder() {
        byte[] order = mSerialProduct.getOrderDataOutputType(isAuto);
        onOrder(order);
    }

    private void postDataImage() {
        mDataImageView.post(new Runnable() {
            @Override
            public void run() {
                mHeatMap = new DefaultHeatMap(mDataImageView.getWidth(),
                        mDataImageView.getHeight(), mSerialProduct.getMeasureParm().radio);
            }
        });
    }

    @Override
    public void measure(String devicePath, int deviceRate) {
        mSerialProduct.startUp(new MeasureResult<byte[]>() {

            @Override
            public void onResult(TemperatureEntity entity, byte[] oneFrame) {
                Message message = mHandler.obtainMessage();
                Bundle data = message.getData();
                data.putByteArray("_byte", oneFrame);
                data.putParcelable("_temp", entity);
                mHandler.sendMessage(message);
            }
        }, devicePath, deviceRate);
        if (mSerialProduct instanceof SYM32A_32x32_XM) {
            byte[][] initOrder = ((SYM32A_32x32_XM) mSerialProduct).getInitOrder();
            for (int i = 0; i < initOrder.length; i++) {
                onOrder(initOrder[i]);
            }
        } else {
            onClick(null);
        }
    }

    DecimalFormat fnum = new DecimalFormat("##0.00");
    DecimalFormat fnumTo = new DecimalFormat("##00.00");
    Bitmap bitmap;
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            Bundle bundle = message.getData();
            byte[] bytes = bundle.getByteArray("_byte");
            String dataText = DataFormatUtil.bytesToHex(bytes);
            mDataText.setText(dataText);

            TemperatureEntity temperature = bundle.getParcelable("_temp");
            if (temperature != null) {
                mToText.setText("TO:" + fnumTo.format(temperature.temperatue) + "°");
                mTaText.setText("TA:" + fnum.format(temperature.ta) + "°");
                if (showHotMap) {
                    recycleBitmap();
                    bitmap = mHeatMap.drawHeatMap(temperature, mSerialProduct.getMeasureParm().xCount,
                            mSerialProduct.getMeasureParm().yCount);
                    if (bitmap != null)
                        mDataImageView.setImageBitmap(bitmap);
                } else if (!isStop) {
                    MeasureParm parm = mSerialProduct.getMeasureParm();
                    if (mMatrixView != null)
                        mMatrixView.setDataResource(temperature, parm.xCount, parm.yCount);
                }
            }
            return true;
        }
    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recycleBitmap();
        if (handler != null)
            handler.removeCallbacks(sendDate);
    }

    private void recycleBitmap() {
        if (bitmap != null) bitmap.recycle();
        bitmap = null;
    }

    private void onOrder(byte[] order) {
        if (mSerialProduct != null) {
            mSerialProduct.order(order);
        }
    }

    @Override
    public void onClick(final View view) {
        if (isAuto || mSerialProduct == null) return;
        byte[] order = mSerialProduct.getOrderDataOutputQuery();
        onOrder(order);
    }

    private Handler handler;

    private void SM32ASendOrder() {
        handler = new Handler();
        if (mSerialProduct instanceof SYM32A_32x32_XM) {
            handler.postDelayed(sendDate, 400);
        }
    }

    private Runnable sendDate = new Runnable() {
        @Override
        public void run() {
            if (mSerialProduct == null) return;
            byte[] order = mSerialProduct.getOrderDataOutputQuery();
            onOrder(order);
            if (mCheckBox.isChecked())
                handler.postDelayed(sendDate, 400);
        }
    };


}
