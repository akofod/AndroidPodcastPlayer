package edu.franklin.androidpodcastplayer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import com.squareup.picasso.Picasso;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
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
	
	// JSON Call to get top 4 tags
	private final String jsonGetTop4Tags = 
			"https://gpodder.net/api/2/tags/4.json";
	private final String jsonGetTop50 =
			"https://gpodder.net/toplist/50.json";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repository);
		
		tLayout = (TableLayout)findViewById(R.id.tableLayout);
		
		new JSONParseTopTags().execute();
		new JSONParseTopFifty().execute();
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
	
	private class JSONParseTopTags extends AsyncTask<Void, Void, JSONArray>
	{	
		@Override
		protected JSONArray doInBackground(Void... params) 
		{
			StringBuffer sb = new StringBuffer();
			String line = "";
			int statusCode;
			
			httpClient = new DefaultHttpClient();
			httpGet = new HttpGet(jsonGetTop4Tags);
			
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
		protected void onPostExecute(JSONArray result)
		{	
			TextView tv1 = (TextView)findViewById(R.id.textViewTop1);
			TextView tv2 = (TextView)findViewById(R.id.textViewTop2);
			TextView tv3 = (TextView)findViewById(R.id.textViewTop3);
			TextView tv4 = (TextView)findViewById(R.id.textViewTop4);
			
			try
			{
				tv1.setText(result.getJSONObject(0).getString("tag"));
				tv2.setText(result.getJSONObject(1).getString("tag"));
				tv3.setText(result.getJSONObject(2).getString("tag"));
				tv4.setText(result.getJSONObject(3).getString("tag"));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	private class JSONParseTopFifty extends AsyncTask<Void, Void, JSONArray>
	{	
		@Override
		protected JSONArray doInBackground(Void... params) 
		{
			StringBuffer sb = new StringBuffer();
			String line = "";
			int statusCode;
			
			httpClient = new DefaultHttpClient();
			httpGet = new HttpGet(jsonGetTop50);
			
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
		protected void onPostExecute(JSONArray result)
		{
			TableRow newRow;
			TextView tv;
			ImageView img;
			
			for(int i = 0; i < result.length(); i++)
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

					tLayout.addView(newRow);
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				} 
			}
		}
	}
}