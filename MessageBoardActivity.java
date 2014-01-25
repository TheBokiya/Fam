package com.example.fam;

import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;

public class MessageBoardActivity extends Activity implements OnClickListener {

	String URL = "jdbc:mysql://";

	ArrayList<String> messageIds, messages, names, pictures, ids;
	ArrayList<Timestamp> timestamps;
	String familyId, userId, firstName;
	String id, content, name; 
	TextView messagesTV;

	ScrollView messagesSV;
	LinearLayout messagesLL;
	RelativeLayout messagesRL;
	EditText newMessageET;
	Button newMessageButton, refreshMsgButton;

	Boolean insertMessageTask = false;
	Boolean getMessagesTask = true;
	Boolean getPicturesTask = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.status_board_activity);

		messagesSV = (ScrollView) findViewById(R.id.messagesSV);
		messagesLL = (LinearLayout) findViewById(R.id.messagesLL);
		messagesRL = (RelativeLayout) findViewById(R.id.messagesRL);

		messageIds = new ArrayList<String>();
		timestamps = new ArrayList<Timestamp>();
		messages = new ArrayList<String>();
		names = new ArrayList<String>();
		pictures = new ArrayList<String>();
		ids = new ArrayList<String>();

		newMessageET = (EditText) findViewById(R.id.newMessageET);
		newMessageButton = (Button) findViewById(R.id.newMessageButton);
		newMessageButton.setOnClickListener(this);
		
		refreshMsgButton = (Button) findViewById(R.id.refreshMsgButton);
		refreshMsgButton.setOnClickListener(this);

		Intent intent = getIntent();
		familyId = intent.getStringExtra("familyId");
		userId = intent.getStringExtra("userId");
		firstName = intent.getStringExtra("firstName");

		new getMessages().execute();

	}
	
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.newMessageButton) {
			
			if (getMessagesTask == false) {
				// Check to make sure that the message isn't empty then input
				// into database
				if ((!newMessageET.getText().toString().isEmpty())) {
					new insertMessage().execute();
				}
			}

			if (insertMessageTask == false) {
				messageIds.clear();
				timestamps.clear();
				messages.clear();
				names.clear();
				pictures.clear();
				ids.clear();
				messagesLL.removeAllViews();
				new getMessages().execute();
			}
			
			newMessageET.setText("");

		}
		
		else if (v.getId() == R.id.refreshMsgButton) {
			
			messageIds.clear();
			timestamps.clear();
			messages.clear();
			names.clear();
			messagesLL.removeAllViews();
			new getMessages().execute();

		}

	}

	// Async task to get messages from the database
	class getMessages extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			getMessagesTask = true;
			Connection con = null;
			PreparedStatement prest = null;
			ResultSet rs = null;

			// Connect to family db, get members from the members table
			try {
				System.out.println("Establishing connection");
				con = (Connection) DriverManager.getConnection(URL + familyId,
						"root", "root");

				System.out.println("Querying message ids");
				String idQuery = "SELECT id from messages";
				prest = (PreparedStatement) con.prepareStatement(idQuery);
				rs = (ResultSet) prest.executeQuery();

				while (rs.next()) {
					String id = rs.getString("id");
					messageIds.add(id);
				}
				prest.close();
				rs.close();
				System.out.println(messageIds);

				System.out.println("Querying timestamps");
				String timestampQuery = "SELECT timestamp from messages";
				prest = (PreparedStatement) con
						.prepareStatement(timestampQuery);
				rs = (ResultSet) prest.executeQuery();

				while (rs.next()) {
					Timestamp timestamp = rs.getTimestamp("timestamp");
					timestamps.add(timestamp);
				}
				prest.close();
				rs.close();
				System.out.println(timestamps);

				System.out.println("Querying content");
				String messageQuery = "SELECT content from messages";
				prest = (PreparedStatement) con.prepareStatement(messageQuery);
				rs = (ResultSet) prest.executeQuery();

				while (rs.next()) {
					String message = rs.getString("content");
					messages.add(message);
				}
				prest.close();
				rs.close();
				System.out.println(messages);

				System.out.println("Querying names");
				String nameQuery = "SELECT name from messages";
				prest = (PreparedStatement) con.prepareStatement(nameQuery);
				rs = (ResultSet) prest.executeQuery();

				while (rs.next()) {
					String name = rs.getString("name");
					names.add(name);
				}
				prest.close();
				rs.close();
				System.out.println(names);

				con.close();
				System.out.println("Closing connection");

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			getMessagesTask = false;
			//new getPictures().execute();

			System.out.println("Ids size: " + messageIds.size());
			System.out.println("Timestamps size: " + timestamps.size());
			System.out.println("Messages size: " + messages.size());
			System.out.println("Names size: " + names.size());
			
			System.out.println(Dashboard.membersNamesArray);
			
			for (int i = 0; i < names.size(); i++) {
				System.out.println(names.get(i));
				for (int j=0; j < Dashboard.membersNamesArray.size(); j++){
					System.out.println(Dashboard.membersNamesArray.get(j));
					if (names.get(i).equals(Dashboard.membersNamesArray.get(j))) {
						pictures.add(Dashboard.picturesArray.get(j));
						ids.add(Dashboard.membersArray.get(j));
					}
				}
			}
			
			System.out.println(pictures);
			System.out.println(getPicturesTask);

			// Add members names to text field
			for (int i = 0; i < messages.size(); i++) {
				
				// Create linear layout for each message
				LinearLayout ll = new LinearLayout(getApplicationContext());
				ll.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				ll.setPadding(8, 8, 8, 8);

				// Create text view for each message
				TextView messageTV = new TextView(getApplicationContext());
				messageTV.setPadding(20, 10, 10, 10);
				
				System.out.println("In getMessages - getPicturesTask: "
						+ getPicturesTask);
				System.out.println("Now adding to view");
				
				// Get pictures for users
				ImageView img = new ImageView(getApplicationContext());
				DownloadImageTask downloadImage = new DownloadImageTask(img);
				downloadImage.execute(pictures.get(i));
				
				String sourceString = "<b>" + names.get(i) + "</b> "
						+ " <small>[" + "<i>" + timestamps.get(i)
						+ "]</i></small><br/>"
						+ messages.get(i);
				messageTV.setText(Html.fromHtml(sourceString));
				messageTV.setId(i);
				messageTV.setTextColor(Color.BLACK);
				
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						100, 100);
				img.setLayoutParams(layoutParams);
				ll.addView(img);
				
			if (ids.get(i).equals(userId)) {
				ll.setBackgroundColor(getResources().getColor(R.color.color_lightgray));
				messageTV.setTextColor(Color.DKGRAY);
			}
				ll.addView(messageTV);
				messagesLL.addView(ll);
			
			messagesSV.post(new Runnable() {            
			    @Override
			    public void run() {
			    	messagesSV.fullScroll(View.FOCUS_DOWN);              
			    }
			});
			}

		}

		@Override
		protected void onCancelled() {
			getMessagesTask = false;
		}

	}

	// Async task to insert new message into database
	class insertMessage extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			insertMessageTask = true;

			content = newMessageET.getText().toString();

			UUID messageID = UUID.randomUUID();
			id = String.valueOf(messageID);

			Connection con = null;
			PreparedStatement prest = null;

			// Connect to family database and insert values into messages table
			try {
				con = (Connection) DriverManager.getConnection(URL + familyId,
						"root", "root");
				String sql = "INSERT into messages (id, timestamp, content, name) Values (?,?,?,?)";
				prest = (PreparedStatement) con.prepareStatement(sql);

				// set value of strings to database
				prest.setString(1, id);
				prest.setTimestamp(2, getCurrentTimeStamp());
				prest.setString(3, content);
				prest.setString(4, firstName);

				prest.executeUpdate();
				con.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			insertMessageTask = false;
			
		}

		@Override
		protected void onCancelled() {
			insertMessageTask = false;
		}

	}

	private static java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	}

	// Asynctask to get pictures from the accounts database
	// On post of this thread displays messages and photos to the layout since its the last thread that runs
	class getPictures extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			getPicturesTask = true;
			Connection con = null;
			PreparedStatement prest = null;
			ResultSet rs = null;

			// Connect to accounts db, get members names and add to array list
			try {
				con = (Connection) DriverManager.getConnection(
						URL + "accounts", "root", "root");
				System.out.println(names);
				
				for (int i = 0; i < names.size(); i++) {
					String query = "SELECT * from accounts WHERE firstName='"
							+ names.get(i) + "'";
					prest = (PreparedStatement) con.prepareStatement(query);
					rs = (ResultSet) prest.executeQuery();
					
					while (rs.next()) {
						String id = rs.getString("id");
						ids.add(id);
						String picture = rs.getString("picture");
						pictures.add(picture);
					}
				}

				con.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			getPicturesTask = false;
			System.out.println(pictures);
			System.out.println(getPicturesTask);

			// Add members names to text field
			for (int i = 0; i < messages.size(); i++) {
				
				// Create linear layout for each message
				LinearLayout ll = new LinearLayout(getApplicationContext());
				ll.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				ll.setPadding(8, 8, 8, 8);

				// Create text view for each message
				TextView messageTV = new TextView(getApplicationContext());
				messageTV.setPadding(20, 10, 10, 10);
				
				System.out.println("In getMessages - getPicturesTask: "
						+ getPicturesTask);
				System.out.println("Now adding to view");
				
				// Get pictures for users
				ImageView img = new ImageView(getApplicationContext());
				DownloadImageTask downloadImage = new DownloadImageTask(img);
				downloadImage.execute(pictures.get(i));
				
					String sourceString = "<b>" + names.get(i) + "</b> "
							+ " <small>[" + "<i>" + timestamps.get(i)
							+ "]</i></small><br/>"
							+ messages.get(i);
					messageTV.setText(Html.fromHtml(sourceString));
					messageTV.setId(i);
					messageTV.setTextColor(Color.BLACK);
					
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							100, 100);
					img.setLayoutParams(layoutParams);
					ll.addView(img);
					
				if (ids.get(i).equals(userId)) {
					ll.setBackgroundColor(getResources().getColor(R.color.color_lightgray));
					messageTV.setTextColor(Color.DKGRAY);
				}
					ll.addView(messageTV);
					messagesLL.addView(ll);
				
				messagesSV.post(new Runnable() {            
				    @Override
				    public void run() {
				    	messagesSV.fullScroll(View.FOCUS_DOWN);              
				    }
				});
			}
		}

		@Override
		protected void onCancelled() {
			getPicturesTask = false;
		}
	}

	// Class to download the image from the internet
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
			System.out.println("Exiting downloadImageTask");
		}
	}

}
