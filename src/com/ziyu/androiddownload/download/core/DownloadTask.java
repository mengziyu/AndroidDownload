

package com.ziyu.androiddownload.download.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import com.ziyu.androiddownload.download.bean.DownloadInfo;
import com.ziyu.androiddownload.download.bean.DownloadStatus;
import com.ziyu.androiddownload.download.callback.IDownloadListener;
import com.ziyu.androiddownload.download.callback.ITaskAction;

/**
 * 分段下载任务
 */
public class DownloadTask implements Runnable,ITaskAction{
	
	//该任务下载开始长度
	private long startLength;
	//该任务下载结束长度
	private long endLength;
	private DownloadInfo mDownloadInfo;
	private IDownloadListener mDownloadListener;
	private boolean isBlockFinished = false;
	private boolean isBlockPause = false;
	private boolean isBlockCancel = false;
	private long blockFinish=0;
	private int status=DownloadStatus.STATUS_NON; 
	private boolean isInterrupt=false;

	public DownloadTask(long startLength, long endLength, DownloadInfo mDownloadInfo,
			IDownloadListener mDownloadListener) {
		this.startLength = startLength;
		this.endLength = endLength;
		this.mDownloadInfo = mDownloadInfo;
		this.mDownloadListener = mDownloadListener;
	}

	public boolean isBlockFinished() {
		return isBlockFinished;
	}
	
	public boolean isBlockPause() {
		return isBlockPause;
	}
	
	public boolean isBlockCancel() {
		return isBlockCancel;
	}

	@Override
	public void run() {
		HttpURLConnection connection=null;
		
		try {
			URL url=new URL(mDownloadInfo.getUrl());
			connection=(HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(15*1000);
			connection.setReadTimeout(15*1000);
			connection.setRequestMethod("GET");
			
			startLength=startLength+blockFinish;
			connection.setRequestProperty("Range", "bytes=" + startLength + "-" + endLength);
			
			if(connection.getResponseCode()==HttpURLConnection.HTTP_PARTIAL){
				
				InputStream inputStream = null;
		        RandomAccessFile raf = null;
		        final byte[] buffer = new byte[1024*8];
		        
		        try {
					inputStream=connection.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
					downloadFail(e.getMessage());
					return;
				}
		        
		        raf=new RandomAccessFile(new File(mDownloadInfo.getDownloadPath()), "rwd");
				raf.seek(startLength);
		        
				while(!isInterrupt){
					int len = -1;
					try {
						len = inputStream.read(buffer);
						if (len == -1) {
							break;
						}
						raf.write(buffer, 0, len);
						synchronized (mDownloadListener) {
							//设置该模块的完成度
							blockFinish=blockFinish+len;
							//设置这个文件的完成度
							mDownloadInfo.setFinishLength(mDownloadInfo.getFinishLength()+len);
							mDownloadListener.onBlockFinished();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						downloadFail(e.getMessage());
						break;
					}
				}
			
				// 下载完成
				if (!isInterrupt) {
					isBlockFinished = true;
					synchronized (mDownloadListener) {
						mDownloadListener.onBlockFinished();
					}
					
				} else {
					if (status == DownloadStatus.STATUS_PAUSE) {
						isBlockPause = true;
						mDownloadListener.onPause();

					} else if (status == DownloadStatus.STATUS_CANCEL) {
						isBlockCancel = true;
						mDownloadListener.onCancel();
					}
				}
				
				inputStream.close();
				raf.close();
				
			}else {
				downloadFail("network error,code:"+connection.getResponseCode());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			downloadFail(e.getMessage());
		}finally{
			connection.disconnect();
		}
	}

	private void downloadFail(String message) {
		synchronized (mDownloadListener) {
			status=DownloadStatus.STATUS_FAIL;
			mDownloadListener.onFailed(message);
		}
	}
	
	/**
	 * 初始化状态 
	 */
	public void clearStatus(){
		status=DownloadStatus.STATUS_NON;
		isBlockPause=false;
		isBlockFinished=false;
		isBlockCancel=false;
		isInterrupt=false;
	}

	@Override
	public void pause() {
		status=DownloadStatus.STATUS_PAUSE;
		isInterrupt=true;
	}

	@Override
	public void cancel() {
		status=DownloadStatus.STATUS_CANCEL;
		isInterrupt=true;
	}

}
