package edu.franklin.androidpodcastplayer;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TestsActivity extends ActionBarActivity {

	//Test commit
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tests, menu);
		return true;
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
		return super.onOptionsItemSelected(item);
	}
	
	public void launchRssTest(View view) {
		Intent intent = new Intent(this, RssTestActivity.class);
		startActivity(intent);
	}
	
	public void launchMediaTest(View view) {
		Intent intent = new Intent(this, PlayPodcastActivity.class);
		startActivity(intent);
	}
	
	public void launchPodcastViewTest(View view) {
		Intent intent = new Intent(this, PodcastDetails.class);
		startActivity(intent);
	}
	
	public void launchRepTest(View view) {
		Intent intent = new Intent(this, RepositoryActivity.class);
		startActivity(intent);
	}
	
	public void launchFileManagerTest(View view) {
		Intent intent = new Intent(this, DownloadManagerActivity.class);
		startActivity(intent);
	}
	
	public void launchSubscriptionSettingsTest(View view) {
		Intent intent = new Intent(this, SubscriptionSettingsActivity.class);
		startActivity(intent);
	}
	
	public void launchMainTest(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
