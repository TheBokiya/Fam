package com.example.fam;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ListActivity extends Activity implements OnClickListener {
	
	String URL = "jdbc:mysql://";
	
	ArrayList<String> list = new ArrayList<String>();
	ArrayList<String> name = new ArrayList<String>();
	
	ArrayList<String> finalList = new ArrayList<String>();
	ArrayList<String> finalName = new ArrayList<String>();
	
	ArrayList<Button> buttonArray = new ArrayList<Button>();

	ScrollView sv;
	LinearLayout ll;

	Button addList;
	Button[] buttonList;
	
	String familyId, userId, firstName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		familyId = intent.getStringExtra("familyId");
		userId = intent.getStringExtra("userId");
		firstName = intent.getStringExtra("firstName");
		
		System.out.println("familyId: " + familyId);

		sv = new ScrollView(this);
		ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		
		sv.addView(ll);
		
		new getLists().execute();
		
		addList = new Button(this);
		addList.setText("Create a new list");
		addList.setId(100);
		addList.setOnClickListener(this);
		addList.setBackgroundResource(R.drawable.orange_button);
		addList.setTextColor(getResources().getColor(R.color.color_white));
		ll.addView(addList);

		setContentView(sv);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == addList.getId()) {
			System.out.println("Add new list is clicked");
			Intent listCreationIntent = new Intent(ListActivity.this, ListCreationActivity.class);
			listCreationIntent.putExtra("familyId", familyId);
			listCreationIntent.putExtra("userId", userId);
			listCreationIntent.putExtra("firstName", firstName);
			startActivity(listCreationIntent);
		} else {
			System.out.println(buttonArray.size());
			for (int i = 0; i < buttonArray.size(); i++) {
				if (v.getId() == (buttonArray.get(i)).getId()) {
					Intent listDetailIntent = new Intent(ListActivity.this, ListDetailActivity.class);
					listDetailIntent.putExtra("familyId", familyId);
					listDetailIntent.putExtra("userId", userId);
					listDetailIntent.putExtra("firstName", firstName);
					listDetailIntent.putExtra("name", finalName.get(i));
					listDetailIntent.putExtra("title", finalList.get(i));
					setResult(RESULT_OK, listDetailIntent);
					startActivity(listDetailIntent);
					System.out.println(name.get(i) + " is clicked");
				}
			}
		}	
	}
	
	// Async task to get lists from the database
	class getLists extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... arg0) {
			
			Connection con = null;
			PreparedStatement prest = null;
			ResultSet rs = null;

			// Connect to family db, get lists from the lists table
			try {
				System.out.println("Establishing connection");
				con = (Connection) DriverManager.getConnection(URL + familyId,
						"root", "root");

				System.out.println("Querying lists titles");
				String titleQuery = "SELECT title from lists";
				prest = (PreparedStatement) con.prepareStatement(titleQuery);
				rs = (ResultSet) prest.executeQuery();

				while (rs.next()) {
					String title = rs.getString("title");
					list.add(title);
				}
				prest.close();
				rs.close();
				
				System.out.println("Querying names");
				String namesQuery = "SELECT name from lists";
				prest = (PreparedStatement) con.prepareStatement(namesQuery);
				rs = (ResultSet) prest.executeQuery();

				while (rs.next()) {
					String username = rs.getString("name");
					name.add(username);
				}
				prest.close();
				rs.close();
				con.close();
				
				System.out.println(list);
				System.out.println(name);
				System.out.println("Closing connection");

			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			getUniqueLists();
		}
		
	}
	
	// Method to single out unique lists and then display them on the screen
	public void getUniqueLists() {
		
		for (int i=0; i<list.size(); i++) {
			if (!finalList.contains(list.get(i))) {
				System.out.println("New list: " + list.get(i));
				System.out.println("New name: " + name.get(i));
				finalList.add(list.get(i));
				finalName.add(name.get(i));
			}
		}
		
		for (int i = 0; i < finalList.size(); i++) {
			Button button = new Button(this);
			button.setText(finalList.get(i) + " (" + finalName.get(i) + ")");
			button.setId(i);
			button.setOnClickListener(this);
			button.setBackgroundResource(R.drawable.white_button);
			
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	        layoutParams.setMargins(0, 10, 0, 0);
			
			buttonArray.add(button);
			ll.addView(button, layoutParams);
		}
	}
}
