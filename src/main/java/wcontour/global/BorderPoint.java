/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wcontour.global;

/**
 * BorderPoint class
 *
 * @author Yaqiang Wang
 */
public class BorderPoint {

    public int Id;
    public int BorderIdx;
    public int BInnerIdx;
    public PointD Point = new PointD();
    public double Value;

    @Override
    public Object clone() {
        BorderPoint aBP = new BorderPoint();
        aBP.Id = Id;
        aBP.BorderIdx = BorderIdx;
        aBP.BInnerIdx = BInnerIdx;
        aBP.Point = Point;
        aBP.Value = Value;

        return aBP;
    }
}
