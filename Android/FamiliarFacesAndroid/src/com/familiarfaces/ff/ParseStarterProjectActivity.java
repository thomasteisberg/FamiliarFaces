package com.familiarfaces.ff;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class ParseStarterProjectActivity extends FragmentActivity {
	
	private static String LOG_TAG = "ParseStarterProjectActivity";
	
	private static final int SPLASH = 0;
	private static final int SELECTION = 1;
	private static final int FRAGMENT_COUNT = SELECTION +1;

	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	
	private boolean isResumed = false;
	
	
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = 
	    new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private Intent locationServiceIntent;
	
	private ParseUser parseUser;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);
		
		
		// Hide the authenticated fragment
		FragmentManager fm = getSupportFragmentManager();
	    fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
	    fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);

	    FragmentTransaction transaction = fm.beginTransaction();
	    for(int i = 0; i < fragments.length; i++) {
	        transaction.hide(fragments[i]);
	    }
	    transaction.commit();
		
		locationServiceIntent = new Intent(this.getApplicationContext(), LocationUpdateService.class);

		ParseAnalytics.trackAppOpened(getIntent());
		
		/*
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
		});*/
		
		

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(LOG_TAG, "onActivityResult");
	  super.onActivityResult(requestCode, resultCode, data);
	  ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	  uiHelper.onActivityResult(requestCode, resultCode, data);
	  
	  
	}
	
	@Override
	public void onResume() {
		Log.d(LOG_TAG, "onResume");
	    super.onResume();
	    isResumed = true;
	    uiHelper.onResume();
	    
	    List<String> permissions = Arrays.asList("basic_info", "user_about_me", "user_friends");
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
		  @Override
		  public void done(ParseUser user, ParseException err) {
		    if (user == null) {
		      Log.d(LOG_TAG, "Uh oh. The user cancelled the Facebook login.");
		    } else if (user.isNew()) {
		      Log.d(LOG_TAG, "User signed up and logged in through Facebook!");
		      parseUser = user;
		      Log.d(LOG_TAG, "parseUser.getUsername(): " + parseUser.getUsername());
		      startService(locationServiceIntent); // Start location by default
		    } else {
		      Log.d(LOG_TAG, "User logged in through Facebook!");
		      parseUser = user;
		      Log.d(LOG_TAG, "parseUser.getUsername(): " + parseUser.getUsername());
		      startService(locationServiceIntent); // Start location by default
		    }
		  }
		});
		Log.d(LOG_TAG, "Facebook login request sent");
	}

	@Override
	public void onPause() {
		Log.d(LOG_TAG, "onPause");
	    super.onPause();
	    isResumed = false;
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(LOG_TAG, "onSaveInstanceState");
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResumeFragments() {
		Log.d(LOG_TAG, "onResumeFragments");
	    super.onResumeFragments();
	    
	    Session session = Session.getActiveSession();

	    if (session != null && session.isOpened()) {
	        // if the session is already open,
	        // try to show the selection fragment
	        showFragment(SELECTION, false);
	    } else {
	        // otherwise present the splash screen
	        // and ask the person to login.
	        showFragment(SPLASH, false);
	    }
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		Log.d(LOG_TAG, "onSessionStateChange");
	    // Only make changes if the activity is visible
	    /*// TODO: this could have consequences
		if (isResumed) {
	        FragmentManager manager = getSupportFragmentManager();
	        // Get the number of entries in the back stack
	        int backStackSize = manager.getBackStackEntryCount();
	        // Clear the back stack
	        for (int i = 0; i < backStackSize; i++) {
	            manager.popBackStack();
	        }
	        if (state.isOpened()) {
	            // If the session state is open:
	            // Show the authenticated fragment
	            showFragment(SELECTION, false);
	        } else if (state.isClosed()) {
	            // If the session state is closed:
	            // Show the login fragment
	            showFragment(SPLASH, false);
	        }
	    }*/
	}
	
	private void showFragment(int fragmentIndex, boolean addToBackStack) {
	    FragmentManager fm = getSupportFragmentManager();
	    FragmentTransaction transaction = fm.beginTransaction();
	    for (int i = 0; i < fragments.length; i++) {
	        if (i == fragmentIndex) {
	            transaction.show(fragments[i]);
	        } else {
	            transaction.hide(fragments[i]);
	        }
	    }
	    if (addToBackStack) {
	        transaction.addToBackStack(null);
	    }
	    transaction.commit();
	}
}
