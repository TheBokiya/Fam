package com.example.fam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class FTPUploadProfilePic extends AsyncTask<Void, Void, Void>{
	
	Context context;
	String ftpAddress, username, password, filePath, familyID, id;
	public static boolean uploadTask = true;
	

	public FTPUploadProfilePic(Context context, String ftpAddress,
			String username, String password, String filePath, String familyID,
			String id) {
		super();
		this.context = context;
		this.ftpAddress = ftpAddress;
		this.username = username;
		this.password = password;
		this.filePath = filePath;
		this.familyID = familyID;
		this.id = id;
	}



	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		FTPClient client = new FTPClient();
		FileInputStream fis = null;
		boolean result;
		boolean makeDirectory;
		boolean changeDirectory;
		boolean rename;
		try {
			client.connect(ftpAddress);
			result = client.login(username, password);
			makeDirectory = client.makeDirectory(""+familyID);
			changeDirectory = client.changeWorkingDirectory(""+familyID);

			if (makeDirectory)
				System.out.println("Directory successfully created");
			else
				System.out.println("Directory failed to create");

			if (changeDirectory)
				System.out.println("Directory changed successfully");
			else
				System.out.println("Directory failed to be changed");

			if (result == true) {
				System.out.println("Successfully logged in!");
			} else {
				System.out.println("Login Fail!");
			}
			File file = new File(filePath);
			String testName = file.getName();
			fis = new FileInputStream(file);

			// Upload file to the ftp server
			result = client.storeFile(testName, fis);
			rename = client.rename(testName, id+".jpg");
			if (result == true) {
				System.out.println("File is uploaded successfully");
			} else {
				System.out.println("File uploading failed");
			}
			
			if (rename) {
				System.out.println("Renamed successfully");
			} else {
				System.out.println("Failed to rename the file");
			}
			publishProgress();
			client.logout();
		} catch (FTPConnectionClosedException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				client.disconnect();
			} catch (FTPConnectionClosedException e) {
				System.out.println(e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		Toast.makeText(context, "Uploading image...", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Toast.makeText(context, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
		uploadTask = false;
	}
}
