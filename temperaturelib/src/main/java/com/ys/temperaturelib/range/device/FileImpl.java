package com.ys.temperaturelib.range.device;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ys.temperaturelib.range.MesureResult;
import com.ys.temperaturelib.range.Ranger;
import com.ys.temperaturelib.utils.FileUtil;

import java.io.File;

public class FileImpl implements Ranger {
    private static final String DEFUALT_FILE = "/sys/kernel/range/getdistance";
    private File mFile;
    private String mPath;
    MesureResult result;

    public FileImpl() {
        this(DEFUALT_FILE);
    }

    public FileImpl(String path) {
        mPath = path;
    }

    @Override
    public void create() {
        mFile = new File(mPath);
    }

    @Override
    public void release() {
        mHandler.removeMessages(1001);
    }

    @Override
    public void onMesure(MesureResult result) {
        if (mFile == null || !mFile.exists()) return;
        this.result = result;
        release();
        mHandler.sendEmptyMessageDelayed(1001, 500);
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            String distance = FileUtil.readFile(mFile);
            if (result != null) result.onResult(distance);
            mHandler.sendEmptyMessageDelayed(1001,300);
            return false;
        }
    });

}
