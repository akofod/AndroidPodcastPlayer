package edu.franklin.androidpodcastplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.picasso.Picasso;

import edu.franklin.androidpodcastplayer.models.Podcast;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RepositoryActivity extends ActionBarActivity
{	
	private HttpClient httpClient;
	private HttpResponse httpResponse;
	private HttpGet httpGet;
	private HttpEntity httpEntity;
	
	private BufferedReader reader;
	
	private TableLayout tLayout;
	private EditText search;
	private JSONArray currentPodcasts;
	
	private int currentIndex;
	
	// JSON Call to get top 50 podcasts
	private final String jsonGetTop50 =
			"https://gpodder.net/toplist/50.json";
	// JSON Call to get Podcast Details
	private final String jsonGetDetails = 
			"https://gpodder.net/api/2/data/podcast.json?url={url}";
	private final String jsonSearch =
			"https://gpodder.net/search.json?q={query}";
	
	
	public void setCurrentPodcasts(JSONArray podcast)
	{
		currentPodcasts = podcast;
	}
	
	public JSONArray getCurrentPodcasts()
	{
		return currentPodcasts;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repository);
		
		tLayout = (TableLayout)findViewById(R.id.tableLayout);
		currentIndex = 0;
		
		new JSONParseTopPodcasts(RepositoryActivity.this).execute();
	}

	private void clearList()
	{
		tLayout.removeAllViews();
		currentIndex = 0;
	}
	
	private void addHasMore()
	{
		if (currentIndex < getCurrentPodcasts().length())
		{
			TableRow newRow = new TableRow(this);
			TextView text = new TextView(this);
			text.setText("Click for More");
			text.setTextColor(-1);
			newRow.addView(text);
			tLayout.addView(newRow);
		}
	}
	
	public void manualEntry(View view)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(RepositoryActivity.this);
		ab.setTitle("Enter URL");
		ab.setMessage("Please Enter the URL for the RSS feed:");
		
		final EditText input = new EditText(this);
		ab.setView(input);
		ab.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  String value = input.getText().toString();
				  // Parsing and subscription logic here
				  }
				});
		
		ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});

			ab.show();
	}
	
	private void populateList(JSONArray result)
	{
		TableRow newRow;
		TextView tv;
		ImageView img;
		
		int endIndex = currentIndex + 10;
		
		if (endIndex > result.length())
		{
			endIndex = result.length();
		}
		
		for(int i = currentIndex; i < endIndex; i++)
		{
			try 
			{
				newRow = new TableRow(RepositoryActivity.this);
				img = new ImageView(RepositoryActivity.this);
				tv = new TextView(RepositoryActivity.this);
				
				// Set up Image View for Album Art
				Picasso.with(RepositoryActivity.this)
					.load(result.getJSONObject(i).getString("logo_url"))
					.resize(100, 100)
					.into(img);
									
				// Set up Text View for Title
				tv = new TextView(RepositoryActivity.this);
				tv.setTextColor(-1);
				tv.setText(result.getJSONObject(i).getString("title"));
				newRow.addView(img);
				newRow.addView(tv);
				
				// Store the URL in a hidden field
				final String url= result.getJSONObject(i).getString("url");

				newRow.setOnClickListener(new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								new JSONGetDetails().execute(url);
							}
						});

				tLayout.addView(newRow);
				
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
		}
		if (endIndex < currentPodcasts.length())
		{
			currentIndex += 10;
		}		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tests, menu);
		return true;
	}

	private JSONArray jsonConnect(String query)
	{
		StringBuffer sb = new StringBuffer();
		String line = "";
		int statusCode;
		
		httpClient = new DefaultHttpClient();
		httpGet = new HttpGet(query);
		
		try
		{
			httpResponse = httpClient.execute(httpGet);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200)
			{
				httpEntity = httpResponse.getEntity();
				reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
				
				while ((line = reader.readLine()) != null)
				{
					sb.append(line);
				}
				return new JSONArray(sb.toString()); 
			}
		}
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
		      e.printStackTrace();
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return null;
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
		if (id == R.id.action_home) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Helper Class to perform a search of the repository
	 * @author Alan Borlie
	 *
	 */
	private class JSONSearch extends AsyncTask<String, Integer, Void>
	{
		private RepositoryActivity parent;
		
		public JSONSearch(RepositoryActivity activity) 
		{
			this.parent = activity;
		}
		@Override
		protected Void doInBackground(String... params) 
		{
			String jsonSearchString;
			
			jsonSearchString = jsonSearch.replace("{query}", params[0]);
			parent.setCurrentPodcasts(jsonConnect(jsonSearchString));
			publishProgress(0);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... notUsed)
		{
			clearList();
			populateList(parent.getCurrentPodcasts());
		}
	}
	
	public void search(View view)
	{
		search = (EditText)findViewById(R.id.editTextSearch);
		new JSONSearch(RepositoryActivity.this).execute(search.getText().toString());
	}
	
	private class JSONParseTopPodcasts extends AsyncTask<Void, Integer, Void>
	{	
		private RepositoryActivity parent;
		public JSONParseTopPodcasts(RepositoryActivity activity) 
		{
			this.parent = activity;
		}

		@Override
		protected Void doInBackground(Void... params) 
		{
			parent.setCurrentPodcasts(jsonConnect(jsonGetTop50));
			//currentPodcasts = jsonConnect(jsonGetTop50);
			publishProgress(0);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... notUsed)
		{
			populateList(parent.getCurrentPodcasts());
		}

	}
	
	private class JSONGetDetails extends AsyncTask<String, Void, JSONObject>
	{

		@Override
		protected JSONObject doInBackground(String... params) 
		{
			StringBuffer sb = new StringBuffer();
			String line = "";
			int statusCode;
			String jsonDetails;
			
			
			jsonDetails = jsonGetDetails.replace("{url}", params[0]);
			
			httpClient = new DefaultHttpClient();
			httpGet = new HttpGet(jsonDetails);
			
			try
			{
				httpResponse = httpClient.execute(httpGet);
				statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode == 200)
				{
					httpEntity = httpResponse.getEntity();
					reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
					
					while ((line = reader.readLine()) != null)
					{
						sb.append(line);
					}
					return new JSONObject(sb.toString());
				}
			}
			catch (ClientProtocolException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
			      e.printStackTrace();
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
			
			return null;
		}
	
		@Override
		protected void onPostExecute(JSONObject result)
		{
			AlertDialog.Builder ab = new AlertDialog.Builder(RepositoryActivity.this);
			StringBuilder sb =  new StringBuilder();
			try 
			{
				sb.append(result.getString("title"));
				sb.append("\n\n");
				sb.append(result.getString("description"));
				
				TextView content = new TextView(RepositoryActivity.this);
				content.setText(sb.toString());
				ab.setView(content);
				
				ab.setPositiveButton("Subscribe", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						  // Parsing and subscription logic here
						  }
						});
				
				ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					    // Canceled.
					  }
					});

				ab.show();
				
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			} 
		}
	}
}