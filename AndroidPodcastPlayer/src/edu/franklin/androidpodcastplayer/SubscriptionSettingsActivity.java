package edu.franklin.androidpodcastplayer;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SubscriptionSettingsActivity extends PreferenceActivity{
	   @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);	
	        addPreferencesFromResource(R.xml.settings);    
	 
	    }

}
