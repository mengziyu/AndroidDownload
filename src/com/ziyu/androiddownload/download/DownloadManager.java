

package com.ziyu.androiddownload.download;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ziyu.androiddownload.download.bean.DownloadInfo;
import com.ziyu.androiddownload.download.bean.DownloadStatus;
import com.ziyu.androiddownload.download.callback.IDownloadAction;
import com.ziyu.androiddownload.download.callback.IResponseListener;
import com.ziyu.androiddownload.download.core.DownloadRequest;
import com.ziyu.androiddownload.download.core.Downloader;

public class DownloadManager implements IDownloadAction {
	private String TAG="DownloadManager";
	private static DownloadManager mDM;
	private DownloadRequest mRequest;
	private IResponseListener mListener;
	private Map<String, Downloader> mDownloaders;
	private Handler mHandler=new Handler(Looper.getMainLooper());
	public static DownloadManager getInstance(){
		if(mDM==null){
			mDM=new DownloadManager();
		}
		return mDM;
	}
	
	public DownloadManager() {
		mDownloaders=new HashMap<>();
	}
	
	public void download(DownloadRequest request,IResponseListener listener){
		this.mRequest=request;
		this.mListener=listener;
		startDownload();
	}
	
	/**
	 * 任务没下载 
	 */
	public boolean isStatusNon(String tag){
		return mDownloaders.get(tag)==null||mDownloaders.get(tag).isStatusNon();
	}
	
	public boolean isStatusDownloading(String tag){
		return mDownloaders.get(tag).isStatusDownloading();
	}
	
	public boolean isStatusPause(String tag){
		return mDownloaders.get(tag).isStatusPause();
	}
	
	
	private boolean isStringEmpty(String s){
		return s==null||"".equals(s);
	}
	
	private void startDownload() {
		if(mRequest==null||mListener==null){
			Log.e(TAG,"request or listener is null");
			return;
		}
		
		if(isStringEmpty(mRequest.getDownloadUrl())){
			Log.e(TAG,"url is empty");
			return;
		}
		
		
		if(isStringEmpty(mRequest.getDownloadPath())){
			Log.e(TAG,"download path is empty");
			return;
		}
		
		clearDownload();
		
		DownloadInfo downloadInfo=new DownloadInfo(
				mRequest.getDownloadUrl(),
				mRequest.getDownloadPath(),0,0,0,
				DownloadStatus.STATUS_NON);
		
		Downloader downloader=new Downloader(mRequest.getTag(),downloadInfo, mListener,mHandler);
		mDownloaders.put(mRequest.getTag(), downloader);
		downloader.connect();
	}

	private void clearDownload() {
		String tag=mRequest.getTag();
		if(isStringEmpty(tag)){
			tag=mRequest.getDownloadUrl();
		}
		
		if(mDownloaders.get(tag)!=null){
			mDownloaders.remove(tag);
		}
	}

	@Override
	public void start(String tag) {
		if(mDownloaders.containsKey(tag)){
			mDownloaders.get(tag).start();
		}
	}

	@Override
	public void pause(String tag) {
		if(mDownloaders.containsKey(tag)){
			mDownloaders.get(tag).pause();
		}
	}

	@Override
	public void cancel(String tag) {
		if(mDownloaders.containsKey(tag)){
			mDownloaders.get(tag).cancel();
			mDownloaders.remove(tag);
		}
		
	}

	@Override
	public void cancelAll() {
		
		Iterator<String> iterator=mDownloaders.keySet().iterator();
		while(iterator.hasNext()){
			mDownloaders.get(iterator.next()).cancel();;
			iterator.remove();
		}
	}
	

}
