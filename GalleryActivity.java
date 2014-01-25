package com.example.fam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.net.ftp.FTPFile;

import com.example.fam.MapActivity.MyInfoWindowAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GalleryActivity extends Activity implements OnClickListener {
	
	//Stores all the urls from the database
	public static ArrayList<String> imageUrls;
	//Stores names of people that post the images
	ArrayList<String> names;
	
	private Uri fileUri;
	Button captureButton, pickButton, refreshButton;
	ScrollView sv;
	LinearLayout ll;
	
	File destination;
	
	GridView gridView;
	private ImageAdapter myImageAdapter;
	
	//Request codes
	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int RESULT_LOAD_IMAGE = 0;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;

	//Provide FTP address, username and password here
	private static final String ftpAddress = "";
	private static final String username = "";
	private static final String password = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_activity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
//		gridView = (GridView) findViewById(R.id.gridview);
		
		imageUrls = new ArrayList<String>();
		names = new ArrayList<String>();
		
		AsyncTask<FTPFile, Object, FTPFile[]> files = new FTPDownload(getApplicationContext(), ftpAddress, username, password, "", Dashboard.familyId).execute();
		try {
			for (int i = 0; i < files.get().length; i ++) {
				if (files.get()[i].getName().indexOf("$") != -1) {
					imageUrls.add(""+Dashboard.familyId+"/"+files.get()[i].getName().toString());
				}
			}
//			Log.v("familyID", Dashboard.familyId);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		names = new ArrayList<String>();
		
		gridView = (GridView) findViewById(R.id.grid_view);
		
		captureButton = (Button) findViewById(R.id.captureBtn);
		pickButton = (Button) findViewById(R.id.pickGalleryBtn);
		refreshButton = (Button) findViewById(R.id.refreshBtn);
		
		captureButton.setOnClickListener(this);
		pickButton.setOnClickListener(this);
		refreshButton.setOnClickListener(this);
		
		try {
			myImageAdapter = new ImageAdapter(this, imageUrls);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gridView.setAdapter(myImageAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
 
                // Sending image id to FullScreenActivity
                Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
                // passing array index
                i.putExtra("id", position);
                startActivity(i);
            }
		});
	}
	
	
	@Override
	public void onClick(View v) {
		//Check if the pick button is clicked or capture button is clicked
				if (v.getId() == pickButton.getId()) {
					Toast.makeText(getApplicationContext(), "Pick is clicked",
							Toast.LENGTH_SHORT).show();
					Intent i = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

					startActivityForResult(i, RESULT_LOAD_IMAGE);
					
				} else if (v.getId() == captureButton.getId()) {
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File imagesFolder = new File(Environment.getExternalStorageDirectory(), "DCIM");
					imagesFolder.mkdir();
					String name = dateToString(new Date(), "yyMMddHHmmssZ");
					destination = new File(imagesFolder, name+"$"+Dashboard.firstName+".jpg");
					fileUri = Uri.fromFile(destination);
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
					startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					Toast.makeText(getApplicationContext(), "Capture is clicked",
							Toast.LENGTH_SHORT).show();
				} else if (v.getId() == refreshButton.getId()) {
					imageUrls.clear();
					AsyncTask<FTPFile, Object, FTPFile[]> files = new FTPDownload(getApplicationContext(), ftpAddress, username, password, "/home/bheng/pub_html/fam/", Dashboard.familyId).execute();
					try {
						for (int i = 0; i < files.get().length; i ++) {
							if (files.get()[i].getName().indexOf("$") != -1) {
								imageUrls.add("http://www.sfu.ca/~bheng/fam/"+Dashboard.familyId+"/"+files.get()[i].getName().toString());
							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						myImageAdapter = new ImageAdapter(this, imageUrls);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					gridView.setAdapter(myImageAdapter);
				}
		
	}
	
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
	
	//Once an image is picked from the gallery, it will be displayed at the bottom of the activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			
			TextView textview = new TextView(this);
			textview.setText("You just posted: ");
			ImageView imageView = new ImageView(this);
			imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
			new FTPUpload(getApplicationContext(), ftpAddress, username, password, picturePath, Dashboard.familyId).execute();
			// String picturePath contains the path of selected Image
		}
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
	        if (resultCode == RESULT_OK) {
	        	try {
	                FileInputStream in = new FileInputStream(destination);
	                BitmapFactory.Options options = new BitmapFactory.Options();
	                options.inSampleSize = 10;
	                String imagePath = destination.getAbsolutePath();
	                Log.v("Camera", imagePath);
	                new FTPUpload(getApplicationContext(), ftpAddress, username, password, imagePath, Dashboard.familyId).execute();
	                
	                TextView textview = new TextView(this);
	                textview.setText("You just posted: ");
	                ImageView imageView = new ImageView(this);
	                imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
	            } catch (FileNotFoundException e) {
	                e.printStackTrace();
	            }

	        } else if (resultCode == RESULT_CANCELED) {
	            // User cancelled the image capture
	        } else {
	            // Image capture failed, advise user
	        }
	    }

	}
		
	public String dateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }
	
	public String getPoster(String fileName) {
		String poster = null;
		int firstIndex = fileName.indexOf("$");
		int lastIndex = fileName.indexOf(".jpg");
		poster = fileName.substring(firstIndex+1, lastIndex);
		return poster;
	}

}
