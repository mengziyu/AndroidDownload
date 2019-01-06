

package com.ziyu.androiddownload.download.callback;

public interface IDownloadAction {
	void start(String tag);
	void pause(String tag);
	void cancel(String tag);
	void cancelAll();
}
