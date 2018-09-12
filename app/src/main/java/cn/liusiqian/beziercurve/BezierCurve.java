package cn.liusiqian.beziercurve;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liusiqian on 2018/9/12.
 *
 * for Bézier Curve
 */
public class BezierCurve {
    private static final String TAG = "Bézier Curve";
    private static final int DEFAULT_ACCURACY = 3000;

    private int accuracy;            //精度
    private double[] calculatedX;    //已经计算出的 X 集合
    private int cursor;

    private boolean defined = false;        //是否确定了基准点
    private int level;                      //阶数
    private BezierPoint[] points;           //基准点

    private Map<String, Long> arrangeCache = new HashMap<>();
    private Map<String, Long> combineCache = new HashMap<>();

    private long arrange(int n, int k) {
        if (k <= 0 || n < k) {
            throw new IllegalArgumentException("wrong params for arrange---- n = " + n + " k = " + k);
        }

        String key = String.valueOf(n) + "," + k;
        Long result = arrangeCache.get(key);
        if (result != null) {
            return result;
        }

        result = 1L;
        for (int i = 0; i < k; i++) {
            result *= (n - i);
        }

        arrangeCache.put(key, result);

        return result;
    }

    private long combine(int n, int k) {
        if (k < 0 || n < k) {
            throw new IllegalArgumentException("wrong params for combine---- n = " + n + " k = " + k);
        }

        //simplify
        if (n < 2 * k) {
            k = n - k;
        }

        if (k == 0) {
            return 1;
        }

        String key = String.valueOf(n) + "," + k;
        Long result = combineCache.get(key);
        if (result != null) {
            return result;
        }

        result = arrange(n, k) / arrange(k, k);
        combineCache.put(key, result);

        return result;
    }

    /**
     * 定义高阶贝塞尔曲线（包括两端的点），需要且只允许调用一次
     *
     * @param points
     */
    public void defineUnlimitedBazier(int accuracy, final BezierPoint... points) {
        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("wrong parameter----points");
        }

        if (defined) {
            throw new IllegalStateException("already defined");
        }

        defined = true;
        level = points.length - 1;
        this.accuracy = accuracy;
        calculatedX = new double[accuracy];
        this.points = points;
        cursor = 0;
    }

    public void defineUnlimitedBazier(final BezierPoint... points) {
        defineUnlimitedBazier(DEFAULT_ACCURACY, points);
    }

    /**
     * 定义高阶贝塞尔曲线（不包括两端的点），需要且只允许调用一次
     *
     * @param points
     */
    public void defineLimitedBazier(int accuracy, final BezierPoint... points) {
        BezierPoint[] unlimitedPoints;
        if (points == null || points.length == 0) {
            unlimitedPoints = new BezierPoint[2];
            unlimitedPoints[0] = new BezierPoint(0.0f, 0.0f);
            unlimitedPoints[1] = new BezierPoint(1.0f, 1.0f);
        } else {
            unlimitedPoints = new BezierPoint[points.length + 2];
            unlimitedPoints[0] = new BezierPoint(0.0f, 0.0f);
            System.arraycopy(points, 0, unlimitedPoints, 1, points.length);
            unlimitedPoints[points.length + 1] = new BezierPoint(1.0f, 1.0f);
        }

        defineUnlimitedBazier(accuracy, unlimitedPoints);
    }

    public void defineLimitedBazier(final BezierPoint... points) {
        defineLimitedBazier(DEFAULT_ACCURACY, points);
    }

    /**
     * 提供给贝塞尔插值器使用，调用时一定要保证 x对t 的导数是非负的，否则会出bug
     *
     * @param x
     * @return
     */
    public double calcBezierCurveYByX(double x) {
        double t;
        calculatedX[0] = singleCalcBezierEquationForX(0);

        if (calculatedX[cursor] > x) {
            //t 的值在已经算出过的x 中
            int start = 0;
            int end = cursor;
            int current = (start + end) / 2;
            while (current > start) {
                if (calculatedX[current] > x) {
                    end = current;
                } else {
                    start = current;
                }

                current = (start + end) / 2;
            }

            t = current * 1.0 / accuracy;
        } else {
            //一直查找下一个 t，直到找到计算值比 x大的
            do {
                if (cursor >= accuracy - 1) {
                    t = 1;
                    break;
                }
                cursor++;
                t = cursor * 1.0 / accuracy;
                double nextX = singleCalcBezierEquationForX((float) t);
                calculatedX[cursor] = nextX;
            } while (calculatedX[cursor] < x);

            if (cursor < accuracy - 1) {
                t = (cursor - 1) * 1.0 / accuracy;
            }
        }

        //计算y值
        return singleCalcBezierEquationForY((float) t);
    }

    private double singleCalcBezierEquationForX(float t) {
        if (!defined) {
            throw new IllegalStateException("should define first");
        }

        double result = 0;
        for (int i = 0; i <= level; i++) {
            result += combine(level, i) * Math.pow(t, i) * Math.pow(1 - t, level - i) * points[i].x;
        }

        return result;
    }

    private double singleCalcBezierEquationForY(float t) {
        if (!defined) {
            throw new IllegalStateException("should define first");
        }

        double result = 0;
        for (int i = 0; i <= level; i++) {
            result += combine(level, i) * Math.pow(t, i) * Math.pow(1 - t, level - i) * points[i].y;
        }

        return result;
    }

    /**
     * Complete calc
     *
     * @param t
     * @param points
     * @return
     */
    public BezierPoint unlimitedBezier(float t, final BezierPoint... points) {

        if (points == null || points.length < 2) {
            throw new IllegalArgumentException("wrong parameter----points");
        }

        int classLevel = points.length - 1;     //贝塞尔曲线阶数
        double resultX = 0;
        double resultY = 0;
        for (int i = 0; i <= classLevel; i++) {
            resultX += combine(classLevel, i) * Math.pow(t, i) * Math.pow(1 - t, classLevel - i) * points[i].x;
            resultY += combine(classLevel, i) * Math.pow(t, i) * Math.pow(1 - t, classLevel - i) * points[i].y;
        }

        return new BezierPoint(resultX, resultY);
    }

    public BezierPoint limitedBezier(float t, final BezierPoint... points) {
        BezierPoint[] unlimitedPoints;
        if (points == null || points.length == 0) {
            unlimitedPoints = new BezierPoint[2];
            unlimitedPoints[0] = new BezierPoint(0.0f, 0.0f);
            unlimitedPoints[1] = new BezierPoint(1.0f, 1.0f);
        } else {
            unlimitedPoints = new BezierPoint[points.length + 2];
            unlimitedPoints[0] = new BezierPoint(0.0f, 0.0f);
            System.arraycopy(points, 0, unlimitedPoints, 1, points.length);
            unlimitedPoints[points.length + 1] = new BezierPoint(1.0f, 1.0f);
        }
        return unlimitedBezier(t, unlimitedPoints);
    }

    public static class BezierPoint {
        public double x;
        public double y;

        public BezierPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
