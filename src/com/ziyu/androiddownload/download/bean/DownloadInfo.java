
package com.ziyu.androiddownload.download.bean;

public class DownloadInfo {
	private String url;
	private String downloadPath;
	private int progress;
	private long length;
	private long finishLength;
	private int status;
	
	
	
	public DownloadInfo(String url, String downloadPath, int progress, long length, long finishLength, int status) {
		super();
		this.url = url;
		this.downloadPath = downloadPath;
		this.progress = progress;
		this.length = length;
		this.finishLength = finishLength;
		this.status = status;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public long getFinishLength() {
		return finishLength;
	}
	public void setFinishLength(long finishLength) {
		this.finishLength = finishLength;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	
}
