
package com.ziyu.androiddownload;

import java.util.Arrays;

import com.ziyu.androiddownload.download.DownloadManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends Activity {

	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listView);

		listView.setAdapter(new ListAdapter(this,
				Arrays.asList(new String[] { "http://d1.music.126.net/dmusic/CloudMusic_official_5.7.2.122043.apk",
						"http://dldir1.qq.com/music/clntupate/QQMusic72282.apk",
						"http://downmobile.kugou.com/Android/KugouPlayer/9108/KugouPlayer_219_V9.1.0.apk" })));

	}
	
	public void cancelAll(View view){
		DownloadManager.getInstance().cancelAll();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DownloadManager.getInstance().cancelAll();
	}

}
