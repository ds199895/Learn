package util.render;

import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Vector;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Halfedge;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;
import wblut.processing.WB_Render3D;

import java.awt.*;

/**
 * TODO
 *
 * @author libiao & hesiyuan
 * @version 1.0
 * @date 2022/4/20 10:33
 * Inst. AAA, S-ARCH, Southeast University
 */
public class HE_Render extends WB_Render3D{
    PApplet home;

    JtsRender jts;

    public HE_Render(PApplet home) {
        super(home);
        this.jts = new JtsRender(home);
        this.home = home;
    }

    public PApplet getApp() {
        return this.home;
    }

    public JtsRender getJtsRender() {
        return this.jts;
    }

    /**
     * 半边数据结构的显示
     *
     * He SiYuan
     * */
    public double calAverageEdgeLength(HE_Mesh mesh){
        double average=0;
        for(HE_Halfedge he:mesh.getHalfedges()){
            average+=he.getLength();

        }
        average/=mesh.getNumberOfHalfedges();
        return average;
    }

    public double[] calEdgeLengthRange(HE_Mesh mesh){
        double[]range=new double[2];
        double minLength=Double.MAX_VALUE;
        double maxLength=0;
        for(HE_Halfedge he:mesh.getHalfedges()){
            if(he.getLength()<minLength){
                minLength=he.getLength();
            }
            if(he.getLength()>maxLength){
                maxLength=he.getLength();
            }
        }
        range[0]=minLength;
        range[1]=maxLength;
        return range;
    }
    public void disPlayHeMesh(HE_Mesh mesh){
        displayHalfEdges(mesh);
        displayHeVertices(mesh);
    }
    public void disPlayHeMeshWithDegree(HE_Mesh mesh,double distance){
        displayHalfEdges(mesh);
        displayHeVerticesWithDegree(mesh,distance);
    }
    public void displayHeVerticesWithDegree(HE_Mesh mesh,double distance){
//        double coefficient=27.4*calAverageEdgeLength(mesh)/distance;
        double r=0;
        double averageLength=calAverageEdgeLength(mesh);
        //为实现在屏幕上的大小总为2mm
        double target=2;
        r=(distance/274)*target;

        //调控防止顶点显示与mesh显示比例过大或过小
        if(distance>averageLength*8){
            r=averageLength*8/274*target;
        }else if(distance<averageLength/2){
            r=averageLength/2/274*target;
        }
//            r=1000/coefficient;
//        }else if(distance>10000) {
//            r=10000/coefficient;
//        }else {
//            r=distance/coefficient;
//        }

        home.pushStyle();
        Color colorless=new Color(251, 199, 190);
        Color color5=new Color(249, 100, 100);
        Color color6=new Color(205, 237, 159);
        Color color7=new Color(95, 178, 199);
        Color colormore=new Color(166, 149, 223);
        for(HE_Vertex v:mesh.getVertices()){
            home.pushMatrix();
            if(v.getVertexDegree()==5){
                displaySingleHeVertex(v,color5,r);
            }else if(v.getVertexDegree()==6){
                displaySingleHeVertex(v,color6,r);
            }else if(v.getVertexDegree()==7){
                displaySingleHeVertex(v,color7,r);
            }else if(v.getVertexDegree()<5){
                displaySingleHeVertex(v,colorless,r);
            }else if(v.getVertexDegree()>7){
                displaySingleHeVertex(v,colormore,r);
            }

            home.popMatrix();
        }
        home.popStyle();
    }
    public void displayHeVertices(HE_Mesh mesh){
        double r=calEdgeLengthRange(mesh)[0];
        home.pushStyle();
        Color color=new Color(205, 237, 159);

        for(HE_Vertex v:mesh.getVertices()){
            home.pushMatrix();

            displaySingleHeVertex(v,color,r);
            home.popMatrix();
        }
        home.popStyle();
    }
    public void displaySingleHeVertex(HE_Vertex v,Color color,double r){

        home.pushStyle();
        home.fill(color.getRGB());
        home.noStroke();
        home.pushMatrix();

        home.translate(v.xf(),v.yf(),v.zf());
        home.sphere((float) r);
        home.popMatrix();
        home.popStyle();
    }
    public void displayHalfEdges(HE_Mesh mesh){
        home.pushStyle();
        Color color=new Color(95, 178, 199);
        home.stroke(color.getRGB());
        home.strokeWeight(2);
        for(HE_Halfedge he:mesh.getHalfedges()){
            displaySingleHalfEdge(he,color);
        }

        home.popStyle();
    }
    public void displaySingleHalfEdge(HE_Halfedge he,Color color){
        double offsetDis=he.getLength()/100.;
        home.pushStyle();
        home.stroke(color.getRGB());
        home.strokeWeight(2);
        HE_Face referFace=he.isOuterBoundary()?he.getPair().getFace():he.getFace();

        WB_Vector vec=new WB_Vector(he.getVertex(),he.getEndVertex());
        WB_Vector v= (WB_Vector) he.getHalfedgeDirection();
        WB_Vector v_ortho=v.rotateAboutAxis(90*home.DEG_TO_RAD,new WB_Point(0,0,0), referFace.getFaceNormal()).mul(offsetDis);
        WB_Point ps=he.getVertex().getPosition().add(v_ortho).add(vec.mul(1/3.));
        WB_Point pe=he.getVertex().getPosition().add(v_ortho).add(vec.mul(2/3.));
        WB_Vector v_arrow=v.rotateAboutAxis(150*home.DEG_TO_RAD, new WB_Point(0,0,0),referFace.getFaceNormal()).mul(he.getLength()/30.);

        WB_Point p_arrow=pe.add(v_arrow);
        line(ps.xf(),ps.yf(),ps.zf(),pe.xf(),pe.yf(),pe.zf());
        line(pe.xf(),pe.yf(),pe.zf(),p_arrow.xf(),p_arrow.yf(),p_arrow.zf());
        home.popStyle();
    }

}
