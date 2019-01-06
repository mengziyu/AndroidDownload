
package com.ziyu.androiddownload.download.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.util.Log;

import com.ziyu.androiddownload.download.ThreadPoolManager;
import com.ziyu.androiddownload.download.bean.DownloadInfo;
import com.ziyu.androiddownload.download.bean.DownloadStatus;
import com.ziyu.androiddownload.download.callback.IConnectListener;
import com.ziyu.androiddownload.download.callback.IDownloadAction;
import com.ziyu.androiddownload.download.callback.IDownloadListener;
import com.ziyu.androiddownload.download.callback.IResponseListener;

/**
 * 真正下载类
 */
public class Downloader implements IDownloadListener,IConnectListener{
	private final static String TAG="Downloader";
	private String tag;
	private DownloadInfo mDownloadInfo;
	private ConnectTask mConnectTask;
	private IResponseListener mListener;
	private Map<String, List<DownloadTask>> mThreadMap;
	private int block=Runtime.getRuntime().availableProcessors() + 1;

	private Handler mHandler;
    
	public Downloader(String tag,DownloadInfo mDownloadInfo, IResponseListener mListener,Handler mHandler) {
		this.tag=tag;
		this.mDownloadInfo = mDownloadInfo;
		this.mListener = mListener;
		this.mHandler=mHandler;
		mThreadMap=new HashMap<>();
		
	}
	
	public void connect(){
		Log.d("ziyu", "connect "+mDownloadInfo.getUrl());
		mConnectTask=new ConnectTask(mDownloadInfo, this);
		ThreadPoolManager.getInstance().excute(mConnectTask);
		
	}
	
	public void startDownload(long length){
		List<DownloadTask> tasks;
		//续传
		if(mDownloadInfo.getFinishLength()>0&&mThreadMap.get(tag)!=null){
			tasks=mThreadMap.get(tag);
		}else {
			tasks=calculateTask(length);
			mThreadMap.put(tag, tasks);
		}
		
		onStart();
		
		for(DownloadTask task:tasks){
			ThreadPoolManager.getInstance().excute(task);
		}
	}
	
	/**
	 * 计算每一块下载多长
	 * @param length 需要下载的大小
	 * @return
	 */
	private List<DownloadTask> calculateTask(long length){
		
		Log.d(TAG, "length= "+length);
		
		List<DownloadTask> tasks=new ArrayList<>();
		
		final long average = length / block;
		for (int i = 0; i < block; i++) {
            final long start = average * i;
            final long end;
            if (i == block - 1) {
                end = mDownloadInfo.getLength();
            } else {
                end = start + average - 1;
            }
            tasks.add(new DownloadTask(start, end, mDownloadInfo, this));
        }
		
		return tasks;
	}
	
	private void checkComplete() {
		callback(DownloadStatus.STATUS_PROGRESS);
		if(isAllBlockFinished()){
			callback(DownloadStatus.STATUS_COMPLETE);
		}
	}
	
	private void checkPause() {
		if(isAllBlockPause()){
			Log.d(TAG, "all pause,has finish "+mDownloadInfo.getFinishLength());
			callback(DownloadStatus.STATUS_PAUSE);
			clearDownloadTask();
		}
	}
	
	private void checkCancel(){
		if(isAllBlockCancel()){
			Log.d(TAG, "all cancel");
			callback(DownloadStatus.STATUS_CANCEL);
			clearDownloadTask();
		}
	}
	
	
	private void clearDownloadTask(){
		for(DownloadTask task:mThreadMap.get(tag)){
			task.clearStatus();
			ThreadPoolManager.getInstance().remove(task);
		}
	}
	
	private boolean isAllBlockPause(){
		boolean pause=true;
		for(DownloadTask task:mThreadMap.get(tag)){
			if(!task.isBlockPause()){
				pause=false;
			}
		}
		return pause;	
	}
	
	private boolean isAllBlockCancel(){
		boolean cancel=true;
		for(DownloadTask task:mThreadMap.get(tag)){
			if(!task.isBlockCancel()){
				cancel=false;
			}
		}
		return cancel;	
	}
	
	
	private boolean isAllBlockFinished(){
		boolean finished=true;
		for(DownloadTask task:mThreadMap.get(tag)){
			if(!task.isBlockFinished()){
				finished=false;
			}
		}
		return finished;	
	}
	
	
	/**
	 * 任务没下载 
	 */
	public boolean isStatusNon(){
		return mDownloadInfo.getStatus()==DownloadStatus.STATUS_NON
				||mDownloadInfo.getStatus()==DownloadStatus.STATUS_CONNECTFAIL;
	}
	
	
	public boolean isStatusDownloading(){
		return mDownloadInfo.getStatus()==DownloadStatus.STATUS_CONNECTING
				||mDownloadInfo.getStatus()==DownloadStatus.STATUS_CONNECTED
				||mDownloadInfo.getStatus()==DownloadStatus.STATUS_START
				||mDownloadInfo.getStatus()==DownloadStatus.STATUS_PROGRESS;
	}
	
	public boolean isStatusPause(){
		return mDownloadInfo.getStatus()==DownloadStatus.STATUS_PAUSE;
	}
	
	private void callback(final int status,final String ...message){
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				switch (status) {
				case DownloadStatus.STATUS_START:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_START);
					mListener.onStart();
					break;
				case DownloadStatus.STATUS_PROGRESS:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_PROGRESS);
					int percent=(int) (mDownloadInfo.getFinishLength()*100/mDownloadInfo.getLength());
					mDownloadInfo.setProgress(percent);
					mListener.onProgeress(percent);
					break;
				case DownloadStatus.STATUS_COMPLETE:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_NON);
					mListener.onComplete();
					break;
				case DownloadStatus.STATUS_FAIL:
					// TODO 
					mListener.onFailed(message.length>0?message[0]:"");
					break;
				case DownloadStatus.STATUS_CONNECTING:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_CONNECTING);
					
					break;
				case DownloadStatus.STATUS_CONNECTED:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_CONNECTED);
					mListener.onConnect();
					break;
				case DownloadStatus.STATUS_PAUSE:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_PAUSE);
					mListener.onPause();
					break;
				case DownloadStatus.STATUS_CONNECTFAIL:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_NON);
					mListener.onFailed(message[0]+":"+message[1]);
					break;
				case DownloadStatus.STATUS_CANCEL:
					mDownloadInfo.setStatus(DownloadStatus.STATUS_NON);
					mListener.onCancel();
					break;
					
				default:
					break;
				}
			}
		});
	}
	
	private void removeConnectTask(){
		ThreadPoolManager.getInstance().remove(mConnectTask);
		mConnectTask=null;
	}

	
	/*-----------访问资源回调-------------*/
	@Override
	public void onConnecting(String url) {
//		if(mDownloadInfo.getStatus()!=DownloadStatus.STATUS_PAUSE){
//		}
		callback(DownloadStatus.STATUS_CONNECTING, url);
	}

	@Override
	public void onConnected(String url, long length) {
//		if(mDownloadInfo.getStatus()!=DownloadStatus.STATUS_PAUSE){
//		}
		callback(DownloadStatus.STATUS_CONNECTED, url,length+"");
		//连接成功开始下载
		startDownload(length);
	}

	@Override
	public void onConnectFail(String url, String message) {
		removeConnectTask();
		callback(DownloadStatus.STATUS_CONNECTFAIL, url,"connect fail:"+message);
	}
	
	@Override
	public void onConnectPause() {
		callback(DownloadStatus.STATUS_PAUSE);
	}

	@Override
	public void onConnectCancel() {
		removeConnectTask();
		callback(DownloadStatus.STATUS_CANCEL);
	}
	
	
	
	/*-----------client调用的操作方法-------------*/
	public void start() {
		connect();
	}

	/**
	 * 暂停下载
	 */
	public void pause() {
		if(mConnectTask!=null){
			mConnectTask.pause();
		}
		
		if(mThreadMap.containsKey(tag)){
			for(DownloadTask task:mThreadMap.get(tag)){
				task.pause();
			}
		}
	}

	/**
	 * 取消下载
	 */
	public void cancel() {
		if(mConnectTask!=null){
			mConnectTask.cancel();
		}
		
		if(mThreadMap.containsKey(tag)){
			for(DownloadTask task:mThreadMap.get(tag)){
				task.cancel();;
			}
		}
	}


	/*-----------每一块下载的回调-------------*/
	@Override
	public void onStart() {
		callback(DownloadStatus.STATUS_START);
	}

	@Override
	public void onBlockFinished() {
		checkComplete();
	}

	@Override
	public void onPause() {
		checkPause();
	}

	@Override
	public void onCancel() {
		checkCancel();
	}

	@Override
	public void onFailed(final String message) {
		
		callback(DownloadStatus.STATUS_FAIL, message);
		//下载过程中出错直接取消该下载
		cancel();
	}

	

}
