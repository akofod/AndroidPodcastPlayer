package edu.franklin.androidpodcastplayer.services;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FileManager extends Service
{
	private IBinder binder = new FileManagerBinder();
	
	public FileManager()
	{
		super();
	}
	
	public boolean deleteFile(String dirname, String filename)
	{
		File file = getFile(dirname, filename);
		if(file.exists())
		{
			return file.delete();
		}
		return false;
	}
	
	/**
	 * Make all the directories up to the path if needed.
	 * @param path
	 * @return
	 */
	public boolean mkDir(String dirname)
	{
		boolean made = false;
		try
		{
			File dir = getDirectory(dirname);
			made = dir.mkdirs();
		}
		catch(Exception e)
		{
			Log.e("FileManager", "Could not create the directories");
		}
		return made;
	}
	
	/**
	 * Make all the directories up to the path if needed.
	 * @param path
	 * @return
	 */
	public boolean deleteDir(String dirname)
	{
		boolean made = false;
		try
		{
			File dir = getDirectory(dirname);
			if(dir.exists())
			{
		        File[] files = dir.listFiles();
		        if(files != null)
		        {
		            for(int i=0; i<files.length; i++) 
		            {
		                if(files[i].isDirectory()) 
		                {
		                	deleteDir(files[i].getName());
		                }
		                else 
		                {
		                    files[i].delete();
		                }
		            }
		        }
		    }
		    made = dir.delete();
		}
		catch(Exception e)
		{
			Log.e("FileManager", "Could not delete the directories");
		}
		return made;
	}
	
	public String[] listFiles(String dirname)
	{
		String[] files = new String[]{};
		try
		{
			File dir = getDirectory(dirname);
			if(dir.exists())
			{
				files = dir.list();
			}
		}
		catch(Exception e)
		{
			Log.e("FileManager", "Could not get the directory list", e);
		}
		return files;
	}
	
	public String getAbsoluteFilePath(String dirname, String filename)
	{
		//need to make the path in case it isn't there
		File directory = getDirectory(dirname);
		directory.mkdirs();
		return directory.getAbsolutePath() + "/" + filename;
	}
	
	private File getFile(String dirname, String filename)
	{
		return new File(getBasePath() + "/" + dirname + "/" + filename);
	}
	
	private File getDirectory(String dirname)
	{
		return new File(getBasePath() + "/" + dirname);
	}
	
	private String getBasePath()
	{
		//Initialize the path with some good value.
		//If we are just using internal storage, external storage, or shared external...
		//only one of these can be set at a time...probably a radio button in 
		//a shared user pref or something.
		boolean useInternal = true;
		boolean useExternal = false;
		boolean useExternalShared = false;
		
		String path = null;
		//only one configured for now.
		if(useInternal)
		{
			path = getFilesDir().getAbsolutePath();
		}
		else if(useExternalShared)
		{
			
		}
		else if(useExternal)
		{
			
		}
		return path;
	}
	
	public IBinder onBind(Intent intent) 
	{
		return binder;
	}

	public class FileManagerBinder extends Binder 
	{
		public FileManager getService() 
		{
			return FileManager.this;
		}
	}
}
