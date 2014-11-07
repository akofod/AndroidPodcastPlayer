package edu.franklin.androidpodcastplayer;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Rss;

public class RssTestActivity extends ActionBarActivity 
{

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rss_test);
		try
		{
			//something to fetch the raw junk with
			Resources resources = getResources();
			//the ids we want to fetch
			int[] rawFeeds = new int[]{
				R.raw.androd_dev_backstage_rss,
				R.raw.coder_radio_rss,
				R.raw.java_posse_rss,
				R.raw.technophilia_rss
			};
			//now go over the feed ids and initialize an Rss object from the xml
			for(int id : rawFeeds)
			{
				Rss rss = new Rss();
				rss.initializeFromRaw(resources.openRawResource(id));
				Log.i("Raw Rss Test", "Got back an Rss object!\n" + rss.toString());
				//fill in the podcast details here.
				Podcast pc = new Podcast();
				pc.setFeedUrl(rss.getChannel().getLink());				
			}
		}
		catch(Exception e)
		{
			Log.e("Raw RSS", "Could not load the raw rss stuff", e);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
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
	
	public void fetchRss(View view)
	{
		int duration = Toast.LENGTH_SHORT;
		//grab the text box and get its text
		EditText editText = (EditText) findViewById(R.id.urlField);
		//get its text
		String url = editText.getText().toString();
		//show what we are doing
		Toast.makeText(getApplicationContext(), "Fetching Rss at " + url, duration).show();
		try
		{
			Rss rss = new Rss();
			rss.initializeFromUrl(url);
			WebView wv = (WebView)findViewById(R.id.webView1);
			wv.loadData(rss.toHtml(url), "text/html", null);
			
		}
		catch(Exception e)
		{
			Log.e("RSS", "Could not parse the RSS!", e);
			Toast.makeText(getApplicationContext(), 
					"Rss could not be parsed. Are you sure the url is valid? Check LogCat for problems.", 
					Toast.LENGTH_LONG).show();
		}
	}
}
