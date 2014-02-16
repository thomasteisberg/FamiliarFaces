package com.familiarfaces.ff;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class SelectionFragment extends Fragment {
	private final static String LOG_TAG = "SelectionFragment";
	
	TextView numNearbyText;
	TextView nobodyNearText;
	Button connectBtn;
	
	Intent locationServiceIntent;
	Intent inviteIntent;
	
	ArrayList<String> friends = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
	}
	
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection, 
	            container, false);
	    
	    numNearbyText = (TextView) view.findViewById(R.id.numNearbyText);
		nobodyNearText = (TextView) view.findViewById(R.id.nobodyNearText);
		connectBtn = (Button) view.findViewById(R.id.connectBtn);
	    
	    locationServiceIntent = new Intent(getActivity(), LocationUpdateService.class);
	    inviteIntent = new Intent(getActivity(), InviteActivity.class);
	    
	    CheckBox enableCheckbox = (CheckBox) view.findViewById(R.id.enableCheckbox);
	    enableCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					Log.d(LOG_TAG, "Starting location service");
					getActivity().startService(locationServiceIntent);
				}
				else{
					Log.d(LOG_TAG, "Stopping location service");
					getActivity().stopService(locationServiceIntent);
				}
			}
		});
	    
	    connectBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(inviteIntent);
			}
		});
	    
	    return view;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d(LOG_TAG, "onResume()");
		if(ParseUser.getCurrentUser() != null){
			
			// Get the friends list
			Request friendreq = Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
				
				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					if(users == null) Log.d(LOG_TAG, "Friend: Null users received");
					else{
						friends = new ArrayList<String>();
						for(GraphUser u : users){
							Log.d(LOG_TAG, "Friend: " + u.getId());
							friends.add(u.getId());
						}
						Log.d(LOG_TAG, "Number of Total Friends: " + users.size());
						
						// Now for the Parse call...
						Map<String, Object> params = new HashMap<String, Object>();
						params.put("username", ParseUser.getCurrentUser().getUsername());
						params.put("friends", friends);
						ParseCloud.callFunctionInBackground("findNumMatches", params, new FunctionCallback<Integer>() {
				
							@Override
							public void done(Integer numNearby, ParseException e) {
								if(numNearby == null) numNearby = 0;
								if(e != null) Log.d(LOG_TAG, "Parse response [findNumMatches]: " + e.getMessage());
								if(numNearby > 0){
									connectBtn.setVisibility(View.VISIBLE);
									nobodyNearText.setVisibility(View.GONE);
								}else{
									connectBtn.setVisibility(View.GONE);
									nobodyNearText.setVisibility(View.VISIBLE);
								}
								numNearbyText.setText(numNearby.toString());
							}
						});
					}
					
				}
			});
			friendreq.executeAsync();
		} else Log.d(LOG_TAG, "User not logged in");
	}
}
