/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 * 
 * This software is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 */
package wcontourdemo;

import wContour.Global.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import wContour.Contour;
import wContour.Interpolate;
import wContour.Legend;

/**
 * DrawingPanel - Paint the graphics
 * 
 * @author Yaqiang Wang
 */
public class DrawingPanel extends JPanel {

    double[][] _gridData = null;
    double[][] _discreteData = null;
    double[] _X = null;
    double[] _Y = null;
    double[] _CValues = null;
    Color[] _colors = null;
    double _undefData = -9999.0;
    Color _startColor = Color.yellow;
    Color _endColor = Color.red;
    List<Border> _borders = new ArrayList<Border>();
    List<PolyLine> _contourLines = new ArrayList<PolyLine>();
    List<PolyLine> _clipContourLines = new ArrayList<PolyLine>();
    List<wContour.Global.Polygon> _contourPolygons = new ArrayList<wContour.Global.Polygon>();
    List<wContour.Global.Polygon> _clipContourPolygons = new ArrayList<wContour.Global.Polygon>();
    List<LPolygon> _legendPolygons = new ArrayList<LPolygon>();
    List<PolyLine> _streamLines = new ArrayList<PolyLine>();
    List<List<PointD>> _clipLines = new ArrayList<List<PointD>>();
    List<List<PointD>> _mapLines = new ArrayList<List<PointD>>();
    private double _minX = 0;
    private double _minY = 0;
    private double _maxX = 0;
    private double _maxY = 0;
    private double _scaleX = 1.0;
    private double _scaleY = 1.0;
    private boolean _drawDiscreteData = false;
    private boolean _drawGridData = false;
    private boolean _drawBorderLine = false;
    private boolean _drawContourLine = false;
    private boolean _drawContourPolygon = false;
    private boolean _drawClipped = false;
    private boolean _antiAlias = false;
    private boolean _highlight = false;
    private int _highlightIdx = 0;

    public DrawingPanel() {
    }

    // <editor-fold desc="Contour Methods">
    public void clearObjects() {
        _discreteData = null;
        _gridData = null;
        _borders = new ArrayList<Border>();
        _contourLines = new ArrayList<PolyLine>();
        _contourPolygons = new ArrayList<wContour.Global.Polygon>();
        _clipLines = new ArrayList<List<PointD>>();
        _clipContourLines = new ArrayList<PolyLine>();
        _clipContourPolygons = new ArrayList<wContour.Global.Polygon>();
        _mapLines = new ArrayList<List<PointD>>();
        _legendPolygons = new ArrayList<LPolygon>();
        _streamLines = new ArrayList<PolyLine>();
    }

    public void sample_Interpolate(int type) {
        clearObjects();
        //_dFormat = "0.0";
        File directory = new File(".");
        String fn = null;
        String dfn = null;
        try {
            fn = directory.getCanonicalPath();
            dfn = directory.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        fn = fn + File.separator + "sample" + File.separator + "China.wmp";
        File aFile = new File(fn);
        try {
            readMapFile_WMP(aFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        _mapLines = new ArrayList<List<PointD>>(_clipLines);
        if (type == 1) {
            dfn = dfn + File.separator + "sample" + File.separator + "Temp_2010101420.csv";
        } else {
            dfn = dfn + File.separator + "sample" + File.separator + "Prec_2010101420.csv";
        }
        File dFile = new File(dfn);
        try {
            readCVSData(dFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Interpolation
        int rows = 80;
        int cols = 80;
        _X = new double[cols];
        _Y = new double[rows];
        Interpolate.createGridXY_Num(70, 15, 140, 55, _X, _Y);
        _gridData = new double[rows][cols];
        double[] values = new double[]{0.1, 1, 2, 5, 10, 20, 25, 50, 100};
        switch (type) {
            case 1:
                _gridData = Interpolate.interpolation_IDW_Neighbor(_discreteData, _X, _Y, 8, _undefData);
                values = new double[]{-5, 0, 5, 10, 15, 20, 25, 30, 35, 40};
                break;
            case 2:
                //_gridData = Interpolate.interpolation_IDW_Radius(_discreteData, _X, _Y, 4, 2, _undefData);
                _gridData = Interpolate.idw_Radius_kdTree(_discreteData, _X, _Y, 4, 2, _undefData);
                values = new double[]{0.1, 1, 2, 5, 10, 20, 25, 50, 100};
                break;
            case 3:
                ArrayList<Double> radList = new ArrayList<Double>();
                radList.add(10.0);
                radList.add(8.0);
                radList.add(6.0);
                radList.add(4.0);
                radList.add(2.0);
                //_gridData = Interpolate.cressman(_discreteData, _X, _Y, _undefData, radList);
                _gridData = Interpolate.cressman_kdTree(_gridData, _X, _Y, _undefData, radList);
                values = new double[]{0.1, 1, 2, 5, 10, 20, 25, 50, 100};
                break;
        }

        //Contour

        //double[] values = new double[]{20, 25};
        setContourValues(values);
        tracingContourLines();
        smoothLines();
        clipLines();
        tracingPolygons();
        clipPolygons();

        setCoordinate(70, 140, 15, 55);
        createLegend();

        //this.drawingPanel1.repaint();        
        this.repaint();
    }

    public void sample_Streamline() throws IOException {
        clearObjects();
        //_dFormat = "0.0";
        File directory = new File(".");
        String fn = null;
        String udfn = null;
        String vdfn = null;
        try {
            fn = directory.getCanonicalPath();
            udfn = fn;
            vdfn = fn;
        } catch (IOException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        fn = fn + File.separator + "sample" + File.separator + "China.wmp";
        File aFile = new File(fn);
        try {
            readMapFile_WMP(aFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(frmMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        _mapLines = new ArrayList<List<PointD>>(_clipLines);
        udfn = udfn + File.separator + "sample" + File.separator + "uwnd.grd";
        double[][] UData = this.readSuferGridData(new File(udfn));
        vdfn = vdfn + File.separator + "sample" + File.separator + "vwnd.grd";
        double[][] VData = this.readSuferGridData(new File(vdfn));

        //Streamline
        _streamLines = Contour.tracingStreamline(UData, VData, _X, _Y, _undefData, 4);

        setCoordinate(70, 140, 14, 55);

        this.repaint();
    }

    public double[][] readSuferGridData(File aFile) throws FileNotFoundException, IOException {
        //Read file
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        int i;
        String aLine;
        String[] dataArray;

        aLine = br.readLine();
        for (i = 1; i <= 4; i++) {
            aLine = aLine + " " + br.readLine();
        }

        dataArray = aLine.split("\\s+");
        int XNum = Integer.parseInt(dataArray[1]);
        int YNum = Integer.parseInt(dataArray[2]);
        double XMin = Double.parseDouble(dataArray[3]);
        double XMax = Double.parseDouble(dataArray[4]);
        double YMin = Double.parseDouble(dataArray[5]);
        double YMax = Double.parseDouble(dataArray[6]);

        double XDelt = (XMax - XMin) / (XNum - 1);
        double YDelt = (YMax - YMin) / (YNum - 1);
        _X = new double[XNum];
        for (i = 0; i < XNum; i++) {
            _X[i] = XMin + i * XDelt;
        }
        _Y = new double[YNum];
        for (i = 0; i < YNum; i++) {
            _Y[i] = YMin + i * YDelt;
        }

        //Read grid data
        double[][] gridData = new double[YNum][XNum];
        aLine = br.readLine();
        int ii, jj;
        int d = 0;
        while (aLine != null) {
            if (aLine.trim().isEmpty()) {
                aLine = br.readLine();
                continue;
            }

            dataArray = aLine.split("\\s+");
            for (i = 0; i < dataArray.length; i++) {
                ii = d / XNum;
                jj = d % XNum;
                gridData[ii][jj] = Double.parseDouble(dataArray[i]);
                d += 1;
            }

            aLine = br.readLine();
        }

        br.close();

        return gridData;
    }

    private double[] getMinMaxValues() {
        int dNum = 0;
        double min = 0, max = 0;
        for (int i = 0; i < _gridData.length; i++) {
            for (int j = 0; j < _gridData[0].length; j++) {
                if (_gridData[i][j] == _undefData) {
                    continue;
                }

                if (dNum == 0) {
                    min = _gridData[i][j];
                    max = min;
                } else {
                    if (min > _gridData[i][j]) {
                        min = _gridData[i][j];
                    } else if (max < _gridData[i][j]) {
                        max = _gridData[i][j];
                    }
                }
                dNum += 1;
            }
        }

        return new double[]{min, max};
    }

    public void outputSurferGridData() throws IOException {
        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(new File("."));
        javax.swing.filechooser.FileFilter filter1 = new javax.swing.filechooser.FileFilter() {

            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".grd")
                        || f.isDirectory();
            }

            public String getDescription() {
                return "Surfer Grid (*.grd)";
            }
        };
        aDlg.setFileFilter(filter1);
        //aDlg.addChoosableFileFilter(filter1);
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(null)) {
            double min = 0, max = 0;
            double[] minmax = getMinMaxValues();
            min = minmax[0];
            max = minmax[1];

            BufferedWriter sw = new BufferedWriter(new FileWriter(aDlg.getSelectedFile()));
            sw.write("DSAA");
            sw.newLine();
            sw.write(String.valueOf(_X.length) + " " + String.valueOf(_Y.length));
            sw.newLine();
            sw.write(String.valueOf(_X[0]) + " " + String.valueOf(_X[_X.length - 1]));
            sw.newLine();
            sw.write(String.valueOf(_Y[0]) + " " + String.valueOf(_Y[_Y.length - 1]));
            sw.newLine();
            sw.write(String.valueOf(min) + " " + String.valueOf(max));
            sw.newLine();
            double value;
            String aLine = "";
            for (int i = 0; i < _Y.length; i++) {
                for (int j = 0; j < _X.length; j++) {
                    //if (_gridData[i][j] == _undefData)
                    //    value = 1.70141e+038;
                    //else
                    //    value = _gridData[i][j];

                    value = _gridData[i][j];

                    if (j == 0) {
                        aLine = String.valueOf(value);
                    } else {
                        aLine = aLine + " " + String.valueOf(value);
                    }
                }
                sw.write(aLine);
                sw.newLine();
            }

            sw.close();
        }
    }

    public void readMapFile_WMP(File aFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        String aLine;
        String shapeType;
        String[] dataArray;
        int shapeNum;
        int i, pNum;
        PointD aPoint;

        //Read shape type
        shapeType = br.readLine().trim();
        //Read shape number
        shapeNum = Integer.parseInt(br.readLine());
        _clipLines = new ArrayList<List<PointD>>();
        if (shapeType.equals("Polygon")) {
            for (int s = 0; s < shapeNum; s++) {
                pNum = Integer.parseInt(br.readLine());
                List<PointD> cLine = new ArrayList<PointD>();
                for (i = 0; i < pNum; i++) {
                    aLine = br.readLine();
                    dataArray = aLine.split(",");
                    aPoint = new PointD();
                    aPoint.X = Double.parseDouble(dataArray[0]);
                    aPoint.Y = Double.parseDouble(dataArray[1]);
                    cLine.add(aPoint);
                }
                _clipLines.add(cLine);
            }
        }

        br.close();
    }

    private void readCVSData(File aFile) throws FileNotFoundException, IOException {
        //Read data
        BufferedReader br = new BufferedReader(new FileReader(aFile));
        String[] dataArray;
        List<String[]> dataList = new ArrayList<String[]>();
        String aLine = br.readLine();    //Title
        aLine = br.readLine();    //First line              
        while (aLine != null) {
            dataArray = aLine.split(",");
            if (dataArray.length < 3) {
                aLine = br.readLine();
                continue;
            }

            dataList.add(dataArray);

            aLine = br.readLine();
        }

        br.close();

        _discreteData = new double[dataList.size()][3];
        for (int i = 0; i < dataList.size(); i++) {
            _discreteData[i][0] = Double.parseDouble(dataList.get(i)[1]);
            _discreteData[i][1] = Double.parseDouble(dataList.get(i)[2]);
            _discreteData[i][2] = Double.parseDouble(dataList.get(i)[3]);
        }
    }

    public void createGridData(int rows, int cols) {
        int i = 0;
        int j = 0;
        double[][] dataArray;
        double XDelt = 0;
        double YDelt = 0;

        //---- Generate X and Y coordinates            
        _X = new double[cols];
        _Y = new double[rows];

        XDelt = this.getWidth() / cols;
        YDelt = this.getHeight() / rows;
        for (i = 0; i <= cols - 1; i++) {
            _X[i] = i * XDelt;
        }
        for (i = 0; i <= rows - 1; i++) {
            _Y[i] = i * YDelt;
        }
        _Y[rows - 1] = _Y[rows - 1] - 10;

        //---- Generate random data between 10 to 100
        Random random = new Random();
        dataArray = new double[rows][cols];
        for (i = 0; i <= rows - 1; i++) {
            for (j = 0; j < cols; j++) {
                dataArray[i][j] = random.nextDouble() * 90 + 10;
            }
        }

        _gridData = dataArray;
    }

    public void createDiscreteData(int dataNum) {
        int i = 0;
        double[][] S = null;

        //---- Generate discrete points
        Random random = new Random();
        S = new double[dataNum][3];
        //---- x,y,value
        for (i = 0; i < dataNum; i++) {
            S[i][0] = random.nextDouble() * this.getWidth();
            S[i][1] = random.nextDouble() * this.getHeight();
            S[i][2] = random.nextDouble() * 90 + 10;
        }

        _discreteData = S;
    }

    public void interpolateData(int rows, int cols) {
        double[][] dataArray = null;
        //double XDelt = 0;
        //double YDelt = 0;

        //---- Generate Grid Coordinate           
        double Xlb = 0;
        double Ylb = 0;
        double Xrt = 0;
        double Yrt = 0;

        Xlb = 0;
        Ylb = 0;
        Xrt = this.getWidth();
        Yrt = this.getHeight();
        //XDelt = this.drawingPanel1.getWidth() / cols;
        //YDelt = this.drawingPanel1.getHeight() / rows;

        _X = new double[cols];
        _Y = new double[rows];
        Interpolate.createGridXY_Num(Xlb, Ylb, Xrt, Yrt, _X, _Y);

        dataArray = new double[rows][cols];
        //dataArray = Interpolate.Interpolation_IDW_Neighbor(_discreteData, _X, _Y, 8, _undefData);
        dataArray = Interpolate.interpolation_IDW_Radius(_discreteData, _X, _Y, 4, 100, _undefData);

        _gridData = dataArray;
    }
    
    public void setStartColor(Color aColor){
        this._startColor = aColor;
    }
    
    public void setEndColor(Color aColor){
        this._endColor = aColor;
    }

    public void setContourValues(double[] values) {
        _CValues = values;
    }

    public void tracingContourLines() {
        int nc = _CValues.length;
        int[][] S1 = new int[_gridData.length][_gridData[0].length];
        _borders = Contour.tracingBorders(_gridData, _X, _Y, S1, _undefData);
        _contourLines = Contour.tracingContourLines(_gridData, _X, _Y, nc, _CValues, _undefData, _borders, S1);
    }

    public void smoothLines() {
        _contourLines = Contour.smoothLines(_contourLines);
    }

    public void getEcllipseClipping() {
        _clipLines = new ArrayList<List<PointD>>();

        //---- Generate border with ellipse
        double x0 = 0;
        double y0 = 0;
        double a = 0;
        double b = 0;
        double c = 0;
        boolean ifX = false;
        x0 = this.getWidth() / 2;
        y0 = this.getHeight() / 2;
        double dist = 0;
        dist = 100;
        a = x0 - dist;
        b = y0 - dist / 2;
        if (a > b) {
            ifX = true;
        } else {
            ifX = false;
            c = a;
            a = b;
            b = c;
        }

        int i = 0;
        int n = 0;
        n = 100;
        double nx = 0;
        double x1 = 0;
        double y1 = 0;
        double ytemp = 0;
        List<PointD> pList = new ArrayList<PointD>();
        List<PointD> pList1 = new ArrayList<PointD>();
        PointD aPoint;
        nx = (x0 * 2 - dist * 2) / n;
        for (i = 1; i <= n; i++) {
            x1 = dist + nx / 2 + (i - 1) * nx;
            if (ifX) {
                ytemp = Math.sqrt((1 - Math.pow((x1 - x0), 2) / Math.pow(a, 2)) * Math.pow(b, 2));
                y1 = y0 + ytemp;
                aPoint = new PointD();
                aPoint.X = x1;
                aPoint.Y = y1;
                pList.add(aPoint);
                aPoint = new PointD();
                aPoint.X = x1;
                y1 = y0 - ytemp;
                aPoint.Y = y1;
                pList1.add(aPoint);
            } else {
                ytemp = Math.sqrt((1 - Math.pow((x1 - x0), 2) / Math.pow(b, 2)) * Math.pow(a, 2));
                y1 = y0 + ytemp;
                aPoint = new PointD();
                aPoint.X = x1;
                aPoint.Y = y1;
                pList1.add(aPoint);
                aPoint = new PointD();
                aPoint.X = x1;
                y1 = y0 - ytemp;
                aPoint.Y = y1;
                pList1.add(aPoint);
            }
        }

        aPoint = new PointD();
        if (ifX) {
            aPoint.X = x0 - a;
        } else {
            aPoint.X = x0 - b;
        }
        aPoint.Y = y0;
        List<PointD> cLine = new ArrayList<PointD>();
        cLine.add(aPoint);
        for (i = 0; i < pList.size(); i++) {
            cLine.add(pList.get(i));
        }
        aPoint = new PointD();
        aPoint.Y = y0;
        if (ifX) {
            aPoint.X = x0 + a;
        } else {
            aPoint.X = x0 + b;
        }
        cLine.add(aPoint);
        for (i = pList1.size() - 1; i >= 0; i += -1) {
            cLine.add(pList1.get(i));
        }
        cLine.add(cLine.get(0));
        _clipLines.add(cLine);
    }

    public void clipLines() {
        _clipContourLines = new ArrayList<PolyLine>();
        for (List< PointD> cLine : _clipLines) {
            _clipContourLines.addAll(Contour.clipPolylines(_contourLines, cLine));
        }
    }

    public void clipPolygons() {
        _clipContourPolygons = new ArrayList<wContour.Global.Polygon>();

        for (List<PointD> cLine : _clipLines) {
            _clipContourPolygons.addAll(Contour.clipPolygons(_contourPolygons, cLine));
        }
    }

    public void tracingPolygons() {
        int nc = _CValues.length;
        //---- Colors
        createColors(_startColor, _endColor, nc + 1);

        _contourPolygons = Contour.tracingPolygons(_gridData, _contourLines, _borders, _CValues);
    }
    
    public void createColors(){
        createColors(_startColor, _endColor, _CValues.length + 1);
    }

    public void createColors(Color sColor, Color eColor, int cNum) {
        _colors = new Color[cNum];
        int sR = 0;
        int sG = 0;
        int sB = 0;
        int eR = 0;
        int eG = 0;
        int eB = 0;
        int rStep = 0;
        int gStep = 0;
        int bStep = 0;
        int i = 0;

        sR = sColor.getRed();
        sG = sColor.getGreen();
        sB = sColor.getBlue();
        eR = eColor.getRed();
        eG = eColor.getGreen();
        eB = eColor.getBlue();
        rStep = (int) ((eR - sR) / cNum);
        gStep = (int) ((eG - sG) / cNum);
        bStep = (int) ((eB - sB) / cNum);
        for (i = 0; i < _colors.length; i++) {
            int r = sR + i * rStep;
            int g = sG + i * gStep;
            int b = sB + i * bStep;
            _colors[i] = new Color(r, g, b);
        }
    }

    public void setCoordinate() {
        this.setCoordinate(-10, this.getWidth(), 0, this.getHeight());
    }

    public void createLegend() {
        PointD aPoint = new PointD();

        double width = _maxX - _minX;
        aPoint.X = _minX + width / 4;
        aPoint.Y = _minY + width / 100;
        LegendPara lPara = new LegendPara();
        lPara.startPoint = aPoint;
        lPara.isTriangle = true;
        lPara.isVertical = false;
        lPara.length = width / 2;
        lPara.width = width / 100;
        lPara.contourValues = _CValues;
        
        _legendPolygons = Legend.createLegend(lPara);
    }

    // </editor-fold>
    // <editor-fold desc="Draw Methods">
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

//        if (_CValues == null) {
//            return;
//        }

        this.setBackground(Color.white);
        Graphics2D g2 = (Graphics2D) g;
        if (_antiAlias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        if (_drawContourPolygon && this._contourPolygons.size() > 0) {
            this.drawContourPolygons(g2);
        }

        if (_drawContourLine && this._contourLines.size() > 0) {
            this.drawContourLines(g2);
        }

        if (_drawClipped) {
            this.drawClipLines(g2);
        }

        if (this._mapLines.size() > 0) {
            this.drawClipLines(g2);
        }

        if (_drawBorderLine && this._borders.size() > 0) {
            this.drawBorder(g2);
        }

        if (_drawDiscreteData) {
            this.drawDiscreteData(g2);
        }

        if (_drawGridData) {
            this.drawGridData(g2);
        }

        if (this._streamLines.size() > 0) {
            this.drawStreamline(g2);
        }
        
        if (_drawContourPolygon && this._contourPolygons.size() > 0) {
            this.drawLegend(g2);
        }
    }

    private void drawBorder(Graphics2D g) {
        PointD aPoint;
        for (int i = 0; i < _borders.size(); i++) {
            Border aBorder = _borders.get(i);
            for (int j = 0; j < aBorder.getLineNum(); j++) {
                BorderLine bLine = aBorder.LineList.get(j);
                int len = bLine.pointList.size();
                int[] xPoints = new int[len];
                int[] yPoints = new int[len];

                for (int k = 0; k < len; k++) {
                    aPoint = bLine.pointList.get(k);
                    int[] sxy = toScreen(aPoint.X, aPoint.Y);
                    xPoints[k] = sxy[0];
                    yPoints[k] = sxy[1];
                }
                g.setColor(Color.black);
                g.drawPolyline(xPoints, yPoints, len);
//                if (j > 0) {
//                    g.drawString(String.valueOf(0), xPoints[0], yPoints[0]);
//                    g.drawString(String.valueOf(1), xPoints[1], yPoints[1]);
//                }
            }
        }
    }

    private void drawContourLines(Graphics2D g) {
        List<PolyLine> drawLines = _contourLines;
        if (_drawClipped) {
            drawLines = _clipContourLines;
        }

        PointD aPoint;
        for (int i = 0; i < drawLines.size(); i++) {
            PolyLine aLine = drawLines.get(i);
            int len = aLine.PointList.size();
            int[] xPoints = new int[len];
            int[] yPoints = new int[len];
            for (int j = 0; j < len; j++) {
                aPoint = aLine.PointList.get(j);
                int[] sxy = toScreen(aPoint.X, aPoint.Y);
                xPoints[j] = sxy[0];
                yPoints[j] = sxy[1];
            }
            g.setColor(Color.red);
            g.drawPolyline(xPoints, yPoints, len);
        }
    }

    private void drawContourPolygons(Graphics2D g) {
        List<wContour.Global.Polygon> drawPolygons = _contourPolygons;
        if (_drawClipped) {
            drawPolygons = _clipContourPolygons;
        }

        List<String> values = new ArrayList<String>();
        for (double v : _CValues) {
            values.add(String.valueOf(v));
        }
        for (int i = 0; i < drawPolygons.size(); i++) {
            wContour.Global.Polygon aPolygon = drawPolygons.get(i);
            drawPolygon(g, aPolygon, values, false);
        }
        if (this._highlight) {
            if (this._highlightIdx < drawPolygons.size()) {
                drawPolygon(g, drawPolygons.get(this._highlightIdx), values, true);
            }
        }
    }

    private void drawPolygon(Graphics2D g, wContour.Global.Polygon aPolygon, List<String> values, boolean isHighlight) {
        PointD aPoint;
        String aValue = String.valueOf(aPolygon.LowValue);
        int idx = values.indexOf(aValue) + 1;
        Color aColor = Color.black;
        Color bColor = Color.gray;
        if (isHighlight) {
            aColor = Color.green;
            bColor = Color.blue;
        } else {
            aColor = _colors[idx];
            if (!aPolygon.IsHighCenter) {
                for (int j = 1; j < _colors.length; j++) {
                    if (aColor.getRGB() == _colors[j].getRGB()) {
                        aColor = _colors[j - 1];
                    }
                }
            }
        }

        int len = aPolygon.OutLine.PointList.size();
        GeneralPath drawPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
        for (int j = 0; j < len; j++) {
            aPoint = aPolygon.OutLine.PointList.get(j);
            int[] sxy = toScreen(aPoint.X, aPoint.Y);
            if (j == 0) {
                drawPolygon.moveTo(sxy[0], sxy[1]);
            } else {
                drawPolygon.lineTo(sxy[0], sxy[1]);
            }
        }

        if (aPolygon.HasHoles()) {
            for (int h = 0; h < aPolygon.HoleLines.size(); h++) {
                List<PointD> newPList = aPolygon.HoleLines.get(h).PointList;
                for (int j = 0; j < newPList.size(); j++) {
                    aPoint = newPList.get(j);
                    int[] sxy = toScreen(aPoint.X, aPoint.Y);
                    if (j == 0) {
                        drawPolygon.moveTo(sxy[0], sxy[1]);
                    } else {
                        drawPolygon.lineTo(sxy[0], sxy[1]);
                    }
                }
            }
        }
        drawPolygon.closePath();

        g.setColor(aColor);
        g.fill(drawPolygon);
        g.setColor(bColor);
        g.draw(drawPolygon);
    }

    private void drawStreamline(Graphics2D g) {
        for (int i = 0; i < _streamLines.size(); i++) {
            PolyLine aLine = _streamLines.get(i);
            List<PointD> newPList = aLine.PointList;
            int alen = newPList.size();
            int[] xPoints = new int[alen];
            int[] yPoints = new int[alen];
            for (int j = 0; j < newPList.size(); j++) {
                PointD aPoint = newPList.get(j);
                int[] sxy = toScreen(aPoint.X, aPoint.Y);
                xPoints[j] = sxy[0];
                yPoints[j] = sxy[1];
            }
            g.setColor(Color.blue);
            g.drawPolyline(xPoints, yPoints, alen);

            int len = 12;
            for (int j = 0; j < alen; j++) {
                if (j > 0 && j < alen - 2 && j % len == 0) {
                    //Draw arraw
                    Point aP = new Point(xPoints[j], yPoints[j]);
                    Point bPoint = new Point(xPoints[j + 1], yPoints[j + 1]);
                    double U = bPoint.x - aP.x;
                    double V = bPoint.y - aP.y;
                    double angle = Math.atan((V) / (U)) * 180 / Math.PI;
                    angle = angle + 90;
                    if (U < 0) {
                        angle = angle + 180;
                    }

                    if (angle >= 360) {
                        angle = angle - 360;
                    }

                    Point eP1 = new Point();
                    double aSize = 8;
                    eP1.x = (int) (aP.x - aSize * Math.sin((angle + 20.0) * Math.PI / 180));
                    eP1.y = (int) (aP.y + aSize * Math.cos((angle + 20.0) * Math.PI / 180));
                    g.drawLine(aP.x, aP.y, eP1.x, eP1.y);

                    eP1.x = (int) (aP.x - aSize * Math.sin((angle - 20.0) * Math.PI / 180));
                    eP1.y = (int) (aP.y + aSize * Math.cos((angle - 20.0) * Math.PI / 180));
                    g.drawLine(aP.x, aP.y, eP1.x, eP1.y);
                }
            }
        }
    }

    private void drawClipLines(Graphics2D g) {
        PointD aPoint;
        for (List<PointD> cLine : _clipLines) {
            int len = cLine.size();
            int[] xPoints = new int[len];
            int[] yPoints = new int[len];
            for (int j = 0; j < len; j++) {
                aPoint = cLine.get(j);
                int[] sxy = toScreen(aPoint.X, aPoint.Y);
                xPoints[j] = sxy[0];
                yPoints[j] = sxy[1];
                //g.drawOval(xPoints[j] - 2, yPoints[j] - 2, 4, 4);
//                if (j < 10){
//                    g.setColor(Color.red);
//                    g.drawString(String.valueOf(j), xPoints[j] - 8, yPoints[j]);
//                }
            }
            g.setColor(Color.black);
            g.drawPolyline(xPoints, yPoints, len);
        }
    }

    private void drawDiscreteData(Graphics2D g) {
        if (_discreteData != null) {
            for (int i = 0; i < _discreteData[0].length; i++) {
                int[] sxy = toScreen(_discreteData[0][i], _discreteData[1][i]);
                g.setColor(Color.red);
                g.fillOval(sxy[0], sxy[1], 4, 4);
                //if (_discreteData[2, i] >= 0.1)
                //    g.DrawString(_discreteData[2, i].ToString(_dFormat), drawFont, drawBrush, sX, sY);
            }
        }
    }

    private void drawGridData(Graphics2D g) {
        if (_gridData != null) {
            for (int i = 0; i < _X.length; i++) {
                for (int j = 0; j < _Y.length; j++) {
                    int[] sxy = toScreen(_X[i], _Y[j]);
                    if (doubleEquals(_gridData[j][i], _undefData)) {
                        g.setColor(Color.gray);
                        g.fillOval(sxy[0], sxy[1], 2, 2);
                    } else {
                        g.setColor(Color.blue);
                        g.fillOval(sxy[0], sxy[1], 4, 4);
                    }
                }
            }
        }
    }

    private void drawLegend(Graphics2D g) {
        if (_legendPolygons.size() > 0) {
            LPolygon aLPolygon;
            int i, j;
            List<Double> values = new ArrayList<Double>();
            for (double v : _CValues) {
                values.add(v);
            }
            PointD aPoint;
            for (i = 0; i < _legendPolygons.size(); i++) {
                aLPolygon = _legendPolygons.get(i);
                double aValue = aLPolygon.value;
                int idx = values.indexOf(aValue) + 1;
                Color aColor;
                if (aLPolygon.isFirst) {
                    aColor = _colors[0];
                } else {
                    aColor = _colors[idx];
                }
                List<PointD> newPList = aLPolygon.pointList;

                int len = newPList.size();
                GeneralPath drawPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, len);
                int dx = 0, dy = 0;
                for (j = 0; j < len; j++) {
                    aPoint = newPList.get(j);
                    int[] sxy = toScreen(aPoint.X, aPoint.Y);
                    if (j == 0) {
                        drawPolygon.moveTo(sxy[0], sxy[1]);
                    } else {
                        drawPolygon.lineTo(sxy[0], sxy[1]);
                        if (j == 2) {
                            dx = sxy[0];
                            dy = sxy[1];
                        }
                    }
                }
                drawPolygon.closePath();

                g.setColor(aColor);
                g.fill(drawPolygon);
                g.setColor(Color.black);
                g.draw(drawPolygon);

                if (i < _legendPolygons.size() - 1) {
                    g.drawString(String.valueOf(_CValues[i]), dx - 10, dy - 10);
                }
            }
        }
    }

    private static boolean doubleEquals(double a, double b) {
        if (Math.abs(a - b) < 0.000001) {
            return true;
        } else {
            return false;
        }
    }

    private int[] toScreen(double pX, double pY) {
        int sX = (int) ((pX - _minX) * _scaleX);
        int sY = (int) ((_maxY - pY) * _scaleY);

        int[] sxy = {sX, sY};
        return sxy;
    }

    // </editor-fold>
    // <editor-fold desc="Set Methods">
    public void setDrawDiscreteData(boolean isDraw) {
        this._drawDiscreteData = isDraw;
    }

    public void setDrawGridData(boolean isDraw) {
        this._drawGridData = isDraw;
    }

    public void setDrawBorderLine(boolean isDraw) {
        this._drawBorderLine = isDraw;
    }

    public void setDrawContourLine(boolean isDraw) {
        this._drawContourLine = isDraw;
    }

    public void setDrawContourPolygon(boolean isDraw) {
        this._drawContourPolygon = isDraw;
    }

    public void setDrawClipped(boolean isDraw) {
        this._drawClipped = isDraw;
    }

    public void setAntiAlias(boolean isTrue) {
        this._antiAlias = isTrue;
    }

    public void setHighlight(boolean isTrue) {
        this._highlight = isTrue;
    }

    public void setHighlightIdx(int idx) {
        this._highlightIdx = idx;
    }

    public void setGridData(double[][] gridData) {
        _gridData = gridData;
    }

    public void setScale() {
        _scaleX = (this.getWidth() - 10) / (_maxX - _minX);
        _scaleY = (this.getHeight() - 10) / (_maxY - _minY);
        this.repaint();
    }

    public void setCoordinate(double minX, double maxX, double minY, double maxY) {
        _minX = minX;
        _maxX = maxX;
        _minY = minY;
        _maxY = maxY;
        _scaleX = (this.getWidth() - 10) / (_maxX - _minX);
        _scaleY = (this.getHeight() - 10) / (_maxY - _minY);
        this.repaint();
    }
    // </editor-fold>    
}
