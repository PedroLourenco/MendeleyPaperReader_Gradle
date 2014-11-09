package com.mendeleypaperreader.utl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Message;
import com.mendeleypaperreader.activities.DocumentsDetailsActivity;
import com.mendeleypaperreader.R;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;

/**
 * Downloads a file in a thread. Will send messages to the
 * DocumentsDetailsActivity activity to update the progress bar.
 */
public class DownloaderThread extends Thread
{
	// constants
	private static final int DOWNLOAD_BUFFER_SIZE = 4096;

	// instance variables
	private DocumentsDetailsActivity parentActivity;
	private String downloadUrl;
	private String fileId;
	private String filename;


	/**
	 * Instantiates a new DownloaderThread object.
	 * @param inParentActivity Reference to DocumentsDetailsActivity activity.
	 * @param inUrl String representing the URL of the file to be downloaded.
	 */
	public DownloaderThread(DocumentsDetailsActivity inParentActivity, String inUrl, String fileId)
	{
		downloadUrl = "";
		this.fileId = fileId;
		if(inUrl != null)
		{
			downloadUrl = inUrl;
		}
		parentActivity = inParentActivity;
	}



	private String getFileName(){

		filename = null;
		try {
			URL url = new URL(downloadUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			conn.setInstanceFollowRedirects(false); 

			try {
				for(int i = 0; i < 10; i++)
				{
					url = new URL(conn.getHeaderField("Location")); 
					conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					conn.setInstanceFollowRedirects(false);
				}
			} catch (Exception e) {
			}

			String depo = conn.getHeaderField("Content-Disposition");
			String depoSplit[] = depo.split(";");
			int size = depoSplit.length;
			for(int i = 0; i < size; i++)
			{
				if(depoSplit[i].startsWith("filename="))
				{
					filename = depoSplit[i].replace("filename=", "").replace("\"", "").trim();

					i = size;
				}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
		}
		//update table Files with filename
		ContentValues values = new ContentValues();
		Uri uri_ = Uri.parse(MyContentProvider.CONTENT_URI_FILES + "/id");
		values.put(DatabaseOpenHelper.FILE_NAME, filename);	
		String where = DatabaseOpenHelper.FILE_ID + " = '" + this.fileId + "'";
		parentActivity.getContentResolver().update(uri_, values, where, null);

		return filename;



	}


	/**
	 * Connects to the URL of the file, begins the download, and notifies the
	 * AndroidFileDownloader activity of changes in state.
	 */
	@Override
	public void run()
	{
		URL url;
		URLConnection conn;
		int fileSize;
		String fileName;
		BufferedInputStream inStream;
		BufferedOutputStream outStream;
		File outFile;
		FileOutputStream fileStream;
		Message msg;

		// we're going to connect now
		msg = Message.obtain(parentActivity.activityHandler,
				DocumentsDetailsActivity.MESSAGE_CONNECTING_STARTED,
				0, 0, downloadUrl);
		parentActivity.activityHandler.sendMessage(msg);

		try
		{
			url = new URL(downloadUrl);
			conn = url.openConnection();
			conn.setUseCaches(false);
			fileSize = conn.getContentLength();

			// get the filename
			fileName = getFileName();

			// notify download start
			int fileSizeInKB = fileSize / 1024;
			msg = Message.obtain(parentActivity.activityHandler,
					DocumentsDetailsActivity.MESSAGE_DOWNLOAD_STARTED,
					fileSizeInKB, 0, fileName);
			parentActivity.activityHandler.sendMessage(msg);

			// start download
			inStream = new BufferedInputStream(conn.getInputStream());

			outFile = new File(parentActivity.getApplicationContext().getExternalFilesDir(null) + "/" + fileName);
			//outFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
			fileStream = new FileOutputStream(outFile);
			outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
			byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
			int bytesRead = 0, totalRead = 0;
			while(!isInterrupted() && (bytesRead = inStream.read(data, 0, data.length)) >= 0)
			{
				outStream.write(data, 0, bytesRead);

				// update progress bar
				totalRead += bytesRead;
				int totalReadInKB = totalRead / 1024;
				msg = Message.obtain(parentActivity.activityHandler,
						DocumentsDetailsActivity.MESSAGE_UPDATE_PROGRESS_BAR,
						totalReadInKB, 0);
				parentActivity.activityHandler.sendMessage(msg);
			}

			outStream.close();
			fileStream.close();
			inStream.close();

			if(isInterrupted())
			{
				// the download was canceled, so let's delete the partially downloaded file
				outFile.delete();
			}
			else
			{
				// notify completion
				msg = Message.obtain(parentActivity.activityHandler,
						DocumentsDetailsActivity.MESSAGE_DOWNLOAD_COMPLETE);
				parentActivity.activityHandler.sendMessage(msg);
			}
		}
		catch(MalformedURLException e)
		{
			String errMsg = parentActivity.getString(R.string.error_message_bad_url);
			msg = Message.obtain(parentActivity.activityHandler,
					DocumentsDetailsActivity.MESSAGE_ENCOUNTERED_ERROR,
					0, 0, errMsg);
			parentActivity.activityHandler.sendMessage(msg);
		}
		catch(FileNotFoundException e)
		{
			String errMsg = parentActivity.getString(R.string.error_message_file_not_found);
			msg = Message.obtain(parentActivity.activityHandler,
					DocumentsDetailsActivity.MESSAGE_ENCOUNTERED_ERROR,
					0, 0, errMsg);
			parentActivity.activityHandler.sendMessage(msg); 
		}
		catch(Exception e)
		{
			String errMsg = parentActivity.getString(R.string.error_message_general);
			msg = Message.obtain(parentActivity.activityHandler,
					DocumentsDetailsActivity.MESSAGE_ENCOUNTERED_ERROR,
					0, 0, errMsg);
			parentActivity.activityHandler.sendMessage(msg); 
		}
	}

}