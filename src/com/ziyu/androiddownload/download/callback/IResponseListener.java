

package com.ziyu.androiddownload.download.callback;

/**
 * client接收的回调接口
 */
public interface IResponseListener{
	
	void onConnect();
	/**
	 * 开始下载了
	 */
	void onStart();
	void onProgeress(int progress);
	void onComplete();
	void onPause();
	void onCancel();
	void onFailed(String message);
}
