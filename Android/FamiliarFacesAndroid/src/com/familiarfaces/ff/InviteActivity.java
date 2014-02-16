package com.familiarfaces.ff;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;

public class InviteActivity extends Activity {
	private static final String LOG_TAG = "InviteActivity";
	
	ImageButton requestCallBtn;
	ImageButton requestChatBtn;
	Button findNewBtn;
	TextView descriptionText;
	
	String description = "";
	private String facebookUserId;
	
	private String matchUserId;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proposeconnection);
		
		requestCallBtn = (ImageButton) findViewById(R.id.requestCallBtn);
		requestChatBtn = (ImageButton) findViewById(R.id.requestChatBtn);
		findNewBtn = (Button) findViewById(R.id.findNewBtn);
		descriptionText = (TextView) findViewById(R.id.descriptionText);
		
		facebookUserId = getIntent().getExtras().getString("facebookUserId", ""); // TODO handle default value better
		
		loadUserMatch();
		
		findNewBtn.setOnClickListener(new OnClickListener() {
			// Report rejection
			@Override
			public void onClick(View v) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("matchUserId", matchUserId);
				ParseCloud.callFunctionInBackground("rejectMatchByInviter", params, new FunctionCallback<Object>() {

					@Override
					public void done(Object object, ParseException e) {
						loadUserMatch(); // Load someone new
					}
				});
			}
		});
		
		requestCallBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("matchUserId", matchUserId);
				ParseCloud.callFunctionInBackground("inviteCall", params, new FunctionCallback<Object>() {

					@Override
					public void done(Object object, ParseException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(InviteActivity.this);
				        builder.setMessage("Awesome! You'll get a call soon if you're both interested.")
				               .setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
				                   public void onClick(DialogInterface dialog, int id) {
				                       finish();
				                   }
				               }).show();
					}
				});
			}
		});
		
		requestChatBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Map<String, String> params = new HashMap<String, String>();
				params.put("matchUserId", matchUserId);
				ParseCloud.callFunctionInBackground("inviteChat", params, new FunctionCallback<Object>() {

					@Override
					public void done(Object object, ParseException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(InviteActivity.this);
				        builder.setMessage("Awesome! You'll get a text soon if you're both interested.")
				               .setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
				                   public void onClick(DialogInterface dialog, int id) {
				                       finish();
				                   }
				               }).show();
					}
				});
			}
		});
		
	}
	
	protected void loadUserMatch(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", facebookUserId);
		ParseCloud.callFunctionInBackground("getMatchName", params, new FunctionCallback<String>() {

			@Override
			public void done(String matchUsername, ParseException e) {
				if(e != null) Log.d(LOG_TAG, "Parse response [getMatchName]: " + e.getMessage());
				
				// TODO: Needs to be implemented server side
				matchUsername = "653691280";// TODO: Testing
				matchUserId = matchUsername;
				Request friendinfo = Request.newGraphPathRequest(ParseFacebookUtils.getSession(),
						"/" + matchUserId, new Request.Callback() {
							
							@Override
							public void onCompleted(Response response) {
								description += response.getGraphObject().getProperty("first_name");
								descriptionText.setText(description);
								description = "";
							}
						});
				friendinfo.executeAsync();

			}
		});
	}
}
