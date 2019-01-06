package com.ziyu.androiddownload.download.core;

public class ThreadUtil {


    public static int ioPoolSize() {
        
        double blockingCoefficient = 0.9;
        return poolSize(blockingCoefficient);
    }

    public static int poolSize(double blockingCoefficient) {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        int poolSize = (int) (numberOfCores / (1 - blockingCoefficient));
        return poolSize;
    }
}
