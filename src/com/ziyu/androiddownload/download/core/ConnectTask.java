

package com.ziyu.androiddownload.download.core;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import com.ziyu.androiddownload.download.bean.DownloadInfo;
import com.ziyu.androiddownload.download.bean.DownloadStatus;
import com.ziyu.androiddownload.download.callback.IConnectListener;
import com.ziyu.androiddownload.download.callback.ITaskAction;

/**
 * 访问文件，获取文件长度 
 *
 */
public class ConnectTask implements Runnable,ITaskAction{
	private static final String TAG="ConnectTask";
	private IConnectListener mConnectListener;
	private DownloadInfo mDownloadInfo;
	private volatile int status=DownloadStatus.STATUS_NON;
	private boolean isInterrupte=false;
	public ConnectTask(DownloadInfo info,IConnectListener listener) {
		mDownloadInfo=info;
		mConnectListener=listener;
	}

	@Override
	public void run() {
		
		if(mConnectListener==null||mDownloadInfo==null){
			Log.e(TAG, "listener or info is null");
			return;
		}
		
		mConnectListener.onConnecting(mDownloadInfo.getUrl());
		
		HttpURLConnection connection=null;
		try {
			URL url=new URL(mDownloadInfo.getUrl());
			
			connection=(HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(15*1000);
			connection.setReadTimeout(15*1000);
			connection.setRequestMethod("GET");
			
			if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
				
				long length=connection.getContentLength();
				
				if(length>0){
					//先创建文件路径
					File file=new File(mDownloadInfo.getDownloadPath());
					//说明已下载了一部分，继续下载
					if(mDownloadInfo.getFinishLength()>0){
						//若文件不存在则重新下载
						if(!file.exists()){
							file.createNewFile();
							mDownloadInfo.setFinishLength(0);
						}
						//新文件
					}else{
						if(file.exists()){
							file.delete();
						}
						file.createNewFile();
					}
					
					mDownloadInfo.setLength(length);
					
					if(isInterrupte){
						if(status==DownloadStatus.STATUS_PAUSE){
							mConnectListener.onConnectPause();
						}else if(status==DownloadStatus.STATUS_CANCEL){
							mConnectListener.onConnectCancel();
						}
					}else{
						mConnectListener.onConnected(mDownloadInfo.getUrl(), length);
					}
					
				}else {
					mConnectListener.onConnectFail(mDownloadInfo.getUrl(), "lenght < 0");
				}
				
			}else{
				mConnectListener.onConnectFail(mDownloadInfo.getUrl(), "network error!!");
			}
			
			
		}catch (Exception e) {
			e.printStackTrace();
			mConnectListener.onConnectFail(mDownloadInfo.getUrl(), e.getMessage());
		}finally{
			if(connection!=null){
				connection.disconnect();
			}
		}
		
	}


	@Override
	public void pause() {
		status=DownloadStatus.STATUS_PAUSE;
		isInterrupte=true;
	}

	@Override
	public void cancel() {
		status=DownloadStatus.STATUS_CANCEL;
		isInterrupte=true;
	}

}
