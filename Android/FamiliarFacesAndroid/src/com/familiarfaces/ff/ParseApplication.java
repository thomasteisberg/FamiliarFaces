package com.familiarfaces.ff;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class ParseApplication extends Application {
	
	private static String LOG_TAG = "ParseApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "zqmvHbnxVWFrs5g1IxKSWjw0bTM8FZ8X4OexawX8", "ABasvvzqquIRp1cc01qu4eYK8o1uxua6qixLgiO3");
		ParseFacebookUtils.initialize("598599153543493");
		
	}

}
