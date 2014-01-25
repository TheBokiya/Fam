package com.example.fam;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class FTPDownload extends AsyncTask<FTPFile, Object, FTPFile[]> {
	
	Context context;
	String ftpAddress, username, password, filePath, familyID;

	public FTPDownload(Context context, String ftpAddress, String username, String password,
			String filePath, String familyID) {
		this.context = context;
		this.ftpAddress = ftpAddress;
		this.username = username;
		this.password = password;
		this.filePath = filePath;
		this.familyID = familyID;
	}

	@Override
	protected FTPFile[] doInBackground(FTPFile... params) {
		// TODO Auto-generated method stub
		FTPFile[] myFiles = null;
		FTPClient client = new FTPClient();
		FileInputStream fis = null;
		boolean result;
		
		try {
			client.connect(ftpAddress);
			result = client.login(username, password);
			myFiles = client.listFiles(filePath+familyID);
			if (result == true) {
				System.out.println("Successfully logged in!");
			} else {
				System.out.println("Login Fail!");
			}
			publishProgress();
			
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return myFiles;
	}
	
	@Override
	protected void onProgressUpdate(Object... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		Toast.makeText(context, "Fetching images...", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPostExecute(FTPFile[] result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
	}

}
