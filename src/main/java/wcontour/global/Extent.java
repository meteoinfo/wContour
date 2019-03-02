/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wcontour.global;

/**
 * Extent class
 *
 * @author Yaqiang Wang
 */
public class Extent {

    public double xMin;
    public double yMin;
    public double xMax;
    public double yMax;

    /**
     * Constructor
     */
    public Extent() {

    }

    /**
     * Constructor
     *
     * @param minX Minimum x
     * @param maxX Maximum x
     * @param minY Minimum y
     * @param maxY Maximum y
     */
    public Extent(double minX, double maxX, double minY, double maxY) {
        xMin = minX;
        xMax = maxX;
        yMin = minY;
        yMax = maxY;
    }

    /**
     * Judge if this extent include another extent
     *
     * @param bExtent The extent
     * @return Is included or not
     */
    public boolean Include(Extent bExtent) {
        return xMin <= bExtent.xMin && xMax >= bExtent.xMax && yMin <= bExtent.yMin && yMax >= bExtent.yMax;
    }
}
