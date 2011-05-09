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
	
	public MapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MapView(Context context) {
		super(context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO figure out a way to remove this dependency on client code
		// Called after the touch handler
		// Not strictly needed, but...
		float posX = event.getX()/(float)(getWidth());
		float posY = event.getY()/(float)(getHeight());
		this.setmPos(posX, posY);
		
		return super.onTouchEvent(event); 
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.rgb(180, 180, 0));
		paint.setStyle(Style.FILL_AND_STROKE);
		
		canvas.drawCircle(mPosX*getWidth(), mPosY*getHeight(), 3.f, paint);
	}
	
	public void setmPos(float mPosX, float mPosY) {
		this.mPosX = mPosX;
		this.mPosY = mPosY;
		this.invalidate();
	}
	
	public float getmPosY() {
		return mPosY;
	}

	public void setmPosY(float mPosY) {
		this.mPosY = mPosY;
	}

	public float getmPosX() {
		return mPosX;
	}

	public void setmPosX(float mPosX) {
		this.mPosX = mPosX;
		this.invalidate();
	}


}
