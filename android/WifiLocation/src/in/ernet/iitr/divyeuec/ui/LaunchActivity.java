package in.ernet.iitr.divyeuec.ui;

import in.ernet.iitr.divyeuec.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LaunchActivity extends ListActivity {
	
	private static final int WIFI_LOCATOR_ACTIVITY = 0;
	private static final int DEAD_RECKONING_ACTIVITY = 1;
	private static final int WIFI_SITE_SURVEY_ACTIVITY = 2;
	private static final int WIFI_SAMPLES_SHOW_ACTIVITY = 3;
	private static final String TAG = "LaunchActivity";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launch_activity);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.activities));
		getListView().setAdapter(adapter);
						
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent launchIntent;
		
		switch(position) {
		case WIFI_LOCATOR_ACTIVITY:
			launchIntent = new Intent(this, WifiLocatorActivity.class);
			startActivityForResult(launchIntent, WIFI_LOCATOR_ACTIVITY);
			break;
		case DEAD_RECKONING_ACTIVITY:
			launchIntent = new Intent(this, DeadReckoningActivity.class);
			startActivityForResult(launchIntent, DEAD_RECKONING_ACTIVITY);
			break;
		case WIFI_SITE_SURVEY_ACTIVITY:
			launchIntent = new Intent(this, WifiSiteSurveyActivity.class);
			startActivityForResult(launchIntent, WIFI_SITE_SURVEY_ACTIVITY);
			break;
		case WIFI_SAMPLES_SHOW_ACTIVITY:
			launchIntent = new Intent(this, WifiSamplesShowActivity.class);
			startActivityForResult(launchIntent, WIFI_SAMPLES_SHOW_ACTIVITY);
			break;
		default:
			throw new RuntimeException("Unexpected position: " + position);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO: Do we need to check the result? 
		// We will assume all results are RESULT_OK
	}
	
}
