package com.familiarfaces.ff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class InviteActivity extends Activity {
	private static final String LOG_TAG = "InviteActivity";
	
	ImageButton requestCallBtn;
	ImageButton requestChatBtn;
	Button findNewBtn;
	TextView descriptionText;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proposeconnection);
		
		requestCallBtn = (ImageButton) findViewById(R.id.requestCallBtn);
		requestChatBtn = (ImageButton) findViewById(R.id.requestChatBtn);
		findNewBtn = (Button) findViewById(R.id.findNewBtn);
		descriptionText = (TextView) findViewById(R.id.descriptionText);
		
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
	            new Request.GraphUserCallback() {
	                @Override
	                public void onCompleted(GraphUser user, Response response) {
	                    if(user != null) Log.d(LOG_TAG, "Username: " + user.getId() + " " );
	                    else Log.d(LOG_TAG, "Username: NULL");
	                }
	            });
		Request friendreq = Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
			
			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				if(users == null) Log.d(LOG_TAG, "Friend: Null users received");
				else{
					for(GraphUser u : users){
						Log.d(LOG_TAG, "Friend: " + u.getId());
					}
					Log.d(LOG_TAG, "Number of Total Friends: " + users.size());
				}
				
			}
		});
	    request.executeAsync();
	    friendreq.executeAsync();
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", ParseUser.getCurrentUser().getUsername());
		params.put("friends", ""); // TODO: Find friends. Hmm... sounds weird that way.
		ParseCloud.callFunctionInBackground("getMatchName", params, new FunctionCallback<String>() {

			@Override
			public void done(String matchUsername, ParseException e) {
				if(e != null) Log.d(LOG_TAG, "Parse response [getMatchName]: " + e.getMessage());
				
				// Query facebook here
			}
		});
		
	}
}
