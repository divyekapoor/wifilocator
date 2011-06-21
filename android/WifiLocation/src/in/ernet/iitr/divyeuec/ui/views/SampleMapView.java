package in.ernet.iitr.divyeuec.ui.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SampleMapView extends ImageView {

	private static final double EPS = 4; // 4 px size of point
	private List<float[]> samples = new ArrayList<float[]>();
	
	public SampleMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SampleMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SampleMapView(Context context) {
		super(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.rgb(0, 180, 180));
		paint.setStrokeWidth(1.f);
		for(float[] sample : getSamples()) {
			float x = getWidth()*sample[0];
			float y = getHeight()*sample[1];
			double radAngle = sample[2]*2*Math.PI/360;
			canvas.drawLine(Math.round(x), Math.round(y), Math.round(x + EPS*Math.sin(radAngle)), Math.round(y - EPS*Math.cos(radAngle)), paint);
			
		}
	}

	public void setSamples(List<float[]> samples) {
		this.samples = samples;
	}

	public List<float[]> getSamples() {
		return samples;
	}
	
	

}
