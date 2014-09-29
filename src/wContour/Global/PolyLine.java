/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wContour.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * PolyLine class
 * 
 * @author Yaqiang Wang
 */
public class PolyLine {
        /// <summary>
        /// Value
        /// </summary>
        public double Value;
        /// <summary>
        /// Type
        /// </summary>
        public String Type;
        /// <summary>
        /// Border index
        /// </summary>
        public int BorderIdx;
        /// <summary>
        /// Point list
        /// </summary>
        public List<PointD> PointList = new ArrayList<PointD>();
}
