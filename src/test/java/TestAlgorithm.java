import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import wcontour.Contour;
import wcontour.global.Line;
import wcontour.global.PointD;
import org.junit.Assert;

public class TestAlgorithm {

    @Test
    public void testLineSegmentCross() {
        Line lineA = new Line();
        Line lineB = new Line();
        lineA.P1 = new PointD(0.1, 0.3);
        lineA.P2 = new PointD(0.2, 0.5);
        lineB.P1 = new PointD(0.15, 0.7);
        lineB.P2 = new PointD(0.5, 0.4);

        boolean cross = Contour.isLineSegmentCross(lineA, lineB);
        Assert.assertFalse(cross);
    }

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestAlgorithm.class);
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }
}
