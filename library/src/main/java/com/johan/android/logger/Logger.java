package com.johan.android.logger;

import java.io.IOException;

/**
 * Created by johan on 2019/5/13.
 */

public class Logger {

    static {
        System.loadLibrary("logger-lib");
    }

    public native void startLog(String path) throws IOException;

    public native void writeLog(String content) throws IOException;

    public native String readLog();

    public native void stopLog();

}
