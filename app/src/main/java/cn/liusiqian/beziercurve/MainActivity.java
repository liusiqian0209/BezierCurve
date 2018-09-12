package cn.liusiqian.beziercurve;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BezierCurve curve = new BezierCurve();
        curve.defineLimitedBazier(new BezierCurve.BezierPoint(0.73, 1), new BezierCurve.BezierPoint(0.97, 0));
        double y = curve.calcBezierCurveYByX(0.4);
        Log.i("Bézier Curve", "x = 0.4, y = " + y);
        y = curve.calcBezierCurveYByX(0.8);
        Log.i("Bézier Curve", "x = 0.8, y = " + y);
        y = curve.calcBezierCurveYByX(0.2);
        Log.i("Bézier Curve", "x = 0.2, y = " + y);
    }
}
