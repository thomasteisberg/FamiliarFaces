package com.familiarfaces.ff;

import com.familiarfaces.ff.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.ParseException;

public class ParseStarterProjectActivity extends Activity {
	
	private static String LOG_TAG = "ParseStarterProjectActivity";
	private Intent locationServiceIntent;
	
	private ParseUser parseUser;
	
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
		
		ParseFacebookUtils.logIn(this, new LogInCallback() {
		  @Override
		  public void done(ParseUser user, ParseException err) {
		    if (user == null) {
		      Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
		    } else if (user.isNew()) {
		      Log.d("MyApp", "User signed up and logged in through Facebook!");
		      parseUser = user;
		    } else {
		      Log.d("MyApp", "User logged in through Facebook!");
		      parseUser = user;
		    }
		  }
		});

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}
}
