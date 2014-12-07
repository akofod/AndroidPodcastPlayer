package edu.franklin.androidpodcastplayer;

import java.util.ArrayList;

import edu.franklin.androidpodcastplayer.data.ConfigData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class UserSettingsActivity extends ActionBarActivity {
	
	private ConfigData configData = new ConfigData(this);
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		configData.init();
		setContentView(R.layout.user_settings);
		this.updateCheckboxes();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onResume()
	{
		super.onResume();
		configData.open();
	}
	
	public void onStop()
	{
		super.onStop();
		configData.close();
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
// Handle action bar item clicks here. The action bar will
// automatically handle clicks on the Home/Up button, so long
// as you specify a parent activity in AndroidManifest.xml.
		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_tests) {
			Intent intent = new Intent(this, TestsActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Reads the checkboxes and updates DB
	 */
	private void updateDB() {	
		//data for wifi is index 0 and storage index 1		
		ArrayList<Integer> val = new ArrayList<Integer>();
		
		CheckBox chkStore = (CheckBox) findViewById(R.id.chkSDcard);
        CheckBox chkWifi = (CheckBox) findViewById(R.id.chkWifi);
        
        if(chkStore.isChecked()) {
        	val.add(Integer.valueOf(1));
        }
        else {
        	val.add(Integer.valueOf(0));
        }
		
        if(chkWifi.isChecked()) {
        	val.add(Integer.valueOf(1));
        }
        else {
        	val.add(Integer.valueOf(0));
        }
        
        configData.updateSettings(val);
	}
	
	private void updateCheckboxes() {
	       
        CheckBox chkStore = (CheckBox) findViewById(R.id.chkSDcard);
        CheckBox chkWifi = (CheckBox) findViewById(R.id.chkWifi);
        
     //get settings from db
        ArrayList<Integer> arrData = configData.getSettings();
        
        if(arrData.get(0) == 1) {
        	chkWifi.setChecked(true);
        }
        else {
        	chkWifi.setChecked(false);        	
        }
        
        if(arrData.get(1) == 1) {
        	chkStore.setChecked(true);
        }
        else {
        	chkStore.setChecked(false);        	
        }
		
	}
	
	public void onClick_Update(View v) {
        TextView textView = (TextView) findViewById(R.id.txtMsg); 
        this.updateDB();
        this.updateCheckboxes();
        textView.setText("Settings Updated!");
	
	}
	
}
