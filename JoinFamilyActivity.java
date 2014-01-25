package com.example.fam;

import java.sql.DriverManager;
import java.sql.SQLException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

public class JoinFamilyActivity extends Activity implements OnClickListener{
	
	String URL = "jdbc:mysql://";
	
	TextView joinFamilyTV;
	EditText joinFamilyET;
	String userId, familyId, familyName, email;
	Button search;
	Boolean emailExists = false;
	Boolean searchForFamTask = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.join_family_activity);
		
		joinFamilyTV = (TextView) findViewById(R.id.joinFamilyTV);
		joinFamilyET = (EditText) findViewById(R.id.joinFamilyET);		
		
		Intent intent = getIntent();
		userId = intent.getStringExtra("userId");

		search = (Button) findViewById(R.id.searchButton);
		search.setOnClickListener(this);
		
	    System.out.println("About to try instantiating driver");
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.setTitle("Driver instantiated");
		} catch (InstantiationException e) {
			this.setTitle(".....Class com.mysql.jdbc.Driver not found!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			this.setTitle("Illegal access");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			this.setTitle("instantiation exc eption");
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if ((!joinFamilyET.getText().toString().isEmpty())){
			new SearchForFam().execute();
		}
	}
	
	class SearchForFam extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			searchForFamTask = true;
			email = joinFamilyET.getText().toString();
			
            //Accessing driver from JAR file
    		//use the JDBC driver and establish connection to the accounts database
			Connection con;
	    	
	    	if (emailExists == false) {
				try {		  
					System.out.println("Checking if email exists");
			    	// Check if email exists using the method exists()
					if (exists(email)) {
						emailExists = true;
					} else {
						System.out.println("Email does not exist.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
	    	}
			
			// If email exists, check if password is the same as entered password
			if (emailExists == true) {
				System.out.println("familyId: " + familyId);
				
				// Store familyId and familyName into new account, then store into family db the member id
				try {
					con = (Connection) DriverManager.getConnection (URL + "accounts", "root", "root");
						Statement familyIdST = (Statement) con.createStatement();
						familyIdST.executeUpdate("UPDATE accounts SET familyId='"+ familyId + "' WHERE id='" + userId +"'");
						Statement familyNameST = (Statement) con.createStatement();
						familyNameST.executeUpdate("UPDATE accounts SET familyName='"+ familyName + "' WHERE id='" + userId +"'");
					con.close();
					
					con = (Connection) DriverManager.getConnection (URL + familyId, "root", "root");
						Statement userIdST = (Statement) con.createStatement();
						userIdST.executeUpdate("INSERT INTO members (id) VALUES ('" + userId + "')");
						System.out.println("Inserted into members table!");
					con.close();	
					
					System.out.println("Family name and id added to member account!");
					searchForFamTask = false;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			return null;
		}
		
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
        	searchForFamTask = false;
        	
        	if (emailExists) {
    			Toast.makeText(getApplicationContext(), "Joined Fam! Please sign in to your new account.", Toast.LENGTH_SHORT).show();
    			Intent intent = new Intent(JoinFamilyActivity.this, MainActivity.class);
    			startActivity(intent);
        	} else {
        		Toast.makeText(getApplicationContext(), "Email does not exist in the database.", Toast.LENGTH_SHORT).show();
        	}
		}
	
	}
	
	// Method to check if email exists in the accounts database
    public boolean exists(String email) throws SQLException, ClassNotFoundException {
    	boolean exists = false;
    	String query = "SELECT * from accounts WHERE emailAddress='" + email + "'";
    	Connection con = null;
    	PreparedStatement prest = null;
    	ResultSet rs = null;
    	
    	// If email exists, get familyId from other account
    	try {
    		System.out.println("Inside exists method");
    		con = (Connection) DriverManager.getConnection (URL + "accounts", "root", "root");
    		System.out.println("Connection established");
    		prest = (PreparedStatement) con.prepareStatement(query);
    		rs = (ResultSet) prest.executeQuery();
    		System.out.println("Resultset executed");
    		
		    while (rs.next()) {
		    	System.out.println("Inside the while statement");
		        familyId = rs.getString("familyId");
		        familyName = rs.getString("familyName");
		        System.out.println("familyid: " + familyId);
		        System.out.println("familyName: " + familyName);
		        System.out.println("While statement successful!");
		    	exists = true;
		    }
		    
    	} finally {
    		prest.close();
    		rs.close();
    		con.close();
    	}
    	
    	System.out.println("Result is: " + exists);
    	return exists;
    }


}
