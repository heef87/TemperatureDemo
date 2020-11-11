package com.ys.temperaturelib.device;

import android.os.Environment;
import android.os.SystemClock;

import com.ys.temperaturelib.utils.FileUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class TemperatureStorager implements Runnable {
    private static final boolean DEBUG = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    private String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/YsTemperature.txt";
    private boolean isWorked = true;
    private Thread mThread;
    private Queue<String> mQueue1 = new LinkedList<>();
    private static TemperatureStorager instance;

    private TemperatureStorager() {

    }

    public static TemperatureStorager getInstance() {
        if (instance == null) {
            synchronized (TemperatureStorager.class) {
                if (instance == null) instance = new TemperatureStorager();
            }
        }
        return instance;
    }

    public void add(String temp) {
        if (!DEBUG) return;
        if (mThread == null) {
            mThread = new Thread(this);
            mThread.start();
        }
        mQueue1.add(temp);
    }

    public void exit() {
        isWorked = false;
        if (mThread != null)
            mThread.interrupt();
        mThread = null;
    }

    @Override
    public void run() {
        FileUtil.writeFileAppend(fileName, new String("\n\n" + "==============================================分割线======" +
                "========================================" + "\n\n"));
        while (isWorked) {
            String poll = mQueue1.poll();
            StringBuffer mBuffer = new StringBuffer();
            if (poll != null) {
                mBuffer.append(simpleDateFormat.format(new Date(System.currentTimeMillis())));
                mBuffer.append(":");
                mBuffer.append("\n");
                mBuffer.append("TT1: " + poll);
                mBuffer.append("\n");
                FileUtil.writeFileAppend(fileName, mBuffer.toString());
            }
            SystemClock.sleep(100);
        }
    }
}
