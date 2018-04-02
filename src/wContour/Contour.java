/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package wContour;

import wContour.Global.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Contour class - including the functions of contour
 *
 * @author Yaqiang Wang
 * @version $Revision: 1.6.1 $
 */
public class Contour {

    private static List<EndPoint> _endPointList = new ArrayList<EndPoint>();

    // <editor-fold desc="Public Contour Methods">
    /**
     * Get version
     *
     * @return Version
     */
    public static String getVersion() {
        return "1.6.1R1";
    }

    /**
     * Tracing contour lines from the grid data with undefine data
     *
     * @param S0 input grid data
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param nc number of contour values
     * @param contour contour value array
     * @param undefData Undefine data
     * @param borders borders
     * @param S1 data flag array
     * @return Contour line list
     */
    public static List<PolyLine> tracingContourLines(double[][] S0, double[] X, double[] Y,
            int nc, double[] contour, double undefData, List<Border> borders, int[][] S1) {
        List<PolyLine> contourLines = createContourLines_UndefData(S0, X, Y, nc, contour, S1, undefData, borders);

        return contourLines;
    }

    /**
     * Tracing contour borders of the grid data with undefined data. Grid data
     * are from left to right and from bottom to top. Grid data array: first
     * dimention is Y, second dimention is X.
     *
     * @param S0 input grid data
     * @param X x coordinate array
     * @param Y y coordinate array
     * @param S1 data flag array
     * @param undefData undefine data
     * @return borderline list
     */
    public static List<Border> tracingBorders(double[][] S0, double[] X, double[] Y, int[][] S1, double undefData) {
        List<BorderLine> borderLines = new ArrayList<>();

        int m, n, i, j;
        m = S0.length;    //Y
        n = S0[0].length;    //X

        //S1 = new int[m][n];    //---- New array (0 with undefine data, 1 with data)
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                if (doubleEquals(S0[i][j], undefData)) //Undefine data
                {
                    S1[i][j] = 0;
                } else {
                    S1[i][j] = 1;
                }
            }
        }

        //---- Border points are 1, undefine points are 0, inside data points are 2
        //l - Left; r - Right; b - Bottom; t - Top; lb - LeftBottom; rb - RightBottom; lt - LeftTop; rt - RightTop
        int l, r, b, t, lb, rb, lt, rt;
        for (i = 1; i < m - 1; i++) {
            for (j = 1; j < n - 1; j++) {
                if (S1[i][j] == 1) //data point
                {
                    l = S1[i][j - 1];
                    r = S1[i][j + 1];
                    b = S1[i - 1][j];
                    t = S1[i + 1][j];
                    lb = S1[i - 1][j - 1];
                    rb = S1[i - 1][j + 1];
                    lt = S1[i + 1][j - 1];
                    rt = S1[i + 1][j + 1];

                    if (l > 0 && r > 0 && b > 0 && t > 0 && lb > 0 && rb > 0 && lt > 0 && rt > 0) {
                        S1[i][j] = 2;    //Inside data point
                    }
                    if (l + r + b + t + lb + rb + lt + rt <= 2) {
                        S1[i][j] = 0;    //Data point, but not more than 3 continued data points together.
                    }                        //So they can't be traced as a border (at least 4 points together).

                }
            }
        }

        //---- Remove isolated data points (up, down, left and right points are all undefine data).
        boolean isContinue;
        while (true) {
            isContinue = false;
            for (i = 1; i < m - 1; i++) {
                for (j = 1; j < n - 1; j++) {
                    if (S1[i][j] == 1) //data point
                    {
                        l = S1[i][j - 1];
                        r = S1[i][j + 1];
                        b = S1[i - 1][j];
                        t = S1[i + 1][j];
                        lb = S1[i - 1][j - 1];
                        rb = S1[i - 1][j + 1];
                        lt = S1[i + 1][j - 1];
                        rt = S1[i + 1][j + 1];
                        if ((l == 0 && r == 0) || (b == 0 && t == 0)) //Up, down, left and right points are all undefine data
                        {
                            S1[i][j] = 0;
                            isContinue = true;
                        }
                        if ((lt == 0 && r == 0 && b == 0) || (rt == 0 && l == 0 && b == 0)
                                || (lb == 0 && r == 0 && t == 0) || (rb == 0 && l == 0 && t == 0)) {
                            S1[i][j] = 0;
                            isContinue = true;
                        }
                    }
                }
            }
            if (!isContinue) //untile no more isolated data point.
            {
                break;
            }
        }
        //Deal with grid data border points
        for (j = 0; j < n; j++) //Top and bottom border points
        {
            if (S1[0][j] == 1) {
                if (S1[1][j] == 0) //up point is undefine
                {
                    S1[0][j] = 0;
                } else if (j == 0) {
                    if (S1[0][j + 1] == 0) {
                        S1[0][j] = 0;
                    }
                } else if (j == n - 1) {
                    if (S1[0][n - 2] == 0) {
                        S1[0][j] = 0;
                    }
                } else if (S1[0][j - 1] == 0 && S1[0][j + 1] == 0) {
                    S1[0][j] = 0;
                }
            }
            if (S1[m - 1][j] == 1) {
                if (S1[m - 2][j] == 0) //down point is undefine
                {
                    S1[m - 1][j] = 0;
                } else if (j == 0) {
                    if (S1[m - 1][j + 1] == 0) {
                        S1[m - 1][j] = 0;
                    }
                } else if (j == n - 1) {
                    if (S1[m - 1][n - 2] == 0) {
                        S1[m - 1][j] = 0;
                    }
                } else if (S1[m - 1][j - 1] == 0 && S1[m - 1][j + 1] == 0) {
                    S1[m - 1][j] = 0;
                }
            }
        }
        for (i = 0; i < m; i++) //Left and right border points
        {
            if (S1[i][0] == 1) {
                if (S1[i][1] == 0) //right point is undefine
                {
                    S1[i][0] = 0;
                } else if (i == 0) {
                    if (S1[i + 1][0] == 0) {
                        S1[i][0] = 0;
                    }
                } else if (i == m - 1) {
                    if (S1[m - 2][0] == 0) {
                        S1[i][0] = 0;
                    }
                } else if (S1[i - 1][0] == 0 && S1[i + 1][0] == 0) {
                    S1[i][0] = 0;
                }
            }
            if (S1[i][n - 1] == 1) {
                if (S1[i][n - 2] == 0) //left point is undefine
                {
                    S1[i][n - 1] = 0;
                } else if (i == 0) {
                    if (S1[i + 1][n - 1] == 0) {
                        S1[i][n - 1] = 0;
                    }
                } else if (i == m - 1) {
                    if (S1[m - 2][n - 1] == 0) {
                        S1[i][n - 1] = 0;
                    }
                } else if (S1[i - 1][n - 1] == 0 && S1[i + 1][n - 1] == 0) {
                    S1[i][n - 1] = 0;
                }
            }
        }

        //---- Generate S2 array from S1, add border to S2 with undefine data.
        int[][] S2 = new int[m + 2][n + 2];
        for (i = 0; i < m + 2; i++) {
            for (j = 0; j < n + 2; j++) {
                if (i == 0 || i == m + 1) //bottom or top border
                {
                    S2[i][j] = 0;
                } else if (j == 0 || j == n + 1) //left or right border
                {
                    S2[i][j] = 0;
                } else {
                    S2[i][j] = S1[i - 1][j - 1];
                }
            }
        }

        //---- Using times number of each point during chacing process.
        int[][] UNum = new int[m + 2][n + 2];
        for (i = 0; i < m + 2; i++) {
            for (j = 0; j < n + 2; j++) {
                if (S2[i][j] == 1) {
                    l = S2[i][j - 1];
                    r = S2[i][j + 1];
                    b = S2[i - 1][j];
                    t = S2[i + 1][j];
                    lb = S2[i - 1][j - 1];
                    rb = S2[i - 1][j + 1];
                    lt = S2[i + 1][j - 1];
                    rt = S2[i + 1][j + 1];
                    //---- Cross point with two boder lines, will be used twice.
                    if (l == 1 && r == 1 && b == 1 && t == 1 && ((lb == 0 && rt == 0) || (rb == 0 && lt == 0))) {
                        UNum[i][j] = 2;
                    } else {
                        UNum[i][j] = 1;
                    }
                } else {
                    UNum[i][j] = 0;
                }
            }
        }

        //---- Tracing borderlines
        PointD aPoint;
        IJPoint aijPoint;
        BorderLine aBLine;
        List<PointD> pointList;
        List<IJPoint> ijPList;
        int sI, sJ, i1, j1, i2, j2, i3 = 0, j3 = 0;
        for (i = 1; i < m + 1; i++) {
            for (j = 1; j < n + 1; j++) {
                if (S2[i][j] == 1) //Tracing border from any border point
                {
                    pointList = new ArrayList<>();
                    ijPList = new ArrayList<>();
                    aPoint = new PointD();
                    aPoint.X = X[j - 1];
                    aPoint.Y = Y[i - 1];
                    aijPoint = new IJPoint();
                    aijPoint.I = i - 1;
                    aijPoint.J = j - 1;
                    pointList.add(aPoint);
                    ijPList.add(aijPoint);
                    sI = i;
                    sJ = j;
                    i2 = i;
                    j2 = j;
                    i1 = i2;
                    j1 = -1;    //Trace from left firstly                        

                    while (true) {
                        int[] ij3 = new int[2];
                        ij3[0] = i3;
                        ij3[1] = j3;
                        if (traceBorder(S2, i1, i2, j1, j2, ij3)) {
                            i3 = ij3[0];
                            j3 = ij3[1];
                            i1 = i2;
                            j1 = j2;
                            i2 = i3;
                            j2 = j3;
                            UNum[i3][j3] = UNum[i3][j3] - 1;
                            if (UNum[i3][j3] == 0) {
                                S2[i3][j3] = 3;    //Used border point
                            }
                        } else {
                            break;
                        }

                        aPoint = new PointD();
                        aPoint.X = X[j3 - 1];
                        aPoint.Y = Y[i3 - 1];
                        aijPoint = new IJPoint();
                        aijPoint.I = i3 - 1;
                        aijPoint.J = j3 - 1;
                        pointList.add(aPoint);
                        ijPList.add(aijPoint);
                        if (i3 == sI && j3 == sJ) {
                            break;
                        }
                    }
                    UNum[i][j] = UNum[i][j] - 1;
                    if (UNum[i][j] == 0) {
                        S2[i][j] = 3;    //Used border point
                    }                        //UNum[i][j] = UNum[i][j] - 1;
                    if (pointList.size() > 1) {
                        aBLine = new BorderLine();
                        aBLine.area = getExtentAndArea(pointList, aBLine.extent);
                        aBLine.isOutLine = true;
                        aBLine.isClockwise = true;
                        aBLine.pointList = pointList;
                        aBLine.ijPointList = ijPList;
                        borderLines.add(aBLine);
                    }
                }
            }
        }

        //---- Form borders
        List<Border> borders = new ArrayList<>();
        Border aBorder;
        BorderLine aLine, bLine;
        //---- Sort borderlines with area from small to big.
        //For inside border line analysis
        for (i = 1; i < borderLines.size(); i++) {
            aLine = borderLines.get(i);
            for (j = 0; j < i; j++) {
                bLine = borderLines.get(j);
                if (aLine.area > bLine.area) {
                    borderLines.remove(i);
                    borderLines.add(j, aLine);
                    break;
                }
            }
        }
        List<BorderLine> lineList;
        if (borderLines.size() == 1) //Only one boder line
        {
            aLine = borderLines.get(0);
            if (!isClockwise(aLine.pointList)) {
                Collections.reverse(aLine.pointList);
                Collections.reverse(aLine.ijPointList);
            }
            aLine.isClockwise = true;
            lineList = new ArrayList<>();
            lineList.add(aLine);
            aBorder = new Border();
            aBorder.LineList = lineList;
            borders.add(aBorder);
        } else //muti border lines
        {
            for (i = 0; i < borderLines.size(); i++) {
                if (i == borderLines.size()) {
                    break;
                }

                aLine = borderLines.get(i);
                if (!isClockwise(aLine.pointList)) {
                    Collections.reverse(aLine.pointList);
                    Collections.reverse(aLine.ijPointList);
                }
                aLine.isClockwise = true;
                lineList = new ArrayList<>();
                lineList.add(aLine);
                //Try to find the boder lines are inside of aLine.
                for (j = i + 1; j < borderLines.size(); j++) {
                    if (j == borderLines.size()) {
                        break;
                    }

                    bLine = borderLines.get(j);
                    if (bLine.extent.xMin > aLine.extent.xMin && bLine.extent.xMax < aLine.extent.xMax
                            && bLine.extent.yMin > aLine.extent.yMin && bLine.extent.yMax < aLine.extent.yMax) {
                        aPoint = bLine.pointList.get(0);
                        if (pointInPolygon(aLine.pointList, aPoint)) //bLine is inside of aLine
                        {
                            bLine.isOutLine = false;
                            if (isClockwise(bLine.pointList)) {
                                Collections.reverse(bLine.pointList);
                                Collections.reverse(bLine.ijPointList);
                            }
                            bLine.isClockwise = false;
                            lineList.add(bLine);
                            borderLines.remove(j);
                            j = j - 1;
                        }
                    }
                }
                aBorder = new Border();
                aBorder.LineList = lineList;
                borders.add(aBorder);
            }
        }

        return borders;
    }

    /**
     * Create contour lines from the grid data with undefine data
     *
     * @param S0 input grid data
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param nc number of contour values
     * @param contour contour value array
     * @param nx interval of X coordinate
     * @param ny interval of Y coordinate
     * @param S1 flag array
     * @param undefData undefine data
     * @param borders border line list
     * @return contour line list
     */
    private static List<PolyLine> createContourLines_UndefData(double[][] S0, double[] X, double[] Y,
            int nc, double[] contour, int[][] S1, double undefData, List<Border> borders) {
        List<PolyLine> contourLineList = new ArrayList<>();
        List<PolyLine> cLineList;
        int m, n, i, j;
        m = S0.length;    //---- Y
        n = S0[0].length;    //---- X

        //---- Add a small value to aviod the contour point as same as data point
        double dShift;
        dShift = contour[0] * 0.00001;
        if (dShift == 0) {
            dShift = 0.00001;
        }
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                if (!(doubleEquals(S0[i][j], undefData))) //S0[i, j] = S0[i, j] + (contour[1] - contour[0]) * 0.0001;
                {
                    S0[i][j] = S0[i][j] + dShift;
                }
            }
        }

        //---- Define if H S are border
        int[][][] SB = new int[2][m][n - 1], HB = new int[2][m - 1][n];   //---- Which border and trace direction
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                if (j < n - 1) {
                    SB[0][i][j] = -1;
                    SB[1][i][j] = -1;
                }
                if (i < m - 1) {
                    HB[0][i][j] = -1;
                    HB[1][i][j] = -1;
                }
            }
        }
        Border aBorder;
        BorderLine aBLine;
        List<IJPoint> ijPList;
        int k, si, sj;
        IJPoint aijP, bijP;
        for (i = 0; i < borders.size(); i++) {
            aBorder = borders.get(i);
            for (j = 0; j < aBorder.getLineNum(); j++) {
                aBLine = aBorder.LineList.get(j);
                ijPList = aBLine.ijPointList;
                for (k = 0; k < ijPList.size() - 1; k++) {
                    aijP = ijPList.get(k);
                    bijP = ijPList.get(k + 1);
                    if (aijP.I == bijP.I) {
                        si = aijP.I;
                        sj = Math.min(aijP.J, bijP.J);
                        SB[0][si][sj] = i;
                        if (bijP.J > aijP.J) //---- Trace from top
                        {
                            SB[1][si][sj] = 1;
                        } else {
                            SB[1][si][sj] = 0;    //----- Trace from bottom
                        }
                    } else {
                        sj = aijP.J;
                        si = Math.min(aijP.I, bijP.I);
                        HB[0][si][sj] = i;
                        if (bijP.I > aijP.I) //---- Trace from left
                        {
                            HB[1][si][sj] = 0;
                        } else {
                            HB[1][si][sj] = 1;    //---- Trace from right
                        }
                    }
                }
            }
        }

        //---- Define horizontal and vertical arrays with the position of the tracing value, -2 means no tracing point. 
        double[][] S = new double[m][n - 1];
        double[][] H = new double[m - 1][n];
        double w;    //---- Tracing value
        int c;
        //ArrayList _endPointList = new ArrayList();    //---- Contour line end points for insert to border
        for (c = 0; c < nc; c++) {
            w = contour[c];
            for (i = 0; i < m; i++) {
                for (j = 0; j < n; j++) {
                    if (j < n - 1) {
                        if (S1[i][j] != 0 && S1[i][j + 1] != 0) {
                            if ((S0[i][j] - w) * (S0[i][j + 1] - w) < 0) //---- Has tracing value
                            {
                                S[i][j] = (w - S0[i][j]) / (S0[i][j + 1] - S0[i][j]);
                            } else {
                                S[i][j] = -2;
                            }
                        } else {
                            S[i][j] = -2;
                        }
                    }
                    if (i < m - 1) {
                        if (S1[i][j] != 0 && S1[i + 1][j] != 0) {
                            if ((S0[i][j] - w) * (S0[i + 1][j] - w) < 0) //---- Has tracing value
                            {
                                H[i][j] = (w - S0[i][j]) / (S0[i + 1][j] - S0[i][j]);
                            } else {
                                H[i][j] = -2;
                            }
                        } else {
                            H[i][j] = -2;
                        }
                    }
                }
            }

            cLineList = isoline_UndefData(S0, X, Y, w, S, H, SB, HB, contourLineList.size());
            contourLineList.addAll(cLineList);
        }

        //---- Set border index for close contours
        PolyLine aLine;
        //List pList = new ArrayList();
        PointD aPoint;
        for (i = 0; i < borders.size(); i++) {
            aBorder = borders.get(i);
            aBLine = aBorder.LineList.get(0);
            for (j = 0; j < contourLineList.size(); j++) {
                aLine = contourLineList.get(j);
                if (aLine.Type.equals("Close")) {
                    aPoint = aLine.PointList.get(0);
                    if (pointInPolygon(aBLine.pointList, aPoint)) {
                        aLine.BorderIdx = i;
                    }
                }
                contourLineList.remove(j);
                contourLineList.add(j, aLine);
            }
        }

        return contourLineList;
    }

    /**
     * Create contour lines
     *
     * @param S0 input grid data array
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param nc number of contour values
     * @param contour contour value array
     * @param nx Interval of X coordinate
     * @param ny Interval of Y coordinate
     * @return contour lines
     */
    private static List<PolyLine> createContourLines(double[][] S0, double[] X, double[] Y, int nc, double[] contour, double nx, double ny) {
        List<PolyLine> contourLineList = new ArrayList<>(), bLineList, lLineList,
                tLineList, rLineList, cLineList;
        int m, n, i, j;
        m = S0.length;    //---- Y
        n = S0[0].length;    //---- X

        //---- Define horizontal and vertical arrays with the position of the tracing value, -2 means no tracing point. 
        double[][] S = new double[m][n - 1], H = new double[m - 1][n];
        double dShift;
        dShift = contour[0] * 0.00001;
        if (dShift == 0) {
            dShift = 0.00001;
        }
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                S0[i][j] = S0[i][j] + dShift;
            }
        }

        double w;    //---- Tracing value
        int c;
        for (c = 0; c < nc; c++) {
            w = contour[c];
            for (i = 0; i < m; i++) {
                for (j = 0; j < n; j++) {
                    if (j < n - 1) {
                        if ((S0[i][j] - w) * (S0[i][j + 1] - w) < 0) //---- Has tracing value
                        {
                            S[i][j] = (w - S0[i][j]) / (S0[i][j + 1] - S0[i][j]);
                        } else {
                            S[i][j] = -2;
                        }
                    }
                    if (i < m - 1) {
                        if ((S0[i][j] - w) * (S0[i + 1][j] - w) < 0) //---- Has tracing value
                        {
                            H[i][j] = (w - S0[i][j]) / (S0[i + 1][j] - S0[i][j]);
                        } else {
                            H[i][j] = -2;
                        }
                    }
                }
            }

            bLineList = isoline_Bottom(S0, X, Y, w, nx, ny, S, H);
            lLineList = isoline_Left(S0, X, Y, w, nx, ny, S, H);
            tLineList = isoline_Top(S0, X, Y, w, nx, ny, S, H);
            rLineList = isoline_Right(S0, X, Y, w, nx, ny, S, H);
            cLineList = isoline_Close(S0, X, Y, w, nx, ny, S, H);
            contourLineList.addAll(bLineList);
            contourLineList.addAll(lLineList);
            contourLineList.addAll(tLineList);
            contourLineList.addAll(rLineList);
            contourLineList.addAll(cLineList);
        }

        return contourLineList;
    }

    /**
     * Cut contour lines with a polygon. Return the polylines inside of the
     * polygon
     *
     * @param alinelist polyline list
     * @param polyList border points of the cut polygon
     * @return Inside Polylines after cut
     */
    public static List<PolyLine> cutContourWithPolygon(List<PolyLine> alinelist, List<PointD> polyList) {
        List<PolyLine> newLineList = new ArrayList<>();
        int i, j, k;
        PolyLine aLine, bLine = new PolyLine();
        List<PointD> aPList;
        double aValue;
        String aType;
        boolean ifInPolygon;
        PointD q1, q2, p1, p2, IPoint;
        Line lineA, lineB;
        EndPoint aEndPoint = new EndPoint();

        _endPointList = new ArrayList<>();
        if (!isClockwise(polyList)) //---- Make cut polygon clockwise
        {
            Collections.reverse(polyList);
        }

        for (i = 0; i < alinelist.size(); i++) {
            aLine = alinelist.get(i);
            aValue = aLine.Value;
            aType = aLine.Type;
            aPList = new ArrayList<>(aLine.PointList);
            ifInPolygon = false;
            List<PointD> newPlist = new ArrayList<>();
            //---- For "Close" type contour,the start point must be outside of the cut polygon.
            if (aType.equals("Close") && pointInPolygon(polyList, aPList.get(0))) {
                boolean isAllIn = true;
                int notInIdx = 0;
                for (j = 0; j < aPList.size(); j++) {
                    if (!pointInPolygon(polyList, aPList.get(j))) {
                        notInIdx = j;
                        isAllIn = false;
                        break;
                    }
                }
                if (!isAllIn) {
                    List<PointD> bPList = new ArrayList<>();
                    for (j = notInIdx; j < aPList.size(); j++) {
                        bPList.add(aPList.get(j));
                    }

                    for (j = 1; j < notInIdx; j++) {
                        bPList.add(aPList.get(j));
                    }

                    bPList.add(bPList.get(0));
                    aPList = bPList;
                }
            }
            p1 = new PointD();
            for (j = 0; j < aPList.size(); j++) {
                p2 = aPList.get(j);
                if (pointInPolygon(polyList, p2)) {
                    if (!ifInPolygon && j > 0) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = polyList.get(polyList.size() - 1);
                        IPoint = new PointD();
                        for (k = 0; k < polyList.size(); k++) {
                            q2 = polyList.get(k);
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aEndPoint.sPoint = q1;
                                aEndPoint.Point = IPoint;
                                aEndPoint.Index = newLineList.size();
                                _endPointList.add(aEndPoint);    //---- Generate _endPointList for border insert
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);
                        aType = "Border";
                    }
                    newPlist.add(aPList.get(j));
                    ifInPolygon = true;
                } else if (ifInPolygon) {
                    lineA = new Line();
                    lineA.P1 = p1;
                    lineA.P2 = p2;
                    q1 = polyList.get(polyList.size() - 1);
                    IPoint = new PointD();
                    for (k = 0; k < polyList.size(); k++) {
                        q2 = polyList.get(k);
                        lineB = new Line();
                        lineB.P1 = q1;
                        lineB.P2 = q2;
                        if (isLineSegmentCross(lineA, lineB)) {
                            IPoint = getCrossPoint(lineA, lineB);
                            aEndPoint.sPoint = q1;
                            aEndPoint.Point = IPoint;
                            aEndPoint.Index = newLineList.size();
                            _endPointList.add(aEndPoint);
                            break;
                        }
                        q1 = q2;
                    }
                    newPlist.add(IPoint);

                    bLine.Value = aValue;
                    bLine.Type = aType;
                    bLine.PointList = newPlist;
                    newLineList.add(bLine);
                    ifInPolygon = false;
                    newPlist = new ArrayList<>();
                    aType = "Border";
                }
                p1 = p2;
            }
            if (ifInPolygon && newPlist.size() > 1) {
                bLine.Value = aValue;
                bLine.Type = aType;
                bLine.PointList = newPlist;
                newLineList.add(bLine);
            }
        }

        return newLineList;
    }

    /**
     * Cut contour lines with a polygon. Return the polylines inside of the
     * polygon
     *
     * @param alinelist polyline list
     * @param aBorder border for clipping
     * @return inside plylines after clipping
     */
    public static List<PolyLine> cutContourLines(List<PolyLine> alinelist, Border aBorder) {
        List<PointD> pointList = aBorder.LineList.get(0).pointList;
        List<PolyLine> newLineList = new ArrayList<>();
        int i, j, k;
        PolyLine aLine, bLine;
        List<PointD> aPList;
        double aValue;
        String aType;
        boolean ifInPolygon;
        PointD q1, q2, p1, p2, IPoint;
        Line lineA, lineB;
        EndPoint aEndPoint = new EndPoint();

        _endPointList = new ArrayList<>();
        if (!isClockwise(pointList)) //---- Make cut polygon clockwise
        {
            Collections.reverse(pointList);
        }

        for (i = 0; i < alinelist.size(); i++) {
            aLine = alinelist.get(i);
            aValue = aLine.Value;
            aType = aLine.Type;
            aPList = new ArrayList<>(aLine.PointList);
            ifInPolygon = false;
            List<PointD> newPlist = new ArrayList<>();
            //---- For "Close" type contour,the start point must be outside of the cut polygon.
            if (aType.equals("Close") && pointInPolygon(pointList, aPList.get(0))) {
                boolean isAllIn = true;
                int notInIdx = 0;
                for (j = 0; j < aPList.size(); j++) {
                    if (!pointInPolygon(pointList, aPList.get(j))) {
                        notInIdx = j;
                        isAllIn = false;
                        break;
                    }
                }
                if (!isAllIn) {
                    List<PointD> bPList = new ArrayList<>();
                    for (j = notInIdx; j < aPList.size(); j++) {
                        bPList.add(aPList.get(j));
                    }

                    for (j = 1; j < notInIdx; j++) {
                        bPList.add(aPList.get(j));
                    }

                    bPList.add(bPList.get(0));
                    aPList = bPList;
                }
            }

            p1 = new PointD();
            for (j = 0; j < aPList.size(); j++) {
                p2 = aPList.get(j);
                if (pointInPolygon(pointList, p2)) {
                    if (!ifInPolygon && j > 0) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = pointList.get(pointList.size() - 1);
                        IPoint = new PointD();
                        for (k = 0; k < pointList.size(); k++) {
                            q2 = pointList.get(k);
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aEndPoint.sPoint = q1;
                                aEndPoint.Point = IPoint;
                                aEndPoint.Index = newLineList.size();
                                _endPointList.add(aEndPoint);    //---- Generate _endPointList for border insert
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);
                        aType = "Border";
                    }
                    newPlist.add(aPList.get(j));
                    ifInPolygon = true;
                } else if (ifInPolygon) {
                    lineA = new Line();
                    lineA.P1 = p1;
                    lineA.P2 = p2;
                    q1 = pointList.get(pointList.size() - 1);
                    IPoint = new PointD();
                    for (k = 0; k < pointList.size(); k++) {
                        q2 = pointList.get(k);
                        lineB = new Line();
                        lineB.P1 = q1;
                        lineB.P2 = q2;
                        if (isLineSegmentCross(lineA, lineB)) {
                            IPoint = getCrossPoint(lineA, lineB);
                            aEndPoint.sPoint = q1;
                            aEndPoint.Point = IPoint;
                            aEndPoint.Index = newLineList.size();
                            _endPointList.add(aEndPoint);
                            break;
                        }
                        q1 = q2;
                    }
                    newPlist.add(IPoint);

                    bLine = new PolyLine();
                    bLine.Value = aValue;
                    bLine.Type = aType;
                    bLine.PointList = newPlist;
                    newLineList.add(bLine);
                    ifInPolygon = false;
                    newPlist = new ArrayList<>();
                    aType = "Border";
                }
                p1 = p2;
            }
            if (ifInPolygon && newPlist.size() > 1) {
                bLine = new PolyLine();
                bLine.Value = aValue;
                bLine.Type = aType;
                bLine.PointList = newPlist;
                newLineList.add(bLine);
            }
        }

        return newLineList;
    }

    /**
     * Smooth polylines
     *
     * @param aLineList polyline list
     * @return polyline list after smoothing
     */
    public static List<PolyLine> smoothLines(List<PolyLine> aLineList) {
        List<PolyLine> newLineList = new ArrayList<>();
        int i;
        PolyLine aline;
        List<PointD> newPList;
        for (i = 0; i < aLineList.size(); i++) {
            aline = aLineList.get(i);
            newPList = new ArrayList<>(aline.PointList);
            if (newPList.size() <= 1) {
                continue;
            }

            if (newPList.size() == 2) {
                PointD bP = new PointD();
                PointD aP = newPList.get(0);
                PointD cP = newPList.get(1);
                bP.X = (cP.X - aP.X) / 4 + aP.X;
                bP.Y = (cP.Y - aP.Y) / 4 + aP.Y;
                newPList.add(1, bP);
                bP = new PointD();
                bP.X = (cP.X - aP.X) / 4 * 3 + aP.X;
                bP.Y = (cP.Y - aP.Y) / 4 * 3 + aP.Y;
                newPList.add(2, bP);
            }
            if (newPList.size() == 3) {
                PointD bP = new PointD();
                PointD aP = newPList.get(0);
                PointD cP = newPList.get(1);
                bP.X = (cP.X - aP.X) / 2 + aP.X;
                bP.Y = (cP.Y - aP.Y) / 2 + aP.Y;
                newPList.add(1, bP);
            }
            newPList = BSplineScanning(newPList, newPList.size());
            aline.PointList = newPList;
            newLineList.add(aline);
        }

        return newLineList;
    }

    /**
     * Smooth points
     *
     * @param pointList point list
     * @return smoothed point list
     */
    public static List<PointD> smoothPoints(List<PointD> pointList) {
        return BSplineScanning(pointList, pointList.size());
    }

    /**
     * Tracing polygons from contour lines and borders
     *
     * @param S0 input grid data
     * @param cLineList contour lines
     * @param borderList borders
     * @param contour contour values
     * @return traced contour polygons
     */
    public static List<Polygon> tracingPolygons(double[][] S0, List<PolyLine> cLineList, List<Border> borderList, double[] contour) {
        List<Polygon> aPolygonList = new ArrayList<>(), newPolygonList = new ArrayList<>();
        List<BorderPoint> newBPList;
        List<BorderPoint> bPList = new ArrayList<>();
        List<PointD> PList;
        Border aBorder;
        BorderLine aBLine;
        PointD aPoint;
        BorderPoint aBPoint;
        int i, j;
        List<PolyLine> lineList = new ArrayList<>();
        List<BorderPoint> aBorderList = new ArrayList<>();
        PolyLine aLine;
        Polygon aPolygon;
        IJPoint aijP;
        double aValue = 0;
        int[] pNums;

        //Borders loop
        for (i = 0; i < borderList.size(); i++) {
            aBorderList.clear();
            bPList.clear();
            lineList.clear();
            aPolygonList.clear();
            aBorder = borderList.get(i);

            aBLine = aBorder.LineList.get(0);
            PList = aBLine.pointList;
            if (!isClockwise(PList)) //Make sure the point list is clockwise
            {
                Collections.reverse(PList);
            }

            if (aBorder.getLineNum() == 1) //The border has just one line
            {
                //Construct border point list
                for (j = 0; j < PList.size(); j++) {
                    aPoint = PList.get(j);
                    aBPoint = new BorderPoint();
                    aBPoint.Id = -1;
                    aBPoint.Point = aPoint;
                    aBPoint.Value = S0[aBLine.ijPointList.get(j).I][aBLine.ijPointList.get(j).J];
                    aBorderList.add(aBPoint);
                }

                //Find the contour lines of this border
                for (j = 0; j < cLineList.size(); j++) {
                    aLine = cLineList.get(j);
                    if (aLine.BorderIdx == i) {
                        lineList.add(aLine);    //Construct contour line list
                        //Construct border point list of the contour line
                        if (aLine.Type.equals("Border")) //The contour line with the start/end point on the border
                        {
                            aPoint = aLine.PointList.get(0);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                            aPoint = aLine.PointList.get(aLine.PointList.size() - 1);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                        }
                    }
                }

                if (lineList.isEmpty()) //No contour lines in this border, the polygon is the border
                {
                    //Judge the value of the polygon
                    aijP = aBLine.ijPointList.get(0);
                    aPolygon = new Polygon();
                    if (S0[aijP.I][aijP.J] < contour[0]) {
                        aValue = contour[0];
                        aPolygon.IsHighCenter = false;
                    } else {
                        for (j = contour.length - 1; j >= 0; j--) {
                            if (S0[aijP.I][aijP.J] > contour[j]) {
                                aValue = contour[j];
                                break;
                            }
                        }
                        aPolygon.IsHighCenter = true;
                    }
                    if (PList.size() > 0) {
                        aPolygon.IsBorder = true;
                        aPolygon.HighValue = aValue;
                        aPolygon.LowValue = aValue;
                        aPolygon.Extent = new Extent();
                        aPolygon.Area = getExtentAndArea(PList, aPolygon.Extent);
                        aPolygon.StartPointIdx = 0;
                        aPolygon.IsClockWise = true;
                        aPolygon.OutLine.Type = "Border";
                        aPolygon.OutLine.Value = aValue;
                        aPolygon.OutLine.BorderIdx = i;
                        aPolygon.OutLine.PointList = PList;
                        aPolygon.HoleLines = new ArrayList<>();
                        aPolygonList.add(aPolygon);
                    }
                } else //Has contour lines in this border
                {
                    //Insert the border points of the contour lines to the border point list of the border
                    if (bPList.size() > 0) {
                        newBPList = insertPoint2Border(bPList, aBorderList);
                    } else {
                        newBPList = aBorderList;
                    }
                    //aPolygonList = TracingPolygons(lineList, newBPList, aBound, contour);
                    aPolygonList = tracingPolygons(lineList, newBPList, bPList.size() > 0);
                }
                aPolygonList = addPolygonHoles(aPolygonList);
            } else //---- The border has holes
            {
                aBLine = aBorder.LineList.get(0);
                //Find the contour lines of this border
                for (j = 0; j < cLineList.size(); j++) {
                    aLine = cLineList.get(j);
                    if (aLine.BorderIdx == i) {
                        lineList.add(aLine);
                        if (aLine.Type.equals("Border")) {
                            aPoint = aLine.PointList.get(0);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                            aPoint = aLine.PointList.get(aLine.PointList.size() - 1);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                        }
                    }
                }
                if (lineList.isEmpty()) //No contour lines in this border, the polygon is the border and the holes
                {
                    aijP = aBLine.ijPointList.get(0);
                    aPolygon = new Polygon();
                    if (S0[aijP.I][aijP.J] < contour[0]) {
                        aValue = contour[0];
                        aPolygon.IsHighCenter = false;
                    } else {
                        for (j = contour.length - 1; j >= 0; j--) {
                            if (S0[aijP.I][aijP.J] > contour[j]) {
                                aValue = contour[j];
                                break;
                            }
                        }
                        aPolygon.IsHighCenter = true;
                    }
                    if (PList.size() > 0) {
                        aPolygon.IsBorder = true;
                        aPolygon.HighValue = aValue;
                        aPolygon.LowValue = aValue;
                        aPolygon.Area = getExtentAndArea(PList, aPolygon.Extent);
                        aPolygon.StartPointIdx = 0;
                        aPolygon.IsClockWise = true;
                        aPolygon.OutLine.Type = "Border";
                        aPolygon.OutLine.Value = aValue;
                        aPolygon.OutLine.BorderIdx = i;
                        aPolygon.OutLine.PointList = PList;
                        aPolygon.HoleLines = new ArrayList<>();
                        aPolygonList.add(aPolygon);
                    }
                } else {
                    pNums = new int[aBorder.getLineNum()];
                    newBPList = insertPoint2Border_Ring(S0, bPList, aBorder, pNums);
                    aPolygonList = tracingPolygons_Ring(lineList, newBPList, aBorder, contour, pNums);

                    //Sort polygons by area
                    List<Polygon> sortList = new ArrayList<>();
                    while (aPolygonList.size() > 0) {
                        boolean isInsert = false;
                        for (j = 0; j < sortList.size(); j++) {
                            if (aPolygonList.get(0).Area > sortList.get(j).Area) {
                                sortList.add(aPolygonList.get(0));
                                isInsert = true;
                                break;
                            }
                        }
                        if (!isInsert) {
                            sortList.add(aPolygonList.get(0));
                        }
                        aPolygonList.remove(0);
                    }
                    aPolygonList = sortList;
                }
                List<List<PointD>> holeList = new ArrayList<>();
                for (j = 0; j < aBorder.getLineNum(); j++) {
//                        if (aBorder.LineList.get(j).pointList.size() == pNums[j]) {
//                            holeList.add(aBorder.LineList.get(j).pointList);
//                        }
                    holeList.add(aBorder.LineList.get(j).pointList);
                }

                if (holeList.size() > 0) {
                    addHoles_Ring(aPolygonList, holeList);
                }
                aPolygonList = addPolygonHoles_Ring(aPolygonList);
            }
            newPolygonList.addAll(aPolygonList);
        }

        //newPolygonList = AddPolygonHoles(newPolygonList);
        for (Polygon nPolygon : newPolygonList) {
            if (!isClockwise(nPolygon.OutLine.PointList)) {
                Collections.reverse(nPolygon.OutLine.PointList);
            }
        }

        return newPolygonList;
    }

    /**
     * Create contour polygons
     *
     * @param LineList contour lines
     * @param aBound grid data extent
     * @param contour contour values
     * @return contour polygons
     */
    private static List<Polygon> createContourPolygons(List<PolyLine> LineList, Extent aBound, double[] contour) {
        List<Polygon> aPolygonList;
        List<BorderPoint> newBorderList;

        //---- Insert points to border list
        newBorderList = insertPoint2RectangleBorder(LineList, aBound);

        //---- Tracing polygons
        aPolygonList = tracingPolygons(LineList, newBorderList, aBound, contour);

        return aPolygonList;
    }

    /**
     * Create polygons from cutted contour lines
     *
     * @param LineList polylines
     * @param polyList border point list
     * @param aBound extent
     * @param contour contour values
     * @return contour polygons
     */
    public static List<Polygon> createCutContourPolygons(List<PolyLine> LineList, List<PointD> polyList, Extent aBound, double[] contour) {
        List<Polygon> aPolygonList;
        List<BorderPoint> newBorderList;
        List<BorderPoint> borderList = new ArrayList<>();
        PointD aPoint;
        BorderPoint aBPoint;
        int i;

        //---- Get border point list
        if (!isClockwise(polyList)) {
            Collections.reverse(polyList);
        }

        for (i = 0; i < polyList.size(); i++) {
            aPoint = polyList.get(i);
            aBPoint = new BorderPoint();
            aBPoint.Id = -1;
            aBPoint.Point = aPoint;
            borderList.add(aBPoint);
        }

        //---- Insert points to border list
        newBorderList = insertEndPoint2Border(_endPointList, borderList);

        //---- Tracing polygons
        aPolygonList = tracingPolygons(LineList, newBorderList, aBound, contour);

        return aPolygonList;
    }

    /**
     * Create contour polygons from borders
     *
     * @param S0 input grid data array
     * @param cLineList contour lines
     * @param borderList borders
     * @param aBound extent
     * @param contour contour values
     * @return contour polygons
     */
    private static List<Polygon> createBorderContourPolygons(double[][] S0, List<PolyLine> cLineList, List<Border> borderList, Extent aBound, double[] contour) {
        List<Polygon> aPolygonList = new ArrayList<>(), newPolygonList = new ArrayList<>();
        List<BorderPoint> newBPList;
        List<BorderPoint> bPList = new ArrayList<>();
        List<PointD> PList = new ArrayList<>();
        Border aBorder;
        BorderLine aBLine;
        PointD aPoint;
        BorderPoint aBPoint;
        int i, j;
        List<PolyLine> lineList = new ArrayList<>();
        List<BorderPoint> aBorderList = new ArrayList<>();
        PolyLine aLine;
        Polygon aPolygon;
        IJPoint aijP;
        double aValue = 0;
        int[] pNums;

        //Borders loop
        for (i = 0; i < borderList.size(); i++) {
            aBorderList.clear();
            bPList.clear();
            lineList.clear();
            aPolygonList.clear();
            aBorder = borderList.get(i);
            if (aBorder.getLineNum() == 1) //The border has just one line
            {
                aBLine = aBorder.LineList.get(0);
                PList = aBLine.pointList;
                if (!isClockwise(PList)) //Make sure the point list is clockwise
                {
                    Collections.reverse(PList);
                }

                //Construct border point list
                for (j = 0; j < PList.size(); j++) {
                    aPoint = PList.get(j);
                    aBPoint = new BorderPoint();
                    aBPoint.Id = -1;
                    aBPoint.Point = aPoint;
                    aBPoint.Value = S0[aBLine.ijPointList.get(j).I][aBLine.ijPointList.get(j).J];
                    aBorderList.add(aBPoint);
                }

                //Find the contour lines of this border
                for (j = 0; j < cLineList.size(); j++) {
                    aLine = cLineList.get(j);
                    if (aLine.BorderIdx == i) {
                        lineList.add(aLine);    //Construct contour line list
                        //Construct border point list of the contour line
                        if (aLine.Type.equals("Border")) //The contour line with the start/end point on the border
                        {
                            aPoint = aLine.PointList.get(0);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                            aPoint = aLine.PointList.get(aLine.PointList.size() - 1);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                        }
                    }
                }

                if (lineList.isEmpty()) //No contour lines in this border, the polygon is the border
                {
                    //Judge the value of the polygon
                    aijP = aBLine.ijPointList.get(0);
                    aPolygon = new Polygon();
                    if (S0[aijP.I][aijP.J] < contour[0]) {
                        aValue = contour[0];
                        aPolygon.IsHighCenter = false;
                    } else {
                        for (j = contour.length - 1; j >= 0; j--) {
                            if (S0[aijP.I][aijP.J] > contour[j]) {
                                aValue = contour[j];
                                break;
                            }
                        }
                        aPolygon.IsHighCenter = true;
                    }
                    if (PList.size() > 0) {
                        aPolygon.HighValue = aValue;
                        aPolygon.LowValue = aValue;
                        aPolygon.Extent = new Extent();
                        aPolygon.Area = getExtentAndArea(PList, aPolygon.Extent);
                        aPolygon.StartPointIdx = 0;
                        aPolygon.IsClockWise = true;
                        aPolygon.OutLine.Type = "Border";
                        aPolygon.OutLine.Value = aValue;
                        aPolygon.OutLine.BorderIdx = i;
                        aPolygon.OutLine.PointList = PList;
                        aPolygonList.add(aPolygon);
                    }
                } else //Has contour lines in this border
                {
                    //Insert the border points of the contour lines to the border point list of the border
                    newBPList = insertPoint2Border(bPList, aBorderList);
                    //aPolygonList = TracingPolygons(lineList, newBPList, aBound, contour);
                    aPolygonList = tracingPolygons(lineList, newBPList, true);
                }
            } else //---- The border has holes
            {
                aBLine = aBorder.LineList.get(0);
                //Find the contour lines of this border
                for (j = 0; j < cLineList.size(); j++) {
                    aLine = cLineList.get(j);
                    if (aLine.BorderIdx == i) {
                        lineList.add(aLine);
                        if (aLine.Type.equals("Border")) {
                            aPoint = aLine.PointList.get(0);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                            aPoint = aLine.PointList.get(aLine.PointList.size() - 1);
                            aBPoint = new BorderPoint();
                            aBPoint.Id = lineList.size() - 1;
                            aBPoint.Point = aPoint;
                            aBPoint.Value = aLine.Value;
                            bPList.add(aBPoint);
                        }
                    }
                }
                if (lineList.isEmpty()) //No contour lines in this border, the polygon is the border and the holes
                {
                    aPolygon = new Polygon();
                    aijP = aBLine.ijPointList.get(0);
                    if (S0[aijP.I][aijP.J] < contour[0]) {
                        aValue = contour[0];
                        aPolygon.IsHighCenter = false;
                    } else {
                        for (j = contour.length - 1; j >= 0; j--) {
                            if (S0[aijP.I][aijP.J] > contour[j]) {
                                aValue = contour[j];
                                break;
                            }
                        }
                        aPolygon.IsHighCenter = true;
                    }
                    if (PList.size() > 0) {
                        aPolygon.HighValue = aValue;
                        aPolygon.LowValue = aValue;
                        aPolygon.Area = getExtentAndArea(PList, aPolygon.Extent);
                        aPolygon.StartPointIdx = 0;
                        aPolygon.IsClockWise = true;
                        aPolygon.OutLine.Type = "Border";
                        aPolygon.OutLine.Value = aValue;
                        aPolygon.OutLine.BorderIdx = i;
                        aPolygon.OutLine.PointList = PList;
                        aPolygonList.add(aPolygon);
                    }
                } else {
                    pNums = new int[aBorder.getLineNum()];
                    newBPList = insertPoint2Border_Ring(S0, bPList, aBorder, pNums);
                    aPolygonList = tracingPolygons_Ring(lineList, newBPList, aBorder, contour, pNums);
                    //aPolygonList = TracingPolygons(lineList, newBPList, contour);
                }
            }
            newPolygonList.addAll(aPolygonList);
        }

        return newPolygonList;
    }

    /**
     * Judge if a point is in a polygon
     *
     * @param poly polygon border
     * @param aPoint point
     * @return if the point is in the polygon
     */
    public static boolean pointInPolygon(List<PointD> poly, PointD aPoint) {
        double xNew, yNew, xOld, yOld;
        double x1, y1, x2, y2;
        int i;
        boolean inside = false;
        int nPoints = poly.size();

        if (nPoints < 3) {
            return false;
        }

        xOld = poly.get(nPoints - 1).X;
        yOld = poly.get(nPoints - 1).Y;
        for (i = 0; i < nPoints; i++) {
            xNew = poly.get(i).X;
            yNew = poly.get(i).Y;
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                y1 = yOld;
                y2 = yNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                y1 = yNew;
                y2 = yOld;
            }

            //---- edge "open" at left end
            if ((xNew < aPoint.X) == (aPoint.X <= xOld)
                    && (aPoint.Y - y1) * (x2 - x1) < (y2 - y1) * (aPoint.X - x1)) {
                inside = !inside;
            }

            xOld = xNew;
            yOld = yNew;
        }

        return inside;
    }

    /**
     * Judge if a point is in a polygon
     *
     * @param aPolygon polygon
     * @param aPoint point
     * @return if the point is in the polygon
     */
    public static boolean pointInPolygon(Polygon aPolygon, PointD aPoint) {
        if (aPolygon.HasHoles()) {
            boolean isIn = pointInPolygon(aPolygon.OutLine.PointList, aPoint);
            if (isIn) {
                for (PolyLine aLine : aPolygon.HoleLines) {
                    if (pointInPolygon(aLine.PointList, aPoint)) {
                        isIn = false;
                        break;
                    }
                }
            }

            return isIn;
        } else {
            return pointInPolygon(aPolygon.OutLine.PointList, aPoint);
        }
    }

    /**
     * Clip polylines with a border polygon
     *
     * @param polylines polyline list
     * @param clipPList clipping border point list
     * @return clipped polylines
     */
    public static List<PolyLine> clipPolylines(List<PolyLine> polylines, List<PointD> clipPList) {
        List<PolyLine> newPolylines = new ArrayList<>();
        for (PolyLine aPolyline : polylines) {
            newPolylines.addAll(cutPolyline(aPolyline, clipPList));
        }

        return newPolylines;
    }

    /**
     * Clip polygons with a border polygon
     *
     * @param polygons polygon list
     * @param clipPList clipping border point list
     * @return clipped polygons
     */
    public static List<Polygon> clipPolygons(List<Polygon> polygons, List<PointD> clipPList) {
        List<Polygon> newPolygons = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            Polygon aPolygon = polygons.get(i);
            if (aPolygon.HasHoles()) {
                newPolygons.addAll(cutPolygon_Hole(aPolygon, clipPList));
            } else {
                newPolygons.addAll(cutPolygon(aPolygon, clipPList));
            }
        }

        //Sort polygons with bording rectangle area
        List<Polygon> outPolygons = new ArrayList<>();
        boolean isInserted;
        for (int i = 0; i < newPolygons.size(); i++) {
            Polygon aPolygon = newPolygons.get(i);
            isInserted = false;
            for (int j = 0; j < outPolygons.size(); j++) {
                if (aPolygon.Area > outPolygons.get(j).Area) {
                    outPolygons.add(j, aPolygon);
                    isInserted = true;
                    break;
                }
            }

            if (!isInserted) {
                outPolygons.add(aPolygon);
            }
        }

        return outPolygons;
    }

    // </editor-fold>
    // <editor-fold desc="Private contour methods">
    private static boolean traceBorder(int[][] S1, int i1, int i2, int j1, int j2, int[] ij3) {
        boolean canTrace = true;
        int a, b, c, d;
        if (i1 < i2) //---- Trace from bottom
        {
            if (S1[i2][j2 - 1] == 1 && S1[i2][j2 + 1] == 1) {
                a = S1[i2 - 1][j2 - 1];
                b = S1[i2 + 1][j2];
                c = S1[i2 + 1][j2 - 1];
                if ((a != 0 && b == 0) || (a == 0 && b != 0 && c != 0)) {
                    ij3[0] = i2;
                    ij3[1] = j2 - 1;
                } else {
                    ij3[0] = i2;
                    ij3[1] = j2 + 1;
                }
            } else if (S1[i2][j2 - 1] == 1 && S1[i2 + 1][j2] == 1) {
                a = S1[i2 + 1][j2 - 1];
                b = S1[i2 + 1][j2 + 1];
                c = S1[i2][j2 - 1];
                d = S1[i2][j2 + 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2;
                        ij3[1] = j2 - 1;
                    } else {
                        ij3[0] = i2 + 1;
                        ij3[1] = j2;
                    }
                } else {
                    ij3[0] = i2;
                    ij3[1] = j2 - 1;
                }
            } else if (S1[i2][j2 + 1] == 1 && S1[i2 + 1][j2] == 1) {
                a = S1[i2 + 1][j2 - 1];
                b = S1[i2 + 1][j2 + 1];
                c = S1[i2][j2 - 1];
                d = S1[i2][j2 + 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2;
                        ij3[1] = j2 + 1;
                    } else {
                        ij3[0] = i2 + 1;
                        ij3[1] = j2;
                    }
                } else {
                    ij3[0] = i2;
                    ij3[1] = j2 + 1;
                }
            } else if (S1[i2][j2 - 1] == 1) {
                ij3[0] = i2;
                ij3[1] = j2 - 1;
            } else if (S1[i2][j2 + 1] == 1) {
                ij3[0] = i2;
                ij3[1] = j2 + 1;
            } else if (S1[i2 + 1][j2] == 1) {
                ij3[0] = i2 + 1;
                ij3[1] = j2;
            } else {
                canTrace = false;
            }
        } else if (j1 < j2) //---- Trace from left
        {
            if (S1[i2 + 1][j2] == 1 && S1[i2 - 1][j2] == 1) {
                a = S1[i2 + 1][j2 - 1];
                b = S1[i2][j2 + 1];
                c = S1[i2 + 1][j2 + 1];
                if ((a != 0 && b == 0) || (a == 0 && b != 0 && c != 0)) {
                    ij3[0] = i2 + 1;
                    ij3[1] = j2;
                } else {
                    ij3[0] = i2 - 1;
                    ij3[1] = j2;
                }
            } else if (S1[i2 + 1][j2] == 1 && S1[i2][j2 + 1] == 1) {
                c = S1[i2 - 1][j2];
                d = S1[i2 + 1][j2];
                a = S1[i2 - 1][j2 + 1];
                b = S1[i2 + 1][j2 + 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2 + 1;
                        ij3[1] = j2;
                    } else {
                        ij3[0] = i2;
                        ij3[1] = j2 + 1;
                    }
                } else {
                    ij3[0] = i2 + 1;
                    ij3[1] = j2;
                }
            } else if (S1[i2 - 1][j2] == 1 && S1[i2][j2 + 1] == 1) {
                c = S1[i2 - 1][j2];
                d = S1[i2 + 1][j2];
                a = S1[i2 - 1][j2 + 1];
                b = S1[i2 + 1][j2 + 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2 - 1;
                        ij3[1] = j2;
                    } else {
                        ij3[0] = i2;
                        ij3[1] = j2 + 1;
                    }
                } else {
                    ij3[0] = i2 - 1;
                    ij3[1] = j2;
                }
            } else if (S1[i2 + 1][j2] == 1) {
                ij3[0] = i2 + 1;
                ij3[1] = j2;
            } else if (S1[i2 - 1][j2] == 1) {
                ij3[0] = i2 - 1;
                ij3[1] = j2;
            } else if (S1[i2][j2 + 1] == 1) {
                ij3[0] = i2;
                ij3[1] = j2 + 1;
            } else {
                canTrace = false;
            }
        } else if (i1 > i2) //---- Trace from top
        {
            if (S1[i2][j2 - 1] == 1 && S1[i2][j2 + 1] == 1) {
                a = S1[i2 + 1][j2 - 1];
                b = S1[i2 - 1][j2];
                c = S1[i2 - 1][j2 + 1];
                if ((a != 0 && b == 0) || (a == 0 && b != 0 && c != 0)) {
                    ij3[0] = i2;
                    ij3[1] = j2 - 1;
                } else {
                    ij3[0] = i2;
                    ij3[1] = j2 + 1;
                }
            } else if (S1[i2][j2 - 1] == 1 && S1[i2 - 1][j2] == 1) {
                a = S1[i2 - 1][j2 - 1];
                b = S1[i2 - 1][j2 + 1];
                c = S1[i2][j2 - 1];
                d = S1[i2][j2 + 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2;
                        ij3[1] = j2 - 1;
                    } else {
                        ij3[0] = i2 - 1;
                        ij3[1] = j2;
                    }
                } else {
                    ij3[0] = i2;
                    ij3[1] = j2 - 1;
                }
            } else if (S1[i2][j2 + 1] == 1 && S1[i2 - 1][j2] == 1) {
                a = S1[i2 - 1][j2 - 1];
                b = S1[i2 - 1][j2 + 1];
                c = S1[i2][j2 - 1];
                d = S1[i2][j2 + 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2;
                        ij3[1] = j2 + 1;
                    } else {
                        ij3[0] = i2 - 1;
                        ij3[1] = j2;
                    }
                } else {
                    ij3[0] = i2;
                    ij3[1] = j2 + 1;
                }
            } else if (S1[i2][j2 - 1] == 1) {
                ij3[0] = i2;
                ij3[1] = j2 - 1;
            } else if (S1[i2][j2 + 1] == 1) {
                ij3[0] = i2;
                ij3[1] = j2 + 1;
            } else if (S1[i2 - 1][j2] == 1) {
                ij3[0] = i2 - 1;
                ij3[1] = j2;
            } else {
                canTrace = false;
            }
        } else if (j1 > j2) //---- Trace from right
        {
            if (S1[i2 + 1][j2] == 1 && S1[i2 - 1][j2] == 1) {
                a = S1[i2 + 1][j2 + 1];
                b = S1[i2][j2 - 1];
                c = S1[i2 - 1][j2 - 1];
                if ((a != 0 && b == 0) || (a == 0 && b != 0 && c != 0)) {
                    ij3[0] = i2 + 1;
                    ij3[1] = j2;
                } else {
                    ij3[0] = i2 - 1;
                    ij3[1] = j2;
                }
            } else if (S1[i2 + 1][j2] == 1 && S1[i2][j2 - 1] == 1) {
                c = S1[i2 - 1][j2];
                d = S1[i2 + 1][j2];
                a = S1[i2 - 1][j2 - 1];
                b = S1[i2 + 1][j2 - 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2 + 1;
                        ij3[1] = j2;
                    } else {
                        ij3[0] = i2;
                        ij3[1] = j2 - 1;
                    }
                } else {
                    ij3[0] = i2 + 1;
                    ij3[1] = j2;
                }
            } else if (S1[i2 - 1][j2] == 1 && S1[i2][j2 - 1] == 1) {
                c = S1[i2 - 1][j2];
                d = S1[i2 + 1][j2];
                a = S1[i2 - 1][j2 - 1];
                b = S1[i2 + 1][j2 - 1];
                if (a == 0 || b == 0 || c == 0 || d == 0) {
                    if ((a == 0 && d == 0) || (b == 0 && c == 0)) {
                        ij3[0] = i2 - 1;
                        ij3[1] = j2;
                    } else {
                        ij3[0] = i2;
                        ij3[1] = j2 - 1;
                    }
                } else {
                    ij3[0] = i2 - 1;
                    ij3[1] = j2;
                }
            } else if (S1[i2 + 1][j2] == 1) {
                ij3[0] = i2 + 1;
                ij3[1] = j2;
            } else if (S1[i2 - 1][j2] == 1) {
                ij3[0] = i2 - 1;
                ij3[1] = j2;
            } else if (S1[i2][j2 - 1] == 1) {
                ij3[0] = i2;
                ij3[1] = j2 - 1;
            } else {
                canTrace = false;
            }
        }

        return canTrace;
    }

    private static boolean traceIsoline_UndefData(int i1, int i2, double[][] H, double[][] S, int j1, int j2, double[] X,
            double[] Y, double a2x, int[] ij3, double[] a3xy, boolean[] IsS) {
        boolean canTrace = true;
        double a3x = 0, a3y = 0;
        int i3 = 0, j3 = 0;
        boolean isS = true;
        if (i1 < i2) //---- Trace from bottom
        {
            if (H[i2][j2] != -2 && H[i2][j2 + 1] != -2) {
                if (H[i2][j2] < H[i2][j2 + 1]) {
                    a3x = X[j2];
                    a3y = Y[i2] + H[i2][j2] * (Y[i2 + 1] - Y[i2]);
                    i3 = i2;
                    j3 = j2;
                    H[i3][j3] = -2;
                    isS = false;
                } else {
                    a3x = X[j2 + 1];
                    a3y = Y[i2] + H[i2][j2 + 1] * (Y[i2 + 1] - Y[i2]);
                    i3 = i2;
                    j3 = j2 + 1;
                    H[i3][j3] = -2;
                    isS = false;
                }
            } else if (H[i2][j2] != -2 && H[i2][j2 + 1] == -2) {
                a3x = X[j2];
                a3y = Y[i2] + H[i2][j2] * (Y[i2 + 1] - Y[i2]);
                i3 = i2;
                j3 = j2;
                H[i3][j3] = -2;
                isS = false;
            } else if (H[i2][j2] == -2 && H[i2][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2] + H[i2][j2 + 1] * (Y[i2 + 1] - Y[i2]);
                i3 = i2;
                j3 = j2 + 1;
                H[i3][j3] = -2;
                isS = false;
            } else if (S[i2 + 1][j2] != -2) {
                a3x = X[j2] + S[i2 + 1][j2] * (X[j2 + 1] - X[j2]);
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else {
                canTrace = false;
            }
        } else if (j1 < j2) //---- Trace from left
        {
            if (S[i2][j2] != -2 && S[i2 + 1][j2] != -2) {
                if (S[i2][j2] < S[i2 + 1][j2]) {
                    a3x = X[j2] + S[i2][j2] * (X[j2 + 1] - X[j2]);
                    a3y = Y[i2];
                    i3 = i2;
                    j3 = j2;
                    S[i3][j3] = -2;
                    isS = true;
                } else {
                    a3x = X[j2] + S[i2 + 1][j2] * (X[j2 + 1] - X[j2]);
                    a3y = Y[i2 + 1];
                    i3 = i2 + 1;
                    j3 = j2;
                    S[i3][j3] = -2;
                    isS = true;
                }
            } else if (S[i2][j2] != -2 && S[i2 + 1][j2] == -2) {
                a3x = X[j2] + S[i2][j2] * (X[j2 + 1] - X[j2]);
                a3y = Y[i2];
                i3 = i2;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else if (S[i2][j2] == -2 && S[i2 + 1][j2] != -2) {
                a3x = X[j2] + S[i2 + 1][j2] * (X[j2 + 1] - X[j2]);
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else if (H[i2][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2] + H[i2][j2 + 1] * (Y[i2 + 1] - Y[i2]);
                i3 = i2;
                j3 = j2 + 1;
                H[i3][j3] = -2;
                isS = false;
            } else {
                canTrace = false;
            }

        } else if (X[j2] < a2x) //---- Trace from top
        {
            if (H[i2 - 1][j2] != -2 && H[i2 - 1][j2 + 1] != -2) {
                if (H[i2 - 1][j2] > H[i2 - 1][j2 + 1]) //---- < changed to >
                {
                    a3x = X[j2];
                    a3y = Y[i2 - 1] + H[i2 - 1][j2] * (Y[i2] - Y[i2 - 1]);
                    i3 = i2 - 1;
                    j3 = j2;
                    H[i3][j3] = -2;
                    isS = false;
                } else {
                    a3x = X[j2 + 1];
                    a3y = Y[i2 - 1] + H[i2 - 1][j2 + 1] * (Y[i2] - Y[i2 - 1]);
                    i3 = i2 - 1;
                    j3 = j2 + 1;
                    H[i3][j3] = -2;
                    isS = false;
                }
            } else if (H[i2 - 1][j2] != -2 && H[i2 - 1][j2 + 1] == -2) {
                a3x = X[j2];
                a3y = Y[i2 - 1] + H[i2 - 1][j2] * (Y[i2] - Y[i2 - 1]);
                i3 = i2 - 1;
                j3 = j2;
                H[i3][j3] = -2;
                isS = false;
            } else if (H[i2 - 1][j2] == -2 && H[i2 - 1][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2 - 1] + H[i2 - 1][j2 + 1] * (Y[i2] - Y[i2 - 1]);
                i3 = i2 - 1;
                j3 = j2 + 1;
                H[i3][j3] = -2;
                isS = false;
            } else if (S[i2 - 1][j2] != -2) {
                a3x = X[j2] + S[i2 - 1][j2] * (X[j2 + 1] - X[j2]);
                a3y = Y[i2 - 1];
                i3 = i2 - 1;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else {
                canTrace = false;
            }
        } else //---- Trace from right
        {
            if (S[i2 + 1][j2 - 1] != -2 && S[i2][j2 - 1] != -2) {
                if (S[i2 + 1][j2 - 1] > S[i2][j2 - 1]) //---- < changed to >
                {
                    a3x = X[j2 - 1] + S[i2 + 1][j2 - 1] * (X[j2] - X[j2 - 1]);
                    a3y = Y[i2 + 1];
                    i3 = i2 + 1;
                    j3 = j2 - 1;
                    S[i3][j3] = -2;
                    isS = true;
                } else {
                    a3x = X[j2 - 1] + S[i2][j2 - 1] * (X[j2] - X[j2 - 1]);
                    a3y = Y[i2];
                    i3 = i2;
                    j3 = j2 - 1;
                    S[i3][j3] = -2;
                    isS = true;
                }
            } else if (S[i2 + 1][j2 - 1] != -2 && S[i2][j2 - 1] == -2) {
                a3x = X[j2 - 1] + S[i2 + 1][j2 - 1] * (X[j2] - X[j2 - 1]);
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2 - 1;
                S[i3][j3] = -2;
                isS = true;
            } else if (S[i2 + 1][j2 - 1] == -2 && S[i2][j2 - 1] != -2) {
                a3x = X[j2 - 1] + S[i2][j2 - 1] * (X[j2] - X[j2 - 1]);
                a3y = Y[i2];
                i3 = i2;
                j3 = j2 - 1;
                S[i3][j3] = -2;
                isS = true;
            } else if (H[i2][j2 - 1] != -2) {
                a3x = X[j2 - 1];
                a3y = Y[i2] + H[i2][j2 - 1] * (Y[i2 + 1] - Y[i2]);
                i3 = i2;
                j3 = j2 - 1;
                H[i3][j3] = -2;
                isS = false;
            } else {
                canTrace = false;
            }
        }

        ij3[0] = i3;
        ij3[1] = j3;
        a3xy[0] = a3x;
        a3xy[1] = a3y;
        IsS[0] = isS;

        return canTrace;
    }

    private static boolean traceIsoline_UndefData_bak(int i1, int i2, double[][] H, double[][] S, int j1, int j2, double[] X,
            double[] Y, double nx, double ny, double a2x, int[] ij3, double[] a3xy, boolean[] IsS) {
        boolean canTrace = true;
        double a3x = 0, a3y = 0;
        int i3 = 0, j3 = 0;
        boolean isS = true;
        if (i1 < i2) //---- Trace from bottom
        {
            if (H[i2][j2] != -2 && H[i2][j2 + 1] != -2) {
                if (H[i2][j2] < H[i2][j2 + 1]) {
                    a3x = X[j2];
                    a3y = Y[i2] + H[i2][j2] * ny;
                    i3 = i2;
                    j3 = j2;
                    H[i3][j3] = -2;
                    isS = false;
                } else {
                    a3x = X[j2 + 1];
                    a3y = Y[i2] + H[i2][j2 + 1] * ny;
                    i3 = i2;
                    j3 = j2 + 1;
                    H[i3][j3] = -2;
                    isS = false;
                }
            } else if (H[i2][j2] != -2 && H[i2][j2 + 1] == -2) {
                a3x = X[j2];
                a3y = Y[i2] + H[i2][j2] * ny;
                i3 = i2;
                j3 = j2;
                H[i3][j3] = -2;
                isS = false;
            } else if (H[i2][j2] == -2 && H[i2][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2] + H[i2][j2 + 1] * ny;
                i3 = i2;
                j3 = j2 + 1;
                H[i3][j3] = -2;
                isS = false;
            } else if (S[i2 + 1][j2] != -2) {
                a3x = X[j2] + S[i2 + 1][j2] * nx;
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else {
                canTrace = false;
            }
        } else if (j1 < j2) //---- Trace from left
        {
            if (S[i2][j2] != -2 && S[i2 + 1][j2] != -2) {
                if (S[i2][j2] < S[i2 + 1][j2]) {
                    a3x = X[j2] + S[i2][j2] * nx;
                    a3y = Y[i2];
                    i3 = i2;
                    j3 = j2;
                    S[i3][j3] = -2;
                    isS = true;
                } else {
                    a3x = X[j2] + S[i2 + 1][j2] * nx;
                    a3y = Y[i2 + 1];
                    i3 = i2 + 1;
                    j3 = j2;
                    S[i3][j3] = -2;
                    isS = true;
                }
            } else if (S[i2][j2] != -2 && S[i2 + 1][j2] == -2) {
                a3x = X[j2] + S[i2][j2] * nx;
                a3y = Y[i2];
                i3 = i2;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else if (S[i2][j2] == -2 && S[i2 + 1][j2] != -2) {
                a3x = X[j2] + S[i2 + 1][j2] * nx;
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else if (H[i2][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2] + H[i2][j2 + 1] * ny;
                i3 = i2;
                j3 = j2 + 1;
                H[i3][j3] = -2;
                isS = false;
            } else {
                canTrace = false;
            }

        } else if (X[j2] < a2x) //---- Trace from top
        {
            if (H[i2 - 1][j2] != -2 && H[i2 - 1][j2 + 1] != -2) {
                if (H[i2 - 1][j2] > H[i2 - 1][j2 + 1]) //---- < changed to >
                {
                    a3x = X[j2];
                    a3y = Y[i2 - 1] + H[i2 - 1][j2] * ny;
                    i3 = i2 - 1;
                    j3 = j2;
                    H[i3][j3] = -2;
                    isS = false;
                } else {
                    a3x = X[j2 + 1];
                    a3y = Y[i2 - 1] + H[i2 - 1][j2 + 1] * ny;
                    i3 = i2 - 1;
                    j3 = j2 + 1;
                    H[i3][j3] = -2;
                    isS = false;
                }
            } else if (H[i2 - 1][j2] != -2 && H[i2 - 1][j2 + 1] == -2) {
                a3x = X[j2];
                a3y = Y[i2 - 1] + H[i2 - 1][j2] * ny;
                i3 = i2 - 1;
                j3 = j2;
                H[i3][j3] = -2;
                isS = false;
            } else if (H[i2 - 1][j2] == -2 && H[i2 - 1][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2 - 1] + H[i2 - 1][j2 + 1] * ny;
                i3 = i2 - 1;
                j3 = j2 + 1;
                H[i3][j3] = -2;
                isS = false;
            } else if (S[i2 - 1][j2] != -2) {
                a3x = X[j2] + S[i2 - 1][j2] * nx;
                a3y = Y[i2 - 1];
                i3 = i2 - 1;
                j3 = j2;
                S[i3][j3] = -2;
                isS = true;
            } else {
                canTrace = false;
            }
        } else //---- Trace from right
        {
            if (S[i2 + 1][j2 - 1] != -2 && S[i2][j2 - 1] != -2) {
                if (S[i2 + 1][j2 - 1] > S[i2][j2 - 1]) //---- < changed to >
                {
                    a3x = X[j2 - 1] + S[i2 + 1][j2 - 1] * nx;
                    a3y = Y[i2 + 1];
                    i3 = i2 + 1;
                    j3 = j2 - 1;
                    S[i3][j3] = -2;
                    isS = true;
                } else {
                    a3x = X[j2 - 1] + S[i2][j2 - 1] * nx;
                    a3y = Y[i2];
                    i3 = i2;
                    j3 = j2 - 1;
                    S[i3][j3] = -2;
                    isS = true;
                }
            } else if (S[i2 + 1][j2 - 1] != -2 && S[i2][j2 - 1] == -2) {
                a3x = X[j2 - 1] + S[i2 + 1][j2 - 1] * nx;
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2 - 1;
                S[i3][j3] = -2;
                isS = true;
            } else if (S[i2 + 1][j2 - 1] == -2 && S[i2][j2 - 1] != -2) {
                a3x = X[j2 - 1] + S[i2][j2 - 1] * nx;
                a3y = Y[i2];
                i3 = i2;
                j3 = j2 - 1;
                S[i3][j3] = -2;
                isS = true;
            } else if (H[i2][j2 - 1] != -2) {
                a3x = X[j2 - 1];
                a3y = Y[i2] + H[i2][j2 - 1] * ny;
                i3 = i2;
                j3 = j2 - 1;
                H[i3][j3] = -2;
                isS = false;
            } else {
                canTrace = false;
            }
        }

        ij3[0] = i3;
        ij3[1] = j3;
        a3xy[0] = a3x;
        a3xy[1] = a3y;
        IsS[0] = isS;

        return canTrace;
    }

    private static List<PolyLine> isoline_UndefData(double[][] S0, double[] X, double[] Y,
            double W, double[][] S, double[][] H, int[][][] SB, int[][][] HB, int lineNum) {

        List<PolyLine> cLineList = new ArrayList<>();
        int m, n, i, j;
        m = S0.length;
        n = S0[0].length;

        int i1, i2, j1, j2, i3 = 0, j3 = 0;
        double a2x, a2y, a3x = 0, a3y = 0, sx, sy;
        PointD aPoint;
        PolyLine aLine;
        List<PointD> pList;
        boolean isS = true;
        EndPoint aEndPoint = new EndPoint();
        //---- Tracing from border
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                if (j < n - 1) {
                    if (SB[0][i][j] > -1) //---- Border
                    {
                        if (S[i][j] != -2) {
                            pList = new ArrayList<>();
                            i2 = i;
                            j2 = j;
                            a2x = X[j2] + S[i2][j2] * (X[j2 + 1] - X[j2]);    //---- x of first point
                            a2y = Y[i2];                   //---- y of first point
                            if (SB[1][i][j] == 0) //---- Bottom border
                            {
                                i1 = -1;
                                aEndPoint.sPoint.X = X[j + 1];
                                aEndPoint.sPoint.Y = Y[i];
                            } else {
                                i1 = i2;
                                aEndPoint.sPoint.X = X[j];
                                aEndPoint.sPoint.Y = Y[i];
                            }
                            j1 = j2;
                            aPoint = new PointD();
                            aPoint.X = a2x;
                            aPoint.Y = a2y;
                            pList.add(aPoint);

                            aEndPoint.Index = lineNum + cLineList.size();
                            aEndPoint.Point = aPoint;
                            aEndPoint.BorderIdx = SB[0][i][j];
                            _endPointList.add(aEndPoint);

                            aLine = new PolyLine();
                            aLine.Type = "Border";
                            aLine.BorderIdx = SB[0][i][j];
                            while (true) {
                                int[] ij3 = {i3, j3};
                                double[] a3xy = {a3x, a3y};
                                boolean[] IsS = {isS};
                                if (traceIsoline_UndefData(i1, i2, H, S, j1, j2, X, Y, a2x, ij3, a3xy, IsS)) {
                                    i3 = ij3[0];
                                    j3 = ij3[1];
                                    a3x = a3xy[0];
                                    a3y = a3xy[1];
                                    isS = IsS[0];
                                    aPoint = new PointD();
                                    aPoint.X = a3x;
                                    aPoint.Y = a3y;
                                    pList.add(aPoint);
                                    if (isS) {
                                        if (SB[0][i3][j3] > -1) {
                                            if (SB[1][i3][j3] == 0) {
                                                aEndPoint.sPoint.X = X[j3 + 1];
                                                aEndPoint.sPoint.Y = Y[i3];
                                            } else {
                                                aEndPoint.sPoint.X = X[j3];
                                                aEndPoint.sPoint.Y = Y[i3];
                                            }
                                            break;
                                        }
                                    } else if (HB[0][i3][j3] > -1) {
                                        if (HB[1][i3][j3] == 0) {
                                            aEndPoint.sPoint.X = X[j3];
                                            aEndPoint.sPoint.Y = Y[i3];
                                        } else {
                                            aEndPoint.sPoint.X = X[j3];
                                            aEndPoint.sPoint.Y = Y[i3 + 1];
                                        }
                                        break;
                                    }
                                    a2x = a3x;
                                    //a2y = a3y;
                                    i1 = i2;
                                    j1 = j2;
                                    i2 = i3;
                                    j2 = j3;
                                } else {
                                    aLine.Type = "Error";
                                    break;
                                }
                            }
                            S[i][j] = -2;
                            if (pList.size() > 1 && !aLine.Type.equals("Error")) {
                                aEndPoint.Point = aPoint;
                                _endPointList.add(aEndPoint);

                                aLine.Value = W;
                                aLine.PointList = pList;
                                cLineList.add(aLine);
                            } else {
                                _endPointList.remove(_endPointList.size() - 1);
                            }

                        }
                    }
                }
                if (i < m - 1) {
                    if (HB[0][i][j] > -1) //---- Border
                    {
                        if (H[i][j] != -2) {
                            pList = new ArrayList<>();
                            i2 = i;
                            j2 = j;
                            a2x = X[j2];
                            a2y = Y[i2] + H[i2][j2] * (Y[i2 + 1] - Y[i2]);
                            i1 = i2;
                            if (HB[1][i][j] == 0) {
                                j1 = -1;
                                aEndPoint.sPoint.X = X[j];
                                aEndPoint.sPoint.Y = Y[i];
                            } else {
                                j1 = j2;
                                aEndPoint.sPoint.X = X[j];
                                aEndPoint.sPoint.Y = Y[i + 1];
                            }
                            aPoint = new PointD();
                            aPoint.X = a2x;
                            aPoint.Y = a2y;
                            pList.add(aPoint);

                            aEndPoint.Index = lineNum + cLineList.size();
                            aEndPoint.Point = aPoint;
                            aEndPoint.BorderIdx = HB[0][i][j];
                            _endPointList.add(aEndPoint);

                            aLine = new PolyLine();
                            aLine.Type = "Border";
                            aLine.BorderIdx = HB[0][i][j];
                            while (true) {
                                int[] ij3 = {i3, j3};
                                double[] a3xy = {a3x, a3y};
                                boolean[] IsS = {isS};
                                if (traceIsoline_UndefData(i1, i2, H, S, j1, j2, X, Y, a2x, ij3, a3xy, IsS)) {
                                    i3 = ij3[0];
                                    j3 = ij3[1];
                                    a3x = a3xy[0];
                                    a3y = a3xy[1];
                                    isS = IsS[0];
                                    aPoint = new PointD();
                                    aPoint.X = a3x;
                                    aPoint.Y = a3y;
                                    pList.add(aPoint);
                                    if (isS) {
                                        if (SB[0][i3][j3] > -1) {
                                            if (SB[1][i3][j3] == 0) {
                                                aEndPoint.sPoint.X = X[j3 + 1];
                                                aEndPoint.sPoint.Y = Y[i3];
                                            } else {
                                                aEndPoint.sPoint.X = X[j3];
                                                aEndPoint.sPoint.Y = Y[i3];
                                            }
                                            break;
                                        }
                                    } else if (HB[0][i3][j3] > -1) {
                                        if (HB[1][i3][j3] == 0) {
                                            aEndPoint.sPoint.X = X[j3];
                                            aEndPoint.sPoint.Y = Y[i3];
                                        } else {
                                            aEndPoint.sPoint.X = X[j3];
                                            aEndPoint.sPoint.Y = Y[i3 + 1];
                                        }
                                        break;
                                    }
                                    a2x = a3x;
                                    //a2y = a3y;
                                    i1 = i2;
                                    j1 = j2;
                                    i2 = i3;
                                    j2 = j3;
                                } else {
                                    aLine.Type = "Error";
                                    break;
                                }
                            }
                            H[i][j] = -2;
                            if (pList.size() > 1 && !aLine.Type.equals("Error")) {
                                aEndPoint.Point = aPoint;
                                _endPointList.add(aEndPoint);

                                aLine.Value = W;
                                aLine.PointList = pList;
                                cLineList.add(aLine);
                            } else {
                                _endPointList.remove(_endPointList.size() - 1);
                            }

                        }
                    }
                }
            }
        }

        //---- Clear border points
        for (j = 0; j < n - 1; j++) {
            if (S[0][j] != -2) {
                S[0][j] = -2;
            }
            if (S[m - 1][j] != -2) {
                S[m - 1][j] = -2;
            }
        }

        for (i = 0; i < m - 1; i++) {
            if (H[i][0] != -2) {
                H[i][0] = -2;
            }
            if (H[i][n - 1] != -2) {
                H[i][n - 1] = -2;
            }
        }

        //---- Tracing close lines
        for (i = 1; i < m - 2; i++) {
            for (j = 1; j < n - 1; j++) {
                if (H[i][j] != -2) {
                    List<PointD> pointList = new ArrayList<>();
                    i2 = i;
                    j2 = j;
                    a2x = X[j2];
                    a2y = Y[i] + H[i][j2] * (Y[i + 1] - Y[i]);
                    j1 = -1;
                    i1 = i2;
                    sx = a2x;
                    sy = a2y;
                    aPoint = new PointD();
                    aPoint.X = a2x;
                    aPoint.Y = a2y;
                    pointList.add(aPoint);
                    aLine = new PolyLine();
                    aLine.Type = "Close";

                    while (true) {
                        int[] ij3 = new int[2];
                        double[] a3xy = new double[2];
                        boolean[] IsS = new boolean[1];
                        if (traceIsoline_UndefData(i1, i2, H, S, j1, j2, X, Y, a2x, ij3, a3xy, IsS)) {
                            i3 = ij3[0];
                            j3 = ij3[1];
                            a3x = a3xy[0];
                            a3y = a3xy[1];
                            //isS = IsS[0];
                            aPoint = new PointD();
                            aPoint.X = a3x;
                            aPoint.Y = a3y;
                            pointList.add(aPoint);
                            if (Math.abs(a3y - sy) < 0.000001 && Math.abs(a3x - sx) < 0.000001) {
                                break;
                            }

                            a2x = a3x;
                            //a2y = a3y;
                            i1 = i2;
                            j1 = j2;
                            i2 = i3;
                            j2 = j3;
                            //If X[j2] < a2x && i2 = 0 )
                            //    aLine.type = "Error"
                            //    Exit Do
                            //End If
                        } else {
                            aLine.Type = "Error";
                            break;
                        }
                    }
                    H[i][j] = -2;
                    if (pointList.size() > 1 && !aLine.Type.equals("Error")) {
                        aLine.Value = W;
                        aLine.PointList = pointList;
                        cLineList.add(aLine);
                    }
                }
            }
        }

        for (i = 1; i < m - 1; i++) {
            for (j = 1; j < n - 2; j++) {
                if (S[i][j] != -2) {
                    List<PointD> pointList = new ArrayList<>();
                    i2 = i;
                    j2 = j;
                    a2x = X[j2] + S[i][j] * (X[j2 + 1] - X[j2]);
                    a2y = Y[i];
                    j1 = j2;
                    i1 = -1;
                    sx = a2x;
                    sy = a2y;
                    aPoint = new PointD();
                    aPoint.X = a2x;
                    aPoint.Y = a2y;
                    pointList.add(aPoint);
                    aLine = new PolyLine();
                    aLine.Type = "Close";

                    while (true) {
                        int[] ij3 = new int[2];
                        double[] a3xy = new double[2];
                        boolean[] IsS = new boolean[1];
                        if (traceIsoline_UndefData(i1, i2, H, S, j1, j2, X, Y, a2x, ij3, a3xy, IsS)) {
                            i3 = ij3[0];
                            j3 = ij3[1];
                            a3x = a3xy[0];
                            a3y = a3xy[1];
                            //isS = IsS[0];
                            aPoint = new PointD();
                            aPoint.X = a3x;
                            aPoint.Y = a3y;
                            pointList.add(aPoint);
                            if (Math.abs(a3y - sy) < 0.000001 && Math.abs(a3x - sx) < 0.000001) {
                                break;
                            }

                            a2x = a3x;
                            //a2y = a3y;
                            i1 = i2;
                            j1 = j2;
                            i2 = i3;
                            j2 = j3;
                        } else {
                            aLine.Type = "Error";
                            break;
                        }
                    }
                    S[i][j] = -2;
                    if (pointList.size() > 1 && !aLine.Type.equals("Error")) {
                        aLine.Value = W;
                        aLine.PointList = pointList;
                        cLineList.add(aLine);
                    }
                }
            }
        }

        return cLineList;
    }

    private static Object[] traceIsoline(int i1, int i2, double[][] H, double[][] S, int j1, int j2, double[] X,
            double[] Y, double nx, double ny, double a2x) {
        int i3, j3;
        double a3x, a3y;
        if (i1 < i2) //---- Trace from bottom
        {
            if (H[i2][j2] != -2 && H[i2][j2 + 1] != -2) {
                if (H[i2][j2] < H[i2][j2 + 1]) {
                    a3x = X[j2];
                    a3y = Y[i2] + H[i2][j2] * ny;
                    i3 = i2;
                    j3 = j2;
                    H[i3][j3] = -2;
                } else {
                    a3x = X[j2 + 1];
                    a3y = Y[i2] + H[i2][j2 + 1] * ny;
                    i3 = i2;
                    j3 = j2 + 1;
                    H[i3][j3] = -2;
                }
            } else if (H[i2][j2] != -2 && H[i2][j2 + 1] == -2) {
                a3x = X[j2];
                a3y = Y[i2] + H[i2][j2] * ny;
                i3 = i2;
                j3 = j2;
                H[i3][j3] = -2;
            } else if (H[i2][j2] == -2 && H[i2][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2] + H[i2][j2 + 1] * ny;
                i3 = i2;
                j3 = j2 + 1;
                H[i3][j3] = -2;
            } else {
                a3x = X[j2] + S[i2 + 1][j2] * nx;
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2;
                S[i3][j3] = -2;
            }
        } else if (j1 < j2) //---- Trace from left
        {
            if (S[i2][j2] != -2 && S[i2 + 1][j2] != -2) {
                if (S[i2][j2] < S[i2 + 1][j2]) {
                    a3x = X[j2] + S[i2][j2] * nx;
                    a3y = Y[i2];
                    i3 = i2;
                    j3 = j2;
                    S[i3][j3] = -2;
                } else {
                    a3x = X[j2] + S[i2 + 1][j2] * nx;
                    a3y = Y[i2 + 1];
                    i3 = i2 + 1;
                    j3 = j2;
                    S[i3][j3] = -2;
                }
            } else if (S[i2][j2] != -2 && S[i2 + 1][j2] == -2) {
                a3x = X[j2] + S[i2][j2] * nx;
                a3y = Y[i2];
                i3 = i2;
                j3 = j2;
                S[i3][j3] = -2;
            } else if (S[i2][j2] == -2 && S[i2 + 1][j2] != -2) {
                a3x = X[j2] + S[i2 + 1][j2] * nx;
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2;
                S[i3][j3] = -2;
            } else {
                a3x = X[j2 + 1];
                a3y = Y[i2] + H[i2][j2 + 1] * ny;
                i3 = i2;
                j3 = j2 + 1;
                H[i3][j3] = -2;
            }
        } else if (X[j2] < a2x) //---- Trace from top
        {
            if (H[i2 - 1][j2] != -2 && H[i2 - 1][j2 + 1] != -2) {
                if (H[i2 - 1][j2] > H[i2 - 1][j2 + 1]) //---- < changed to >
                {
                    a3x = X[j2];
                    a3y = Y[i2 - 1] + H[i2 - 1][j2] * ny;
                    i3 = i2 - 1;
                    j3 = j2;
                    H[i3][j3] = -2;
                } else {
                    a3x = X[j2 + 1];
                    a3y = Y[i2 - 1] + H[i2 - 1][j2 + 1] * ny;
                    i3 = i2 - 1;
                    j3 = j2 + 1;
                    H[i3][j3] = -2;
                }
            } else if (H[i2 - 1][j2] != -2 && H[i2 - 1][j2 + 1] == -2) {
                a3x = X[j2];
                a3y = Y[i2 - 1] + H[i2 - 1][j2] * ny;
                i3 = i2 - 1;
                j3 = j2;
                H[i3][j3] = -2;
            } else if (H[i2 - 1][j2] == -2 && H[i2 - 1][j2 + 1] != -2) {
                a3x = X[j2 + 1];
                a3y = Y[i2 - 1] + H[i2 - 1][j2 + 1] * ny;
                i3 = i2 - 1;
                j3 = j2 + 1;
                H[i3][j3] = -2;
            } else {
                a3x = X[j2] + S[i2 - 1][j2] * nx;
                a3y = Y[i2 - 1];
                i3 = i2 - 1;
                j3 = j2;
                S[i3][j3] = -2;
            }
        } else //---- Trace from right
        {
            if (S[i2 + 1][j2 - 1] != -2 && S[i2][j2 - 1] != -2) {
                if (S[i2 + 1][j2 - 1] > S[i2][j2 - 1]) //---- < changed to >
                {
                    a3x = X[j2 - 1] + S[i2 + 1][j2 - 1] * nx;
                    a3y = Y[i2 + 1];
                    i3 = i2 + 1;
                    j3 = j2 - 1;
                    S[i3][j3] = -2;
                } else {
                    a3x = X[j2 - 1] + S[i2][j2 - 1] * nx;
                    a3y = Y[i2];
                    i3 = i2;
                    j3 = j2 - 1;
                    S[i3][j3] = -2;
                }
            } else if (S[i2 + 1][j2 - 1] != -2 && S[i2][j2 - 1] == -2) {
                a3x = X[j2 - 1] + S[i2 + 1][j2 - 1] * nx;
                a3y = Y[i2 + 1];
                i3 = i2 + 1;
                j3 = j2 - 1;
                S[i3][j3] = -2;
            } else if (S[i2 + 1][j2 - 1] == -2 && S[i2][j2 - 1] != -2) {
                a3x = X[j2 - 1] + S[i2][j2 - 1] * nx;
                a3y = Y[i2];
                i3 = i2;
                j3 = j2 - 1;
                S[i3][j3] = -2;
            } else {
                a3x = X[j2 - 1];
                a3y = Y[i2] + H[i2][j2 - 1] * ny;
                i3 = i2;
                j3 = j2 - 1;
                H[i3][j3] = -2;
            }
        }

        return new Object[]{i3, j3, a3x, a3y};
    }

    private static List<PolyLine> isoline_Bottom(double[][] S0, double[] X, double[] Y, double W, double nx, double ny,
            double[][] S, double[][] H) {
        List<PolyLine> bLineList = new ArrayList<>();
        int m, n, j;
        m = S0.length;
        n = S0[0].length;

        int i1, i2, j1 = 0, j2, i3, j3;
        double a2x, a2y, a3x, a3y;
        Object[] returnVal;
        PointD aPoint = new PointD();
        PolyLine aLine = new PolyLine();
        for (j = 0; j < n - 1; j++) //---- Trace isoline from bottom
        {
            if (S[0][j] != -2) //---- Has tracing value
            {
                List<PointD> pointList = new ArrayList<>();
                i2 = 0;
                j2 = j;
                a2x = X[j] + S[0][j] * nx;    //---- x of first point
                a2y = Y[0];                   //---- y of first point
                i1 = -1;
                aPoint.X = a2x;
                aPoint.Y = a2y;
                pointList.add(aPoint);
                while (true) {
                    returnVal = traceIsoline(i1, i2, H, S, j1, j2, X, Y, nx, ny, a2x);
                    i3 = Integer.parseInt(returnVal[0].toString());
                    j3 = Integer.parseInt(returnVal[1].toString());
                    a3x = Double.parseDouble(returnVal[2].toString());
                    a3y = Double.parseDouble(returnVal[3].toString());
                    aPoint.X = a3x;
                    aPoint.Y = a3y;
                    pointList.add(aPoint);
                    if (i3 == m - 1 || j3 == n - 1 || a3y == Y[0] || a3x == X[0]) {
                        break;
                    }

                    a2x = a3x;
                    //a2y = a3y;
                    i1 = i2;
                    j1 = j2;
                    i2 = i3;
                    j2 = j3;
                }
                S[0][j] = -2;
                if (pointList.size() > 4) {
                    aLine.Value = W;
                    aLine.Type = "Bottom";
                    aLine.PointList = new ArrayList<>(pointList);
                    //m_LineList.Add(aLine);
                    bLineList.add(aLine);
                }
            }
        }

        return bLineList;
    }

    private static List<PolyLine> isoline_Left(double[][] S0, double[] X, double[] Y, double W, double nx, double ny,
            double[][] S, double[][] H) {
        List<PolyLine> lLineList = new ArrayList<>();
        int m, n, i;
        m = S0.length;
        n = S0[0].length;

        int i1, i2, j1, j2, i3, j3;
        double a2x, a2y, a3x, a3y;
        Object[] returnVal;
        PointD aPoint = new PointD();
        PolyLine aLine = new PolyLine();
        for (i = 0; i < m - 1; i++) //---- Trace isoline from Left
        {
            if (H[i][0] != -2) {
                List<PointD> pointList = new ArrayList<>();
                i2 = i;
                j2 = 0;
                a2x = X[0];
                a2y = Y[i] + H[i][0] * ny;
                j1 = -1;
                i1 = i2;
                aPoint.X = a2x;
                aPoint.Y = a2y;
                pointList.add(aPoint);
                while (true) {
                    returnVal = traceIsoline(i1, i2, H, S, j1, j2, X, Y, nx, ny, a2x);
                    i3 = Integer.parseInt(returnVal[0].toString());
                    j3 = Integer.parseInt(returnVal[1].toString());
                    a3x = Double.parseDouble(returnVal[2].toString());
                    a3y = Double.parseDouble(returnVal[3].toString());
                    aPoint.X = a3x;
                    aPoint.Y = a3y;
                    pointList.add(aPoint);
                    if (i3 == m - 1 || j3 == n - 1 || a3y == Y[0] || a3x == X[0]) {
                        break;
                    }

                    a2x = a3x;
                    //a2y = a3y;
                    i1 = i2;
                    j1 = j2;
                    i2 = i3;
                    j2 = j3;
                }
                if (pointList.size() > 4) {
                    aLine.Value = W;
                    aLine.Type = "Left";
                    aLine.PointList = new ArrayList<>(pointList);
                    //m_LineList.Add(aLine);
                    lLineList.add(aLine);
                }
            }
        }

        return lLineList;
    }

    private static List<PolyLine> isoline_Top(double[][] S0, double[] X, double[] Y, double W, double nx, double ny,
            double[][] S, double[][] H) {
        List<PolyLine> tLineList = new ArrayList<>();
        int m, n, j;
        m = S0.length;
        n = S0[0].length;

        int i1, i2, j1, j2, i3, j3;
        double a2x, a2y, a3x, a3y;
        Object[] returnVal;
        PointD aPoint = new PointD();
        PolyLine aLine = new PolyLine();
        for (j = 0; j < n - 1; j++) {
            if (S[m - 1][j] != -2) {
                List<PointD> pointList = new ArrayList<>();
                i2 = m - 1;
                j2 = j;
                a2x = X[j] + S[i2][j] * nx;
                a2y = Y[i2];
                i1 = i2;
                j1 = j2;
                aPoint.X = a2x;
                aPoint.Y = a2y;
                pointList.add(aPoint);
                while (true) {
                    returnVal = traceIsoline(i1, i2, H, S, j1, j2, X, Y, nx, ny, a2x);
                    i3 = Integer.parseInt(returnVal[0].toString());
                    j3 = Integer.parseInt(returnVal[1].toString());
                    a3x = Double.parseDouble(returnVal[2].toString());
                    a3y = Double.parseDouble(returnVal[3].toString());
                    aPoint.X = a3x;
                    aPoint.Y = a3y;
                    pointList.add(aPoint);
                    if (i3 == m - 1 || j3 == n - 1 || a3y == Y[0] || a3x == X[0]) {
                        break;
                    }

                    a2x = a3x;
                    //a2y = a3y;
                    i1 = i2;
                    j1 = j2;
                    i2 = i3;
                    j2 = j3;
                }
                S[m - 1][j] = -2;
                if (pointList.size() > 4) {
                    aLine.Value = W;
                    aLine.Type = "Top";
                    aLine.PointList = new ArrayList<>(pointList);
                    //m_LineList.Add(aLine);
                    tLineList.add(aLine);
                }
            }
        }

        return tLineList;
    }

    private static List<PolyLine> isoline_Right(double[][] S0, double[] X, double[] Y, double W, double nx, double ny,
            double[][] S, double[][] H) {
        List<PolyLine> rLineList = new ArrayList<>();
        int m, n, i;
        m = S0.length;
        n = S0[0].length;

        int i1, i2, j1, j2, i3, j3;
        double a2x, a2y, a3x, a3y;
        Object[] returnVal;
        PointD aPoint = new PointD();
        PolyLine aLine = new PolyLine();
        for (i = 0; i < m - 1; i++) {
            if (H[i][n - 1] != -2) {
                List<PointD> pointList = new ArrayList<>();
                i2 = i;
                j2 = n - 1;
                a2x = X[j2];
                a2y = Y[i] + H[i][j2] * ny;
                j1 = j2;
                i1 = i2;
                aPoint.X = a2x;
                aPoint.Y = a2y;
                pointList.add(aPoint);
                while (true) {
                    returnVal = traceIsoline(i1, i2, H, S, j1, j2, X, Y, nx, ny, a2x);
                    i3 = Integer.parseInt(returnVal[0].toString());
                    j3 = Integer.parseInt(returnVal[1].toString());
                    a3x = Double.parseDouble(returnVal[2].toString());
                    a3y = Double.parseDouble(returnVal[3].toString());
                    aPoint.X = a3x;
                    aPoint.Y = a3y;
                    pointList.add(aPoint);
                    if (i3 == m - 1 || j3 == n - 1 || a3y == Y[0] || a3x == X[0]) {
                        break;
                    }

                    a2x = a3x;
                    //a2y = a3y;
                    i1 = i2;
                    j1 = j2;
                    i2 = i3;
                    j2 = j3;
                }
                if (pointList.size() > 4) {
                    aLine.Value = W;
                    aLine.Type = "Right";
                    aLine.PointList = new ArrayList<>(pointList);
                    rLineList.add(aLine);
                }
            }
        }

        return rLineList;
    }

    private static List<PolyLine> isoline_Close(double[][] S0, double[] X, double[] Y, double W, double nx, double ny,
            double[][] S, double[][] H) {
        List<PolyLine> cLineList = new ArrayList<>();
        int m, n, i, j;
        m = S0.length;
        n = S0[0].length;

        int i1, i2, j1, j2, i3, j3;
        double a2x, a2y, a3x, a3y, sx, sy;
        Object[] returnVal;
        PointD aPoint = new PointD();
        PolyLine aLine = new PolyLine();
        for (i = 1; i < m - 2; i++) {
            for (j = 1; j < n - 1; j++) {
                if (H[i][j] != -2) {
                    List<PointD> pointList = new ArrayList<>();
                    i2 = i;
                    j2 = j;
                    a2x = X[j2];
                    a2y = Y[i] + H[i][j2] * ny;
                    j1 = 0;
                    i1 = i2;
                    sx = a2x;
                    sy = a2y;
                    aPoint.X = a2x;
                    aPoint.Y = a2y;
                    pointList.add(aPoint);
                    while (true) {
                        returnVal = traceIsoline(i1, i2, H, S, j1, j2, X, Y, nx, ny, a2x);
                        i3 = Integer.parseInt(returnVal[0].toString());
                        j3 = Integer.parseInt(returnVal[1].toString());
                        a3x = Double.parseDouble(returnVal[2].toString());
                        a3y = Double.parseDouble(returnVal[3].toString());
                        if (i3 == 0 && j3 == 0) {
                            break;
                        }

                        aPoint.X = a3x;
                        aPoint.Y = a3y;
                        pointList.add(aPoint);
                        if (Math.abs(a3y - sy) < 0.000001 && Math.abs(a3x - sx) < 0.000001) {
                            break;
                        }

                        a2x = a3x;
                        //a2y = a3y;
                        i1 = i2;
                        j1 = j2;
                        i2 = i3;
                        j2 = j3;
                        if (i2 == m - 1 || j2 == n - 1) {
                            break;
                        }

                    }
                    H[i][j] = -2;
                    if (pointList.size() > 4) {
                        aLine.Value = W;
                        aLine.Type = "Close";
                        aLine.PointList = new ArrayList<>(pointList);
                        cLineList.add(aLine);
                    }
                }
            }
        }

        for (i = 1; i < m - 1; i++) {
            for (j = 1; j < n - 2; j++) {
                if (S[i][j] != -2) {
                    List<PointD> pointList = new ArrayList<>();
                    i2 = i;
                    j2 = j;
                    a2x = X[j2] + S[i][j] * nx;
                    a2y = Y[i];
                    j1 = j2;
                    i1 = 0;
                    sx = a2x;
                    sy = a2y;
                    aPoint.X = a2x;
                    aPoint.Y = a2y;
                    pointList.add(aPoint);
                    while (true) {
                        returnVal = traceIsoline(i1, i2, H, S, j1, j2, X, Y, nx, ny, a2x);
                        i3 = Integer.parseInt(returnVal[0].toString());
                        j3 = Integer.parseInt(returnVal[1].toString());
                        a3x = Double.parseDouble(returnVal[2].toString());
                        a3y = Double.parseDouble(returnVal[3].toString());
                        aPoint.X = a3x;
                        aPoint.Y = a3y;
                        pointList.add(aPoint);
                        if (Math.abs(a3y - sy) < 0.000001 && Math.abs(a3x - sx) < 0.000001) {
                            break;
                        }

                        a2x = a3x;
                        i1 = i2;
                        j1 = j2;
                        i2 = i3;
                        j2 = j3;
                        if (i2 == m - 1 || j2 == n - 1) {
                            break;
                        }
                    }
                    S[i][j] = -2;
                    if (pointList.size() > 4) {
                        aLine.Value = W;
                        aLine.Type = "Close";
                        aLine.PointList = new ArrayList<>(pointList);
                        //m_LineList.Add(aLine)
                        cLineList.add(aLine);
                    }
                }
            }
        }

        return cLineList;
    }

    private static List<Polygon> tracingPolygons(List<PolyLine> LineList, List<BorderPoint> borderList, Extent bBound, double[] contour) {
        if (LineList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Polygon> aPolygonList = new ArrayList<>();
        List<PolyLine> aLineList;
        PolyLine aLine;
        PointD aPoint;
        Polygon aPolygon;
        Extent aBound;
        int i, j;

        aLineList = new ArrayList<>(LineList);

        //---- Tracing border polygon
        List<PointD> aPList;
        List<PointD> newPList = new ArrayList<>();
        BorderPoint bP;
        int[] timesArray = new int[borderList.size() - 1];
        for (i = 0; i < timesArray.length; i++) {
            timesArray[i] = 0;
        }

        int pIdx, pNum, vNum;
        double aValue = 0, bValue = 0;
        List<BorderPoint> lineBorderList = new ArrayList<>();

        pNum = borderList.size() - 1;
        for (i = 0; i < pNum; i++) {
            if ((borderList.get(i)).Id == -1) {
                continue;
            }

            pIdx = i;
            aPList = new ArrayList<>();
            lineBorderList.add(borderList.get(i));

            //---- Clockwise traceing
            if (timesArray[pIdx] < 2) {
                aPList.add((borderList.get(pIdx)).Point);
                pIdx += 1;
                if (pIdx == pNum) {
                    pIdx = 0;
                }

                vNum = 0;
                while (true) {
                    bP = borderList.get(pIdx);
                    if (bP.Id == -1) //---- Not endpoint of contour
                    {
                        if (timesArray[pIdx] == 1) {
                            break;
                        }

                        aPList.add(bP.Point);
                        timesArray[pIdx] += +1;
                    } else //---- endpoint of contour
                    {
                        if (timesArray[pIdx] == 2) {
                            break;
                        }

                        timesArray[pIdx] += +1;
                        aLine = aLineList.get(bP.Id);
                        if (vNum == 0) {
                            aValue = aLine.Value;
                            bValue = aLine.Value;
                            vNum += 1;
                        } else if (aValue == bValue) {
                            if (aLine.Value > aValue) {
                                bValue = aLine.Value;
                            } else if (aLine.Value < aValue) {
                                aValue = aLine.Value;
                            }

                            vNum += 1;
                        }
                        newPList = new ArrayList<>(aLine.PointList);
                        aPoint = newPList.get(0);
                        if (!(bP.Point.X == aPoint.X && bP.Point.Y == aPoint.Y)) //---- Start point
                        {
                            Collections.reverse(newPList);
                        }

                        aPList.addAll(newPList);
                        for (j = 0; j < borderList.size() - 1; j++) {
                            if (j != pIdx) {
                                if ((borderList.get(j)).Id == bP.Id) {
                                    pIdx = j;
                                    timesArray[pIdx] += +1;
                                    break;
                                }
                            }
                        }
                    }

                    if (pIdx == i) {
                        if (aPList.size() > 0) {
                            aPolygon = new Polygon();
                            aPolygon.LowValue = aValue;
                            aPolygon.HighValue = bValue;
                            aBound = new Extent();
                            aPolygon.Area = getExtentAndArea(aPList, aBound);
                            aPolygon.IsClockWise = true;
                            aPolygon.StartPointIdx = lineBorderList.size() - 1;
                            aPolygon.Extent = aBound;
                            aPolygon.OutLine.PointList = aPList;
                            aPolygon.OutLine.Value = aValue;
                            aPolygon.IsHighCenter = true;
                            aPolygon.OutLine.Type = "Border";
                            aPolygonList.add(aPolygon);
                        }
                        break;
                    }
                    pIdx += 1;
                    if (pIdx == pNum) {
                        pIdx = 0;
                    }

                }
            }

            //---- Anticlockwise traceing
            pIdx = i;
            if (timesArray[pIdx] < 2) {
                aPList = new ArrayList<>();
                aPList.add((borderList.get(pIdx)).Point);
                pIdx += -1;
                if (pIdx == -1) {
                    pIdx = pNum - 1;
                }

                vNum = 0;
                while (true) {
                    bP = borderList.get(pIdx);
                    if (bP.Id == -1) //---- Not endpoint of contour
                    {
                        if (timesArray[pIdx] == 1) {
                            break;
                        }
                        aPList.add(bP.Point);
                        timesArray[pIdx] += +1;
                    } else //---- endpoint of contour
                    {
                        if (timesArray[pIdx] == 2) {
                            break;
                        }

                        timesArray[pIdx] += +1;
                        aLine = aLineList.get(bP.Id);
                        if (vNum == 0) {
                            aValue = aLine.Value;
                            bValue = aLine.Value;
                            vNum += 1;
                        } else if (aValue == bValue) {
                            if (aLine.Value > aValue) {
                                bValue = aLine.Value;
                            } else if (aLine.Value < aValue) {
                                aValue = aLine.Value;
                            }

                            vNum += 1;
                        }
                        newPList = new ArrayList<>(aLine.PointList);
                        aPoint = newPList.get(0);
                        if (!(bP.Point.X == aPoint.X && bP.Point.Y == aPoint.Y)) //---- Start point
                        {
                            Collections.reverse(newPList);
                        }

                        aPList.addAll(newPList);
                        for (j = 0; j < borderList.size() - 1; j++) {
                            if (j != pIdx) {
                                if ((borderList.get(j)).Id == bP.Id) {
                                    pIdx = j;
                                    timesArray[pIdx] += +1;
                                    break;
                                }
                            }
                        }
                    }

                    if (pIdx == i) {
                        if (aPList.size() > 0) {
                            aPolygon = new Polygon();
                            aPolygon.LowValue = aValue;
                            aPolygon.HighValue = bValue;
                            aBound = new Extent();
                            aPolygon.Area = getExtentAndArea(aPList, aBound);
                            aPolygon.IsClockWise = false;
                            aPolygon.StartPointIdx = lineBorderList.size() - 1;
                            aPolygon.Extent = aBound;
                            aPolygon.OutLine.PointList = aPList;
                            aPolygon.OutLine.Value = aValue;
                            aPolygon.IsHighCenter = true;
                            aPolygon.OutLine.Type = "Border";
                            aPolygonList.add(aPolygon);
                        }
                        break;
                    }
                    pIdx += -1;
                    if (pIdx == -1) {
                        pIdx = pNum - 1;
                    }

                }
            }
        }

        //---- tracing close polygons
        List<Polygon> cPolygonlist = new ArrayList<>();
        boolean isInserted;
        for (i = 0; i < aLineList.size(); i++) {
            aLine = aLineList.get(i);
            if (aLine.Type.equals("Close") && aLine.PointList.size() > 0) {
                aPolygon = new Polygon();
                aPolygon.LowValue = aLine.Value;
                aPolygon.HighValue = aLine.Value;
                aBound = new Extent();
                aPolygon.Area = getExtentAndArea(aLine.PointList, aBound);
                aPolygon.IsClockWise = isClockwise(aLine.PointList);
                aPolygon.Extent = aBound;
                aPolygon.OutLine = aLine;
                aPolygon.IsHighCenter = true;

                //---- Sort from big to small
                isInserted = false;
                for (j = 0; j < cPolygonlist.size(); j++) {
                    if (aPolygon.Area > (cPolygonlist.get(j)).Area) {
                        cPolygonlist.add(j, aPolygon);
                        isInserted = true;
                        break;
                    }
                }
                if (!isInserted) {
                    cPolygonlist.add(aPolygon);
                }

            }
        }

        //---- Juge isHighCenter for border polygons
        Extent cBound1, cBound2;
        if (aPolygonList.size() > 0) {
            int outPIdx;
            boolean IsSides;
            boolean IfSameValue = false;    //---- If all boder polygon lines have same value
            aPolygon = aPolygonList.get(0);
            if (aPolygon.LowValue == aPolygon.HighValue) {
                outPIdx = aPolygon.StartPointIdx;
                while (true) {
                    if (aPolygon.IsClockWise) {
                        outPIdx = outPIdx - 1;
                        if (outPIdx == -1) {
                            outPIdx = lineBorderList.size() - 1;
                        }

                    } else {
                        outPIdx = outPIdx + 1;
                        if (outPIdx == lineBorderList.size()) {
                            outPIdx = 0;
                        }

                    }
                    bP = lineBorderList.get(outPIdx);
                    aLine = aLineList.get(bP.Id);
                    if (aLine.Value == aPolygon.LowValue) {
                        if (outPIdx == aPolygon.StartPointIdx) {
                            IfSameValue = true;
                            break;
                        }
                    } else {
                        IfSameValue = false;
                        break;
                    }
                }
            }

            if (IfSameValue) {
                if (cPolygonlist.size() > 0) {
                    Polygon cPolygon;
                    cPolygon = cPolygonlist.get(0);
                    cBound1 = cPolygon.Extent;
                    for (i = 0; i < aPolygonList.size(); i++) {
                        aPolygon = aPolygonList.get(i);
                        cBound2 = aPolygon.Extent;
                        if (cBound1.xMin > cBound2.xMin && cBound1.yMin > cBound2.yMin
                                && cBound1.xMax < cBound2.xMax && cBound1.yMax < cBound2.yMax) {
                            aPolygon.IsHighCenter = false;
                        } else {
                            aPolygon.IsHighCenter = true;
                        }
                    }
                } else {
                    boolean tf = true;    //---- Temperal solution, not finished
                    for (i = 0; i < aPolygonList.size(); i++) {
                        aPolygon = aPolygonList.get(i);
                        tf = !tf;
                        aPolygon.IsHighCenter = tf;
                    }
                }
            } else {
                for (i = 0; i < aPolygonList.size(); i++) {
                    aPolygon = aPolygonList.get(i);
                    if (aPolygon.LowValue == aPolygon.HighValue) {
                        IsSides = false;
                        outPIdx = aPolygon.StartPointIdx;
                        while (true) {
                            if (aPolygon.IsClockWise) {
                                outPIdx = outPIdx - 1;
                                if (outPIdx == -1) {
                                    outPIdx = lineBorderList.size() - 1;
                                }
                            } else {
                                outPIdx = outPIdx + 1;
                                if (outPIdx == lineBorderList.size()) {
                                    outPIdx = 0;
                                }

                            }
                            bP = lineBorderList.get(outPIdx);
                            aLine = aLineList.get(bP.Id);
                            if (aLine.Value == aPolygon.LowValue) {
                                if (outPIdx == aPolygon.StartPointIdx) {
                                    break;
                                } else {
                                    IsSides = !IsSides;
                                }
                            } else {
                                if (IsSides) {
                                    if (aLine.Value < aPolygon.LowValue) {
                                        aPolygon.IsHighCenter = false;
                                    }
                                } else if (aLine.Value > aPolygon.LowValue) {
                                    aPolygon.IsHighCenter = false;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else //Add border polygon
        {
            //Get max & min contour values
            double max = aLineList.get(0).Value, min = aLineList.get(0).Value;
            for (PolyLine aPLine : aLineList) {
                if (aPLine.Value > max) {
                    max = aPLine.Value;
                }
                if (aPLine.Value < min) {
                    min = aPLine.Value;
                }
            }
            aPolygon = new Polygon();
            aLine = new PolyLine();
            aLine.Type = "Border";
            aLine.Value = contour[0];
            aPolygon.IsHighCenter = false;
            if (cPolygonlist.size() > 0) {
                if ((cPolygonlist.get(0).LowValue == max)) {
                    aLine.Value = contour[contour.length - 1];
                    aPolygon.IsHighCenter = true;
                }
            }
            newPList.clear();
            aPoint = new PointD();
            aPoint.X = bBound.xMin;
            aPoint.Y = bBound.yMin;
            newPList.add(aPoint);
            aPoint = new PointD();
            aPoint.X = bBound.xMin;
            aPoint.Y = bBound.yMax;
            newPList.add(aPoint);
            aPoint = new PointD();
            aPoint.X = bBound.xMax;
            aPoint.Y = bBound.yMax;
            newPList.add(aPoint);
            aPoint = new PointD();
            aPoint.X = bBound.xMax;
            aPoint.Y = bBound.yMin;
            newPList.add(aPoint);
            newPList.add(newPList.get(0));
            aLine.PointList = new ArrayList<>(newPList);

            if (aLine.PointList.size() > 0) {
                aPolygon.LowValue = aLine.Value;
                aPolygon.HighValue = aLine.Value;
                aBound = new Extent();
                aPolygon.Area = getExtentAndArea(aLine.PointList, aBound);
                aPolygon.IsClockWise = isClockwise(aLine.PointList);
                aPolygon.Extent = aBound;
                aPolygon.OutLine = aLine;
                aPolygonList.add(aPolygon);
            }
        }

        //---- Add close polygons to form total polygons list
        aPolygonList.addAll(cPolygonlist);

        //---- Juge IsHighCenter for close polygons
        int polygonNum = aPolygonList.size();
        Polygon bPolygon;
        for (i = polygonNum - 1; i >= 0; i--) {
            aPolygon = aPolygonList.get(i);
            if (aPolygon.OutLine.Type.equals("Close")) {
                cBound1 = aPolygon.Extent;
                aValue = aPolygon.LowValue;
                aPoint = aPolygon.OutLine.PointList.get(0);
                for (j = i - 1; j >= 0; j--) {
                    bPolygon = aPolygonList.get(j);
                    cBound2 = bPolygon.Extent;
                    bValue = bPolygon.LowValue;
                    newPList = new ArrayList<>(bPolygon.OutLine.PointList);
                    if (pointInPolygon(newPList, aPoint)) {
                        if (cBound1.xMin > cBound2.xMin && cBound1.yMin > cBound2.yMin
                                && cBound1.xMax < cBound2.xMax && cBound1.yMax < cBound2.yMax) {
                            if (aValue < bValue) {
                                aPolygon.IsHighCenter = false;
                            } else if (aValue == bValue) {
                                if (bPolygon.IsHighCenter) {
                                    aPolygon.IsHighCenter = false;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        return aPolygonList;
    }

    private static List<Polygon> tracingPolygons(List<PolyLine> LineList, List<BorderPoint> borderList, boolean hasBorder) {
        if (LineList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Polygon> aPolygonList = new ArrayList<>();
        List<PolyLine> aLineList;
        PolyLine aLine;
        PointD aPoint;
        Polygon aPolygon;
        Extent aBound;
        int i, j;

        aLineList = new ArrayList<>(LineList);

        //---- Tracing border polygon
        if (hasBorder) {
            List<PointD> aPList;
            List<PointD> newPList;
            BorderPoint bP;
            int[] timesArray = new int[borderList.size() - 1];
            for (i = 0; i < timesArray.length; i++) {
                timesArray[i] = 0;
            }

            int pIdx, pNum, vNum, vvNum;
            double aValue = 0, bValue = 0, cValue = 0;
            List<BorderPoint> lineBorderList = new ArrayList<>();

            pNum = borderList.size() - 1;
            for (i = 0; i < pNum; i++) {
                if ((borderList.get(i)).Id == -1) {
                    continue;
                }

                pIdx = i;
                aPList = new ArrayList<>();
                lineBorderList.add(borderList.get(i));

                //---- Clockwise traceing
                if (timesArray[pIdx] < 2) {
                    aPList.add((borderList.get(pIdx)).Point);
                    pIdx += 1;
                    if (pIdx == pNum) {
                        pIdx = 0;
                    }

                    vNum = 0;
                    vvNum = 0;
                    while (true) {
                        bP = borderList.get(pIdx);
                        if (bP.Id == -1) //---- Not endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            cValue = bP.Value;
                            vvNum += 1;
                            aPList.add(bP.Point);
                            timesArray[pIdx] += +1;
                        } else //---- endpoint of contour
                        {
                            if (timesArray[pIdx] == 2) {
                                break;
                            }

                            timesArray[pIdx] += +1;
                            aLine = aLineList.get(bP.Id);
                            if (vNum == 0) {
                                aValue = aLine.Value;
                                bValue = aLine.Value;
                                vNum += 1;
                            } else {
                                if (aLine.Value > aValue) {
                                    bValue = aLine.Value;
                                } else if (aLine.Value < aValue) {
                                    aValue = aLine.Value;
                                }

                                vNum += 1;
                            }
                            newPList = new ArrayList<>(aLine.PointList);
                            aPoint = newPList.get(0);
                            if (!(bP.Point.X == aPoint.X && bP.Point.Y == aPoint.Y)) //---- Start point
                            {
                                Collections.reverse(newPList);
                            }

                            aPList.addAll(newPList);
                            for (j = 0; j < borderList.size() - 1; j++) {
                                if (j != pIdx) {
                                    if ((borderList.get(j)).Id == bP.Id) {
                                        pIdx = j;
                                        timesArray[pIdx] += +1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (pIdx == i) {
                            if (aPList.size() > 0) {
                                aPolygon = new Polygon();
                                aPolygon.IsBorder = true;
                                aPolygon.LowValue = aValue;
                                aPolygon.HighValue = bValue;
                                aBound = new Extent();
                                aPolygon.Area = getExtentAndArea(aPList, aBound);
                                aPolygon.IsClockWise = true;
                                aPolygon.StartPointIdx = lineBorderList.size() - 1;
                                aPolygon.Extent = aBound;
                                aPolygon.OutLine.PointList = aPList;
                                aPolygon.OutLine.Value = aValue;
                                aPolygon.IsHighCenter = true;
                                aPolygon.HoleLines = new ArrayList<>();
                                if (vvNum > 0) {
                                    if (cValue < aValue) {
                                        aPolygon.IsHighCenter = false;
                                        aPolygon.HighValue = aValue;
                                    }
                                }
                                aPolygon.OutLine.Type = "Border";
                                aPolygonList.add(aPolygon);
                            }
                            break;
                        }
                        pIdx += 1;
                        if (pIdx == pNum) {
                            pIdx = 0;
                        }

                    }
                }

                //---- Anticlockwise traceing
                pIdx = i;
                if (timesArray[pIdx] < 2) {
                    aPList = new ArrayList<>();
                    aPList.add((borderList.get(pIdx)).Point);
                    pIdx += -1;
                    if (pIdx == -1) {
                        pIdx = pNum - 1;
                    }

                    vNum = 0;
                    vvNum = 0;
                    while (true) {
                        bP = borderList.get(pIdx);
                        if (bP.Id == -1) //---- Not endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            cValue = bP.Value;
                            vvNum += 1;
                            aPList.add(bP.Point);
                            timesArray[pIdx] += +1;
                        } else //---- endpoint of contour
                        {
                            if (timesArray[pIdx] == 2) {
                                break;
                            }

                            timesArray[pIdx] += +1;
                            aLine = aLineList.get(bP.Id);
                            if (vNum == 0) {
                                aValue = aLine.Value;
                                bValue = aLine.Value;
                                vNum += 1;
                            } else {
                                if (aLine.Value > aValue) {
                                    bValue = aLine.Value;
                                } else if (aLine.Value < aValue) {
                                    aValue = aLine.Value;
                                }

                                vNum += 1;
                            }
                            newPList = new ArrayList<>(aLine.PointList);
                            aPoint = newPList.get(0);
                            if (!(bP.Point.X == aPoint.X && bP.Point.Y == aPoint.Y)) //---- Start point
                            {
                                Collections.reverse(newPList);
                            }

                            aPList.addAll(newPList);
                            for (j = 0; j < borderList.size() - 1; j++) {
                                if (j != pIdx) {
                                    if ((borderList.get(j)).Id == bP.Id) {
                                        pIdx = j;
                                        timesArray[pIdx] += +1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (pIdx == i) {
                            if (aPList.size() > 0) {
                                aPolygon = new Polygon();
                                aPolygon.IsBorder = true;
                                aPolygon.LowValue = aValue;
                                aPolygon.HighValue = bValue;
                                aBound = new Extent();
                                aPolygon.Area = getExtentAndArea(aPList, aBound);
                                aPolygon.IsClockWise = false;
                                aPolygon.StartPointIdx = lineBorderList.size() - 1;
                                aPolygon.Extent = aBound;
                                aPolygon.OutLine.PointList = aPList;
                                aPolygon.OutLine.Value = aValue;
                                aPolygon.IsHighCenter = true;
                                aPolygon.HoleLines = new ArrayList<>();
                                if (vvNum > 0) {
                                    if (cValue < aValue) {
                                        aPolygon.IsHighCenter = false;
                                        aPolygon.HighValue = aValue;
                                    }
                                }
                                aPolygon.OutLine.Type = "Border";
                                aPolygonList.add(aPolygon);
                            }
                            break;
                        }
                        pIdx += -1;
                        if (pIdx == -1) {
                            pIdx = pNum - 1;
                        }

                    }
                }
            }
        }

        //---- tracing close polygons
        List<Polygon> cPolygonlist = new ArrayList<>();
        boolean isInserted;
        for (i = 0; i < aLineList.size(); i++) {
            aLine = aLineList.get(i);
            if (aLine.Type.equals("Close") && aLine.PointList.size() > 0) {
                aPolygon = new Polygon();
                aPolygon.IsBorder = false;
                aPolygon.LowValue = aLine.Value;
                aPolygon.HighValue = aLine.Value;
                aBound = new Extent();
                aPolygon.Area = getExtentAndArea(aLine.PointList, aBound);
                aPolygon.IsClockWise = isClockwise(aLine.PointList);
                aPolygon.Extent = aBound;
                aPolygon.OutLine = aLine;
                aPolygon.IsHighCenter = true;
                aPolygon.HoleLines = new ArrayList<>();

                //---- Sort from big to small
                isInserted = false;
                for (j = 0; j < cPolygonlist.size(); j++) {
                    if (aPolygon.Area > (cPolygonlist.get(j)).Area) {
                        cPolygonlist.add(j, aPolygon);
                        isInserted = true;
                        break;
                    }
                }
                if (!isInserted) {
                    cPolygonlist.add(aPolygon);
                }

            }
        }

        //---- Juge isHighCenter for border polygons
        aPolygonList = judgePolygonHighCenter(aPolygonList, cPolygonlist, aLineList, borderList);

        return aPolygonList;
    }

    private static List<Polygon> tracingClipPolygons(Polygon inPolygon, List<PolyLine> LineList, List<BorderPoint> borderList) {
        if (LineList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Polygon> aPolygonList = new ArrayList<>();
        List<PolyLine> aLineList;
        PolyLine aLine;
        PointD aPoint;
        Polygon aPolygon;
        Extent aBound;
        int i, j;

        aLineList = new ArrayList<>(LineList);

        //---- Tracing border polygon
        List<PointD> aPList;
        List<PointD> newPList;
        BorderPoint bP;
        int[] timesArray = new int[borderList.size() - 1];
        for (i = 0; i < timesArray.length; i++) {
            timesArray[i] = 0;
        }

        int pIdx, pNum;
        List<BorderPoint> lineBorderList = new ArrayList<>();

        pNum = borderList.size() - 1;
        PointD bPoint, b1Point;
        for (i = 0; i < pNum; i++) {
            if ((borderList.get(i)).Id == -1) {
                continue;
            }

            pIdx = i;
            lineBorderList.add(borderList.get(i));
            //bP = borderList.get(pIdx);
            b1Point = borderList.get(pIdx).Point;

            //---- Clockwise tracing
            if (timesArray[pIdx] < 1) {
                aPList = new ArrayList<>();
                aPList.add((borderList.get(pIdx)).Point);
                pIdx += 1;
                if (pIdx == pNum) {
                    pIdx = 0;
                }

                bPoint = (PointD) borderList.get(pIdx).Point.clone();
                if (borderList.get(pIdx).Id == -1) {
                    int aIdx = pIdx + 10;
                    for (int o = 1; o <= 10; o++) {
                        if (borderList.get(pIdx + o).Id > -1) {
                            aIdx = pIdx + o - 1;
                            break;
                        }
                    }
                    bPoint = (PointD) borderList.get(aIdx).Point.clone();
                } else {
                    bPoint.X = (bPoint.X + b1Point.X) / 2;
                    bPoint.Y = (bPoint.Y + b1Point.Y) / 2;
                }
                if (pointInPolygon(inPolygon, bPoint)) {
                    while (true) {
                        bP = borderList.get(pIdx);
                        if (bP.Id == -1) //---- Not endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            aPList.add(bP.Point);
                            timesArray[pIdx] += 1;
                        } else //---- endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            timesArray[pIdx] += 1;
                            aLine = aLineList.get(bP.Id);

                            newPList = new ArrayList<>(aLine.PointList);
                            aPoint = newPList.get(0);

                            if (!(doubleEquals(bP.Point.X, aPoint.X) && doubleEquals(bP.Point.Y, aPoint.Y))) //---- Start point
                            {
                                Collections.reverse(newPList);
                            }

                            aPList.addAll(newPList);
                            for (j = 0; j < borderList.size() - 1; j++) {
                                if (j != pIdx) {
                                    if ((borderList.get(j)).Id == bP.Id) {
                                        pIdx = j;
                                        timesArray[pIdx] += 1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (pIdx == i) {
                            if (aPList.size() > 0) {
                                aPolygon = new Polygon();
                                aPolygon.IsBorder = true;
                                aPolygon.LowValue = inPolygon.LowValue;
                                aPolygon.HighValue = inPolygon.HighValue;
                                aBound = new Extent();
                                aPolygon.Area = getExtentAndArea(aPList, aBound);
                                aPolygon.IsClockWise = true;
                                aPolygon.StartPointIdx = lineBorderList.size() - 1;
                                aPolygon.Extent = aBound;
                                aPolygon.OutLine.PointList = aPList;
                                aPolygon.OutLine.Value = inPolygon.LowValue;
                                aPolygon.IsHighCenter = inPolygon.IsHighCenter;
                                aPolygon.OutLine.Type = "Border";
                                aPolygon.HoleLines = new ArrayList<>();
                                aPolygonList.add(aPolygon);
                            }
                            break;
                        }
                        pIdx += 1;
                        if (pIdx == pNum) {
                            pIdx = 0;
                        }
                    }
                }
            }

            //---- Anticlockwise traceing
            pIdx = i;
            if (timesArray[pIdx] < 1) {
                aPList = new ArrayList<>();
                aPList.add((borderList.get(pIdx)).Point);
                pIdx += -1;
                if (pIdx == -1) {
                    pIdx = pNum - 1;
                }

                bPoint = (PointD) borderList.get(pIdx).Point.clone();
                if (borderList.get(pIdx).Id == -1) {
                    int aIdx = pIdx + 10;
                    for (int o = 1; o <= 10; o++) {
                        if (borderList.get(pIdx + o).Id > -1) {
                            aIdx = pIdx + o - 1;
                            break;
                        }
                    }
                    bPoint = (PointD) borderList.get(aIdx).Point.clone();
                } else {
                    bPoint.X = (bPoint.X + b1Point.X) / 2;
                    bPoint.Y = (bPoint.Y + b1Point.Y) / 2;
                }
                if (pointInPolygon(inPolygon, bPoint)) {
                    while (true) {
                        bP = borderList.get(pIdx);
                        if (bP.Id == -1) //---- Not endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            aPList.add(bP.Point);
                            timesArray[pIdx] += 1;
                        } else //---- endpoint of contour
                        {
                            if (timesArray[pIdx] == 1) {
                                break;
                            }

                            timesArray[pIdx] += 1;
                            aLine = aLineList.get(bP.Id);

                            newPList = new ArrayList<>(aLine.PointList);
                            aPoint = newPList.get(0);

                            if (!(doubleEquals(bP.Point.X, aPoint.X) && doubleEquals(bP.Point.Y, aPoint.Y))) //---- Start point
                            {
                                Collections.reverse(newPList);
                            }

                            aPList.addAll(newPList);
                            for (j = 0; j < borderList.size() - 1; j++) {
                                if (j != pIdx) {
                                    if ((borderList.get(j)).Id == bP.Id) {
                                        pIdx = j;
                                        timesArray[pIdx] += 1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (pIdx == i) {
                            if (aPList.size() > 0) {
                                aPolygon = new Polygon();
                                aPolygon.IsBorder = true;
                                aPolygon.LowValue = inPolygon.LowValue;
                                aPolygon.HighValue = inPolygon.HighValue;
                                aBound = new Extent();
                                aPolygon.Area = getExtentAndArea(aPList, aBound);
                                aPolygon.IsClockWise = false;
                                aPolygon.StartPointIdx = lineBorderList.size() - 1;
                                aPolygon.Extent = aBound;
                                aPolygon.OutLine.PointList = aPList;
                                aPolygon.OutLine.Value = inPolygon.LowValue;
                                aPolygon.IsHighCenter = inPolygon.IsHighCenter;
                                aPolygon.OutLine.Type = "Border";
                                aPolygon.HoleLines = new ArrayList<>();
                                aPolygonList.add(aPolygon);
                            }
                            break;
                        }
                        pIdx += -1;
                        if (pIdx == -1) {
                            pIdx = pNum - 1;
                        }

                    }
                }
            }
        }

        return aPolygonList;
    }

    private static List<Polygon> judgePolygonHighCenter(List<Polygon> borderPolygons, List<Polygon> closedPolygons,
            List<PolyLine> aLineList, List<BorderPoint> borderList) {
        int i, j;
        Polygon aPolygon;
        PolyLine aLine;
        List<PointD> newPList = new ArrayList<>();
        Extent aBound;
        double aValue;
        PointD aPoint;

        if (borderPolygons.isEmpty()) //Add border polygon
        {
            //Get max & min values
            aValue = borderList.get(0).Value;
            double max = Double.MAX_VALUE, min = Double.MIN_VALUE;
            for (PolyLine aPLine : aLineList) {
                if (aPLine.Value < aValue && aPLine.Value > min) {
                    min = aPLine.Value;
                }
                if (aPLine.Value > aValue && aPLine.Value < max) {
                    max = aPLine.Value;
                }
            }
            aPolygon = new Polygon();
            if (min == Double.MIN_VALUE) {
                min = aValue;
                aPolygon.IsHighCenter = true;
            } else if (max == Double.MAX_VALUE) {
                max = aValue;
                aPolygon.IsHighCenter = false;
            }
            aLine = new PolyLine();
            aLine.Type = "Border";
            aLine.Value = aValue;
            newPList.clear();
            for (BorderPoint aP : borderList) {
                newPList.add(aP.Point);
            }
            aLine.PointList = new ArrayList<>(newPList);
            if (aLine.PointList.size() > 0) {
                aPolygon.IsBorder = true;
                aPolygon.LowValue = min;
                aPolygon.HighValue = max;
                aBound = new Extent();
                aPolygon.Area = getExtentAndArea(aLine.PointList, aBound);
                aPolygon.IsClockWise = isClockwise(aLine.PointList);
                aPolygon.Extent = aBound;
                aPolygon.OutLine = aLine;
                aPolygon.HoleLines = new ArrayList<>();
                borderPolygons.add(aPolygon);
            }
        }

        //---- Add close polygons to form total polygons list
        borderPolygons.addAll(closedPolygons);

        //---- Juge IsHighCenter for close polygons
        Extent cBound1, cBound2;
        int polygonNum = borderPolygons.size();
        Polygon bPolygon;
        for (i = 1; i < polygonNum; i++) {
            aPolygon = borderPolygons.get(i);
            if (aPolygon.OutLine.Type.equals("Close")) {
                cBound1 = aPolygon.Extent;
                aPoint = aPolygon.OutLine.PointList.get(0);
                for (j = i - 1; j >= 0; j--) {
                    bPolygon = borderPolygons.get(j);
                    cBound2 = bPolygon.Extent;
                    newPList = new ArrayList<>(bPolygon.OutLine.PointList);
                    if (pointInPolygon(newPList, aPoint)) {
                        if (cBound1.xMin > cBound2.xMin && cBound1.yMin > cBound2.yMin
                                && cBound1.xMax < cBound2.xMax && cBound1.yMax < cBound2.yMax) {
                            if (bPolygon.IsHighCenter) {
                                aPolygon.IsHighCenter = aPolygon.HighValue != bPolygon.LowValue;
                            } else {
                                aPolygon.IsHighCenter = aPolygon.LowValue == bPolygon.HighValue;
                            }
                            break;
                        }
                    }
                }
            }
        }

        return borderPolygons;
    }

    private static List<Polygon> tracingPolygons_Ring(List<PolyLine> LineList, List<BorderPoint> borderList, Border aBorder,
            double[] contour, int[] pNums) {
        List<Polygon> aPolygonList = new ArrayList<>();
        List<PolyLine> aLineList;
        PolyLine aLine;
        PointD aPoint;
        Polygon aPolygon;
        Extent aBound;
        int i;
        int j;

        aLineList = new ArrayList<>(LineList);

        //---- Tracing border polygon
        List<PointD> aPList;
        List<PointD> newPList;
        BorderPoint bP;
        BorderPoint bP1;
        int[] timesArray = new int[borderList.size()];
        for (i = 0; i < timesArray.length; i++) {
            timesArray[i] = 0;
        }
        int pIdx;
        int pNum;
        int vNum;
        double aValue = 0;
        double bValue = 0;
        double cValue = 0;
        List<BorderPoint> lineBorderList = new ArrayList<>();
        int borderIdx1;
        int borderIdx2;
        int innerIdx;

        pNum = borderList.size();
        for (i = 0; i < pNum; i++) {
            if ((borderList.get(i)).Id == -1) {
                continue;
            }
            pIdx = i;
            lineBorderList.add(borderList.get(i));

            boolean sameBorderIdx = false;    //The two end points of the contour line are on same inner border
            boolean innerStart;
            //---- Clockwise traceing
            if (timesArray[pIdx] < 2) {
                bP = borderList.get(pIdx);
                innerStart = bP.BorderIdx > 0;
                innerIdx = bP.BInnerIdx;
                aPList = new ArrayList<>();
                List<Integer> bIdxList = new ArrayList<>();
                aPList.add(bP.Point);
                borderIdx1 = bP.BorderIdx;
                borderIdx2 = borderIdx1;
                pIdx += 1;
                innerIdx += 1;
                if (innerIdx == pNums[borderIdx1] - 1) {
                    pIdx = pIdx - (pNums[borderIdx1] - 1);
                }
                vNum = 0;
                boolean isRepeat = false;
                do {
                    bP = borderList.get(pIdx);
                    //---- Not endpoint of contour
                    if (bP.Id == -1) {
                        if (timesArray[pIdx] == 1) {
                            break;
                        }
                        cValue = bP.Value;
                        aPList.add(bP.Point);
                        timesArray[pIdx] += 1;
                        bIdxList.add(pIdx);
                        //---- endpoint of contour
                    } else {
                        if (timesArray[pIdx] == 2) {
                            for (int bidx : bIdxList) {
                                timesArray[bidx] -= 1;
                            }
                            break;
                        }
                        timesArray[pIdx] += 1;
                        bIdxList.add(pIdx);
                        aLine = aLineList.get(bP.Id);
                        //---- Set high and low value of the polygon
                        if (vNum == 0) {
                            aValue = aLine.Value;
                            bValue = aLine.Value;
                            vNum += 1;
                        } else if (aValue == bValue) {
                            if (aLine.Value > aValue) {
                                bValue = aLine.Value;
                            } else if (aLine.Value < aValue) {
                                aValue = aLine.Value;
                            }
                            vNum += 1;
                        }
                        newPList = new ArrayList<>(aLine.PointList);
                        aPoint = newPList.get(0);
                        //---- Not start point
                        if (!(bP.Point.X == aPoint.X && bP.Point.Y == aPoint.Y)) {
                            Collections.reverse(newPList);
                        }
                        aPList.addAll(newPList);
                        //---- Find corresponding border point
                        for (j = 0; j < borderList.size(); j++) {
                            if (j != pIdx) {
                                bP1 = borderList.get(j);
                                if (bP1.Id == bP.Id) {
                                    pIdx = j;
                                    innerIdx = bP1.BInnerIdx;
                                    timesArray[pIdx] += 1;
                                    bIdxList.add(pIdx);
                                    borderIdx2 = bP1.BorderIdx;
                                    if (bP.BorderIdx > 0 && bP.BorderIdx == bP1.BorderIdx) {
                                        sameBorderIdx = true;
                                    }
                                    if (innerStart && bP1.BorderIdx == 0) {
                                        for (int bidx : bIdxList) {
                                            timesArray[bidx] -= 1;
                                        }
                                        isRepeat = true;
                                    }
                                    break;
                                }
                            }
                        }
                        if (isRepeat) {
                            break;
                        }
                    }

                    //---- Return to start point, tracing finish
                    if (pIdx == i) {
                        if (aPList.size() > 0) {
                            if (sameBorderIdx) {
                                boolean isTooBig = false;
                                int baseNum = 0;
                                for (int idx = 0; idx < bP.BorderIdx; idx++) {
                                    baseNum += pNums[idx];
                                }
                                int sIdx = baseNum;
                                int eIdx = baseNum + pNums[bP.BorderIdx];
                                int theIdx = sIdx;
                                for (int idx = sIdx; idx < eIdx; idx++) {
                                    if (!bIdxList.contains(idx)) {
                                        theIdx = idx;
                                        break;
                                    }
                                }
                                if (pointInPolygon(aPList, borderList.get(theIdx).Point)) {
                                    isTooBig = true;
                                }

                                if (isTooBig) {
                                    break;
                                }
                            }
                            aPolygon = new Polygon();
                            aPolygon.IsBorder = true;
                            aPolygon.IsInnerBorder = sameBorderIdx;
                            aPolygon.LowValue = aValue;
                            aPolygon.HighValue = bValue;
                            aBound = new Extent();
                            aPolygon.Area = getExtentAndArea(aPList, aBound);
                            aPolygon.IsClockWise = true;
                            aPolygon.StartPointIdx = lineBorderList.size() - 1;
                            aPolygon.Extent = aBound;
                            aPolygon.OutLine.PointList = aPList;
                            aPolygon.OutLine.Value = aValue;
                            aPolygon.IsHighCenter = true;
                            if (aValue == bValue) {
                                if (cValue < aValue) {
                                    aPolygon.IsHighCenter = false;
                                }
                            }
                            aPolygon.OutLine.Type = "Border";
                            aPolygon.HoleLines = new ArrayList<>();
                            aPolygonList.add(aPolygon);
                        }
                        break;
                    }
                    pIdx += 1;
                    innerIdx += 1;
                    if (borderIdx1 != borderIdx2) {
                        borderIdx1 = borderIdx2;
                    }

                    if (innerIdx == pNums[borderIdx1] - 1) {
                        pIdx = pIdx - (pNums[borderIdx1] - 1);
                        innerIdx = 0;
                    }
                } while (true);
            }

            sameBorderIdx = false;
            //---- Anticlockwise traceing
            pIdx = i;
            if (timesArray[pIdx] < 2) {
                aPList = new ArrayList<>();
                List<Integer> bIdxList = new ArrayList<>();
                bP = borderList.get(pIdx);
                innerStart = bP.BorderIdx > 0;
                innerIdx = bP.BInnerIdx;
                aPList.add(bP.Point);
                borderIdx1 = bP.BorderIdx;
                borderIdx2 = borderIdx1;
                pIdx += -1;
                innerIdx += -1;
                if (innerIdx == -1) {
                    pIdx = pIdx + (pNums[borderIdx1] - 1);
                }
                vNum = 0;
                boolean isRepeat = false;
                do {
                    bP = borderList.get(pIdx);
                    //---- Not endpoint of contour
                    if (bP.Id == -1) {
                        if (timesArray[pIdx] == 1) {
                            break;
                        }
                        cValue = bP.Value;
                        aPList.add(bP.Point);
                        bIdxList.add(pIdx);
                        timesArray[pIdx] += 1;
                        //---- endpoint of contour
                    } else {
                        if (timesArray[pIdx] == 2) {
                            for (int bidx : bIdxList) {
                                timesArray[bidx] -= 1;
                            }
                            break;
                        }
                        timesArray[pIdx] += 1;
                        bIdxList.add(pIdx);
                        aLine = aLineList.get(bP.Id);
                        if (vNum == 0) {
                            aValue = aLine.Value;
                            bValue = aLine.Value;
                            vNum += 1;
                        } else if (aValue == bValue) {
                            if (aLine.Value > aValue) {
                                bValue = aLine.Value;
                            } else if (aLine.Value < aValue) {
                                aValue = aLine.Value;
                            }
                            vNum += 1;
                        }
                        newPList = new ArrayList<>(aLine.PointList);
                        aPoint = newPList.get(0);
                        //---- Start point
                        if (!(bP.Point.X == aPoint.X && bP.Point.Y == aPoint.Y)) {
                            Collections.reverse(newPList);
                        }
                        aPList.addAll(newPList);
                        for (j = 0; j < borderList.size(); j++) {
                            if (j != pIdx) {
                                bP1 = borderList.get(j);
                                if (bP1.Id == bP.Id) {
                                    pIdx = j;
                                    innerIdx = bP1.BInnerIdx;
                                    timesArray[pIdx] += 1;
                                    bIdxList.add(pIdx);
                                    borderIdx2 = bP1.BorderIdx;
                                    if (bP.BorderIdx > 0 && bP.BorderIdx == bP1.BorderIdx) {
                                        sameBorderIdx = true;
                                    }
                                    if (innerStart && bP1.BorderIdx == 0) {
                                        for (int bidx : bIdxList) {
                                            timesArray[bidx] -= 1;
                                        }
                                        isRepeat = true;
                                    }
                                    break;
                                }
                            }
                        }
                        if (isRepeat) {
                            break;
                        }
                    }

                    if (pIdx == i) {
                        if (aPList.size() > 0) {
                            if (sameBorderIdx) {
                                boolean isTooBig = false;
                                int baseNum = 0;
                                for (int idx = 0; idx < bP.BorderIdx; idx++) {
                                    baseNum += pNums[idx];
                                }
                                int sIdx = baseNum;
                                int eIdx = baseNum + pNums[bP.BorderIdx];
                                int theIdx = sIdx;
                                for (int idx = sIdx; idx < eIdx; idx++) {
                                    if (!bIdxList.contains(idx)) {
                                        theIdx = idx;
                                        break;
                                    }
                                }
                                if (pointInPolygon(aPList, borderList.get(theIdx).Point)) {
                                    isTooBig = true;
                                }

                                if (isTooBig) {
                                    break;
                                }
                            }
                            aPolygon = new Polygon();
                            aPolygon.IsBorder = true;
                            aPolygon.IsInnerBorder = sameBorderIdx;
                            aPolygon.LowValue = aValue;
                            aPolygon.HighValue = bValue;
                            aBound = new Extent();
                            aPolygon.Area = getExtentAndArea(aPList, aBound);
                            aPolygon.IsClockWise = false;
                            aPolygon.StartPointIdx = lineBorderList.size() - 1;
                            aPolygon.Extent = aBound;
                            aPolygon.OutLine.PointList = aPList;
                            aPolygon.OutLine.Value = aValue;
                            aPolygon.IsHighCenter = true;
                            if (aValue == bValue) {
                                if (cValue < aValue) {
                                    aPolygon.IsHighCenter = false;
                                }
                            }
                            aPolygon.OutLine.Type = "Border";
                            aPolygon.HoleLines = new ArrayList<>();
                            aPolygonList.add(aPolygon);
                        }
                        break;
                    }
                    pIdx += -1;
                    innerIdx += -1;
                    if (borderIdx1 != borderIdx2) {
                        borderIdx1 = borderIdx2;
                    }
                    if (innerIdx == -1) {
                        pIdx = pIdx + pNums[borderIdx1];
                        innerIdx = pNums[borderIdx1] - 1;
                    }
                } while (true);
            }
        }

        //---- tracing close polygons
        List<Polygon> cPolygonlist = new ArrayList<>();
        boolean isInserted;
        for (i = 0; i < aLineList.size(); i++) {
            aLine = aLineList.get(i);
            if (aLine.Type.equals("Close")) {
                aPolygon = new Polygon();
                aPolygon.IsBorder = false;
                aPolygon.LowValue = aLine.Value;
                aPolygon.HighValue = aLine.Value;
                aBound = new Extent();
                aPolygon.Area = getExtentAndArea(aLine.PointList, aBound);
                aPolygon.IsClockWise = isClockwise(aLine.PointList);
                aPolygon.Extent = aBound;
                aPolygon.OutLine = aLine;
                aPolygon.IsHighCenter = true;
                aPolygon.HoleLines = new ArrayList<>();

                //---- Sort from big to small
                isInserted = false;
                for (j = 0; j < cPolygonlist.size(); j++) {
                    if (aPolygon.Area > (cPolygonlist.get(j)).Area) {
                        cPolygonlist.add(j, aPolygon);
                        isInserted = true;
                        break;
                    }
                }
                if (!isInserted) {
                    cPolygonlist.add(aPolygon);
                }
            }
        }

        //---- Juge isHighCenter for border polygons
        if (aPolygonList.isEmpty()) {
            aLine = new PolyLine();
            aLine.Type = "Border";
            //aLine.Value = contour[0];
            aLine.Value = borderList.get(0).Value;
            aLine.PointList = new ArrayList<>(aBorder.LineList.get(0).pointList);

            if (aLine.PointList.size() > 0) {
                aPolygon = new Polygon();
                aPolygon.LowValue = aLine.Value;
                aPolygon.HighValue = aLine.Value;
                aBound = new Extent();
                aPolygon.Area = getExtentAndArea(aLine.PointList, aBound);
                aPolygon.IsClockWise = isClockwise(aLine.PointList);
                aPolygon.Extent = aBound;
                aPolygon.OutLine = aLine;
                aPolygon.IsHighCenter = false;
                aPolygonList.add(aPolygon);
            }
        }

        //---- Add close polygons to form total polygons list
        aPolygonList.addAll(cPolygonlist);

        //---- Juge siHighCenter for close polygons
        Extent cBound1;
        Extent cBound2;
        int polygonNum = aPolygonList.size();
        Polygon bPolygon;
        for (i = polygonNum - 1; i >= 0; i += -1) {
            aPolygon = aPolygonList.get(i);
            if (aPolygon.OutLine.Type.equals("Close")) {
                cBound1 = aPolygon.Extent;
                aValue = aPolygon.LowValue;
                aPoint = aPolygon.OutLine.PointList.get(0);
                for (j = i - 1; j >= 0; j += -1) {
                    bPolygon = aPolygonList.get(j);
                    cBound2 = bPolygon.Extent;
                    bValue = bPolygon.LowValue;
                    newPList = new ArrayList<>(bPolygon.OutLine.PointList);
                    if (pointInPolygon(newPList, aPoint)) {
                        if (cBound1.xMin > cBound2.xMin & cBound1.yMin > cBound2.yMin & cBound1.xMax < cBound2.xMax & cBound1.yMax < cBound2.yMax) {
                            if (aValue < bValue) {
                                aPolygon.IsHighCenter = false;
                            } else if (aValue == bValue) {
                                if (bPolygon.IsHighCenter) {
                                    aPolygon.IsHighCenter = false;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        return aPolygonList;
    }

    private static List<Polygon> addPolygonHoles(List<Polygon> polygonList) {
        List<Polygon> holePolygons = new ArrayList<>();
        int i, j;
        for (i = 0; i < polygonList.size(); i++) {
            Polygon aPolygon = polygonList.get(i);
            if (!aPolygon.IsBorder) {
                aPolygon.HoleIndex = 1;
                holePolygons.add(aPolygon);
            }
        }

        if (holePolygons.isEmpty()) {
            return polygonList;
        } else {
            List<Polygon> newPolygons = new ArrayList<>();
            for (i = 1; i < holePolygons.size(); i++) {
                Polygon aPolygon = holePolygons.get(i);
                for (j = i - 1; j >= 0; j--) {
                    Polygon bPolygon = holePolygons.get(j);
                    if (bPolygon.Extent.Include(aPolygon.Extent)) {
                        if (pointInPolygon(bPolygon.OutLine.PointList, aPolygon.OutLine.PointList.get(0))) {
                            aPolygon.HoleIndex = bPolygon.HoleIndex + 1;
                            bPolygon.AddHole(aPolygon);
                            break;
                        }
                    }
                }
            }
            List<Polygon> hole1Polygons = new ArrayList<>();
            for (i = 0; i < holePolygons.size(); i++) {
                if (holePolygons.get(i).HoleIndex == 1) {
                    hole1Polygons.add(holePolygons.get(i));
                }
            }

            for (i = 0; i < polygonList.size(); i++) {
                Polygon aPolygon = polygonList.get(i);
                if (aPolygon.IsBorder == true) {
                    for (j = 0; j < hole1Polygons.size(); j++) {
                        Polygon bPolygon = hole1Polygons.get(j);
                        if (aPolygon.Extent.Include(bPolygon.Extent)) {
                            if (pointInPolygon(aPolygon.OutLine.PointList, bPolygon.OutLine.PointList.get(0))) {
                                aPolygon.AddHole(bPolygon);
                            }
                        }
                    }
                    newPolygons.add(aPolygon);
                }
            }
            newPolygons.addAll(holePolygons);

            return newPolygons;
        }
    }

    private static List<Polygon> addPolygonHoles_Ring(List<Polygon> polygonList) {
        List<Polygon> holePolygons = new ArrayList<>();
        int i, j;
        for (i = 0; i < polygonList.size(); i++) {
            Polygon aPolygon = polygonList.get(i);
            if (!aPolygon.IsBorder || aPolygon.IsInnerBorder) {
                aPolygon.HoleIndex = 1;
                holePolygons.add(aPolygon);
            }
        }

        if (holePolygons.isEmpty()) {
            return polygonList;
        } else {
            List<Polygon> newPolygons = new ArrayList<>();
            for (i = 1; i < holePolygons.size(); i++) {
                Polygon aPolygon = holePolygons.get(i);
                for (j = i - 1; j >= 0; j--) {
                    Polygon bPolygon = holePolygons.get(j);
                    if (bPolygon.Extent.Include(aPolygon.Extent)) {
                        if (pointInPolygon(bPolygon.OutLine.PointList, aPolygon.OutLine.PointList.get(0))) {
                            aPolygon.HoleIndex = bPolygon.HoleIndex + 1;
                            bPolygon.AddHole(aPolygon);
                            break;
                        }
                    }
                }
            }
            List<Polygon> hole1Polygons = new ArrayList<>();
            for (i = 0; i < holePolygons.size(); i++) {
                if (holePolygons.get(i).HoleIndex == 1) {
                    hole1Polygons.add(holePolygons.get(i));
                }
            }

            for (i = 0; i < polygonList.size(); i++) {
                Polygon aPolygon = polygonList.get(i);
                if (aPolygon.IsBorder && !aPolygon.IsInnerBorder) {
                    for (j = 0; j < hole1Polygons.size(); j++) {
                        Polygon bPolygon = hole1Polygons.get(j);
                        if (aPolygon.Extent.Include(bPolygon.Extent)) {
                            if (pointInPolygon(aPolygon.OutLine.PointList, bPolygon.OutLine.PointList.get(0))) {
                                aPolygon.AddHole(bPolygon);
                            }
                        }
                    }
                    newPolygons.add(aPolygon);
                }
            }
            newPolygons.addAll(holePolygons);

            return newPolygons;
        }
    }

    private static void addHoles_Ring(List<Polygon> polygonList, List<List<PointD>> holeList) {
        int i, j;
        for (i = 0; i < holeList.size(); i++) {
            List<PointD> holePs = holeList.get(i);
            Extent aExtent = getExtent(holePs);
            for (j = polygonList.size() - 1; j >= 0; j--) {
                Polygon aPolygon = polygonList.get(j);
                if (aPolygon.Extent.Include(aExtent)) {
                    boolean isHole = true;
                    for (PointD aP : holePs) {
                        if (!pointInPolygon(aPolygon.OutLine.PointList, aP)) {
                            isHole = false;
                            break;
                        }
                    }
                    if (isHole) {
                        aPolygon.AddHole(holePs);
                        break;
                    }
                }
            }
        }
    }

    //</editor-fold>
    // <editor-fold desc="Clipping">
    private static List<PolyLine> cutPolyline(PolyLine inPolyline, List<PointD> clipPList) {
        List<PolyLine> newPolylines = new ArrayList<>();
        List<PointD> aPList = inPolyline.PointList;
        Extent plExtent = getExtent(aPList);
        Extent cutExtent = getExtent(clipPList);

        if (!isExtentCross(plExtent, cutExtent)) {
            return newPolylines;
        }

        int i, j;

        if (!isClockwise(clipPList)) //---- Make cut polygon clockwise
        {
            Collections.reverse(clipPList);
        }

        //Judge if all points of the polyline are in the cut polygon
        if (pointInPolygon(clipPList, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInPolygon(clipPList, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            //Put start point outside of the cut polygon
            if (!isAllIn) {
                if (inPolyline.Type.equals("Close")) {
                    List<PointD> bPList = new ArrayList<>();
                    for (i = notInIdx; i < aPList.size(); i++) {
                        bPList.add(aPList.get(i));
                    }

                    for (i = 1; i < notInIdx; i++) {
                        bPList.add(aPList.get(i));
                    }

                    bPList.add(bPList.get(0));
                    aPList = new ArrayList<>(bPList);
                } else {
                    Collections.reverse(aPList);
                }
            } else //the input polygon is inside the cut polygon
            {
                newPolylines.add(inPolyline);
                return newPolylines;
            }
        }

        //Cutting            
        boolean isInPolygon = pointInPolygon(clipPList, aPList.get(0));
        PointD q1, q2, p1, p2, IPoint;
        Line lineA, lineB;
        List<PointD> newPlist = new ArrayList<>();
        PolyLine bLine;
        p1 = aPList.get(0);
        for (i = 1; i < aPList.size(); i++) {
            p2 = aPList.get(i);
            if (pointInPolygon(clipPList, p2)) {
                if (!isInPolygon) {
                    IPoint = new PointD();
                    lineA = new Line();
                    lineA.P1 = p1;
                    lineA.P2 = p2;
                    q1 = clipPList.get(clipPList.size() - 1);
                    for (j = 0; j < clipPList.size(); j++) {
                        q2 = clipPList.get(j);
                        lineB = new Line();
                        lineB.P1 = q1;
                        lineB.P2 = q2;
                        if (isLineSegmentCross(lineA, lineB)) {
                            IPoint = getCrossPoint(lineA, lineB);
                            break;
                        }
                        q1 = q2;
                    }
                    newPlist.add(IPoint);
                }
                newPlist.add(aPList.get(i));
                isInPolygon = true;
            } else if (isInPolygon) {
                IPoint = new PointD();
                lineA = new Line();
                lineA.P1 = p1;
                lineA.P2 = p2;
                q1 = clipPList.get(clipPList.size() - 1);
                for (j = 0; j < clipPList.size(); j++) {
                    q2 = clipPList.get(j);
                    lineB = new Line();
                    lineB.P1 = q1;
                    lineB.P2 = q2;
                    if (isLineSegmentCross(lineA, lineB)) {
                        IPoint = getCrossPoint(lineA, lineB);
                        break;
                    }
                    q1 = q2;
                }
                newPlist.add(IPoint);

                bLine = new PolyLine();
                bLine.Value = inPolyline.Value;
                bLine.Type = inPolyline.Type;
                bLine.PointList = newPlist;
                newPolylines.add(bLine);
                isInPolygon = false;
                newPlist = new ArrayList<>();
            }
            p1 = p2;
        }

        if (isInPolygon && newPlist.size() > 1) {
            bLine = new PolyLine();
            bLine.Value = inPolyline.Value;
            bLine.Type = inPolyline.Type;
            bLine.PointList = newPlist;
            newPolylines.add(bLine);
        }

        return newPolylines;
    }

    private static List<Polygon> cutPolygon_Hole(Polygon inPolygon, List<PointD> clipPList) {
        List<Polygon> newPolygons = new ArrayList<>();
        List<PolyLine> newPolylines = new ArrayList<>();
        List<PointD> aPList = inPolygon.OutLine.PointList;
        Extent plExtent = getExtent(aPList);
        Extent cutExtent = getExtent(clipPList);

        if (!isExtentCross(plExtent, cutExtent)) {
            return newPolygons;
        }

        int i, j;

        if (!isClockwise(clipPList)) //---- Make cut polygon clockwise
        {
            Collections.reverse(clipPList);
        }

        //Judge if all points of the polyline are in the cut polygon - outline   
        List<List<PointD>> newLines = new ArrayList<>();
        if (pointInPolygon(clipPList, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInPolygon(clipPList, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                List<PointD> bPList = new ArrayList<>();
                for (i = notInIdx; i < aPList.size(); i++) {
                    bPList.add(aPList.get(i));
                }

                for (i = 1; i < notInIdx; i++) {
                    bPList.add(aPList.get(i));
                }

                bPList.add(bPList.get(0));
                newLines.add(bPList);
            } else //the input polygon is inside the cut polygon
            {
                newPolygons.add(inPolygon);
                return newPolygons;
            }
        } else {
            newLines.add(aPList);
        }

        //Holes
        List<List<PointD>> holeLines = new ArrayList<>();
        for (int h = 0; h < inPolygon.HoleLines.size(); h++) {
            List<PointD> holePList = inPolygon.HoleLines.get(h).PointList;
            plExtent = getExtent(holePList);
            if (!isExtentCross(plExtent, cutExtent)) {
                continue;
            }

            if (pointInPolygon(clipPList, holePList.get(0))) {
                boolean isAllIn = true;
                int notInIdx = 0;
                for (i = 0; i < holePList.size(); i++) {
                    if (!pointInPolygon(clipPList, holePList.get(i))) {
                        notInIdx = i;
                        isAllIn = false;
                        break;
                    }
                }
                if (!isAllIn) //Put start point outside of the cut polygon
                {
                    List<PointD> bPList = new ArrayList<>();
                    for (i = notInIdx; i < holePList.size(); i++) {
                        bPList.add(holePList.get(i));
                    }

                    for (i = 1; i < notInIdx; i++) {
                        bPList.add(holePList.get(i));
                    }

                    bPList.add(bPList.get(0));
                    newLines.add(bPList);
                } else //the hole is inside the cut polygon
                {
                    holeLines.add(holePList);
                }
            } else {
                newLines.add(holePList);
            }
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        for (PointD aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting                     
        for (int l = 0; l < newLines.size(); l++) {
            aPList = newLines.get(l);
            boolean isInPolygon = false;
            PointD q1, q2, p1, p2, IPoint;
            Line lineA, lineB;
            List<PointD> newPlist = new ArrayList<>();
            PolyLine bLine;
            p1 = aPList.get(0);
            int inIdx = -1, outIdx = -1;
            boolean newLine = true;
            int a1 = 0;
            for (i = 1; i < aPList.size(); i++) {
                p2 = aPList.get(i);
                if (pointInPolygon(clipPList, p2)) {
                    if (!isInPolygon) {
                        lineA = new Line();
                        lineA.P1 = p1;
                        lineA.P2 = p2;
                        q1 = borderList.get(borderList.size() - 1).Point;
                        IPoint = new PointD();
                        for (j = 0; j < borderList.size(); j++) {
                            q2 = borderList.get(j).Point;
                            lineB = new Line();
                            lineB.P1 = q1;
                            lineB.P2 = q2;
                            if (isLineSegmentCross(lineA, lineB)) {
                                IPoint = getCrossPoint(lineA, lineB);
                                aBP = new BorderPoint();
                                aBP.Id = newPolylines.size();
                                aBP.Point = IPoint;
                                borderList.add(j, aBP);
                                inIdx = j;
                                break;
                            }
                            q1 = q2;
                        }
                        newPlist.add(IPoint);
                    }
                    newPlist.add(aPList.get(i));
                    isInPolygon = true;
                } else if (isInPolygon) {
                    lineA = new Line();
                    lineA.P1 = p1;
                    lineA.P2 = p2;
                    q1 = borderList.get(borderList.size() - 1).Point;
                    IPoint = new PointD();
                    for (j = 0; j < borderList.size(); j++) {
                        q2 = borderList.get(j).Point;
                        lineB = new Line();
                        lineB.P1 = q1;
                        lineB.P2 = q2;
                        if (isLineSegmentCross(lineA, lineB)) {
                            if (!newLine) {
                                if (inIdx - outIdx >= 1 && inIdx - outIdx <= 10) {
                                    if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                        borderList.remove(inIdx);
                                        borderList.add(outIdx, aBP);
                                    }
                                } else if (inIdx - outIdx <= -1 && inIdx - outIdx >= -10) {
                                    if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                        borderList.remove(inIdx);
                                        borderList.add(outIdx + 1, aBP);
                                    }
                                } else if (inIdx == outIdx) {
                                    if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                        borderList.remove(inIdx);
                                        borderList.add(inIdx + 1, aBP);
                                    }
                                }
                            }
                            IPoint = getCrossPoint(lineA, lineB);
                            aBP = new BorderPoint();
                            aBP.Id = newPolylines.size();
                            aBP.Point = IPoint;
                            borderList.add(j, aBP);
                            outIdx = j;
                            a1 = inIdx;

                            newLine = false;
                            break;
                        }
                        q1 = q2;
                    }
                    newPlist.add(IPoint);

                    bLine = new PolyLine();
                    bLine.Value = inPolygon.OutLine.Value;
                    bLine.Type = inPolygon.OutLine.Type;
                    bLine.PointList = newPlist;
                    newPolylines.add(bLine);

                    isInPolygon = false;
                    newPlist = new ArrayList<>();
                }
                p1 = p2;
            }
        }

        if (newPolylines.size() > 0) {
            //Tracing polygons
            newPolygons = tracingClipPolygons(inPolygon, newPolylines, borderList);
        } else if (pointInPolygon(aPList, clipPList.get(0))) {
            Extent aBound = new Extent();
            Polygon aPolygon = new Polygon();
            aPolygon.IsBorder = true;
            aPolygon.LowValue = inPolygon.LowValue;
            aPolygon.HighValue = inPolygon.HighValue;
            aPolygon.Area = getExtentAndArea(clipPList, aBound);
            aPolygon.IsClockWise = true;
            aPolygon.Extent = aBound;
            aPolygon.OutLine.PointList = clipPList;
            aPolygon.OutLine.Value = inPolygon.LowValue;
            aPolygon.IsHighCenter = inPolygon.IsHighCenter;
            aPolygon.OutLine.Type = "Border";
            aPolygon.HoleLines = new ArrayList<>();

            newPolygons.add(aPolygon);
        }

        if (holeLines.size() > 0) {
            addHoles_Ring(newPolygons, holeLines);
        }

        return newPolygons;
    }

    private static List<Polygon> cutPolygon(Polygon inPolygon, List<PointD> clipPList) {
        List<Polygon> newPolygons = new ArrayList<>();
        List<PolyLine> newPolylines = new ArrayList<>();
        List<PointD> aPList = inPolygon.OutLine.PointList;
        Extent plExtent = getExtent(aPList);
        Extent cutExtent = getExtent(clipPList);

        if (!isExtentCross(plExtent, cutExtent)) {
            return newPolygons;
        }

        int i, j;

        if (!isClockwise(clipPList)) //---- Make cut polygon clockwise
        {
            Collections.reverse(clipPList);
        }

        //Judge if all points of the polyline are in the cut polygon            
        if (pointInPolygon(clipPList, aPList.get(0))) {
            boolean isAllIn = true;
            int notInIdx = 0;
            for (i = 0; i < aPList.size(); i++) {
                if (!pointInPolygon(clipPList, aPList.get(i))) {
                    notInIdx = i;
                    isAllIn = false;
                    break;
                }
            }
            if (!isAllIn) //Put start point outside of the cut polygon
            {
                List<PointD> bPList = new ArrayList<>();
                for (i = notInIdx; i < aPList.size(); i++) {
                    bPList.add(aPList.get(i));
                }

                for (i = 1; i < notInIdx; i++) {
                    bPList.add(aPList.get(i));
                }

                bPList.add(bPList.get(0));
                aPList = new ArrayList<>(bPList);
            } else //the input polygon is inside the cut polygon
            {
                newPolygons.add(inPolygon);
                return newPolygons;
            }
        }

        //Prepare border point list
        List<BorderPoint> borderList = new ArrayList<>();
        BorderPoint aBP = new BorderPoint();
        for (PointD aP : clipPList) {
            aBP = new BorderPoint();
            aBP.Point = aP;
            aBP.Id = -1;
            borderList.add(aBP);
        }

        //Cutting            
        boolean isInPolygon = false;
        PointD q1, q2, p1, p2, IPoint;
        Line lineA, lineB;
        List<PointD> newPlist = new ArrayList<>();
        PolyLine bLine;
        p1 = aPList.get(0);
        int inIdx = -1, outIdx = -1;
        int a1 = 0;
        boolean isNewLine = true;
        for (i = 1; i < aPList.size(); i++) {
            p2 = aPList.get(i);
            if (pointInPolygon(clipPList, p2)) {
                if (!isInPolygon) {
                    lineA = new Line();
                    lineA.P1 = p1;
                    lineA.P2 = p2;
                    q1 = borderList.get(borderList.size() - 1).Point;
                    IPoint = new PointD();
                    for (j = 0; j < borderList.size(); j++) {
                        q2 = borderList.get(j).Point;
                        lineB = new Line();
                        lineB.P1 = q1;
                        lineB.P2 = q2;
                        if (isLineSegmentCross(lineA, lineB)) {
                            IPoint = getCrossPoint(lineA, lineB);
                            aBP = new BorderPoint();
                            aBP.Id = newPolylines.size();
                            aBP.Point = IPoint;
                            borderList.add(j, aBP);
                            inIdx = j;
                            break;
                        }
                        q1 = q2;
                    }
                    newPlist.add(IPoint);
                }
                newPlist.add(aPList.get(i));
                isInPolygon = true;
            } else if (isInPolygon) {
                lineA = new Line();
                lineA.P1 = p1;
                lineA.P2 = p2;
                q1 = borderList.get(borderList.size() - 1).Point;
                IPoint = new PointD();
                for (j = 0; j < borderList.size(); j++) {
                    q2 = borderList.get(j).Point;
                    lineB = new Line();
                    lineB.P1 = q1;
                    lineB.P2 = q2;
                    if (isLineSegmentCross(lineA, lineB)) {
                        if (!isNewLine) {
                            if (inIdx - outIdx >= 1 && inIdx - outIdx <= 10) {
                                if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                    borderList.remove(inIdx);
                                    borderList.add(outIdx, aBP);
                                }
                            } else if (inIdx - outIdx <= -1 && inIdx - outIdx >= -10) {
                                if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                    borderList.remove(inIdx);
                                    borderList.add(outIdx + 1, aBP);
                                }
                            } else if (inIdx == outIdx) {
                                if (!twoPointsInside(a1, outIdx, inIdx, j)) {
                                    borderList.remove(inIdx);
                                    borderList.add(inIdx + 1, aBP);
                                }
                            }
                        }
                        IPoint = getCrossPoint(lineA, lineB);
                        aBP = new BorderPoint();
                        aBP.Id = newPolylines.size();
                        aBP.Point = IPoint;
                        borderList.add(j, aBP);
                        outIdx = j;
                        a1 = inIdx;
                        isNewLine = false;
                        break;
                    }
                    q1 = q2;
                }
                newPlist.add(IPoint);

                bLine = new PolyLine();
                bLine.Value = inPolygon.OutLine.Value;
                bLine.Type = inPolygon.OutLine.Type;
                bLine.PointList = newPlist;
                newPolylines.add(bLine);

                isInPolygon = false;
                newPlist = new ArrayList<>();
            }
            p1 = p2;
        }

        if (newPolylines.size() > 0) {
            //Tracing polygons
            newPolygons = tracingClipPolygons(inPolygon, newPolylines, borderList);
        } else if (pointInPolygon(aPList, clipPList.get(0))) {
            Extent aBound = new Extent();
            Polygon aPolygon = new Polygon();
            aPolygon.IsBorder = true;
            aPolygon.LowValue = inPolygon.LowValue;
            aPolygon.HighValue = inPolygon.HighValue;
            aPolygon.Area = getExtentAndArea(clipPList, aBound);
            aPolygon.IsClockWise = true;
            aPolygon.Extent = aBound;
            aPolygon.OutLine.PointList = clipPList;
            aPolygon.OutLine.Value = inPolygon.LowValue;
            aPolygon.IsHighCenter = inPolygon.IsHighCenter;
            aPolygon.OutLine.Type = "Border";
            aPolygon.HoleLines = new ArrayList<>();

            newPolygons.add(aPolygon);
        }

        return newPolygons;
    }

    private static boolean twoPointsInside(int a1, int a2, int b1, int b2) {
        if (a2 < a1) {
            a1 += 1;
        }
        if (b1 < a1) {
            a1 += 1;
        }
        if (b1 < a2) {
            a2 += 1;
        }

        if (a2 < a1) {
            int c = a1;
            a1 = a2;
            a2 = c;
        }

        if (b1 > a1 && b1 <= a2) {
            if (b2 > a1 && b2 <= a2) {
                return true;
            } else {
                return false;
            }
        } else if (!(b2 > a1 && b2 <= a2)) {
            return true;
        } else {
            return false;
        }
    }

    // </editor-fold>
    // <editor-fold desc="Smoothing">
    private static List<PointD> BSplineScanning(List<PointD> pointList, int sum) {
        float t;
        int i;
        double X, Y;
        PointD aPoint;
        List<PointD> newPList = new ArrayList<>();

        if (sum < 4) {
            return null;
        }

        boolean isClose = false;
        aPoint = pointList.get(0);
        PointD bPoint = pointList.get(sum - 1);
        if (aPoint.X == bPoint.X && aPoint.Y == bPoint.Y) {
            pointList.remove(0);
            pointList.add(pointList.get(0));
            pointList.add(pointList.get(1));
            pointList.add(pointList.get(2));
            pointList.add(pointList.get(3));
            pointList.add(pointList.get(4));
            pointList.add(pointList.get(5));
            pointList.add(pointList.get(6));
            isClose = true;
        }

        sum = pointList.size();
        for (i = 0; i < sum - 3; i++) {
            for (t = 0; t <= 1; t += 0.05F) {
                double[] xy = BSpline(pointList, t, i);
                X = xy[0];
                Y = xy[1];
                if (isClose) {
                    if (i > 3) {
                        aPoint = new PointD();
                        aPoint.X = X;
                        aPoint.Y = Y;
                        newPList.add(aPoint);
                    }
                } else {
                    aPoint = new PointD();
                    aPoint.X = X;
                    aPoint.Y = Y;
                    newPList.add(aPoint);
                }
            }
        }

        if (isClose) {
            newPList.add(newPList.get(0));
        } else {
            newPList.add(0, pointList.get(0));
            newPList.add(pointList.get(pointList.size() - 1));
        }

        return newPList;
    }

    private static double[] BSpline(List<PointD> pointList, double t, int i) {
        double[] f = new double[4];
        fb(t, f);
        int j;
        double X = 0;
        double Y = 0;
        PointD aPoint;
        for (j = 0; j < 4; j++) {
            aPoint = pointList.get(i + j);
            X = X + f[j] * aPoint.X;
            Y = Y + f[j] * aPoint.Y;
        }

        double[] xy = new double[2];
        xy[0] = X;
        xy[1] = Y;

        return xy;
    }

    private static double f0(double t) {
        return 1.0 / 6 * (-t + 1) * (-t + 1) * (-t + 1);
    }

    private static double f1(double t) {
        return 1.0 / 6 * (3 * t * t * t - 6 * t * t + 4);
    }

    private static double f2(double t) {
        return 1.0 / 6 * (-3 * t * t * t + 3 * t * t + 3 * t + 1);
    }

    private static double f3(double t) {
        return 1.0 / 6 * t * t * t;
    }

    private static void fb(double t, double[] fs) {
        fs[0] = f0(t);
        fs[1] = f1(t);
        fs[2] = f2(t);
        fs[3] = f3(t);
    }

    // </editor-fold>
    // <editor-fold desc="Streamline">
    /**
     * Tracing stream lines
     *
     * @param U U component array
     * @param V V component array
     * @param X X coordinate array
     * @param Y Y coordinate array
     * @param UNDEF undefine data
     * @param density stream line density
     * @return streamlines
     */
    public static List<PolyLine> tracingStreamline(double[][] U, double[][] V, double[] X, double[] Y, double UNDEF, int density) {
        List<PolyLine> streamLines = new ArrayList<>();
        int xNum = U[1].length;
        int yNum = U.length;
        double[][] Dx = new double[yNum][xNum];
        double[][] Dy = new double[yNum][xNum];
        double deltX = X[1] - X[0];
        double deltY = Y[1] - Y[0];
        if (density == 0) {
            density = 1;
        }
        double radius = deltX / (Math.pow(density, 2));
        double smallRadius = radius * 1.5;
        int i, j;

        //Normalize wind components
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                if (Math.abs(U[i][j] / UNDEF - 1) < 0.01) {
                    Dx[i][j] = 0.1;
                    Dy[i][j] = 0.1;
                } else {
                    double WS = Math.sqrt(U[i][j] * U[i][j] + V[i][j] * V[i][j]);
                    if (WS == 0) {
                        WS = 1;
                    }
                    Dx[i][j] = (U[i][j] / WS) * deltX / density;
                    Dy[i][j] = (V[i][j] / WS) * deltY / density;
                }
            }
        }

        //Flag the grid boxes
        List[][] SPoints = new ArrayList[yNum - 1][xNum - 1];
        int[][] flags = new int[yNum - 1][xNum - 1];
        for (i = 0; i < yNum - 1; i++) {
            for (j = 0; j < xNum - 1; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    flags[i][j] = 0;
                } else {
                    flags[i][j] = 1;
                }

                SPoints[i][j] = new ArrayList<BorderPoint>();
            }
        }

        //Tracing streamline      
        double dis;
        BorderPoint borderP;
        int lineN = 0;
        for (i = 0; i < yNum - 1; i++) {
            for (j = 0; j < xNum - 1; j++) {
                if (flags[i][j] == 0) //No streamline started form this grid box, a new streamline started
                {
                    List<PointD> pList = new ArrayList<>();
                    PointD aPoint = new PointD();
                    int ii, jj;
                    int loopN;
                    PolyLine aPL = new PolyLine();

                    //Start point - the center of the grid box
                    aPoint.X = X[j] + deltX / 2;
                    aPoint.Y = Y[i] + deltY / 2;
                    pList.add((PointD) aPoint.clone());
                    borderP = new BorderPoint();
                    borderP.Point = (PointD) aPoint.clone();
                    borderP.Id = lineN;
                    SPoints[i][j].add(borderP);
                    flags[i][j] = 1;    //Flag the grid box and no streamline will start from this box again
                    ii = i;
                    jj = j;
                    int loopLimit = 500;

                    //Tracing forward
                    loopN = 0;
                    while (loopN < loopLimit) {
                        //Trace next streamline point
                        int[] iijj = new int[2];
                        iijj[0] = ii;
                        iijj[1] = jj;
                        boolean isInDomain = tracingStreamlinePoint(aPoint, Dx, Dy, X, Y, iijj, true);
                        ii = iijj[0];
                        jj = iijj[1];

                        //Terminating the streamline
                        if (isInDomain) {
                            if (Math.abs(U[ii][jj] / UNDEF - 1) < 0.01 || Math.abs(U[ii][jj + 1] / UNDEF - 1) < 0.01
                                    || Math.abs(U[ii + 1][jj] / UNDEF - 1) < 0.01 || Math.abs(U[ii + 1][jj + 1] / UNDEF - 1) < 0.01) {
                                break;
                            } else {
                                boolean isTerminating = false;
                                for (BorderPoint sPoint : (List<BorderPoint>) SPoints[ii][jj]) {
                                    if (Math.sqrt((aPoint.X - sPoint.Point.X) * (aPoint.X - sPoint.Point.X)
                                            + (aPoint.Y - sPoint.Point.Y) * (aPoint.Y - sPoint.Point.Y)) < radius) {
                                        isTerminating = true;
                                        break;
                                    }
                                }
                                if (!isTerminating) {
                                    if (SPoints[ii][jj].size() > 1) {
                                        BorderPoint pointStart = (BorderPoint) SPoints[ii][jj].get(0);
                                        BorderPoint pointEnd = (BorderPoint) SPoints[ii][jj].get(1);
                                        if (!(lineN == pointStart.Id && lineN == pointEnd.Id)) {
                                            dis = distance_point2line(pointStart.Point, pointEnd.Point, aPoint);
                                            if (dis < smallRadius) {
                                                isTerminating = true;
                                            }
                                        }
                                    }
                                }
                                if (!isTerminating) {
                                    pList.add((PointD) aPoint.clone());
                                    borderP = new BorderPoint();
                                    borderP.Point = (PointD) aPoint.clone();
                                    borderP.Id = lineN;
                                    SPoints[ii][jj].add(borderP);
                                    flags[ii][jj] = 1;
                                } else {
                                    break;
                                }
                            }
                        } else {
                            break;
                        }

                        loopN += 1;
                    }

                    //Tracing backword
                    aPoint.X = X[j] + deltX / 2;
                    aPoint.Y = Y[i] + deltY / 2;
                    ii = i;
                    jj = j;
                    loopN = 0;
                    while (loopN < loopLimit) {
                        //Trace next streamline point
                        int[] iijj = new int[2];
                        iijj[0] = ii;
                        iijj[1] = jj;
                        boolean isInDomain = tracingStreamlinePoint(aPoint, Dx, Dy, X, Y, iijj, false);
                        ii = iijj[0];
                        jj = iijj[1];

                        //Terminating the streamline
                        if (isInDomain) {
                            if (Math.abs(U[ii][jj] / UNDEF - 1) < 0.01 || Math.abs(U[ii][jj + 1] / UNDEF - 1) < 0.01
                                    || Math.abs(U[ii + 1][jj] / UNDEF - 1) < 0.01 || Math.abs(U[ii + 1][jj + 1] / UNDEF - 1) < 0.01) {
                                break;
                            } else {
                                boolean isTerminating = false;
                                for (BorderPoint sPoint : (List<BorderPoint>) SPoints[ii][jj]) {
                                    if (Math.sqrt((aPoint.X - sPoint.Point.X) * (aPoint.X - sPoint.Point.X)
                                            + (aPoint.Y - sPoint.Point.Y) * (aPoint.Y - sPoint.Point.Y)) < radius) {
                                        isTerminating = true;
                                        break;
                                    }
                                }
                                if (!isTerminating) {
                                    if (SPoints[ii][jj].size() > 1) {
                                        BorderPoint pointStart = (BorderPoint) SPoints[ii][jj].get(0);
                                        BorderPoint pointEnd = (BorderPoint) SPoints[ii][jj].get(1);
                                        if (!(lineN == pointStart.Id && lineN == pointEnd.Id)) {
                                            dis = distance_point2line(pointStart.Point, pointEnd.Point, aPoint);
                                            if (dis < smallRadius) {
                                                isTerminating = true;
                                            }
                                        }
                                    }
                                }
                                if (!isTerminating) {
                                    pList.add(0, (PointD) aPoint.clone());
                                    borderP = new BorderPoint();
                                    borderP.Point = (PointD) aPoint.clone();
                                    borderP.Id = lineN;
                                    SPoints[ii][jj].add(borderP);
                                    flags[ii][jj] = 1;
                                } else {
                                    break;
                                }
                            }
                        } else {
                            break;
                        }

                        loopN += 1;
                    }
                    if (pList.size() > 1) {
                        aPL.PointList = pList;
                        streamLines.add(aPL);
                        lineN += 1;
                    }

                }
            }
        }

        //Return
        return streamLines;
    }

    private static boolean tracingStreamlinePoint(PointD aPoint, double[][] Dx, double[][] Dy, double[] X, double[] Y,
            int[] iijj, boolean isForward) {
        double a, b, c, d, val1, val2;
        double dx, dy;
        int xNum = X.length;
        int yNum = Y.length;
        double deltX = X[1] - X[0];
        double deltY = Y[1] - Y[0];
        int ii = iijj[0];
        int jj = iijj[1];

        //Interpolation the U/V displacement components to the point
        a = Dx[ii][jj];
        b = Dx[ii][jj + 1];
        c = Dx[ii + 1][jj];
        d = Dx[ii + 1][jj + 1];
        val1 = a + (c - a) * ((aPoint.Y - Y[ii]) / deltY);
        val2 = b + (d - b) * ((aPoint.Y - Y[ii]) / deltY);
        dx = val1 + (val2 - val1) * ((aPoint.X - X[jj]) / deltX);
        a = Dy[ii][jj];
        b = Dy[ii][jj + 1];
        c = Dy[ii + 1][jj];
        d = Dy[ii + 1][jj + 1];
        val1 = a + (c - a) * ((aPoint.Y - Y[ii]) / deltY);
        val2 = b + (d - b) * ((aPoint.Y - Y[ii]) / deltY);
        dy = val1 + (val2 - val1) * ((aPoint.X - X[jj]) / deltX);

        //Tracing forward by U/V displacement components            
        if (isForward) {
            aPoint.X += dx;
            aPoint.Y += dy;
        } else {
            aPoint.X -= dx;
            aPoint.Y -= dy;
        }

        //Find the grid box that the point is located 
        if (!(aPoint.X >= X[jj] && aPoint.X <= X[jj + 1] && aPoint.Y >= Y[ii] && aPoint.Y <= Y[ii + 1])) {
            if (aPoint.X < X[0] || aPoint.X > X[X.length - 1] || aPoint.Y < Y[0] || aPoint.Y > Y[Y.length - 1]) {
                return false;
            }

            //Get the grid box of the point located
            for (int ti = ii - 2; ti < ii + 3; ti++) {
                if (ti >= 0 && ti < yNum) {
                    if (aPoint.Y >= Y[ti] && aPoint.Y <= Y[ti + 1]) {
                        ii = ti;
                        for (int tj = jj - 2; tj < jj + 3; tj++) {
                            if (tj >= 0 && tj < xNum) {
                                if (aPoint.X >= X[tj] && aPoint.X <= X[tj + 1]) {
                                    jj = tj;
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        iijj[0] = ii;
        iijj[1] = jj;
        return true;
    }

    private static double distance_point2line(PointD pt1, PointD pt2, PointD point) {
        double k = (pt2.Y - pt1.Y) / (pt2.X - pt1.X);
        double x = (k * k * pt1.X + k * (point.Y - pt1.Y) + point.X) / (k * k + 1);
        double y = k * (x - pt1.X) + pt1.Y;
        double dis = Math.sqrt((point.Y - y) * (point.Y - y) + (point.X - x) * (point.X - x));
        return dis;
    }

    // </editor-fold>    
    // <editor-fold desc="Others">
    private static Extent getExtent(List<PointD> pList) {
        double minX, minY, maxX, maxY;
        int i;
        PointD aPoint;
        aPoint = pList.get(0);
        minX = aPoint.X;
        maxX = aPoint.X;
        minY = aPoint.Y;
        maxY = aPoint.Y;
        for (i = 1; i < pList.size(); i++) {
            aPoint = pList.get(i);
            if (aPoint.X < minX) {
                minX = aPoint.X;
            }

            if (aPoint.X > maxX) {
                maxX = aPoint.X;
            }

            if (aPoint.Y < minY) {
                minY = aPoint.Y;
            }

            if (aPoint.Y > maxY) {
                maxY = aPoint.Y;
            }
        }

        Extent aExtent = new Extent();
        aExtent.xMin = minX;
        aExtent.yMin = minY;
        aExtent.xMax = maxX;
        aExtent.yMax = maxY;

        return aExtent;
    }

    private static double getExtentAndArea(List<PointD> pList, Extent aExtent) {
        double bArea, minX, minY, maxX, maxY;
        int i;
        PointD aPoint;
        aPoint = pList.get(0);
        minX = aPoint.X;
        maxX = aPoint.X;
        minY = aPoint.Y;
        maxY = aPoint.Y;
        for (i = 1; i < pList.size(); i++) {
            aPoint = pList.get(i);
            if (aPoint.X < minX) {
                minX = aPoint.X;
            }

            if (aPoint.X > maxX) {
                maxX = aPoint.X;
            }

            if (aPoint.Y < minY) {
                minY = aPoint.Y;
            }

            if (aPoint.Y > maxY) {
                maxY = aPoint.Y;
            }
        }

        aExtent.xMin = minX;
        aExtent.yMin = minY;
        aExtent.xMax = maxX;
        aExtent.yMax = maxY;
        bArea = (maxX - minX) * (maxY - minY);

        return bArea;
    }

    /**
     * Determine if the point list is clockwise
     *
     * @param pointList point list
     * @return is or not clockwise
     */
    public static boolean isClockwise(List<PointD> pointList) {
        int i;
        PointD aPoint;
        double yMax = 0;
        int yMaxIdx = 0;
        for (i = 0; i < pointList.size() - 1; i++) {
            aPoint = pointList.get(i);
            if (i == 0) {
                yMax = aPoint.Y;
                yMaxIdx = 0;
            } else if (yMax < aPoint.Y) {
                yMax = aPoint.Y;
                yMaxIdx = i;
            }
        }
        PointD p1, p2, p3;
        int p1Idx, p2Idx, p3Idx;
        p1Idx = yMaxIdx - 1;
        p2Idx = yMaxIdx;
        p3Idx = yMaxIdx + 1;
        if (yMaxIdx == 0) {
            p1Idx = pointList.size() - 2;
        }

        p1 = pointList.get(p1Idx);
        p2 = pointList.get(p2Idx);
        p3 = pointList.get(p3Idx);
        if ((p3.X - p1.X) * (p2.Y - p1.Y) - (p2.X - p1.X) * (p3.Y - p1.Y) > 0) {
            return true;
        } else {
            return false;
        }

    }

    private static boolean isLineSegmentCross(Line lineA, Line lineB) {
        Extent boundA = new Extent(), boundB = new Extent();
        List<PointD> PListA = new ArrayList<>(), PListB = new ArrayList<>();
        PListA.add(lineA.P1);
        PListA.add(lineA.P2);
        PListB.add(lineB.P1);
        PListB.add(lineB.P2);
        getExtentAndArea(PListA, boundA);
        getExtentAndArea(PListB, boundB);

        if (!isExtentCross(boundA, boundB)) {
            return false;
        } else {
            double XP1 = (lineB.P1.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                    - (lineA.P2.X - lineA.P1.X) * (lineB.P1.Y - lineA.P1.Y);
            double XP2 = (lineB.P2.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                    - (lineA.P2.X - lineA.P1.X) * (lineB.P2.Y - lineA.P1.Y);
            if (XP1 * XP2 > 0) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean isExtentCross(Extent aBound, Extent bBound) {
        if (aBound.xMin > bBound.xMax || aBound.xMax < bBound.xMin || aBound.yMin > bBound.yMax
                || aBound.yMax < bBound.yMin) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * Get cross point of two line segments
     *
     * @param aP1 point 1 of line a
     * @param aP2 point 2 of line a
     * @param bP1 point 1 of line b
     * @param bP2 point 2 of line b
     * @return cross point
     */
    public static PointF getCrossPoint(PointF aP1, PointF aP2, PointF bP1, PointF bP2) {
        PointF IPoint = new PointF(0, 0);
        PointF p1, p2, q1, q2;
        double tempLeft, tempRight;

        double XP1 = (bP1.X - aP1.X) * (aP2.Y - aP1.Y)
                - (aP2.X - aP1.X) * (bP1.Y - aP1.Y);
        double XP2 = (bP2.X - aP1.X) * (aP2.Y - aP1.Y)
                - (aP2.X - aP1.X) * (bP2.Y - aP1.Y);
        if (XP1 == 0) {
            IPoint = bP1;
        } else if (XP2 == 0) {
            IPoint = bP2;
        } else {
            p1 = aP1;
            p2 = aP2;
            q1 = bP1;
            q2 = bP2;

            tempLeft = (q2.X - q1.X) * (p1.Y - p2.Y) - (p2.X - p1.X) * (q1.Y - q2.Y);
            tempRight = (p1.Y - q1.Y) * (p2.X - p1.X) * (q2.X - q1.X) + q1.X * (q2.Y - q1.Y) * (p2.X - p1.X) - p1.X * (p2.Y - p1.Y) * (q2.X - q1.X);
            IPoint.X = (float) (tempRight / tempLeft);

            tempLeft = (p1.X - p2.X) * (q2.Y - q1.Y) - (p2.Y - p1.Y) * (q1.X - q2.X);
            tempRight = p2.Y * (p1.X - p2.X) * (q2.Y - q1.Y) + (q2.X - p2.X) * (q2.Y - q1.Y) * (p1.Y - p2.Y) - q2.Y * (q1.X - q2.X) * (p2.Y - p1.Y);
            IPoint.Y = (float) (tempRight / tempLeft);
        }

        return IPoint;
    }

    private static PointD getCrossPoint(Line lineA, Line lineB) {
        PointD IPoint = new PointD();
        PointD p1, p2, q1, q2;
        double tempLeft, tempRight;

        double XP1 = (lineB.P1.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                - (lineA.P2.X - lineA.P1.X) * (lineB.P1.Y - lineA.P1.Y);
        double XP2 = (lineB.P2.X - lineA.P1.X) * (lineA.P2.Y - lineA.P1.Y)
                - (lineA.P2.X - lineA.P1.X) * (lineB.P2.Y - lineA.P1.Y);
        if (XP1 == 0) {
            IPoint = lineB.P1;
        } else if (XP2 == 0) {
            IPoint = lineB.P2;
        } else {
            p1 = lineA.P1;
            p2 = lineA.P2;
            q1 = lineB.P1;
            q2 = lineB.P2;

            tempLeft = (q2.X - q1.X) * (p1.Y - p2.Y) - (p2.X - p1.X) * (q1.Y - q2.Y);
            tempRight = (p1.Y - q1.Y) * (p2.X - p1.X) * (q2.X - q1.X) + q1.X * (q2.Y - q1.Y) * (p2.X - p1.X) - p1.X * (p2.Y - p1.Y) * (q2.X - q1.X);
            IPoint.X = tempRight / tempLeft;

            tempLeft = (p1.X - p2.X) * (q2.Y - q1.Y) - (p2.Y - p1.Y) * (q1.X - q2.X);
            tempRight = p2.Y * (p1.X - p2.X) * (q2.Y - q1.Y) + (q2.X - p2.X) * (q2.Y - q1.Y) * (p1.Y - p2.Y) - q2.Y * (q1.X - q2.X) * (p2.Y - p1.Y);
            IPoint.Y = tempRight / tempLeft;
        }

        return IPoint;
    }

    private static List<BorderPoint> insertPoint2Border(List<BorderPoint> bPList, List<BorderPoint> aBorderList) {
        BorderPoint aBPoint, bP;
        int i, j;
        PointD p1, p2, p3;
        List<BorderPoint> BorderList = new ArrayList<>(aBorderList);

        for (i = 0; i < bPList.size(); i++) {
            bP = bPList.get(i);
            p3 = bP.Point;
            aBPoint = BorderList.get(0);
            p1 = aBPoint.Point;
            for (j = 1; j < BorderList.size(); j++) {
                aBPoint = BorderList.get(j);
                p2 = aBPoint.Point;
                if ((p3.X - p1.X) * (p3.X - p2.X) <= 0) {
                    if ((p3.Y - p1.Y) * (p3.Y - p2.Y) <= 0) {
                        if ((p3.X - p1.X) * (p2.Y - p1.Y) - (p2.X - p1.X) * (p3.Y - p1.Y) == 0) {
                            BorderList.add(j, bP);
                            break;
                        }
                    }
                }
                p1 = p2;
            }
        }

        return BorderList;
    }

    private static List<BorderPoint> insertPoint2RectangleBorder(List<PolyLine> LineList, Extent aBound) {
        BorderPoint bPoint, bP;
        PolyLine aLine;
        PointD aPoint;
        int i, j, k;
        List<BorderPoint> LBPList = new ArrayList<>(), TBPList = new ArrayList<>();
        List<BorderPoint> RBPList = new ArrayList<>(), BBPList = new ArrayList<>();
        List<BorderPoint> BorderList = new ArrayList<>();
        List<PointD> aPointList;
        boolean IsInserted;

        //---- Get four border point list
        for (i = 0; i < LineList.size(); i++) {
            aLine = LineList.get(i);
            if (!"Close".equals(aLine.Type)) {
                aPointList = new ArrayList<>(aLine.PointList);
                bP = new BorderPoint();
                bP.Id = i;
                for (k = 0; k <= 1; k++) {
                    if (k == 0) {
                        aPoint = aPointList.get(0);
                    } else {
                        aPoint = aPointList.get(aPointList.size() - 1);
                    }

                    bP.Point = aPoint;
                    IsInserted = false;
                    if (aPoint.X == aBound.xMin) {
                        for (j = 0; j < LBPList.size(); j++) {
                            bPoint = LBPList.get(j);
                            if (aPoint.Y < bPoint.Point.Y) {
                                LBPList.add(j, bP);
                                IsInserted = true;
                                break;
                            }
                        }
                        if (!IsInserted) {
                            LBPList.add(bP);
                        }

                    } else if (aPoint.X == aBound.xMax) {
                        for (j = 0; j < RBPList.size(); j++) {
                            bPoint = RBPList.get(j);
                            if (aPoint.Y > bPoint.Point.Y) {
                                RBPList.add(j, bP);
                                IsInserted = true;
                                break;
                            }
                        }
                        if (!IsInserted) {
                            RBPList.add(bP);
                        }

                    } else if (aPoint.Y == aBound.yMin) {
                        for (j = 0; j < BBPList.size(); j++) {
                            bPoint = BBPList.get(j);
                            if (aPoint.X > bPoint.Point.X) {
                                BBPList.add(j, bP);
                                IsInserted = true;
                                break;
                            }
                        }
                        if (!IsInserted) {
                            BBPList.add(bP);
                        }

                    } else if (aPoint.Y == aBound.yMax) {
                        for (j = 0; j < TBPList.size(); j++) {
                            bPoint = TBPList.get(j);
                            if (aPoint.X < bPoint.Point.X) {
                                TBPList.add(j, bP);
                                IsInserted = true;
                                break;
                            }
                        }
                        if (!IsInserted) {
                            TBPList.add(bP);
                        }

                    }
                }
            }
        }

        //---- Get border list
        bP = new BorderPoint();
        bP.Id = -1;

        aPoint = new PointD();
        aPoint.X = aBound.xMin;
        aPoint.Y = aBound.yMin;
        bP.Point = aPoint;
        BorderList.add(bP);

        BorderList.addAll(LBPList);

        bP = new BorderPoint();
        bP.Id = -1;
        aPoint = new PointD();
        aPoint.X = aBound.xMin;
        aPoint.Y = aBound.yMax;
        bP.Point = aPoint;
        BorderList.add(bP);

        BorderList.addAll(TBPList);

        bP = new BorderPoint();
        bP.Id = -1;
        aPoint = new PointD();
        aPoint.X = aBound.xMax;
        aPoint.Y = aBound.yMax;
        bP.Point = aPoint;
        BorderList.add(bP);

        BorderList.addAll(RBPList);

        bP = new BorderPoint();
        bP.Id = -1;
        aPoint = new PointD();
        aPoint.X = aBound.xMax;
        aPoint.Y = aBound.yMin;
        bP.Point = aPoint;
        BorderList.add(bP);

        BorderList.addAll(BBPList);

        BorderList.add(BorderList.get(0));

        return BorderList;
    }

    private static List<BorderPoint> insertEndPoint2Border(List<EndPoint> EPList, List<BorderPoint> aBorderList) {
        BorderPoint aBPoint, bP;
        int i, j, k;
        PointD p1, p2;
        List<EndPoint> aEPList;
        List<EndPoint> temEPList = new ArrayList<>();
        ArrayList dList = new ArrayList();
        EndPoint aEP;
        double dist;
        boolean IsInsert;
        List<BorderPoint> BorderList = new ArrayList<>();

        aEPList = new ArrayList<>(EPList);

        aBPoint = aBorderList.get(0);
        p1 = aBPoint.Point;
        BorderList.add(aBPoint);
        for (i = 1; i < aBorderList.size(); i++) {
            aBPoint = aBorderList.get(i);
            p2 = aBPoint.Point;
            temEPList.clear();
            for (j = 0; j < aEPList.size(); j++) {
                if (j == aEPList.size()) {
                    break;
                }

                aEP = aEPList.get(j);
                if (Math.abs(aEP.sPoint.X - p1.X) < 0.000001 && Math.abs(aEP.sPoint.Y - p1.Y) < 0.000001) {
                    temEPList.add(aEP);
                    aEPList.remove(j);
                    j -= 1;
                }
            }
            if (temEPList.size() > 0) {
                dList.clear();
                if (temEPList.size() > 1) {
                    for (j = 0; j < temEPList.size(); j++) {
                        aEP = temEPList.get(j);
                        dist = Math.pow(aEP.Point.X - p1.X, 2) + Math.pow(aEP.Point.Y - p1.Y, 2);
                        if (j == 0) {
                            dList.add(new Object[]{dist, j});
                        } else {
                            IsInsert = false;
                            for (k = 0; k < dList.size(); k++) {
                                if (dist < Double.parseDouble(((Object[]) dList.get(k))[0].toString())) {
                                    dList.add(k, new Object[]{dist, j});
                                    IsInsert = true;
                                    break;
                                }
                            }
                            if (!IsInsert) {
                                dList.add(new Object[]{dist, j});
                            }

                        }
                    }
                    for (j = 0; j < dList.size(); j++) {
                        aEP = temEPList.get(Integer.parseInt(((Object[]) dList.get(j))[1].toString()));
                        bP = new BorderPoint();
                        bP.Id = aEP.Index;
                        bP.Point = aEP.Point;
                        BorderList.add(bP);
                    }
                } else {
                    aEP = temEPList.get(0);
                    bP = new BorderPoint();
                    bP.Id = aEP.Index;
                    bP.Point = aEP.Point;
                    BorderList.add(bP);
                }
            }

            BorderList.add(aBPoint);

            p1 = p2;
        }

        return BorderList;
    }

    private static List<BorderPoint> insertPoint2Border_Ring(double[][] S0, List<BorderPoint> bPList, Border aBorder, int[] pNums) {
        BorderPoint aBPoint, bP;
        int i, j, k;
        PointD p1, p2, p3;
        BorderLine aBLine;
        List<BorderPoint> newBPList = new ArrayList<>(), tempBPList = new ArrayList<>(), tempBPList1 = new ArrayList<>();

        for (k = 0; k < aBorder.getLineNum(); k++) {
            aBLine = aBorder.LineList.get(k);
            tempBPList.clear();
            for (i = 0; i < aBLine.pointList.size(); i++) {
                aBPoint = new BorderPoint();
                aBPoint.Id = -1;
                aBPoint.BorderIdx = k;
                aBPoint.Point = aBLine.pointList.get(i);
                aBPoint.Value = S0[aBLine.ijPointList.get(i).I][aBLine.ijPointList.get(i).J];
                tempBPList.add(aBPoint);
            }
            for (i = 0; i < bPList.size(); i++) {
                bP = (BorderPoint) bPList.get(i).clone();
                bP.BorderIdx = k;
                p3 = bP.Point;
                p1 = (PointD) tempBPList.get(0).Point.clone();
                for (j = 1; j < tempBPList.size(); j++) {
                    p2 = (PointD) tempBPList.get(j).Point.clone();
                    if ((p3.X - p1.X) * (p3.X - p2.X) <= 0) {
                        if ((p3.Y - p1.Y) * (p3.Y - p2.Y) <= 0) {
                            if ((p3.X - p1.X) * (p2.Y - p1.Y) - (p2.X - p1.X) * (p3.Y - p1.Y) == 0) {
                                tempBPList.add(j, bP);
                                break;
                            }
                        }
                    }
                    p1 = p2;
                }
            }
            tempBPList1.clear();
            for (i = 0; i < tempBPList.size(); i++) {
                bP = tempBPList.get(i);
                bP.BInnerIdx = i;
                tempBPList1.add(bP);
            }
            pNums[k] = tempBPList1.size();
            newBPList.addAll(tempBPList1);
        }

        return newBPList;
    }

    private static boolean doubleEquals(double a, double b) {
        double difference = Math.abs(a * 0.00001);
        if (Math.abs(a - b) <= difference) {
            return true;
        } else {
            return false;
        }
    }
    // </editor-fold>
}
