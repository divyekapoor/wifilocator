package in.ernet.iitr.divyeuec.ui;


import in.ernet.iitr.divyeuec.R;
import in.ernet.iitr.divyeuec.ui.views.SampleMapView;
import in.iitr.ernet.divyeuec.db.LocationFingerprint;
import in.iitr.ernet.divyeuec.db.PersistenceFactory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class WifiSamplesShowActivity extends Activity {
	private SampleMapView mSampleMap;
	private TextView mNumSamples;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_samples_show_activity);

		mNumSamples = (TextView) findViewById(R.id.num_samples);
		mSampleMap = (SampleMapView) findViewById(R.id.sample_map_view);
		List<LocationFingerprint> allSamples = PersistenceFactory.getInstance(this).getAllSamples();
		ArrayList<float[]> sampleLocations = new ArrayList<float[]>();
		for(LocationFingerprint fingerprint : allSamples) {
			sampleLocations.add(new float[] { fingerprint.getmX(), fingerprint.getmY(), fingerprint.getmAngle()});
		}
		mNumSamples.setText("" + sampleLocations.size());
		mSampleMap.setSamples(sampleLocations);
		setResult(RESULT_OK);
	}
}
