package cn.liusiqian.beziercurve;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liusiqian on 2018/9/12.
 */
public class BezierView extends View {
    private BezierCurve curve;
    private BezierCurve.BezierPoint[] points;
    private BezierCurve.BezierPoint prePoint, curPoint;
    private Paint paint;
    private int width, height;

    public BezierView(Context context) {
        super(context);
    }

    public BezierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        curve = new BezierCurve();
        points = new BezierCurve.BezierPoint[2];
        points[0] = new BezierCurve.BezierPoint(0.73, 1);
        points[1] = new BezierCurve.BezierPoint(0.97, 0);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xff0000ff);
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        prePoint = null;
        for (int i = 0; i < 3000; i++) {
            float t = (float) (i * 1.0 / 3000);
            curPoint = curve.limitedBezier(t, points);
            canvas.drawCircle( 100 + (width - 200) * (float)curPoint.x, 100 + (height - 200) * (float)( 1 - curPoint.y ), 3, paint);
            if (prePoint != null) {
                canvas.drawLine((float) prePoint.x, (float)prePoint.y, (float)curPoint.x, (float)curPoint.y, paint);
            }

            prePoint = curPoint;
        }
    }
}
