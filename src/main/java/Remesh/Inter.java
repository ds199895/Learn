package Remesh;

import guo_cam.CameraController;
import org.kabeja.dxf.DXFPolyline;
import processing.core.PApplet;
import util.render.HE_Render;
import wblut.geom.*;

import java.applet.Applet;
import java.util.ArrayList;
import java.util.List;

/***
 * @description //todo
 * @author HeSiyuan
 * @version 1.0
 * @date 2023/10/27 20:11
 * Inst. AAA, S-ARCH, Southeast University
 */
public class Inter extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Remesh.Inter");
    }

    CameraController cam;
    Import_dxf dxfImporter = new Import_dxf(this);
    List<DXFPolyline> polylineList = new ArrayList<>();
    List<List<DXFPolyline>> input_dxf_polyline = new ArrayList<>();
    WB_Polygon poly;
    WB_Ray ray;
    WB_Point rayOri = new WB_Point(0, 0, 0);
    WB_Point rayDir = new WB_Point(-1, -1, 0);
    HE_Render render;
    List<WB_Point>interPts;
    WB_Point interPt;
    public void setup() {
        size(800, 800, P3D);
        input_dxf_polyline = dxfImporter.input_dxf_polyline("E://13-study/Java/testintersect.dxf");
        poly = dxfImporter.getPolygons("E://13-study/Java/testintersect.dxf").get(0);
        cam = new CameraController(this, 200);
        render = new HE_Render(this);
        ray = new WB_Ray(rayOri, rayDir);
//        WB_IntersectionResult result = WB_GeometryOp.getIntersection3D(ray, poly);
        interPt=getIntersection2D(ray,poly);
//
//        println(result.intersection);
//        Test();

    }

    public void draw() {
        background(255);
        cam.drawSystem(200);
//        dxfImporter.drawDxf_polyline(input_dxf_polyline);
        render.drawPolygonEdges(poly);
        pushStyle();
        stroke(255, 0, 0);
        render.drawRay(ray, 10000);

        popStyle();
        pushStyle();
        fill(0,255,0);
        if(interPt!=null){
            render.drawPoint(interPt,20);
        }

        popStyle();
    }
//    public static void Test(){
//        WB_Point p1=new WB_Point(0,0);
//        WB_Point p2=new WB_Point(1,0);
//        WB_Point p3=new WB_Point(0,-1);
//        WB_Point p4=new WB_Point(0,-2);
//        WB_Segment s1=new WB_Segment(p1,p2);
//        WB_Segment s2=new WB_Segment(p3,p4);
//        WB_Line l1=new WB_Line(p1,new WB_Vector(1,0));
//        WB_Line l2=new WB_Line(p3,new WB_Vector(0,-1));
//        WB_IntersectionResult result= WB_GeometryOp.getClosestPoint2D(l1,l2);
//        WB_GeometryOp.getClosestPointToRay2D()
//        System.out.println(result.intersection);
//        System.out.println(result.t1);
//        System.out.println(result.t2);
//    }

//
//    public static  WB_Point getIntersection2D1(WB_Ray ray, WB_Polygon poly) {
//        WB_Point p1=new WB_Point(0,0);
//        WB_Point p2=new WB_Point(1,0);
//        WB_Point p3=new WB_Point(0,-1);
//        WB_Point p4=new WB_Point(0,-2);
//        WB_Segment s1=new WB_Segment(p1,p2);
//        WB_Segment s2=new WB_Segment(p3,p4);
//       WB_IntersectionResult result= WB_GeometryOp.getIntersection2D(s1,s2);
//        System.out.println(result.intersection);
//        System.out.println(result.t1);
//        System.out.println(result.t2);
//
//    }

    public static  WB_Point getIntersection2D(WB_Ray ray, WB_Polygon poly) {
        WB_Line line=new WB_Line(ray.getOrigin(),ray.getDirection());
        double minDis=Double.MAX_VALUE;
        WB_Point interPt=null;
        for(WB_Segment segment:poly.toSegments()){
            WB_IntersectionResult result= WB_GeometryOp2D.getClosestPoint2D(line,segment);
            if(result.intersection&&result.t1>0&&result.t2>=0&&result.t2<=segment.getLength()){
                System.out.println("params: " +result.t1+" , "+ result.t2);
                WB_Point p=(WB_Point) result.object;
                System.out.println("intersected at: " +p.xf()+" , "+ p.yf());
                double dis=result.t1;
                if(dis<minDis){
                    minDis=dis;
                    interPt=p;

                }
                System.out.println("distance t1: "+dis);
            }
        }
        return interPt;
    }


//    public static  WB_Point getIntersection2D(WB_Ray ray, WB_Polygon poly) {
//        WB_Segment raySeg=new WB_Segment(ray.getOrigin(),ray.getPoint(Double.MAX_VALUE));
//
//        List<WB_Point>interPts=new ArrayList<>();
//        double minDis=Double.MAX_VALUE;
//        WB_Point interPt=null;
//        for(WB_Segment segment:poly.toSegments()){
//            WB_IntersectionResult result= WB_GeometryOp2D.getIntersection2D(raySeg,segment);
//            if(result.intersection){
//                System.out.println("intersected!");
//                WB_Point p=(WB_Point) result.object;
//                System.out.println(p.xf()+" , "+ p.yf());
//                double dis=WB_GeometryOp.getDistance3D(ray.getOrigin(),p);
//                if(dis<minDis){
//                    minDis=dis;
//                    interPt=p;
//                }
//                System.out.println("distance: "+dis);
//                interPts.add(p);
//            }
//        }
//        //System.out.println(interPt.xf()+" , "+ interPt.yf());
//        return interPt;
//    }



}