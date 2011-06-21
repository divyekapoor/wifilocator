package in.ernet.iitr.divyeuec.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MapView extends ImageView {

	private float mPosX = 0.f;
	private float mPosY = 0.f;
	private final Paint mDotPaint;
	private final Paint mBluePaint;
	public static final double PIXELS_PER_METER =  11.3; // 11.01587542; // 10.935387205; // 11.096363636; //10.774410774;
	public static final double GRID_WIDTH = PIXELS_PER_METER/640.;
	public static final double GRID_HEIGHT = PIXELS_PER_METER/480.;
	public static final double OFFSET_X = -0.01;
	public static final double OFFSET_Y = 0.008 -GRID_WIDTH;
	
	
	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBluePaint = new Paint();
		
		initPaint();
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		initPaint();
	}

	public MapView(Context context) {
		super(context);
		mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		initPaint();
	}
	
	private void initPaint() {
		mDotPaint.setColor(Color.rgb(180, 180, 0));
		mDotPaint.setStyle(Style.FILL_AND_STROKE);
		
		mBluePaint.setColor(Color.rgb(0, 120, 200));
		mBluePaint.setStyle(Style.FILL_AND_STROKE);		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO figure out a way to remove this dependency on client code
		// Called after the touch handler
		// Not strictly needed, but...
		float posX = event.getX();
		float posY = event.getY();
		this.setmPos(posX, posY);
		
		return super.onTouchEvent(event); 
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		double x = OFFSET_X, y = 0.;
		while(x < 1.) {
			while(x < 0) {
				x += GRID_WIDTH;
			}
			
			y = OFFSET_Y; 			// y = 0.;
			while(y < 0) {
				y += GRID_HEIGHT;
			}
			while(y < 1.) {
				canvas.drawCircle((float)x*getWidth(), (float)y*getHeight(), 1.f, mBluePaint);
				y += GRID_HEIGHT;
			}
			x += GRID_WIDTH;
		}
		canvas.drawCircle(mPosX*getWidth(), mPosY*getHeight(), 3.f, mDotPaint);
	}
	
	public void setmPos(float mPosX, float mPosY) {
		//Snapping mode - 1m snapping
		this.mPosX = (float)((Math.floor((mPosX)/(this.getWidth()*GRID_WIDTH))*GRID_WIDTH) + OFFSET_X);
		this.mPosY = (float)((Math.floor((mPosY)/(this.getHeight()*GRID_HEIGHT))*GRID_HEIGHT) + OFFSET_Y);
		 
		this.invalidate();
	}
	
	public float getmPosY() {
		return mPosY;
	}

	public void setmPosY(float mPosY) {
		setmPos(this.mPosX, mPosY);
	}

	public float getmPosX() {
		return mPosX;
	}

	public void setmPosX(float mPosX) {
		setmPos(mPosX, this.mPosY);
	}

	public double getGridWidth() {
		return GRID_WIDTH;
	}
	
	public double getGridHeight() {
		return GRID_HEIGHT;
	}


}
