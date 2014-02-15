package com.familiarfaces.ff;

import com.familiarfaces.ff.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.ParseAnalytics;

public class ParseStarterProjectActivity extends Activity {
	
	private static String LOG_TAG = "ParseStarterProjectActivity";
	private Intent locationServiceIntent;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		locationServiceIntent = new Intent(this.getApplicationContext(), LocationUpdateService.class);

		ParseAnalytics.trackAppOpened(getIntent());
		
		Button startServiceBtn = (Button) findViewById(R.id.startLocationServiceBtn);
		startServiceBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startService(locationServiceIntent);
				Log.d(LOG_TAG, "Manually started location service");
			}
		});
		Button stopServiceBtn = (Button) findViewById(R.id.stopLocationServiceBtn);
		stopServiceBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				stopService(locationServiceIntent);
				Log.d(LOG_TAG, "Manually stopped location service");
			}
		});

	}
}
