package Remesh;

import guo_cam.CameraController;
import guo_cam.Vec_Guo;
import processing.core.PApplet;
import util.render.HE_Render;
import wblut.geom.*;
import wblut.hemesh.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class subDTest extends PApplet{
    public static void main(String[] args) {
        PApplet.main("Remesh.subDTest");
    }
    HE_Render render;
    HE_Mesh mesh;
    CameraController cam;
    Import_dxf import_dxf;
    List<HE_Vertex>vers=new ArrayList<>();
    HE_Halfedge edge=null;
    List<HE_Halfedge>edges=new ArrayList<>();
    HE_Face face=null;
    WB_Polygon shell;
    List<WB_Point>pts=new ArrayList<>();
    Util util;
    List<WB_Polygon>polys=new ArrayList<>();
    HE_Mesh newMesh;
    ImportObj im;
    public void setup() {
        size(1600, 1000, P3D);
        render = new HE_Render(this);
        cam = new CameraController(this, 2000);
        cam.top();
//        im=new ImportObj("E:\\0917.3dm");
//        mesh=im.getObj();
//        println("change");
        import_dxf = new Import_dxf(this);
        mesh=import_dxf.getMesh("E:\\14.dxf");
        util=new Util(this);
//        shell=mesh.getPolygonList().get(0);
//        int count=0;
//        while (count<100){
//            WB_Point p=new WB_Point(random(width),random(height));
//            if(WB_GeometryOp.contains2D(p,shell)){
//                pts.add(p);
//                count++;
//            }
//        }

    }

    public void draw() {
        background(255);
        stroke(0);
        render.drawEdges(mesh);

        if(display){
            render.disPlayHeMeshWithDegree(mesh,cam.getCamera().getPosition().dist(cam.getCamera().getLookAt()));
        }
        if(display){
            if(edges!=null) {
                pushStyle();
                Color color = new Color(255, 0, 0);
                for(HE_Halfedge he:edges)
                render.displaySingleHalfEdge(he, color);
                popStyle();
            }

        }
        if(vers!=null){
            pushStyle();
            Color color = new Color(255, 0, 0);
            for(HE_Vertex v:vers)
                render.displaySingleHeVertex(v, color,5);
            popStyle();
        }
        if(face!=null){
            pushStyle();
            Color color=new Color(237, 231, 174);
            fill(color.getRGB());
            render.drawPolygonEdges(face.getPolygon());

            popStyle();
        }

        if(polys!=null){
            pushStyle();
            stroke(255,0,0);
            strokeWeight(4);
            render.drawPolygonEdges(polys);
            popStyle();
        }
    }
    boolean display=true;
    public void keyReleased() {
        if (key == 's') {
           subD(mesh);
        }
        if (key == 'd') {
            display=!display;
        }
        if(key=='o'){
            polys=util.diffOffset(mesh,render);
        }
        if(key=='c'){
            println("Position:  "+cam.getCamera().getPosition());
            println("LookAt:  "+cam.getCamera().getLookAt());
           println(cam.getCamera().getPosition().dist(cam.getCamera().getLookAt()));
//            println(cam.getCamera().getLookAt());
        }
        if(key=='p'){
            cam.setCurrentViewToPerspective();
            cam.perspective();
        }
        if(key=='t'){
            cam.top();
        }
        if(key=='z'){
            cam.setCurrentViewToOrtho();
        }
        if(key=='r'){
            WB_AABB aabb=mesh.getAABB();
            double dis=0;
            dis=max((float) (996*aabb.getWidth()/(width-100)),(float) (996*aabb.getHeight()/(height-100)));
            if(cam.getCamera().getPerspective()){
                cam=new CameraController(this,dis);
                Vec_Guo newPos=new Vec_Guo(mesh.getAABB().getCenter().xf(), mesh.getAABB().getCenter().yf(),dis);
                println(newPos);
                cam.getCamera().setPosition(new Vec_Guo(dis*sin(PI/3),dis*cos(PI/3),dis/tan(PI/3)));
                cam.getCamera().setLookAt(new Vec_Guo(newPos.x,newPos.y,1200));
            }else{
                println("ok");
                if(cam.getCamera().getLookAt().z!=0){
                    cam=new CameraController(this,dis);
                    Vec_Guo newPos=new Vec_Guo(mesh.getAABB().getCenter().xf(), mesh.getAABB().getCenter().yf(),dis);
                    println(newPos);
                    cam.getCamera().setPosition(new Vec_Guo(dis*sin(PI/3),dis*cos(PI/3),dis/tan(PI*2/3)));
                    cam.getCamera().setLookAt(new Vec_Guo(newPos.x,newPos.y,1200));
                    cam.setCurrentViewToOrtho();
                }else{
                    cam=new CameraController(this,dis);
                    cam.top();
                    Vec_Guo newPos=new Vec_Guo(mesh.getAABB().getCenter().xf(), mesh.getAABB().getCenter().yf(),dis);
                    println(newPos);
                    cam.getCamera().setPosition(newPos);
                    cam.getCamera().setLookAt(new Vec_Guo(newPos.x,newPos.y,0));
                }
            }
        }
        if(key=='b'){
            edges=mesh.getAllBoundaryHalfedges();

        }
    }

    public void subD(HE_Mesh mesh){
        List<WB_Triangle>triangles=new ArrayList<>();
        for(HE_Face f: mesh.getFaces()){
            List<HE_Halfedge> hes=f.getFaceHalfedges();
            List<WB_Point[]>divVers=new ArrayList<>();
            for(HE_Halfedge he:hes){
                WB_Vector v= (WB_Vector) he.getHalfedgeDirection();
                WB_Point a1=he.getVertex().getPosition().add(v.mul(he.getLength()/3.));
                WB_Point a2=he.getVertex().getPosition().add(v.mul(he.getLength()*2/3.));
                WB_Point[] div=new WB_Point[2];
                div[0]=a1;
                div[1]=a2;
                divVers.add(div);
            }
            WB_Point mid=WB_GeometryFactory.instance().createMidpoint(divVers.get(0)[1],divVers.get(2)[0]);

            for(int i=0;i<hes.size();i++){
                WB_Triangle tri1=new WB_Triangle( mid, divVers.get(i)[0],divVers.get(i)[1]);
                int pre=(i-1+hes.size())%hes.size();
                WB_Triangle tri2=new WB_Triangle(mid, divVers.get(pre)[1], divVers.get(i)[0]);
                WB_Triangle tri3=new WB_Triangle(hes.get(i).getVertex().getPosition(), divVers.get(i)[0], divVers.get(pre)[1]);
                triangles.add(tri1);
                triangles.add(tri2);
                triangles.add(tri3);
                println("cc!");
            }
        }

        HEC_FromTriangles hec_t=new HEC_FromTriangles().setTriangles(triangles);
        mesh.set(new HE_Mesh(hec_t));
        mesh.getAABB();
    }

    //封装质心生成voronoi函数
    public static HE_Mesh drawVoronoiByCentroids(List<WB_Point> vbasepnt, WB_Polygon shell) {
        List<WB_VoronoiCell2D>voronoi=new ArrayList<WB_VoronoiCell2D>();
        List<WB_Point>vcentroids= new ArrayList<>();

        voronoi=WB_VoronoiCreator.getClippedVoronoi2D(vbasepnt, shell).getCells();
        for(WB_VoronoiCell2D vor:voronoi) {
            vcentroids.add(vor.getCentroid());
        }
        while(!vcentroids.equals(vbasepnt)) {
            //将生成点移至质心，以质心的点再次生成voronoi
            vbasepnt.clear();
            vbasepnt.addAll(vcentroids);
            vcentroids.clear();
            voronoi=WB_VoronoiCreator.getClippedVoronoi2D(vbasepnt, shell).getCells();
            for(WB_VoronoiCell2D vn:voronoi) {
                vcentroids.add(vn.getCentroid());
            }
        }
        List<WB_Polygon>polys=new ArrayList<>();
        List<WB_Point>centers=new ArrayList<>();
        for(WB_VoronoiCell2D v:voronoi){
            centers.add(v.getCentroid());
        }
        WB_Triangulation2D triangulation=WB_Triangulate.triangulate2D(centers);
        HE_Mesh mesh=new HE_Mesh(new HEC_FromTriangulation().setTriangulation(triangulation).setPoints(centers));
        return mesh;
    }
}
