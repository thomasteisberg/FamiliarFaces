package com.familiarfaces.ff;

import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class LocationUpdateService extends Service {
	
	private static String LOG_TAG = "LocationUpdateService";
	private static int LOCATION_UPDATE_PERIOD = 10000;
	
	// Acquire a reference to the system Location Manager
	private LocationManager locationManager;
	
	private boolean killThread = false;

	@Override
	public void onStart(final Intent intent, final int startId) {
		Log.d(LOG_TAG, "onStart");
		
		Thread t = new Thread("LocationUpdateService(" + startId + ")") {
			@Override
	        public void run() {
				while(!killThread){
					updateLocation(intent, startId);
					try{
						Thread.sleep(LOCATION_UPDATE_PERIOD);
					}catch(InterruptedException e){
						Log.d(LOG_TAG, "Sleep interrupted... grumpy process expected.");
					}
				}
	        }
	    };
	    t.start();
	}
	
	private void updateLocation(final Intent intent, final int startId) {
	     Log.d(LOG_TAG, "Location update beginning.");
	 }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}