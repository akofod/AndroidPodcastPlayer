package edu.franklin.androidpodcastplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import edu.franklin.androidpodcastplayer.services.DownloadService;
import edu.franklin.androidpodcastplayer.services.FileManager;

public class DownloadManagerActivity extends ActionBarActivity 
{
	private FileManager fileManager = null;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		fileManager = new FileManager(this);
		setContentView(R.layout.activity_downloadmanager_test);
	}
	
	protected void onResume() 
	{
		super.onResume();
		registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
	}

	protected void onPause() 
	{
		super.onPause();
		unregisterReceiver(receiver);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_settings) 
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void downloadFile(View view) 
	{
		try 
		{
			int duration = Toast.LENGTH_LONG;
			// grab the text box and get its text
			EditText urlText = (EditText) findViewById(R.id.dmUrlField);
			EditText dirText = (EditText) findViewById(R.id.dmDirField);
			EditText fileNameText = (EditText) findViewById(R.id.dmFilenameField);
			// get its text
			String urlString = urlText.getText().toString();
			String dirString = dirText.getText().toString();
			String filenameString = fileNameText.getText().toString();
			
			Intent intent = new Intent(this, DownloadService.class);
			intent.putExtra(DownloadService.URL, urlString);
			intent.putExtra(DownloadService.DIR, dirString);
		    intent.putExtra(DownloadService.FILE, filenameString);
		    startService(intent);
		    
			// show what we are doing
			Toast.makeText(getApplicationContext(),
				"Downloading file : " + urlString + ", Filepath is : "+ 
				filenameString, duration).show();
		} 
		catch (Exception e) 
		{
			Log.e("FileManager", "Could not create the File system", e);
		}
	}
	
	
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			Bundle bundle = intent.getExtras();
			if (bundle != null) 
			{
				String d = bundle.getString(DownloadService.DIR);
				String f = bundle.getString(DownloadService.FILE);
				String fileDest = fileManager.getAbsoluteFilePath(d, f);
				int resultCode = bundle.getInt(DownloadService.RESULT);
				if (resultCode == RESULT_OK) 
				{
					Toast.makeText(DownloadManagerActivity.this,
							"Download complete. Download URI: " + fileDest,
							Toast.LENGTH_LONG).show();
				} 
				else 
				{
					Toast.makeText(DownloadManagerActivity.this,
							"Download failed", Toast.LENGTH_LONG).show();
				}
			}
		}
	};
}
