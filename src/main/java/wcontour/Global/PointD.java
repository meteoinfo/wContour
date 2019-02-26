/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wContour.Global;

/**
 * PointD class
 * 
 * @author Yaqiang Wang
 */
public class PointD {
        /// <summary>
        /// x
        /// </summary>
        public double X;
        /// <summary>
        /// y
        /// </summary>
        public double Y;

        public PointD()
        {
            X = 0.0;
            Y = 0.0;
        }
        
        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="x">x</param>
        /// <param name="y">y</param>
        public PointD(double x, double y)
        {
            X = x;
            Y = y;
        }
        
        /// <summary>
        /// Clone
        /// </summary>
        /// <returns>Cloned object</returns>
    @Override
        public Object clone()
        {
            return new PointD(X, Y);
        }
}
