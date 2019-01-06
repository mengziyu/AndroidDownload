

package com.ziyu.androiddownload.download;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ziyu.androiddownload.download.core.ThreadUtil;

import android.util.Log;

public class ThreadPoolManager {

	private int poolSize = ThreadUtil.ioPoolSize();
    private int maxPoolSize = poolSize*2;
    private long keepAliveTime = 20;
	private ThreadPoolExecutor mExecutors;
	private static ThreadPoolManager mPoolManager;
	public static ThreadPoolManager getInstance(){
		if(mPoolManager==null){
			synchronized (ThreadPoolManager.class) {
				if(mPoolManager==null){
					mPoolManager=new ThreadPoolManager();
				}
			}
		}
		return mPoolManager;
	}
	
	private ThreadPoolManager(){
		mExecutors=new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingDeque());
	}
	
	public void excute(Runnable runnable){
		if(runnable!=null){
			mExecutors.execute(runnable);
		}
	}
	
	public void remove(Runnable runnable){
		if(runnable!=null){
			mExecutors.remove(runnable);
		}
	}
}
