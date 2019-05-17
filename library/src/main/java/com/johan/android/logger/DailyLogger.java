package com.johan.android.logger;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by johan on 2019/5/16.
 */

public class DailyLogger {

    private static class Holder {
        private static DailyLogger Instance = new DailyLogger();
    }

    private static final String TAG = "DailyLogger";

    private Logger logger;
    private String logPath;
    private boolean debug;

    private DailyLogger() {
    }

    private void init(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        logPath = path + date + ".log";
        synchronized (this) {
            if (logger != null) return;
            logger = new Logger();
            try {
                logger.startLog(logPath);
                print("初始化日志成功：" + logPath);
            } catch (IOException e) {
                logger = null;
                print("初始化日志失败：" + logPath);
                e.printStackTrace();
            }
        }
    }

    private void write(String content) {
        synchronized (this) {
            if (logger == null) return;
            try {
                logger.writeLog(content);
                print("写入日志成功：" + content);
            } catch (IOException e) {
                print("写入日志失败：" + content);
                e.printStackTrace();
            }
        }
    }

    private String read() {
        synchronized (this) {
            if (logger == null) return null;
            String content = logger.readLog();
            print("读取日志成功：" + content);
            return content;
        }
    }

    private void release() {
        synchronized (this) {
            if (logger == null) return;
            logger.stopLog();
            logger = null;
            print("释放日志成功：" + logPath);
        }
    }

    private void print(String message) {
        if (debug) {
            Log.e(TAG, message);
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getLogPath() {
        return logPath;
    }

    /**
     * 启动Log
     * @param folder 保存Log文件夹路径
     */
    public static void startLog(String folder) {
        Holder.Instance.init(folder);
    }

    /**
     * 写入Log
     * @param tag
     * @param content
     */
    public static void writeLog(String tag, String content) {
        String message = currentTime() + " " + tag + "：" + content;
        Holder.Instance.write(message);
    }

    /**
     * 读取Log
     * @return
     */
    public static String readLog() {
        return Holder.Instance.read();
    }

    /**
     * 停止Log
     */
    public static void stopLog() {
        Holder.Instance.release();
    }

    private static String currentTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return formatNumber(year) + "-" + formatNumber(month) + "-" + formatNumber(day) +
                " " + formatNumber(hour) + ":" + formatNumber(minute) + ":" + formatNumber(second);
    }

    private static String formatNumber(int number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

}
