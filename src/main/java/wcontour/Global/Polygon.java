/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wContour.Global;

import wContour.Contour;
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
     *  If there is only inner border
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
    /// <summary>
    /// Start point index
    /// </summary>
    public int StartPointIdx;
    /// <summary>
    /// If high center
    /// </summary>
    public boolean IsHighCenter;
    /// <summary>
    /// Extent - bordering rectangle
    /// </summary>
    public Extent Extent = new Extent();
    /// <summary>
    /// Area
    /// </summary>
    public double Area;
    /// <summary>
    /// Outline
    /// </summary>
    public PolyLine OutLine = new PolyLine();
    /// <summary>
    /// Hole lines
    /// </summary>
    public List<PolyLine> HoleLines = new ArrayList<PolyLine>();
    /// <summary>
    /// Hole index
    /// </summary>
    public int HoleIndex;

    // </editor-fold>
    // <editor-fold desc="Methods">

    /**
     *  Clone the object
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
        aPolygon.HoleLines = new ArrayList<PolyLine>(HoleLines);
        aPolygon.HoleIndex = HoleIndex;

        return aPolygon;
    }

    /// <summary>
    /// Get if has holes
    /// </summary>
    public boolean HasHoles() {
        return (HoleLines.size() > 0);
    }

    /// <summary>
    /// Add a hole by a polygon
    /// </summary>
    /// <param name="aPolygon">polygon</param>
    public void AddHole(Polygon aPolygon) {
        HoleLines.add(aPolygon.OutLine);
    }

    /// <summary>
    /// Add a hole by point list
    /// </summary>
    /// <param name="pList">point list</param>
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
