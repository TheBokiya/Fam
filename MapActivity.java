package com.example.fam;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fam.TabLayoutActivity.insertLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

public class MapActivity extends FragmentActivity implements OnClickListener,
		LocationListener {

	String URL = "jdbc:mysql://";

	LocationManager locManager;
	String provider, userId, familyId, timestamp;
	Location location;
	Geocoder geocoder;

	double lat;
	double lon;

	TextView memberLocationTV, currentLocationTV;
	Button checkInButton;
	RelativeLayout rl;

	List<Address> addressList = null;
	List<Address> latLonList = null;

	Boolean insertLocationTask = false;
	Boolean getLocationsTask = true;

	ArrayList<String> lats, lons, ids, names, finalLats, finalLons, finalNames;
	ArrayList<Timestamp> timestamps, finalTimestamps;
	String address1, address2, address3;
	String currentAddress1, currentAddress2, currentAddress3;

	GoogleMap mapView;
	GoogleMap googleMap;
	com.google.android.gms.maps.Projection projection;
	SupportMapFragment mFRaFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		lats = new ArrayList<String>();
		lons = new ArrayList<String>();
		ids = new ArrayList<String>();
		timestamps = new ArrayList<Timestamp>();
		names = new ArrayList<String>();

		finalLats = new ArrayList<String>();
		finalLons = new ArrayList<String>();
		finalTimestamps = new ArrayList<Timestamp>();
		finalNames = new ArrayList<String>();

		FragmentTransaction mTransaction = getSupportFragmentManager()
				.beginTransaction();
		mFRaFragment = new MapFragment();
		mTransaction.add(R.id.mapactivity, mFRaFragment);
		mTransaction.commit();

		try {
			MapsInitializer.initialize(this);
		} catch (GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}

		Intent intent = getIntent();
		familyId = intent.getStringExtra("familyId");
		userId = intent.getStringExtra("userId");

		geocoder = new Geocoder(getApplicationContext());
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

			try {
				addressList = geocoder.getFromLocation(lat, lon, 1);
				currentAddress1 = addressList.get(0).getAddressLine(0);
				currentAddress2 = addressList.get(0).getAddressLine(1);
				currentAddress3 = addressList.get(0).getAddressLine(2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("About to try instantiating driver");
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.setTitle("Driver instantiated");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			this.setTitle(".....Class com.mysql.jdbc.Driver not found!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			this.setTitle("Illegal access");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			this.setTitle("instantiation exc eption");
			e.printStackTrace();
		}

		new getLocations().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		locManager.requestLocationUpdates(provider, 400, 1, this);
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    public void run() {
		        new insertLocation().execute();
		        handler.postDelayed(this, 120000); //now is every 2 minutes
		    }
		 }, 60000); //Every 60000 ms (2 minutes)
	}

	// Remove the location listener updates when Activity is paused
	@Override
	protected void onPause() {
		super.onPause();
		locManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location arg0) {
	}

	@Override
	public void onProviderDisabled(String arg0) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String arg0) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAttachedToWindow() {
		// Load the map here such that the fragment has a chance to completely
		// Load or else the GoogleMap value may be null
		googleMap = (mFRaFragment).getMap();
		LatLng latLng = new LatLng(lat, lon);
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
		
		java.util.Date date = new java.util.Date();
		Timestamp time = new Timestamp(date.getTime());

		// Current location marker
		Marker marker = googleMap.addMarker(new MarkerOptions()
				.position(latLng)
				.title("Current Location")
				.snippet(time.toString())
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		marker.showInfoWindow();

		googleMap.getUiSettings().setCompassEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
		super.onAttachedToWindow();
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
			Toast.makeText(getApplicationContext(), "Checked in!", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onCancelled() {
			insertLocationTask = false;
		}

	}

	// AsyncTask to get locations from the database
	class getLocations extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			getLocationsTask = true;

			Connection con = null;
			PreparedStatement prest = null;
			ResultSet rs = null;

			// Connect to accounts database, get members and their locations
			try {
				System.out.println("Establishing connection");
				con = (Connection) DriverManager.getConnection(URL + familyId,
						"root", "root");

				System.out.println("Querying member ids");
				String idQuery = "SELECT id from members";
				prest = (PreparedStatement) con.prepareStatement(idQuery);
				rs = (ResultSet) prest.executeQuery();
				while (rs.next()) {
					String id = rs.getString("id");
					ids.add(id);
				}
				prest.close();
				rs.close();
				System.out.println(ids);

				System.out.println("Querying latitudes");
				String latQuery = "SELECT latitude from members";
				prest = (PreparedStatement) con.prepareStatement(latQuery);
				rs = (ResultSet) prest.executeQuery();
				while (rs.next()) {
					String latitude = rs.getString("latitude");
					lats.add(latitude);
				}
				prest.close();
				rs.close();
				System.out.println(lats);

				System.out.println("Querying longitudes");
				String lonQuery = "SELECT longitude from members";
				prest = (PreparedStatement) con.prepareStatement(lonQuery);
				rs = (ResultSet) prest.executeQuery();
				while (rs.next()) {
					String longitude = rs.getString("longitude");
					lons.add(longitude);
				}
				prest.close();
				rs.close();
				System.out.println(lons);

				System.out.println("Querying timestamps");
				String timestampQuery = "SELECT timestamp from members";
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

				con.close();
				System.out.println("Closing connection to family database");

				names = getNames(ids);

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			getLocationsTask = false;

			// Get locations of family members who have checked in
			for (int i = 0; i < lats.size(); i++) {
				if (lats.get(i) != null) {
					System.out.println(lats.get(i));
					System.out.println(lons.get(i));

					finalLats.add(lats.get(i));
					finalLons.add(lons.get(i));
					finalTimestamps.add(timestamps.get(i));
					finalNames.add(names.get(i));
				}
			}

			// Display markers of family members
			for (int i = 0; i < finalLats.size(); i++) {
				System.out.println("Inserting marker for " + finalNames.get(i));
				LatLng memberLoc = new LatLng(Double.parseDouble(finalLats
						.get(i)), Double.parseDouble(finalLons.get(i)));
				Marker marker = googleMap
						.addMarker(new MarkerOptions()
								.position(memberLoc)
								.title(finalNames.get(i))
								.snippet((finalTimestamps.get(i).toString()))
								.draggable(true)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
				marker.showInfoWindow();
			}
		}

		@Override
		protected void onCancelled() {
			getLocationsTask = false;
		}

	}

	public Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	// ArrayList to convert user IDs to first names
	public ArrayList<String> getNames(ArrayList<String> ids) {

		Connection con = null;
		PreparedStatement prest = null;
		ResultSet rs = null;

		// Connect to accounts database, get members and their locations
		try {
			System.out.println("Establishing connection");
			con = (Connection) DriverManager.getConnection(URL + "accounts",
					"root", "root");
			System.out.println("Querying member names");
			for (int i = 0; i < ids.size(); i++) {
				String nameQuery = "SELECT * from accounts WHERE id='"
						+ ids.get(i) + "'";
				prest = (PreparedStatement) con.prepareStatement(nameQuery);
				rs = (ResultSet) prest.executeQuery();
				while (rs.next()) {
					String firstName = rs.getString("firstName");
					names.add(firstName);
				}
			}

			prest.close();
			rs.close();
			System.out.println("Closing connection to accounts database");
			System.out.println(names);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return names;
	}

	@Override
	public void onClick(View arg0) {
	}

	// Custom info window to display multiline snippets
	class MyInfoWindowAdapter implements InfoWindowAdapter {

		private final View infoWindow;

		MyInfoWindowAdapter() {
			infoWindow = getLayoutInflater().inflate(
					R.layout.maps_infowindow, null);
		}

		@Override
		public View getInfoContents(Marker marker) {

			LatLng latLng = marker.getPosition();
			double lat = latLng.latitude;
			double lon = latLng.longitude;
			
			System.out.println("In info window adapter");
			
			try {
				System.out.println("In try statement");
				
				addressList = geocoder.getFromLocation(lat, lon, 1);
				address1 = addressList.get(0).getAddressLine(0);
				address2 = addressList.get(0).getAddressLine(1);
				address3 = addressList.get(0).getAddressLine(2);
				
				// Set the title to the name
				TextView tvTitle = ((TextView) infoWindow
						.findViewById(R.id.title));
				tvTitle.setText(marker.getTitle());
				
				// Set timestamp
				TextView tvTime = ((TextView) infoWindow
						.findViewById(R.id.checkInTime));
				tvTime.setText(marker.getSnippet());
				
				// Set info to location
				TextView tvSnippet = ((TextView) infoWindow
						.findViewById(R.id.snippet));
				tvSnippet.setText(address1);
				tvSnippet.append("\n" + address2);
				tvSnippet.append("\n" + address3);
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("IOException");
			}
			
			System.out.println("Exiting info window adapter");
			return infoWindow;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}

	}

}
