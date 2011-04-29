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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launch_activity);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, R.array.activities);
		getListView().setAdapter(adapter);
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent launchIntent;
		
		switch(position) {
		case WIFI_LOCATOR_ACTIVITY:
			launchIntent = new Intent("in.ernet.iitr.divyeuec.WifiLocatorActivity");
			startActivityForResult(launchIntent, 1);
			break;
		case DEAD_RECKONING_ACTIVITY:
			launchIntent = new Intent("in.ernet.iitr.divyeuec.DeadReckoningActivity");
			startActivityForResult(launchIntent, 1);
			break;
		default:
			throw new RuntimeException("Unexpected position: " + position);
		}
	}
	
}
