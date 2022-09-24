package Remesh;

import guo_cam.CameraController;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Transform3D;
import wblut.geom.WB_Triangle;
import wblut.geom.WB_Vector;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

public class FlipTest extends PApplet{
    public static void main(String[] args) {
        PApplet.main("Remesh.FlipTest");
    }
    HE_Mesh mesh;
    HE_Mesh dual;
    HE_Mesh tri_mesh;
    WB_Render render;
    HE_Mesh copy;
    int most=0;
    CameraController cam;
    ImportObj im;
    double target_length=0;
    public void setup(){
        size(1600,1000,P3D);
        render=new WB_Render(this);
        cam=new CameraController(this,200);
        cam.top();
        im=new ImportObj("D:\\360MoveData\\Users\\Administrator\\Documents\\WeChat Files\\wxid_h82vihn9t4rl22\\FileStorage\\File\\2022-09\\yuan.3dm");
//        HEC_Beethoven creator=new HEC_Beethoven();
//        tri_mesh=new HE_Mesh(creator);
        tri_mesh=im.getObj();
        copy=tri_mesh.copy();
        for(HE_Vertex vertex:copy.getVertices()){
            vertex.setX(vertex.xf()+80000);
        }
        most=0;
        for(HE_Vertex vertex:tri_mesh.getVertices()){
            if(vertex.getVertexStar().size()>most){
                most=vertex.getVertexStar().size();
            }
        }
//        target_length=Util.getSplitDistance(copy);
//        createMeshes();
    }

    public void draw(){
        background(0);
        stroke(0);
        render.drawFaces(tri_mesh);
        if(copy!=null){
            render.drawFaces(copy);
        }
    }

    public void keyPressed() {
        if(key=='c'){
            System.out.println("PREV: "+most);
            copy.simplify(new HES_TriDec().setGoal(0.9));
            int most_temp=0;
            for(HE_Vertex vertex:copy.getVertices()){
                if(vertex.getVertexStar().size()>most_temp){
                    most_temp=vertex.getVertexStar().size();
                }
            }
            System.out.println("NEXT: "+most_temp);
            most=most_temp;
        }

        if (key == 'f') {
            System.out.println("PREV: "+most);
            System.out.println("FlipPreMesh=............"+tri_mesh.getNumberOfHalfedges());
//            Util.applyFlip(copy);
            Util.flipEdgeControl(copy,8);
            System.out.println("FlipAfterMesh=............"+tri_mesh.getNumberOfHalfedges());

            int most_temp=0;
            for(HE_Vertex vertex:copy.getVertices()){
//                print(vertex);
                if(vertex.getVertexStar().size()>most_temp){
                    most_temp=vertex.getVertexStar().size();
                }
            }
            System.out.println("NEXT: "+most_temp);
            most=most_temp;
        }
        if (key == 's') {
            System.out.println("PREV: "+most);
            System.out.println("FlipPreMesh=............"+tri_mesh.getNumberOfHalfedges());
//            Util.applyFlip(copy);
            Util.SplitAllLongEdges(copy, target_length);
            System.out.println("FlipAfterMesh=............"+tri_mesh.getNumberOfHalfedges());

            int most_temp=0;
            for(HE_Vertex vertex:copy.getVertices()){
//                print(vertex);
                if(vertex.getVertexStar().size()>most_temp){
                    most_temp=vertex.getVertexStar().size();
                }
            }
            System.out.println("NEXT: "+most_temp);
            most=most_temp;
        }
        if (key == 'd') {
            System.out.println("PREV: "+most);
            System.out.println("FlipPreMesh=............"+tri_mesh.getNumberOfHalfedges());
//            Util.flipEdgeControl(copy,most);
            Util.controlLength(copy,target_length);
            System.out.println("FlipAfterMesh=............"+tri_mesh.getNumberOfHalfedges());

            int most_temp=0;
            for(HE_Vertex vertex:copy.getVertices()){
//                print(vertex);
                if(vertex.getVertexStar().size()>most_temp){
                    most_temp=vertex.getVertexStar().size();
                }
            }
            System.out.println("NEXT: "+most_temp);
            most=most_temp;
//            HEM_EqualizeValence
        }
        if(key=='t'){
            cam.top();
        }
        if(key=='p'){
            cam.perspective();
        }
    }

    private void createMeshes(){
        mesh = new HE_Mesh(new HEC_Cube().setEdge(4000));
        //mesh.smooth();
        HEC_Dual creator = new HEC_Dual();
        creator.setSource(mesh);
        dual = new HE_Mesh(creator);

        WB_Transform3D tran = new WB_Transform3D();
        tran.addRotateAboutAxis(PI/6,new WB_Point(0,0,0),new WB_Point(0,0,1));
        tran.addTranslate(new WB_Vector(-4000,2000,4000));

        mesh.applySelf(tran);
        dual.applySelf(tran);

        WB_Point[] points = new WB_Point[121];
        int index = 0;
        for (int j = 0; j < 11; j++) {
            for (int i = 0; i < 11; i++) {
                points[index] = new WB_Point(-200 + i * 40 + (((i != 0) && (i != 10)) ? random(-20, 20) : 0),
                        -200 + j * 40 + (((j != 0) && (j != 10)) ? random(-20, 20) : 0),
                        sin(TWO_PI / 20 * i) * 40 + cos(TWO_PI / 10 * j) * 40);
                index++;
            }
        }

        // create triangles from point grid
        WB_Triangle[] tris = new WB_Triangle[200];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tris[2 * (i + 10 * j)] = new WB_Triangle(points[i + 11 * j], points[i + 1 + 11 * j],
                        points[i + 11 * j + 11]);
                tris[2 * (i + 10 * j) + 1] = new WB_Triangle(points[i + 1 + 11 * j], points[i + 11 * j + 12],
                        points[i + 11 * j + 11]);
            }
        }

        HEC_FromTriangles tri_creator = new HEC_FromTriangles();
        tri_creator.setTriangles(tris);
        // alternatively tris can be any Collection<WB_Triangle>
        tri_mesh = new HE_Mesh(tri_creator);
    }

}
