

package com.ziyu.androiddownload.download.core;

public class DownloadRequest {
	
	private String downloadUrl;
	private String downloadPath;
	private String tag;
	
	public DownloadRequest() {
	}
	
	private DownloadRequest(String downloadUrl, String downloadPath,String tag) {
		this.downloadUrl = downloadUrl;
		this.downloadPath = downloadPath;
		this.tag=tag;
	}
	

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getDownloadPath() {
		return downloadPath;
	}
	
	public String getTag() {
		return tag;
	}


	public static class Builder{
		private String downloadUrl;
		private String downloadPath;
		private String tag;
		
		public Builder setDownloadUrl(String downloadUrl) {
			this.downloadUrl = downloadUrl;
			return this;
		}
		public Builder setDownloadPath(String downloadPath) {
			this.downloadPath = downloadPath;
			return this;
		}
		public Builder setTag(String tag) {
			this.tag = tag;
			return this;
		}
		
		public DownloadRequest build(){
			return new DownloadRequest(this.downloadUrl, this.downloadPath,this.tag);
		}
	}
}
