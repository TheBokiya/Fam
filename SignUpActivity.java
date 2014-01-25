package com.example.fam;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends Activity {

	String URL = "jdbc:mysql://"; 
	
	EditText inputFirstName, inputLastName, inputPassword, inputEmail,
			inputRetypePassword, inputNumber;
	String userId, familyId, newFamilyId, number;
	Button submit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.signup_activity);

		inputFirstName = (EditText) findViewById(R.id.firstNameET);
		inputLastName = (EditText) findViewById(R.id.lastNameET);
		inputNumber = (EditText) findViewById(R.id.numberET);
		inputEmail = (EditText) findViewById(R.id.emailET);
		inputPassword = (EditText) findViewById(R.id.passwordET);
		inputRetypePassword = (EditText) findViewById(R.id.retypePasswordET);
		
		//Generate unique user id and family id
		UUID userID = UUID.randomUUID();
		userId = String.valueOf(userID);
		
		UUID familyID = UUID.randomUUID();
		familyId = String.valueOf(familyID);
		newFamilyId = editUUID(familyId);
	    System.out.println(newFamilyId);
		
		submit = (Button) findViewById(R.id.submitButton);
		submit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if ((!inputFirstName.getText().toString().isEmpty())
						&& (!inputLastName.getText().toString().isEmpty())
						&& (!inputEmail.getText().toString().isEmpty())
						&& (!inputNumber.getText().toString().isEmpty())
						&& (!inputPassword.getText().toString().isEmpty())
						&& (!inputRetypePassword.getText().toString().isEmpty())) {
					if (isValidEmail(inputEmail.getText().toString())) {
						if (inputPassword
								.getText()
								.toString()
								.equals(inputRetypePassword.getText()
										.toString())) {
							new CreateNewAccount().execute();
						} else {
							Toast.makeText(
									getApplicationContext(),
									"Retyped Password doesn't match the password",
									Toast.LENGTH_SHORT).show();
						}
					} else
						Toast.makeText(getApplicationContext(),
								"Please enter a valid email",
								Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Please complete the form", Toast.LENGTH_SHORT)
							.show();
				}
			}
			
			
		});
		
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

	}

	class CreateNewAccount extends AsyncTask<String, String, String> {

		String account = null;
		
		protected String doInBackground(String... args) {
			String firstName = inputFirstName.getText().toString();
			String lastName = inputLastName.getText().toString();
			String email = inputEmail.getText().toString();
			String password = inputPassword.getText().toString();
			String picture = "http://www.thealmanac.org/assets/img/default_avatar.png";
			String number = inputNumber.getText().toString();

            //Accessing driver from JAR file
    		//use the JDBC driver and establish connection to the accounts database
			Connection con;
			try {
				publishProgress();
				con = (Connection) DriverManager.getConnection (URL, "root", "root");
				System.out.println("Entering into database");
				//prepare a string for inserting into the accounts table
				String sql = "INSERT into accounts (id, firstName, lastName, emailAddress, password, picture, number, familyId) Values (?,?,?,?,?,?,?,?)";
				PreparedStatement prest = (PreparedStatement) con.prepareStatement(sql);
				
				//set value of strings to database
				prest.setString(1, userId);
				prest.setString(2, firstName);
				prest.setString(3, lastName);
				prest.setString(4, email);
				prest.setString(5, password);
				prest.setString(6, picture);
				prest.setString(7, number);
				prest.setString(8, newFamilyId);
					
				prest.executeUpdate();
				con.close();
				
				account = "CREATED";
				publishProgress(account);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return account;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result.equals("CREATED")) {
				Toast.makeText(getApplicationContext(), "Account created", Toast.LENGTH_SHORT).show();
				Intent familyIntent = new Intent(
						SignUpActivity.this, FamilyActivity.class);
				familyIntent.putExtra("lastName", inputLastName.getText().toString());
				familyIntent.putExtra("email", inputEmail.getText().toString());
				familyIntent.putExtra("userId", userId);
				familyIntent.putExtra("familyId", newFamilyId);
				startActivity(familyIntent);
			}
		}

	}

	public final static boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
					.matches();
		}
	}
	
	public String editUUID(String badId) {
		String editedBadId = badId.replace("-", "");
		String goodId = "a" + editedBadId;
		return goodId;
	}
}