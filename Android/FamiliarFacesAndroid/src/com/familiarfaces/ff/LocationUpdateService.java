package com.familiarfaces.ff;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class LocationUpdateService extends Service {
	
	private static final String LOG_TAG = "LocationUpdateService";
	
	// Time intervals
	private static final int NEWER_LOCATION_TIME = 1000 * 60 * 10; // 10 minutes counts as much newer location
	private static final int MIN_LOCATION_UPDATE_TIME = (int)(60 * 1000 * 0.1); // 5 minutes between updates
	private static final int MAX_LOCATION_UPDATE_TIME = (int)(60 * 1000 * 0.25); // Maximum time between updates
	
	// Acquire a reference to the system Location Manager
	private LocationManager locationManager;
	
	// Store prior location (to check if new one is better or not)
	private Location lastLocation = null;
	private long lastUpdateTime = 0;
	
	// Flag to destroy background thread
	private boolean killThread = false;
	
	private LocationListener locationListener;
	
	private String facebookUserId = "";

	@Override
	public void onStart(final Intent intent, final int startId) {
		Log.d(LOG_TAG, "onStart");
		
//		// Setup Parse
 		//Parse.initialize(this, "zqmvHbnxVWFrs5g1IxKSWjw0bTM8FZ8X4OexawX8", "ABasvvzqquIRp1cc01qu4eYK8o1uxua6qixLgiO3");

 		Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	    	Log.d(LOG_TAG, "Got a facebook session");
	    }else{
	    	Log.d(LOG_TAG, "Facebook still isn't working!");
	    }
 		
// 		ParseUser.enableAutomaticUser();
// 		ParseACL defaultACL = new ParseACL();
// 	    
// 		// If you would like all objects to be private by default, remove this line.
// 		defaultACL.setPublicReadAccess(true);
// 		
// 		ParseACL.setDefaultACL(defaultACL, true);
		
		// Get a location manager
		locationManager = (LocationManager) this.getSystemService(this.getApplicationContext().LOCATION_SERVICE);
		
		// Create the location callbacks
		locationListener = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLocationChanged(Location location) {
				if(isBetterLocation(location, lastLocation)){
					lastLocation = location;
					sendLocationUpdate();
				}
			}
		};
	    
		// Register for both network and GPS location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_LOCATION_UPDATE_TIME, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_LOCATION_UPDATE_TIME, 0, locationListener);
		
		
		// Create a thread to make sure we're sending enough location updates
		Thread t = new Thread("LocationUpdateService(" + startId + ")") {
			@Override
	        public void run() {
				while(!killThread){
					checkLocationStatus();
					try{
						Thread.sleep(MAX_LOCATION_UPDATE_TIME/2);
					}catch(InterruptedException e){
						Log.d(LOG_TAG, "Sleep interrupted... grumpy process expected.");
					}
				}
	        }
	    };
	    t.start();
	    
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		killThread = true;
		locationManager.removeUpdates(locationListener);
		Log.d(LOG_TAG, "onDestory called for location service");
	}
	
	@SuppressWarnings("unchecked")
	protected void sendLocationUpdate(){
		if(ParseUser.getCurrentUser() == null){
			Log.d(LOG_TAG, "Not logged in. Can't upload data");
			return;
		}
		
		if(facebookUserId == ""){
			Log.d(LOG_TAG, "Waiting to get facebook id");
			Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
		            new Request.GraphUserCallback() {
		                @Override
		                public void onCompleted(GraphUser user, Response response) {
		                    if(user != null){
		                    	facebookUserId = user.getId();
		                    	Log.d(LOG_TAG, "Got facebook user id");
		                    }else{
		                    	Log.d(LOG_TAG, "Got NULL facebook user id");
		                    }
		                }
		            });
			request.executeAndWait();
		}
		
		if(lastLocation == null) return;
		Log.d(LOG_TAG, "Sending location "+facebookUserId+":(" + lastLocation.getLatitude() + ", " + lastLocation.getLongitude() + ") from " + lastLocation.getProvider());
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("posLat", lastLocation.getLatitude());
		params.put("posLong", lastLocation.getLongitude());
		params.put("timestamp", System.currentTimeMillis());
		params.put("userId", facebookUserId);
		try {
			ParseCloud.callFunction("storeLocation", params);
		} catch (ParseException e) {
			Log.d(LOG_TAG, "Parse response: " + e.getMessage());
		}
		
		
	}
	
	/**
	 * Check if an update has been sent recently. If one hasn't been sent, see if one can be sent.
	 */
	protected void checkLocationStatus(){
		// If too long has elapsed
		if(System.currentTimeMillis() - lastUpdateTime > MAX_LOCATION_UPDATE_TIME){
			Location loc;
			loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(loc == null){ // No GPS available, fall back to network
				loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if(loc == null){
					// No location available at all
					Log.d(LOG_TAG, "No location available.");
					return;
				}
			}
			// Should have a valid location or have already returned
			if(isBetterLocation(loc, lastLocation)) lastLocation = loc;
			sendLocationUpdate();
		}
	}

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > NEWER_LOCATION_TIME;
	    boolean isSignificantlyOlder = timeDelta < -NEWER_LOCATION_TIME;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

}
