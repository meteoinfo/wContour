/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wContour.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * BorderLine class
 * 
 * @author Yaqiang Wang
 */
public class BorderLine {
    /// <summary>
        /// Area
        /// </summary>
        public double area;
        /// <summary>
        /// Extent
        /// </summary>
        public Extent extent = new Extent();
        /// <summary>
        /// Is outline
        /// </summary>
        public boolean isOutLine;
        /// <summary>
        /// Is clockwise
        /// </summary>
        public boolean isClockwise;
        /// <summary>
        /// Point list
        /// </summary>
        public List<PointD> pointList = new ArrayList<PointD>();
        /// <summary>
        /// IJPoint list
        /// </summary>
        public List<IJPoint> ijPointList = new ArrayList<IJPoint>();
}
