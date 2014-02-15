package com.familiarfaces.ff;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SelectionFragment extends Fragment {
	private final static String LOG_TAG = "SelectionFragment";
	
	Intent locationServiceIntent;
	
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.selection, 
	            container, false);
	    
	    locationServiceIntent = new Intent(getActivity(), LocationUpdateService.class);
	    
	    getActivity().startService(locationServiceIntent); // Start location by default
	    
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
	    
	    return view;
	}
}
