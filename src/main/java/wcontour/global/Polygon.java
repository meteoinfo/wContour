/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wcontour.global;

import wcontour.Contour;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Polygon class
 *
 * @author Yaqiang Wang
 */
public class Polygon {

    // <editor-fold desc="Variables">
    /**
     * If is border contour polygon
     */
    public boolean IsBorder;
    /**
     * If there is only inner border
     */
    public boolean IsInnerBorder = false;
    /**
     * Start value
     */
    public double LowValue;
    /**
     * End value
     */
    public double HighValue;
    /**
     * If clockwise
     */
    public boolean IsClockWise;
    /**
     * Start point index
     */
    public int StartPointIdx;

    /**
     * Is high center or not
     */
    public boolean IsHighCenter;

    /**
     * Extent - bordering rectangle
     */
    public Extent Extent = new Extent();

    /**
     * Area
     */
    public double Area;

    /**
     * Outline
     */
    public PolyLine OutLine = new PolyLine();

    /**
     * Hole lines
     */
    public List<PolyLine> HoleLines = new ArrayList<>();

    /**
     * Hole index
     */
    public int HoleIndex;

    // </editor-fold>
    // <editor-fold desc="Methods">
    /**
     * Clone the object
     *
     * @return cloned Polygon object
     */
    public Object Clone() {
        Polygon aPolygon = new Polygon();
        aPolygon.IsBorder = IsBorder;
        aPolygon.LowValue = LowValue;
        aPolygon.HighValue = HighValue;
        aPolygon.IsClockWise = IsClockWise;
        aPolygon.StartPointIdx = StartPointIdx;
        aPolygon.IsHighCenter = IsHighCenter;
        aPolygon.Extent = Extent;
        aPolygon.Area = Area;
        aPolygon.OutLine = OutLine;
        aPolygon.HoleLines = new ArrayList<>(HoleLines);
        aPolygon.HoleIndex = HoleIndex;

        return aPolygon;
    }

    /**
     * Get if has holes
     *
     * @return Boolean
     */
    public boolean HasHoles() {
        return (HoleLines.size() > 0);
    }

    /**
     * Add a pohygon hole
     *
     * @param aPolygon The polygon hole
     */
    public void AddHole(Polygon aPolygon) {
        HoleLines.add(aPolygon.OutLine);
    }

    /**
     * Add a hole by point list
     *
     * @param pList The point list
     */
    public void AddHole(List<PointD> pList) {
        if (Contour.isClockwise(pList)) {
            Collections.reverse(pList);
        }

        PolyLine aLine = new PolyLine();
        aLine.PointList = pList;
        HoleLines.add(aLine);
    }
    // </editor-fold>
}
