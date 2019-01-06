package com.ziyu.androiddownload;

import java.io.File;
import java.util.List;

import com.ziyu.androiddownload.download.DownloadManager;
import com.ziyu.androiddownload.download.callback.IResponseListener;
import com.ziyu.androiddownload.download.core.DownloadRequest;

import android.content.Context;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter{

	private Context mContext;
	private List<String> mList;
	
	
	public ListAdapter(Context mContext, List<String> mList) {
		this.mContext = mContext;
		this.mList = mList;
	}

	@Override
	public int getCount() {
		return mList==null?0:mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
			
			holder.percent=(TextView) convertView.findViewById(R.id.percent);
			holder.progressBar=(ProgressBar) convertView.findViewById(R.id.progressBar);
			holder.btnStart=(Button) convertView.findViewById(R.id.btnStart);
			holder.btnCancel=(Button) convertView.findViewById(R.id.btnCancel);
			
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}
		
		holder.btnStart.setOnClickListener(new ButtonClick(holder, position));
		holder.btnCancel.setOnClickListener(new ButtonClick(holder, position));
		
		return convertView;
	}
	
	
	private class ViewHolder{
		public TextView percent;
		public ProgressBar progressBar;
		public Button btnStart;
		public Button btnCancel;
	}
	
	
	
	private class ButtonClick implements OnClickListener{
		
		private ViewHolder holder;
		private int position;
		private DownloadRequest mRequest;
		
		public ButtonClick(ViewHolder holder, int position) {
			this.holder = holder;
			this.position = position;
			mRequest=createRequest(mList.get(position), "APK"+(position+1)+".apk");
		}


		@Override
		public void onClick(View v) {
			if(v==holder.btnStart){
				startDownload(mList.get(position));
			}else if(v==holder.btnCancel){
				DownloadManager.getInstance().cancel(mList.get(position));
			}
		}
		
		
		 public void startDownload(String url){
		    	Log.d("ziyu", " downlad "+url);
		    	if(DownloadManager.getInstance().isStatusNon(url)){
		    		
		    		DownloadManager.getInstance().download(mRequest, new IResponseListener() {
		    			long startTime;
		    			
		    			
		    			@Override
		    			public void onConnect() {
		    				holder.btnStart.setText("connecting");
		    			}
		    			
		    			@Override
		    			public void onStart() {
		    				Log.i("ziyu","onStart:"+position);
		    				startTime=System.currentTimeMillis();
		    				holder.btnStart.setText("Pause");
		    				holder.percent.setText("0%");
		    			}
		    			
		    			@Override
		    			public void onProgeress(final int progress) {
		    				holder.progressBar.setProgress(progress);
		    				holder.percent.setText(progress+"%");
		    				
		    			}
		    			
		    			@Override
		    			public void onPause() {
		    				Log.i("ziyu","onPause:"+position);
		    				holder.btnStart.setText("Start");
		    			}
		    			
		    			@Override
		    			public void onComplete() {
		    				Log.i("ziyu","onComplete:"+position+" time:"+((System.currentTimeMillis()-startTime)));
		    				holder.btnStart.setText("Start");
		    				
		    			}
		    			
		    			@Override
		    			public void onCancel() {
		    				holder.btnStart.setText("Start");
		    				holder.percent.setText("");
		    				holder.progressBar.setProgress(0);
		    			}
		    			
		    			@Override
		    			public void onFailed(String message) {
		    				Log.i("ziyu","onFailed:"+message);
		    			}
		    			
		    		});
		    		
		    	}else if(DownloadManager.getInstance().isStatusPause(url)){

		    		DownloadManager.getInstance().start(url);
		    		
		    	}else if(DownloadManager.getInstance().isStatusDownloading(url)){
		
		    		DownloadManager.getInstance().pause(url);
		    	}
		    
		    }
		    
		    private DownloadRequest createRequest(String url,String name){
		    	return new DownloadRequest.Builder()
				.setDownloadUrl(url)
				.setDownloadPath(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+name)
				.setTag(url)
				.build();
		    }
		
	}
	
	
	

}
