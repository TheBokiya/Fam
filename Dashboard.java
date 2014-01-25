package com.example.fam;

import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;

public class Dashboard extends Activity implements OnClickListener, OnLongClickListener {
	
	String URL = "jdbc:mysql://";
	
	private static final int CHECK_IN_CODE = 1000;
	
	public static ArrayList<String> membersArray = new ArrayList<String>();
	ArrayList<String> newMembersArray = new ArrayList<String>();
	ArrayList<String> newMembersNamesArray = new ArrayList<String>();
	ArrayList<String> newPicturesArray = new ArrayList<String>();
	ArrayList<String> newNumbersArray = new ArrayList<String>();
	public static ArrayList<String> membersNamesArray = new ArrayList<String>();
	public static ArrayList<String> numbersArray = new ArrayList<String>();
	public static ArrayList<String> picturesArray = new ArrayList<String>();
	ArrayList<ImageView> membersButtons = new ArrayList<ImageView>();
	
	Button[] callMemberButtons;
	
	Button statusBoard, gallery, calendar, map, checkIn, lists;

	public static String firstName, familyId, familyName, userId, userPicture;

	TextView userInfo, memberInfo;
	Handler mHandler;
	
	ImageView profilePic, mood;
	TextView userName;
	LinearLayout mainLL, ll, ll2, ll3, ll4, ll5, ll6, ll7;

	LocationManager locManager;
	String provider;
	Location location;
	Geocoder geocoder;

	double lat, lon;

	TextView currentLocationTV;
	Button checkInButton;
	ScrollView sv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sv = new ScrollView(this);
		mainLL = new LinearLayout(this);
		mainLL.setOrientation(LinearLayout.VERTICAL);
		sv.addView(mainLL);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Intent intent = getIntent();
		familyName = intent.getStringExtra("familyName");
		familyId = intent.getStringExtra("familyId");
		userId = intent.getStringExtra("userId");
		firstName = intent.getStringExtra("firstName");
		
		userInfo = new TextView(this);
		userInfo.setText(familyName + " Family");
		
		userName = new TextView(this);
		userName.setText("First Name: " + firstName);
		userName.setTextSize(16);
	
		setContentView(sv);
		
		new getFamilyInfo().execute();  
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	class getFamilyInfo extends AsyncTask<String, String, String> implements OnClickListener, OnLongClickListener{

		@Override
		protected String doInBackground(String... arg0) {
			membersArray.clear();
			newMembersArray.clear();
        	membersNamesArray.clear();
        	
	    	Connection con = null;
	    	PreparedStatement prest = null;
	    	ResultSet rs = null;
			
			// Connect to accounts db, get members names and add to array list
			try {
				con = (Connection) DriverManager.getConnection (URL + "accounts", "root", "root");
				
				//for (int i=0; i<membersArray.size(); i++) {
					//String query = "SELECT * from accounts WHERE id='" + newMembersArray.get(i) + "'";
					String query = "SELECT * from accounts WHERE familyId='" + familyId + "'";
					prest = (PreparedStatement) con.prepareStatement(query);
					rs = (ResultSet) prest.executeQuery();
					
					while (rs.next()) {
				    	String firstName = rs.getString("firstName");
				    	membersNamesArray.add(firstName);
				    	System.out.println(membersNamesArray);
				    	
				    	String id = rs.getString("id");
				    	membersArray.add(id);
				    	System.out.println(membersArray);
				    	
				    	String picture = rs.getString("picture");
				    	picturesArray.add(picture);
				    	System.out.println(picturesArray);
				    	
				    	String number = rs.getString("number");
				    	numbersArray.add(number);
				    	System.out.println(numbersArray);
					}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// Create new array list without the user in it
			for (int i=0; i<membersArray.size(); i++) {
				if(!membersArray.get(i).equals(userId)) {
					newMembersArray.add(membersArray.get(i));
					newMembersNamesArray.add(membersNamesArray.get(i));
					newPicturesArray.add(picturesArray.get(i));
					newNumbersArray.add(numbersArray.get(i));
				}
				if (membersArray.get(i).equals(userId)) {
					userPicture = picturesArray.get(i);
				}
			}
			
			System.out.println(newMembersArray.size());
			System.out.println(newMembersNamesArray.size());
			System.out.println(newPicturesArray.size());
			System.out.println(newNumbersArray.size());
			
			return null; 
		}
		
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
    		Boolean rowFilled = false;
    		int numPics = 0;
    		
    		ll = new LinearLayout(getApplicationContext());
    		ll.setOrientation(LinearLayout.HORIZONTAL);
    		mainLL.addView(ll);
    		
    		for (int i = 0; i < newMembersNamesArray.size(); i++) {
    			
				// Get pictures for users
				ImageView img = new ImageView(getApplicationContext());
				DownloadImageTask downloadImage = new DownloadImageTask(img);
				downloadImage.execute(newPicturesArray.get(i));
			
				img.setId(i);
				img.setOnClickListener(this);
				img.setOnLongClickListener(this);
				membersButtons.add(img);
				
				TextView tv = new TextView(getApplicationContext());
				tv.setText(newMembersNamesArray.get(i));
				tv.setTextSize(17);
				tv.setTextColor(Color.WHITE);
				tv.setPadding(10,0,0,10);
				tv.setGravity(Gravity.CENTER);
				
				RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(
					    RelativeLayout.LayoutParams.WRAP_CONTENT, 
					    RelativeLayout.LayoutParams.WRAP_CONTENT);
				
				lay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				RelativeLayout rl = new RelativeLayout(getApplicationContext());
				rl.addView(img);
				rl.addView(tv, lay);
				
				//Create new row if there are already two images in the linear layout
				if (numPics==0) {
					ll.addView(rl);
				}
				else if (numPics==1) {
					ll.addView(rl);
				}
				else if (numPics==2) {
		    		ll2 = new LinearLayout(getApplicationContext());
		    		ll2.setOrientation(LinearLayout.HORIZONTAL);
		    		mainLL.addView(ll2);
		    		ll2.addView(rl);
				} 
				else if (numPics==3) {
					ll2.addView(rl);
				}
				else if (numPics==4) {
		    		ll3 = new LinearLayout(getApplicationContext());
		    		ll3.setOrientation(LinearLayout.HORIZONTAL);
		    		mainLL.addView(ll3);
		    		ll3.addView(rl);
				}
				else if (numPics==5) {
					ll3.addView(rl);
				}
				
				numPics++;
				System.out.println(numPics);
    		}
        	
		}

		@Override
		public boolean onLongClick(View v) {
			for (int i = 0; i < membersButtons.size(); i++) {
				if (v.getId() == (membersButtons.get(i)).getId()) {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent
							.setData(Uri.parse("tel:" + numbersArray.get(i)));
					startActivity(callIntent);
				}
			}
			return false;
		}

		@Override
		public void onClick(View v) {
			for (int i = 0; i < membersButtons.size(); i++) {
				if (v.getId() == (membersButtons.get(i)).getId()) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
							+ numbersArray.get(i)));
					startActivity(intent);
				}
			}
		}
	}
	
	class checkInLocation extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			
			return null;
		}
		
	}
	
	// class to download the image
		private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
			ImageView bmImage;

			public DownloadImageTask(ImageView bmImage) {
				this.bmImage = bmImage;
			}

			protected Bitmap doInBackground(String... urls) {
				String urldisplay = urls[0];
				Bitmap mIcon11 = null;
				Bitmap bitmap = null;
				try {
					InputStream in = new java.net.URL(urldisplay).openStream();
					mIcon11 = BitmapFactory.decodeStream(in);
					
					float screenWidth = getResources().getDisplayMetrics().widthPixels;
					int iw=mIcon11.getWidth();
					int ih=mIcon11.getHeight();
					float scalefactor = screenWidth/iw;
					bitmap = android.graphics.Bitmap.createScaledBitmap(mIcon11, (int)((iw*scalefactor)/2), (int)((ih*scalefactor)/2), true);
					
					
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
					e.printStackTrace();
				}
				return bitmap;
			}

			protected void onPostExecute(Bitmap result) {
				bmImage.setImageBitmap(result);
			}
		}
		
		@Override
		public boolean onLongClick(View v) {
			for (int i = 0; i < callMemberButtons.length; i++) {
				if (v.getId() == callMemberButtons[i].getId()) {
					Intent callIntent = new Intent(Intent.ACTION_CALL);
					callIntent
							.setData(Uri.parse("tel:" + numbersArray.get(i)));
					startActivity(callIntent);
				}
			}
			return false;
		}
		
		@Override
		public void onClick(View v) {
			
			if (v.getId() == calendar.getId()) {
				Intent calendarIntent = new Intent(Dashboard.this, CalendarActivity.class);
				calendarIntent.putExtra("familyId", familyId);
				calendarIntent.putExtra("userId", userId);
				startActivity(calendarIntent);
			}
		}


}
