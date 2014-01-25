package com.example.fam;

import java.sql.DriverManager;
import java.sql.SQLException;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class TabLayoutActivity extends TabActivity implements LocationListener {

	String URL = "jdbc:mysql://";
	
	private static final int RESULT_SETTINGS = 1;
	String familyId, userId, firstName, familyName, storedPass;
	TabHost tabHost;
	
	LocationManager locManager;
	double lat, lon;
	Boolean insertLocationTask = false;
	String provider;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_layout);
		showUserSettings();
		
		// Get Intent extras
		Intent intent = getIntent();
		familyName = intent.getStringExtra("familyName");
		familyId = intent.getStringExtra("familyId");
		userId = intent.getStringExtra("userId");
		firstName = intent.getStringExtra("firstName");

		// Instantiate TabHost object to manage the tabs
		tabHost = getTabHost();

		// Create a tab for dashboard
		TabSpec dashboard = tabHost.newTabSpec("Dashboard");
		dashboard.setIndicator(null, getResources().getDrawable(R.drawable.dashboard));
		Intent dashboardIntent = new Intent(this, Dashboard.class);
		dashboardIntent.putExtra("familyName", familyName);
		dashboardIntent.putExtra("familyId", familyId);
		dashboardIntent.putExtra("userId", userId);
		dashboardIntent.putExtra("firstName", firstName);
		dashboard.setContent(dashboardIntent);

		// Create a tab for message board
		TabSpec messageBoard = tabHost.newTabSpec("Message Board");
		messageBoard.setIndicator(null, getResources().getDrawable(R.drawable.message));
		Intent messageBoardIntent = new Intent(this, MessageBoardActivity.class);
		messageBoardIntent.putExtra("familyId", familyId);
		messageBoardIntent.putExtra("userId", userId);
		messageBoardIntent.putExtra("firstName", firstName);
		messageBoard.setContent(messageBoardIntent);

		// create a tab for gallery
		TabSpec gallery = tabHost.newTabSpec("Gallery");
		gallery.setIndicator(null, getResources().getDrawable(R.drawable.gallery));
		Intent galleryIntent = new Intent(this, GalleryActivity.class);
		gallery.setContent(galleryIntent);
		
		TabSpec list = tabHost.newTabSpec("List");
		list.setIndicator(null, getResources().getDrawable(R.drawable.list));
		Intent listIntent = new Intent(this, ListActivity.class);
		listIntent.putExtra("familyId", familyId);
		listIntent.putExtra("userId", userId);
		listIntent.putExtra("firstName", firstName);
		list.setContent(listIntent);

		// create a tab for map
		TabSpec map = tabHost.newTabSpec("Map");
		map.setIndicator(null, getResources().getDrawable(R.drawable.map));
		Intent mapIntent = new Intent(this, MapActivity.class);
		mapIntent.putExtra("familyId", familyId);
		mapIntent.putExtra("userId", userId);		
		map.setContent(mapIntent);

		// add the tabs
		tabHost.addTab(dashboard);
		tabHost.addTab(messageBoard);
		tabHost.addTab(gallery);
		tabHost.addTab(list);
		tabHost.addTab(map);
		
		locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.NO_REQUIREMENT);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

		provider = locManager.getBestProvider(criteria, true);
		boolean network_enabled = locManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		locManager.requestLocationUpdates(provider, 5000, 0, this);

		// If network is enabled, get current location
		if (network_enabled) {
			Location location = locManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			System.out.println(location);

			lat = location.getLatitude();
			lon = location.getLongitude();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		getMenuInflater().inflate(R.menu.sign_out, menu);
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	// Go to the UserSettingActivity if setting is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

        case R.id.menu_checkin:                
            Toast.makeText(this, "Checking in...", Toast.LENGTH_SHORT).show();
            new insertLocation().execute();
            return true;
		
		case R.id.menu_sign_out:
			System.out.println("Sign out is clicked");
			Toast.makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT).show();
			Intent signOutIntent = new Intent(this, MainActivity.class);
			startActivity(signOutIntent);
			break;

		case R.id.action_profile:
			Intent profileIntent = new Intent(this, ProfileActivity.class);
			startActivity(profileIntent);
			break;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
			break;
		}

	}

	// Get the preferences
	private void showUserSettings() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		StringBuilder builder = new StringBuilder();

		builder.append("\n First Name: "
				+ sharedPrefs.getString("prefFirstName", "NULL"));

		builder.append("\n Last Name: "
				+ sharedPrefs.getString("prefLastName", "NULL"));

		builder.append("\n Automatic Check in:"
				+ sharedPrefs.getBoolean("prefCheckin", false));
	}
	
	// AsyncTask to insert latitude and longitude of location
	class insertLocation extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			insertLocationTask = true;

			String latitude = String.valueOf(lat);
			String longitude = String.valueOf(lon);

			Connection con = null;

			// Connect to accounts database and insert lat and lon into user's
			// account
			try {
				con = (Connection) DriverManager.getConnection(URL + familyId,
						"root", "root");
				Statement latitudeST = (Statement) con.createStatement();
				latitudeST.executeUpdate("UPDATE members SET latitude='"
						+ latitude + "' WHERE id='" + userId + "'");
				Statement longitudeST = (Statement) con.createStatement();
				longitudeST.executeUpdate("UPDATE members SET longitude='"
						+ longitude + "' WHERE id='" + userId + "'");
				Statement timestampST = (Statement) con.createStatement();
				timestampST
						.executeUpdate("UPDATE members SET timestamp=now() WHERE id='"
								+ userId + "'");
				con.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			insertLocationTask = false;
			Toast.makeText(getApplicationContext(), "Checked in successfully!", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onCancelled() {
			insertLocationTask = false;
		}

	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
