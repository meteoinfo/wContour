/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wcontour.global;

import java.util.ArrayList;
import java.util.List;

/**
 * PolyLine class
 *
 * @author Yaqiang Wang
 */
public class PolyLine {

    public double Value;
    public String Type;
    public int BorderIdx;
    public List<PointD> PointList = new ArrayList<>();
}
