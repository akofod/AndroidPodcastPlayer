package edu.franklin.androidpodcastplayer;

import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.franklin.androidpodcastplayer.data.ConfigData;
import edu.franklin.androidpodcastplayer.data.EpisodesData;
import edu.franklin.androidpodcastplayer.data.PodcastData;
import edu.franklin.androidpodcastplayer.models.Podcast;
import edu.franklin.androidpodcastplayer.models.Rss;

public class MainActivity extends ActionBarActivity {
	TableLayout table1;
	Context context = this;
	ConfigData configData = new ConfigData(context);
	PodcastData podcastData = new PodcastData(this);
	EpisodesData episodesData = new EpisodesData(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		table1 = (TableLayout) findViewById(R.id.table1);
		this.addRow("", "Title", "Saved","Available","Auto", 2);
		this.addRow("@drawable/droid", "Android Central", "3","3","yes" ,4);
		this.addRow("@drawable/cleveland", "Cleveland Browns", "3","3","Yes", 5);
		this.addRow("@drawable/ign", "IGN Gaing News","0", "512", "No", 6);
		this.addRow("@drawable/droid", "Android Central", "3","3","yes" ,4);
		this.addRow("@drawable/cleveland", "Cleveland Browns", "3","3","Yes", 5);
		this.addRow("@drawable/ign", "IGN Gaing News","0", "512", "No", 6);
		this.addRow("@drawable/droid", "Android Central", "3","3","yes" ,4);
		this.addRow("@drawable/cleveland", "Cleveland Browns", "3","3","Yes", 5);
		this.addRow("@drawable/ign", "IGN Gaing News","0", "512", "No", 6);
		
		
		configData.open();
		podcastData.open();
		episodesData.open();
		
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
				Podcast pc = new Podcast();
				pc.setFeedUrl(rss.getChannel().getLink());
				
//				podcastData.createPodcast(pc);
			}
		}
		catch(Exception e)
		{
			Log.e("Raw RSS", "Could not load the raw rss stuff", e);
		}
		
		
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
		if (id == R.id.action_tests) {
			Intent intent = new Intent(this, TestsActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void addRow(String imageUri, String title, String saved,
			String available, String isAuto ,int id){
		
		TableRow row = new TableRow(this);
		row.setId(id);
		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		ImageView label_icon = new ImageView(this);
		label_icon.setId(20+id);
		if(imageUri != ""){
		int imageResource = getResources().getIdentifier(imageUri, null, getPackageName());
		Drawable res = getResources().getDrawable(imageResource);
		label_icon.setImageDrawable(res);
		}
		row.addView(label_icon);// add the column to the table row here

		TextView label_title = new TextView(this);
		label_title.setId(21+id);// define id that must be unique
		String finaltitle = title.length()>12?title.substring(0,12):title;
		label_title.setText(finaltitle); // set the text for the header
		label_title.setTextColor(Color.WHITE); // set the color
		label_title.setPadding(5, 5, 5, 0); // set the padding (if required)
		label_title.setTextSize(12);
		row.addView(label_title); // add the column to the table row here
		
		TextView label_saved = new TextView(this);
		label_saved.setId(21+id);// define id that must be unique
		label_saved.setText(saved); // set the text for the header
		label_saved.setTextColor(Color.WHITE); // set the color
		label_saved.setPadding(5, 5, 5, 0); // set the padding (if required)
		row.addView(label_saved); // add the column to the table row here
		
		TextView label_available = new TextView(this);
		label_available.setId(22+id);// define id that must be unique
		label_available.setText(available); // set the text for the header
		label_available.setTextColor(Color.WHITE); // set the color
		label_available.setPadding(5, 5, 5, 0); // set the padding (if required)
		row.addView(label_available); // add the column to the table row here
		
		TextView label_is_auto = new TextView(this);
		label_is_auto.setId(23+id);// define id that must be unique
		label_is_auto.setText(isAuto); // set the text for the header
		label_is_auto.setTextColor(Color.WHITE); // set the color
		label_is_auto.setPadding(5, 5, 5, 0); // set the padding (if required)
		row.addView(label_is_auto); // add the column to the table row here
		table1.addView(row);
	}
}
