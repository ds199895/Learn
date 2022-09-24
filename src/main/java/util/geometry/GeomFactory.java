package util.geometry;


import guo_cam.Vec_Guo;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Halfedge;
import wblut.hemesh.HE_Mesh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 	JTS与HE_Mesh的2D图元转换类
 * */
public class GeomFactory {
    public static WB_GeometryFactory gf_hemesh = WB_GeometryFactory.instance();
    public static GeometryFactory gf_jts = new GeometryFactory();

    public static  WB_PolyLine jtsLineString2WB_Polyline2D(LineString ls){
        Coordinate[] coords = ls.getCoordinates();
        List<WB_Point> pts = new ArrayList<>();
        for(Coordinate c :coords){
            pts.add(new WB_Point(c.x, c.y, 0.0));
        }
        WB_PolyLine wb_polyLine = new WB_PolyLine(pts);
        return wb_polyLine;
    }
    public static  WB_PolyLine jtsLineString2WB_Polyline3D(LineString ls){
        Coordinate[] coords = ls.getCoordinates();
        List<WB_Point> pts = new ArrayList<>();
        for(Coordinate c :coords){
            pts.add(new WB_Point(c.x, c.y, c.z));
        }
        WB_PolyLine wb_polyLine = new WB_PolyLine(pts);
        return wb_polyLine;
    }

    public static  WB_Polygon jtsLineString2WB_Polygon2D(LineString ls){
        Coordinate[] coords = ls.getCoordinates();
        List<WB_Point> pts = new ArrayList<>();
        for(Coordinate c :coords){
            pts.add(new WB_Point(c.x, c.y, 0.0));
        }
        WB_Polygon wb_polygon = new WB_Polygon(pts);
        return wb_polygon;
    }

    public static Point createJtsPointFromVec(Vec_Guo vec){
        return gf_jts.createPoint(new Coordinate(vec.x(),vec.y()));
    }

    /**
     * HE_Mesh的Polygon转成JTS的Polygon.(可带洞或不带洞)
     * */
    public static Polygon toJTSPolygon(WB_Polygon poly) {
        int num = poly.getNumberOfContours();
        if (num == 1)
            return toJTSPolygonSimple(poly);

        WB_Coord[] shell_points = getShellPts(poly);
        LinearRing outRing = gf_jts.createLinearRing(toLinearRing(shell_points));

        WB_Coord[][] holes_points = getInnerPts(poly);
        LinearRing[] holeRings = new LinearRing[holes_points.length];
        for (int i = 0; i < holes_points.length; i++) {
            holeRings[i] = gf_jts.createLinearRing(toLinearRing(holes_points[i]));
        }
        return gf_jts.createPolygon(outRing, holeRings);
    }

    public static Polygon toJTSPolygon3D(WB_Polygon poly) {
        int num = poly.getNumberOfContours();
        if (num == 1)
            return toJTSPolygonSimple3D(poly);

        WB_Coord[] shell_points = getShellPts(poly);
        LinearRing outRing = gf_jts.createLinearRing(toLinearRing(shell_points));

        WB_Coord[][] holes_points = getInnerPts(poly);
        LinearRing[] holeRings = new LinearRing[holes_points.length];
        for (int i = 0; i < holes_points.length; i++) {
            holeRings[i] = gf_jts.createLinearRing(toLinearRing(holes_points[i]));
        }
        return gf_jts.createPolygon(outRing, holeRings);
    }

    /**
     * 	将HE_Mesh的不带洞的多边形Polygon转成JTS的简单多边形
     * */
    public static Polygon toJTSPolygonSimple3D(WB_Polygon poly) {
        WB_Coord[] wb_coord = getShellPts(poly);
//        if ()
        Coordinate[] coords = new Coordinate[wb_coord.length + 1];
        for (int i = 0; i < wb_coord.length; i++) {
            coords[i] = new Coordinate(wb_coord[i].xd(), wb_coord[i].yd(),wb_coord[i].zd());
        }
        coords[wb_coord.length] = coords[0];
        // TODO: 2021/4/13/013
        if (coords.length>1){
            return gf_jts.createPolygon(coords);
        }else {
            return null;
        }

    }

    public static Polygon toJTSPolygon(HE_Face face) {
        WB_Polygon wb_polygon = face.getPolygon();
        return toJTSPolygon(wb_polygon);
    }

    /**
     * 	返回多边形（可带洞与否）的边集合
     * */
    public static List<LineString> toJTSLineString(WB_Polygon poly) {
        List<LineString> lss = new ArrayList<LineString>();
        int num = poly.getNumberOfContours();
        WB_Coord[] shell_points = getShellPts(poly);

        List<LineString> shell = toLineStrings(shell_points);
        if (num == 1) {
            return shell;
        }

        lss.addAll(shell);

        WB_Coord[][] holes_points = getInnerPts(poly);
        for (int i = 0; i < holes_points.length; i++) {
            lss.addAll(toLineStrings(holes_points[i]));
        }
        return lss;
    }

    /**
     * 	返回JTS多边形（可带洞与否）的边集合
     * */
    public static MultiLineString toJTSLineString(Polygon poly) {
        List<LineString> lss = new ArrayList<LineString>();

        LineString shell = poly.getExteriorRing();
        Coordinate[] shell_coords = shell.getCoordinates();

        for (int i = 0; i < shell_coords.length - 1; i++) {
            lss.add(gf_jts.createLineString(new Coordinate[] { shell_coords[i], shell_coords[i + 1] }));
        }

        int holesNum = poly.getNumInteriorRing();

        for (int i = 0; i < holesNum; i++) {
            LineString ls = poly.getInteriorRingN(i);
            Coordinate[] holes = ls.getCoordinates();
            for (int j = 0; j < holes.length - 1; j++) {
                lss.add(gf_jts.createLineString(new Coordinate[] { holes[j], holes[j + 1] }));
            }
        }
        return gf_jts.createMultiLineString(lss.toArray(new LineString[lss.size()]));
    }

    // 返回points点集中两两相连的JTS线段(包含首尾相连的线段)
    private static List<LineString> toLineStrings(WB_Coord[] points) {
        List<LineString> lss = new ArrayList<LineString>();
        int leng = points.length;
        for (int i = 0; i < points.length; i++) {
            Coordinate sp = new Coordinate(points[i].xd(), points[i].yd());
            Coordinate ep = new Coordinate(points[(i + 1) % leng].xd(), points[(i + 1) % leng].yd());
            lss.add(gf_jts.createLineString(new Coordinate[] { sp, ep }));
        }
        return lss;
    }
    public static LineString toLineString(WB_Segment s){
        Coordinate sp = new Coordinate(s.getOrigin().xd(), s.getOrigin().yd());
        Coordinate ep = new Coordinate(s.getEndpoint().xd(),s.getEndpoint().yd());
        return gf_jts.createLineString(new Coordinate[]{sp,ep});
    }

    /**
     * 	将HE_Mesh的不带洞的多边形Polygon转成JTS的简单多边形
     * */
    public static Polygon toJTSPolygonSimple(WB_Polygon poly) {
        WB_Coord[] wb_coord = getShellPts(poly);
//        if ()
        Coordinate[] coords = new Coordinate[wb_coord.length + 1];
        for (int i = 0; i < wb_coord.length; i++) {
            coords[i] = new Coordinate(wb_coord[i].xd(), wb_coord[i].yd());
        }
        coords[wb_coord.length] = coords[0];
        // TODO: 2021/4/13/013  
        if (coords.length>1){
            return gf_jts.createPolygon(coords);
        }else {
            return null;
        }
        
    }

    /**
     * 	将点集wb_point变成满足JTS的LinearRing需求的Cooedinate集合(首尾实例相同,封闭的...)
     * */
    public static Coordinate[] toLinearRing(WB_Coord[] wb_points) {
        int leng = wb_points.length;
        Coordinate[] coords = new Coordinate[leng + 1];
        for (int i = 0; i < leng; i++) {
            coords[i] = new Coordinate(wb_points[i].xd(), wb_points[i].yd());
        }
        coords[leng] = coords[0];
        return coords;
    }

    /**
     * get shell points of WB_Poygon in CCW
     * 	返回HE_Mesh的多边形外部shell的点集
     * @param poly
     * @return
     */
    public static WB_Coord[] getShellPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1)
            return poly.getPoints().toArray();
        int numOut = poly.getNumberOfShellPoints();
        WB_Coord[] out = new WB_Point[numOut];
        for (int i = 0; i < numOut; i++) {
            out[i] = poly.getPoint(i);
        }
        return out;
    }

    /**
     * get inner points of WB_Poygon in CW
     * 	返回HE_Mesh的多边形内部洞口(可以多个)的点集
     * @param poly
     * @return
     */
    public static WB_Point[][] getInnerPts(WB_Polygon poly) {
        if (poly.getNumberOfContours() == 1)
            return null;
        WB_Point[][] in = new WB_Point[poly.getNumberOfHoles()][];
        int[] num = poly.getNumberOfPointsPerContour();// 从外开始
        int count = num[0];
        for (int i = 0; i < in.length; i++) {
            WB_Point[] pts = new WB_Point[num[i + 1]];
            for (int j = 0; j < pts.length; j++) {
                pts[j] = poly.getPoint(count + j);
            }
            in[i] = pts;
            count += pts.length;
        }
        return in;
    }

    public static WB_Polygon jtsPolygon2WB_Polygon2D(Geometry g) {
        if (g.getGeometryType().equalsIgnoreCase("Polygon")) {
            Polygon p = (Polygon) g;
            Coordinate[] coordOut = p.getExteriorRing().getCoordinates();
            coordOut = subLast(coordOut);
            WB_Point[] outPt = new WB_Point[coordOut.length];
            for (int i = 0; i < coordOut.length; i++) {
                outPt[i] = new WB_Point(coordOut[i].x, coordOut[i].y);
            }
            int num = p.getNumInteriorRing();

            if (num == 0) {
                return new WB_Polygon(outPt);
            } else {
                WB_Point[][] ptsIn = new WB_Point[num][];
                for (int i = 0; i < num; i++) {
                    Coordinate[] coords = p.getInteriorRingN(i).getCoordinates();
                    /**
                     * LineString 也需sublast
                     */
                    WB_Point[] pts = new WB_Point[coords.length - 1];
                    for (int j = 0; j < coords.length - 1; j++) {
                        pts[j] = new WB_Point(coords[j].x, coords[j].y);
                    }
                    ptsIn[i] = pts;
                }
                return new WB_Polygon(outPt, ptsIn);
            }
        } else {
            System.out.println("this Geometry is not a Polygon!");
            return null;
        }
    }
    public static WB_Polygon jtsPolygon2WB_Polygon3D(Geometry g) {
        if (g.getGeometryType().equalsIgnoreCase("Polygon")) {
            Polygon p = (Polygon) g;
            Coordinate[] coordOut = p.getExteriorRing().getCoordinates();
            coordOut = subLast(coordOut);
            WB_Point[] outPt = new WB_Point[coordOut.length];
            for (int i = 0; i < coordOut.length; i++) {
                outPt[i] = new WB_Point(coordOut[i].x, coordOut[i].y,coordOut[i].z);
            }
            int num = p.getNumInteriorRing();

            if (num == 0) {
                return new WB_Polygon(outPt);
            } else {
                WB_Point[][] ptsIn = new WB_Point[num][];
                for (int i = 0; i < num; i++) {
                    Coordinate[] coords = p.getInteriorRingN(i).getCoordinates();
                    /**
                     * LineString 也需sublast
                     */
                    WB_Point[] pts = new WB_Point[coords.length - 1];
                    for (int j = 0; j < coords.length - 1; j++) {
                        pts[j] = new WB_Point(coords[j].x, coords[j].y,coords[j].z);
                    }
                    ptsIn[i] = pts;
                }
                return new WB_Polygon(outPt, ptsIn);
            }
        } else {
            System.out.println("this Geometry is not a Polygon!");
            return null;
        }
    }

    public static Coordinate[] subLast(Coordinate[] ori_coord) {

        int leng = ori_coord.length - 1;

        if (leng < 1)
            return ori_coord;
        Coordinate[] wb_poly = new Coordinate[leng];
        for (int i = 0; i < leng; i++) {
            wb_poly[i] = ori_coord[i];
        }
        return wb_poly;
    }

    /**
     * 	将彼此相连的JTS多边形集合转变成HE_Mesh的半边数据结构面
     * */
    public static HE_Mesh jtsSimplePolygons2He_mesh(List<Polygon> jtsPolygons) {
        int leng = jtsPolygons.size();
        WB_Polygon[] polygons = new WB_Polygon[leng];
        for (int i = 0; i < leng; i++) {
            Coordinate[] coords = jtsPolygons.get(i).getCoordinates();
            WB_Coord[] wb_coords = new WB_Point[coords.length - 1];
            for (int j = 0; j < coords.length - 1; j++) {
                wb_coords[j] = new WB_Point(coords[j].x, coords[j].y);
            }
            polygons[i] = gf_hemesh.createSimplePolygon(wb_coords);
        }
        HEC_FromPolygons creator = new HEC_FromPolygons();

        creator.setPolygons(polygons);
        return new HE_Mesh(creator);
    }

    /**
     * 	简化众多点集构成的HE_Mesh的多边形(容差为tolerance),并将之转为JTS的LineString
     * */
    public static LineString simplify(WB_PolyLine polyline, double tolerance) {
        int pntNum = polyline.getNumberOfPoints();
        Coordinate[] coords = new Coordinate[pntNum];
        for (int i = 0; i < pntNum; i++) {
            WB_Point sb_pnt = polyline.getPoint(i);
            coords[i] = new Coordinate(sb_pnt.xd(), sb_pnt.yd());
        }

        LineString lineString = gf_jts.createLineString(coords);
        return (LineString) DouglasPeuckerSimplifier.simplify(lineString, tolerance);
    }

    /**
     * 	简化众多点集构成的JTS的LineString(容差为tolerance).
     * */
    public static LineString simplify(LineString lineString, double tolerance) {
        return (LineString) DouglasPeuckerSimplifier.simplify(lineString, tolerance);
    }

    /**
     * 	从众多JTS的LineString中（包括basePolygon）"提取出"包含于basePolygon中的多边形集合
     * */
    public static List<Polygon> extractLines2JTSPolygon(List<Geometry> lineStringCollection, Polygon basePolygon) {
        Geometry nodedLineStrings = lineStringCollection.get(0);
        for (int i = 1; i < lineStringCollection.size(); i++) {
            nodedLineStrings = nodedLineStrings.union(lineStringCollection.get(i));
        }
        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(nodedLineStrings);
        Collection polys = polygonizer.getPolygons();

        // 删除basePolygon以外的多边形
        // 构建JTS的多边形
        //Geometry jts_basePoly = toJTSPolygon(basePolygon);
        Geometry jts_basePoly = basePolygon.buffer(0.01);// dirty
        List<Polygon> containedPolys = new ArrayList<Polygon>();// 储存被basePolygon包含的polygon
        Iterator it = polys.iterator();
        while (it.hasNext()) {
            Polygon c_p = (Polygon) it.next();
            if (jts_basePoly.contains(c_p)) {
                containedPolys.add(c_p);
            }
        }
        return containedPolys;
    }
    /**
     * 	从众多JTS的LineString中（包括basePolygon）"提取出"包含于basePolygon中的多边形集合
     * */
    public static List<Polygon> extractLines2JTSPolygon(List<Geometry> lineStringCollection, Polygon basePolygon,double d) {
        Geometry nodedLineStrings = lineStringCollection.get(0);
        for (int i = 1; i < lineStringCollection.size(); i++) {
            nodedLineStrings = nodedLineStrings.union(lineStringCollection.get(i));
        }


        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(nodedLineStrings);
        Collection polys = polygonizer.getPolygons();

        // 删除basePolygon以外的多边形
        // 构建JTS的多边形
        //Geometry jts_basePoly = toJTSPolygon(basePolygon);
        Geometry jts_basePoly = basePolygon.buffer(0.01);// dirty
        List<Polygon> containedPolys = new ArrayList<Polygon>();// 储存被basePolygon包含的polygon
        Iterator it = polys.iterator();
        while (it.hasNext()) {
            Geometry c_p = (Geometry) it.next();
            if (jts_basePoly.contains(c_p)) {
                containedPolys.add((Polygon) c_p.buffer(d));
            }
        }
        return containedPolys;
    }

    public static List<Polygon> extractLines2JTSPolygon(List<Geometry> lineStringCollection) {
        // Geometry nodedLineStrings = lineStringCollection.get(0);
        List<LineString> lsss = new ArrayList<LineString>();
        for (int i = 0; i < lineStringCollection.size(); i++) {
            Geometry c_g = lineStringCollection.get(i);

            if (c_g.getGeometryType() == "LineString") {
                lsss.add((LineString) c_g);
            } else if (c_g.getGeometryType() == "MultiLineString") {
                MultiLineString ms = (MultiLineString) c_g;

                int g_num = ms.getNumGeometries();
                // System.out.println("g_num "+g_num);
                for (int j = 0; j < g_num; j++) {
                    // System.out.println(ms.getGeometryN(j).getGeometryType());
                    lsss.add((LineString) ms.getGeometryN(j));
                }
            }
        }

        Geometry nodedLineStrings = (LineString) lsss.get(0);
        for (int i = 1; i < lsss.size(); i++) {
            nodedLineStrings = nodedLineStrings.union((LineString) lsss.get(i));
        }

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(nodedLineStrings);
        Collection polys = polygonizer.getPolygons();

        // 构建JTS的多边形
        List<Polygon> newPolygons = new ArrayList<Polygon>();// 储存被basePolygon包含的polygon
        Iterator it = polys.iterator();
        while (it.hasNext()) {
            Polygon c_p = (Polygon) it.next();
            newPolygons.add(c_p);
        }
        return newPolygons;
    }

    //dirty test
    public static HE_Mesh extractLines2JTSPolygon(HE_Mesh mesh) {
        List<Geometry>lss = new ArrayList<Geometry>();

        List<HE_Halfedge>es = mesh.getEdges();
        for(HE_Halfedge e:es) {
            WB_Point st = e.getStartPosition();
            WB_Point et = e.getEndPosition();
            LineString ls = GeomFactory.gf_jts.createLineString(new Coordinate[] {new Coordinate(st.xd(),st.yd()),new Coordinate(et.xd(),et.yd())});

            lss.add(ls);
        }

        List<Polygon>ploygons = extractLines2JTSPolygon(lss);
        return jtsSimplePolygons2He_mesh(ploygons);

    }

    public static WB_Polygon transAABBtoWBPolygon(WB_AABB aabb){
        WB_Point p1=aabb.getMin();
        WB_Point p2=new WB_Point(p1.xd()+aabb.getWidth(),p1.yd());
        WB_Point p3=new WB_Point(p1.xd()+aabb.getWidth(),p1.yd()+aabb.getHeight());
        WB_Point p4=new WB_Point(p1.xd(),p1.yd()+aabb.getHeight());
        return new WB_Polygon(p1,p2,p3,p4);
    }

    public static  WB_Point creatRandomPt(Polygon boundary,WB_AABB aabb ){
        double randX = aabb.getMinX() + aabb.getWidth() * Math.random();
        double randY = aabb.getMinY() + aabb.getHeight() * Math.random();

        Point tmp_pnt;
        for(tmp_pnt = GeomFactory.gf_jts.createPoint(new Coordinate(randX, randY)); !boundary.contains(tmp_pnt); tmp_pnt = GeomFactory.gf_jts.createPoint(new Coordinate(randX, randY))) {
            randX = aabb.getMinX() + aabb.getWidth() * Math.random();
            randY = aabb.getMinY() + aabb.getHeight() * Math.random();
        }
        return new WB_Point(tmp_pnt.getX(),tmp_pnt.getY());
    }
    public static  WB_Point creatRandomPt(Polygon boundary){
        WB_AABB aabb = GeomFactory.jtsPolygon2WB_Polygon2D(boundary).getAABB();
        double randX = aabb.getMinX() + aabb.getWidth() * Math.random();
        double randY = aabb.getMinY() + aabb.getHeight() * Math.random();

        Point tmp_pnt;
        for(tmp_pnt = GeomFactory.gf_jts.createPoint(new Coordinate(randX, randY)); !boundary.contains(tmp_pnt); tmp_pnt = GeomFactory.gf_jts.createPoint(new Coordinate(randX, randY))) {
            randX = aabb.getMinX() + aabb.getWidth() * Math.random();
            randY = aabb.getMinY() + aabb.getHeight() * Math.random();
        }
        return new WB_Point(tmp_pnt.getX(),tmp_pnt.getY());
    }
}
