
package com.ziyu.androiddownload.download.callback;

/**
 * 
 *每一块下载的回调接口
 */
public interface IDownloadListener {
	void onStart();
	void onBlockFinished();
	void onPause();
	void onCancel();
	void onFailed(String message);
}
