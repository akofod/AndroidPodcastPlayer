package edu.franklin.androidpodcastplayer.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class FileManager
{
	private Context context = null;
	
	public FileManager(Context context)
	{
		this.context = context;
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
	
	public boolean moveFile(String absoluteSourcePath, String absoluteDestinationPath)
	{
		String[] tokens = breakFilePath(absoluteDestinationPath);
		return moveFile(absoluteSourcePath, tokens[0], tokens[1]);
	}
	
	public boolean moveFile(String absolutePath, String dir, String file)
	{
		return copyFile(absolutePath, dir, file, true);
	}
	
	public boolean copyFile(String absoluteSourcePath, String absoluteDestinationPath)
	{
		String[] tokens = breakFilePath(absoluteDestinationPath);
		return copyFile(absoluteSourcePath, tokens[0], tokens[1]);
	}
	
	public boolean copyFile(String absolutePath, String dir, String file)
	{
		return copyFile(absolutePath, dir, file, false);
	}
	
	private String[] breakFilePath(String path)
	{
		String[] pathTokens = new String[2];
		pathTokens[0] = path.substring(0, path.lastIndexOf("/"));
		pathTokens[1] = path.substring(path.lastIndexOf("/") + 1);
		return pathTokens;
	}
	
	private boolean copyFile(String absolutePath, String dir, String file, boolean remove)
	{
		try
		{
			File source = new File(absolutePath);
			//anything to copy?
			if(!source.exists())
			{
				return false;
			}
			File dest = new File(getAbsoluteFilePath(dir, file));
			//about to clobber this guy, so get rid of him
			if(dest.exists())
			{
				dest.delete();
			}
			FileInputStream inputStream = new FileInputStream(source);
			FileOutputStream outputStream = new FileOutputStream(dest);
			int read;
			//now copy the bytes
			while((read = inputStream.read()) != -1)
			{
				outputStream.write(read);
			}
			//close em up
			inputStream.close();
			outputStream.close();
			//get rid of the source?
			if(remove)
			{
				source.delete();
			}
		}
		catch(Exception e)
		{
			Log.e("FileManager", "Could not copy the file", e);
			return false;
		}
		return true;
	}
	
	public String getAbsoluteFilePath(String dirname, String filename)
	{
		//need to make the path in case it isn't there
		File directory = getDirectory(dirname);
		if(!directory.exists())
		{
			directory.mkdirs();
		}
		if(filename != null)
		{
			Log.d("FM", "Getting absoluteFilePath for " + dirname + " and " + filename + " + is returning " + directory.getAbsolutePath() + "/" + filename);
			return directory.getAbsolutePath() + "/" + filename;
		}
		Log.d("FM", "Getting absoluteFilePath for " + dirname + " + is returning " + directory.getAbsoluteFile());
		return directory.getAbsolutePath();
	}
	
	public Uri getUriFromPath(String filePath)
	{
		File file = new File(filePath);
		return Uri.fromFile(file);
	}
	
	private File getFile(String dirname, String filename)
	{
		return new File(getBasePath() + "/" + dirname + "/" + filename);
	}
	
	private File getDirectory(String dirname)
	{
		File f = new File(dirname);
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
			path = context.getFilesDir().getAbsolutePath();
		}
		else if(useExternalShared)
		{
			
		}
		else if(useExternal)
		{
			
		}
		return path;
	}
}
