package com.example.fam;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateFamilyActivity extends Activity implements OnClickListener{
	
	String URL = "jdbc:mysql://";
	
	String userId, familyId, familyName, email;
	TextView familyNameTV;
	EditText familyNameET;
	Button create;
	Boolean createFamilyTask = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		 setContentView(R.layout.create_family_activity);
		 
		 familyNameTV = (TextView) findViewById(R.id.firstnameTV);
		 familyNameET = (EditText) findViewById(R.id.familyNameET);
		 
		 Intent intent = getIntent();
		 String lastName = intent.getStringExtra("lastName");
		 userId = intent.getStringExtra("userId");
		 familyId = intent.getStringExtra("familyId");
		 System.out.println(familyId);
		 email = intent.getStringExtra("email");
		 familyNameET.setHint(lastName);
		 
		 create = (Button) findViewById(R.id.createBtn);
		 create.setOnClickListener(this);
		 
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
		if ((!familyNameET.getText().toString().isEmpty())){
			createFamilyTask = true;
			new CreateNewFamily().execute();
		}

	}
	
	class CreateNewFamily extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			familyName = familyNameET.getText().toString();
			
            //Accessing driver from JAR file
    		//use the JDBC driver and establish connection to the accounts database
			Connection con;
			try {
				con = (Connection) DriverManager.getConnection (URL, "root", "root");
					System.out.println("Creating family database");
					Statement st = (Statement) con.createStatement();
					int Result = st.executeUpdate("CREATE DATABASE " + familyId + "");
					st.close();
				con.close();
				
				con = (Connection) DriverManager.getConnection (URL + familyId, "root", "root");
					//Create members table
					Statement membersST = (Statement) con.createStatement();
					String membersTable = "CREATE TABLE members (" +
							"	id				VARCHAR(100)" +
							" , latitude		VARCHAR(100) " +
							" , longitude		VARCHAR(100) " +
							" , timestamp		DATETIME " +
							" , status			VARCHAR(100))" ;
					membersST.executeUpdate(membersTable);
					System.out.println("Members table created successfully!");
					membersST.close();
					
					//Add account to members table
					Statement userIdST = (Statement) con.createStatement();
					userIdST.executeUpdate("INSERT INTO members (id) VALUES ('" + userId + "')");
					System.out.println("Inserted into members table!");
					
					//Create messages table
					Statement messagesST = (Statement) con.createStatement();
					String messagesTable = "CREATE TABLE messages (" +
							"	id				VARCHAR(100)" +
							" , timestamp		DATETIME " +
							" , content			VARCHAR(100) " +
							" , name			VARCHAR(100))" ;
					messagesST.executeUpdate(messagesTable);
					System.out.println("Messages table created successfully!");
					messagesST.close();
					
					//Create gallery table
					Statement galleryST = (Statement) con.createStatement();
					String galleryTable = "CREATE TABLE gallery (" +
							"	id				VARCHAR(100)" +
							" , timestamp		DATETIME " +
							" , uri				VARCHAR(100) " +
							" , name			VARCHAR(100))" ;
					galleryST.executeUpdate(galleryTable);
					System.out.println("Gallery table created successfully!");	
					galleryST.close();
	
					//Create calendar table
					Statement calendarST = (Statement) con.createStatement();
					String calendarTable = "CREATE TABLE calendar (" +
							"	year		VARCHAR(100)" +
							" , month		VARCHAR(100) " +
							" , date		VARCHAR(100) " +
							" , event		VARCHAR(100) " +
							" , name		VARCHAR(100))" ;
					calendarST.executeUpdate(calendarTable);
					System.out.println("Calendar table created successfully!");	
					calendarST.close();
					
					//Create lists table
					Statement listsST = (Statement) con.createStatement();
					String listsTable = "CREATE TABLE lists (" +
							"	id			VARCHAR(100)" +
							",	timestamp	DATETIME " +
							" , title		VARCHAR(100) " +
							" , item		VARCHAR(100) " +
							" , complete	VARCHAR(100) " +
							" , name		VARCHAR(100))" ;
					listsST.executeUpdate(listsTable);
					System.out.println("Lists table created successfully!");	
					listsST.close();
				con.close();
				
				con = (Connection) DriverManager.getConnection (URL + "accounts", "root", "root");
					Statement familyST = (Statement) con.createStatement();
					familyST.executeUpdate("UPDATE accounts SET familyName='"+ familyName + "' WHERE emailAddress='" + email +"'");
					System.out.println(familyName);
					System.out.println("Family name added to familyName in member account!");
				con.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			//This will take the user to the main page once they've created a family
			Intent intent = new Intent(CreateFamilyActivity.this, MainActivity.class);
			Toast.makeText(getApplicationContext(), "New Fam created! Please sign in to your new account.", Toast.LENGTH_SHORT).show();
			startActivity(intent);
		}
	
	}

}
