/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wContour;

import wContour.Global.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Legend class - including the functions of legend
 *
 * @author Yaqiang Wang
 * @version $Revision: 1.6 $
 */
public class Legend {
    
    /**
     * Create legend polygons
     * 
     * @param aLegendPara legend parameters
     * @return legend polygons
     */
    public static List<LPolygon> createLegend(LegendPara aLegendPara) {
        List<LPolygon> polygonList = new ArrayList<>();
        List<PointD> pList;
        LPolygon aLPolygon;
        PointD aPoint;
        int i, pNum;
        double aLength;
        boolean ifRectangle;

        pNum = aLegendPara.contourValues.length + 1;
        aLength = aLegendPara.length / pNum;
        if (aLegendPara.isVertical) {
            for (i = 0; i < pNum; i++) {
                pList = new ArrayList<>();
                ifRectangle = true;
                aLPolygon = new LPolygon();
                if (i == 0) {
                    aLPolygon.value = aLegendPara.contourValues[0];
                    aLPolygon.isFirst = true;
                    if (aLegendPara.isTriangle) {
                        aPoint = new PointD();
                        aPoint.X = aLegendPara.startPoint.X + aLegendPara.width / 2;
                        aPoint.Y = aLegendPara.startPoint.Y;
                        pList.add(aPoint);
                        aPoint = new PointD();
                        aPoint.X = aLegendPara.startPoint.X + aLegendPara.width;
                        aPoint.Y = aLegendPara.startPoint.Y + aLength;
                        pList.add(aPoint);
                        aPoint = new PointD();
                        aPoint.X = aLegendPara.startPoint.X;
                        aPoint.Y = aLegendPara.startPoint.Y + aLength;
                        pList.add(aPoint);
                        ifRectangle = false;
                    }
                } else {
                    aLPolygon.value = aLegendPara.contourValues[i - 1];
                    aLPolygon.isFirst = false;
                    if (i == pNum - 1) {
                        if (aLegendPara.isTriangle) {
                            aPoint = new PointD();
                            aPoint.X = aLegendPara.startPoint.X;
                            aPoint.Y = aLegendPara.startPoint.Y + i * aLength;
                            pList.add(aPoint);
                            aPoint = new PointD();
                            aPoint.X = aLegendPara.startPoint.X + aLegendPara.width;
                            aPoint.Y = aLegendPara.startPoint.Y + i * aLength;
                            pList.add(aPoint);
                            aPoint = new PointD();
                            aPoint.X = aLegendPara.startPoint.X + aLegendPara.width / 2;
                            aPoint.Y = aLegendPara.startPoint.Y + (i + 1) * aLength;
                            pList.add(aPoint);
                            ifRectangle = false;
                        }
                    }
                }

                if (ifRectangle) {
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X;
                    aPoint.Y = aLegendPara.startPoint.Y + i * aLength;
                    pList.add(aPoint);
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X + aLegendPara.width;
                    aPoint.Y = aLegendPara.startPoint.Y + i * aLength;
                    pList.add(aPoint);
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X + aLegendPara.width;
                    aPoint.Y = aLegendPara.startPoint.Y + (i + 1) * aLength;
                    pList.add(aPoint);
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X;
                    aPoint.Y = aLegendPara.startPoint.Y + (i + 1) * aLength;
                    pList.add(aPoint);
                }

                pList.add(pList.get(0));
                aLPolygon.pointList = pList;

                polygonList.add(aLPolygon);
            }
        } else {
            for (i = 0; i < pNum; i++) {
                pList = new ArrayList<>();
                ifRectangle = true;
                aLPolygon = new LPolygon();
                if (i == 0) {
                    aLPolygon.value = aLegendPara.contourValues[0];
                    aLPolygon.isFirst = true;
                    if (aLegendPara.isTriangle) {
                        aPoint = new PointD();
                        aPoint.X = aLegendPara.startPoint.X;
                        aPoint.Y = aLegendPara.startPoint.Y + aLegendPara.width / 2;
                        pList.add(aPoint);
                        aPoint = new PointD();
                        aPoint.X = aLegendPara.startPoint.X + aLength;
                        aPoint.Y = aLegendPara.startPoint.Y;
                        pList.add(aPoint);
                        aPoint = new PointD();
                        aPoint.X = aLegendPara.startPoint.X + aLength;
                        aPoint.Y = aLegendPara.startPoint.Y + aLegendPara.width;
                        pList.add(aPoint);
                        ifRectangle = false;
                    }
                } else {
                    aLPolygon.value = aLegendPara.contourValues[i - 1];
                    aLPolygon.isFirst = false;
                    if (i == pNum - 1) {
                        if (aLegendPara.isTriangle) {
                            aPoint = new PointD();
                            aPoint.X = aLegendPara.startPoint.X + i * aLength;
                            aPoint.Y = aLegendPara.startPoint.Y;
                            pList.add(aPoint);
                            aPoint = new PointD();
                            aPoint.X = aLegendPara.startPoint.X + (i + 1) * aLength;
                            aPoint.Y = aLegendPara.startPoint.Y + aLegendPara.width / 2;
                            pList.add(aPoint);
                            aPoint = new PointD();
                            aPoint.X = aLegendPara.startPoint.X + i * aLength;
                            aPoint.Y = aLegendPara.startPoint.Y + aLegendPara.width;
                            pList.add(aPoint);
                            ifRectangle = false;
                        }
                    }
                }

                if (ifRectangle) {
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X + i * aLength;
                    aPoint.Y = aLegendPara.startPoint.Y;
                    pList.add(aPoint);
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X + (i + 1) * aLength;
                    aPoint.Y = aLegendPara.startPoint.Y;
                    pList.add(aPoint);
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X + (i + 1) * aLength;
                    aPoint.Y = aLegendPara.startPoint.Y + aLegendPara.width;
                    pList.add(aPoint);
                    aPoint = new PointD();
                    aPoint.X = aLegendPara.startPoint.X + i * aLength;
                    aPoint.Y = aLegendPara.startPoint.Y + aLegendPara.width;
                    pList.add(aPoint);
                }

                pList.add(pList.get(0));
                aLPolygon.pointList = pList;

                polygonList.add(aLPolygon);
            }
        }

        return polygonList;
    }
}
