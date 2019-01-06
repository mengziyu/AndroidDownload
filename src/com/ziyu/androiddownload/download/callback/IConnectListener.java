

package com.ziyu.androiddownload.download.callback;

/**
 * 连接服务器回调监听 
 */
public interface IConnectListener {
	/**
	 * 正在连接
	 */
	void onConnecting(String url);
	/**
	 * 已经访问到了文件资源
	 * @param url 资源地址
	 * @param length 文件大小
	 */
	void onConnected(String url, long length);
	void onConnectPause();
	void onConnectCancel();
	void onConnectFail(String url,String message);
}
