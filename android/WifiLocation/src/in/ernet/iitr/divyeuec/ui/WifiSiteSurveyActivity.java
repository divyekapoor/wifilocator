package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.ui.views.MapView;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.db.PersistenceFactory;
import in.iitr.ernet.divyeuec.db.SQLiteDB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MotionEvent.PointerCoords;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WifiSiteSurveyActivity extends Activity {
	protected static final int WIFI_RECORD_SAMPLES = 1;
	private static final int MENU_LOCATION_A = Menu.FIRST;
	private static final int MENU_LOCATION_B = Menu.FIRST + 1;
	private static final String TAG = "WifiSiteSurveyActivity";
	
	private MapView mMap;
	private TextView mXPos;
	private TextView mYPos;
	private Button mRecordButton;
	private EditText mSampleName;
	private FileWriter mLocationMarksFile;
	private double mLocationX;
	private double mLocationY;
	private Button mBtnExportDB;
	private Button mBtnImportDB;
	private Button mBtnNextX;
	private Button mBtnNextY;
	private Button mBtnPrevY;
	private Button mBtnPrevX;
	
	private MotionEvent mLastMotionEvent;
	private Button mBtnResetDb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_site_survey_activity);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		mXPos = (TextView) findViewById(R.id.map_position_x);
		mYPos = (TextView) findViewById(R.id.map_position_y);
		mRecordButton = (Button) findViewById(R.id.btn_wifi_record_sample);
		mSampleName = (EditText) findViewById(R.id.wifi_sample_title);
		
		// The button should start off the WifiRecordSamplesActivity
		mRecordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent launchIntent = new Intent(WifiSiteSurveyActivity.this, WifiRecordSamplesActivity.class);
				String sampleName = mSampleName.getText().toString();
				launchIntent.putExtra(LocationFingerprint.KEY_SAMPLE_NAME, sampleName);
				launchIntent.putExtra(LocationFingerprint.KEY_SAMPLE_X, mMap.getmPosX());
				launchIntent.putExtra(LocationFingerprint.KEY_SAMPLE_Y, mMap.getmPosY());
				startActivityForResult(launchIntent, WIFI_RECORD_SAMPLES);
			}
		});
		
		
		// The Map should update the sample's location on every touch event.
		// So, use the touch listener to set the location on the map and update the labels.
		mMap = (MapView) findViewById(R.id.map);
		mMap.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mLastMotionEvent = event;
				
				// event.getX() and event.getY() return pixel locations within mMap.getWidth() and mMap.getHeight()
				double locationX = Math.floor(event.getX()/(mMap.getWidth()*mMap.getGridWidth()))*mMap.getGridWidth();
				double locationY = Math.floor(event.getY()/(mMap.getHeight()*mMap.getGridHeight()))*mMap.getGridHeight();
				
				if(locationX == mLocationX && locationY == mLocationY) {
					Toast.makeText(WifiSiteSurveyActivity.this, "You've marked your location correctly!", Toast.LENGTH_SHORT).show();
					try {
						if(mLocationMarksFile != null) {
							mLocationMarksFile.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Couldn't write to Location Marks file!", e);
						throw new RuntimeException(e);
					}
				}
				
				try {
					if(mLocationMarksFile != null) {
						mLocationMarksFile.write(System.currentTimeMillis() + "," + locationX + "," + locationY + "," + mLocationX + "," + mLocationY + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Couldn't write to Location Marks file!", e);
					throw new RuntimeException(e);
				}
				
				mXPos.setText("" + Math.floor(event.getX()/(mMap.getWidth()*mMap.getGridWidth())) + " (" + locationX + ") ");
				mYPos.setText("" + Math.floor(event.getY()/(mMap.getHeight()*mMap.getGridHeight())) + " (" + locationY + ") ");
				
				// Note: these values are actually overridden in the onTouch handler of the class.??
				
				return false;
			}
		});
		
		mBtnExportDB = (Button) findViewById(R.id.btn_export_db);
		mBtnImportDB = (Button) findViewById(R.id.btn_import_db);
		
		mBtnExportDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Date now = new Date(System.currentTimeMillis());
				File sqliteDB = new File(SQLiteDB.getInstance(WifiSiteSurveyActivity.this).getPath());
				File backupDB = new File(
						Environment.getExternalStorageDirectory() + File.separator + "samples", 
						"backupDB." + DateFormat.format("yyyy-MM-dd-kk-mm-ss", now) + "." + sqliteDB.getName());
				try {
					FileUtils.copyFile(sqliteDB, backupDB);
					Toast.makeText(WifiSiteSurveyActivity.this, "File exported successfully!", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Couldn't extract backup of sqliteDB!", e);
					throw new RuntimeException(e);
				}
			}
		});
		
		
		mBtnImportDB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBtnExportDB.performClick(); // First back up the original DB
				if(false) { // Note: Disabled for safety reasons. Accidental overwrite is diastrous!
					// Import path: replaceDB.sqlite
					File replacementDB = new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "replaceDB.sqlite");
					File originalDBPath = new File(SQLiteDB.getInstance(WifiSiteSurveyActivity.this).getPath());
					
					try {
						FileUtils.copyFile(replacementDB, originalDBPath);
						Toast.makeText(WifiSiteSurveyActivity.this, "File imported successfully!", Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "Couldn't replace the SQLite DB with my version!", e);
						throw new RuntimeException(e);
					}
				}
			}
		});
		
		mBtnResetDb = (Button) findViewById(R.id.btn_reset_db);
		mBtnResetDb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(false) { // Note: Disabled for safety reasons. Accidental deletion is disastrous!
					PersistenceFactory.getInstance(WifiSiteSurveyActivity.this).resetDB();
				}
			}
		});
		
		// TODO: Apparently there are issues with the MotionEvent code that prevent this 
		// from working right now. Leave it as it is.
		mBtnNextX = (Button) findViewById(R.id.btn_next_x);
		mBtnNextY = (Button) findViewById(R.id.btn_next_y);
		mBtnPrevX = (Button) findViewById(R.id.btn_prev_x);
		mBtnPrevY = (Button) findViewById(R.id.btn_prev_y);
		
		mBtnNextX.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MotionEvent newTouchPoint = MotionEvent.obtain(mLastMotionEvent);
				newTouchPoint.offsetLocation(1.f, 0.f);
				mMap.dispatchTouchEvent(newTouchPoint);
			}
		});
		
		mBtnNextY.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MotionEvent newTouchPoint = MotionEvent.obtain(mLastMotionEvent);
				newTouchPoint.offsetLocation(0.f, 1.f);
				mMap.dispatchTouchEvent(newTouchPoint);
			}
		});
		
		mBtnPrevX.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MotionEvent newTouchPoint = MotionEvent.obtain(mLastMotionEvent);
				newTouchPoint.offsetLocation(-1.f, 0.f);
				mMap.dispatchTouchEvent(newTouchPoint);
			}
		});
		
		mBtnPrevY.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MotionEvent newTouchPoint = MotionEvent.obtain(mLastMotionEvent);
				newTouchPoint.offsetLocation(0.f, -1.f);
				mMap.dispatchTouchEvent(newTouchPoint);
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_LOCATION_A, 0, "Location S-152 (A)");
		menu.add(0, MENU_LOCATION_B, 0, "Location S-159 (B)");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Date now = new Date(System.currentTimeMillis());
		if(mLocationMarksFile != null) {
			try {
				mLocationMarksFile.flush();
				mLocationMarksFile.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Location marks file couldn't be closed!", e);
				throw new RuntimeException(e);
			}
		}
		
		try {
			mLocationMarksFile = new FileWriter(new File(Environment.getExternalStorageDirectory() + File.separator + "samples", "locationMarks." + DateFormat.format("yyyy-MM-dd-kk-mm-ss", now) + ".csv"));
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Location marks file couldn't be opened!", e);
			throw new RuntimeException(e);
		}
		
		switch(item.getItemId()) {
		case MENU_LOCATION_A:
			mLocationX = 0.218855218846875;
			mLocationY = 0.15712682378749998;
			break;
		case MENU_LOCATION_B:
			mLocationX = 0.690235690209375;
			mLocationY = 0.15712682378749998;
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
