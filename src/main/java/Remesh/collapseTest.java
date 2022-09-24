package Remesh;

import guo_cam.CameraController;
import processing.core.PApplet;
import processing.core.PFont;
import wblut.geom.WB_Point;
import wblut.geom.WB_Transform3D;
import wblut.geom.WB_Triangle;
import wblut.geom.WB_Vector;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class collapseTest extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Remesh.collapseTest");
    }
    HE_Mesh mesh;
    List<WB_Triangle>tris=new ArrayList<WB_Triangle>();
    WB_Render render;
    double r=300;
    HE_Vertex ver=null;
    HE_Halfedge he=null;
    List<HE_Halfedge>reduceEdges=new ArrayList<>();
    List<HE_Halfedge>oppBound=new ArrayList<>();
    HE_Vertex vn=null;
    CameraController cam;
    HE_Halfedge singH=null;
    HE_Face singF=null;
    List<WB_Point>out_arrow=new ArrayList<>();
    int most=0;
    PFont font = createFont(PFont.list()[274],10);
    List<HE_Halfedge>test=new ArrayList<>();
    ImportObj im;
    HE_Mesh copy;
    HE_Vertex vMax=null;
    Util util;
    Import_dxf import_dxf;
    HE_Halfedge edge=null;
    double MinLength=Double.MAX_VALUE;
    public void setup(){
        size(1600,1600,P3D);
        render=new WB_Render(this);
        cam=new CameraController(this,200);
        cam.perspective();
        im=new ImportObj("D:\\360MoveData\\Users\\Administrator\\Documents\\WeChat Files\\wxid_h82vihn9t4rl22\\FileStorage\\File\\2022-09\\0917.3dm");
        import_dxf=new Import_dxf(this);
//        mesh=import_dxf.getMesh("E:\\12.dxf");
        mesh=im.getObj();
        textFont(font);
//        for(int i=0;i<10;i++){
//            int nxt=(i+1)%10;
//            WB_Point p=new WB_Point(width/2+r*sin(i*2*PI/10),height/2+r*cos(i*2*PI/10),0);
//            WB_Point p_nxt=new WB_Point(width/2+r*sin(nxt*2*PI/10),height/2+r*cos(nxt*2*PI/10),0);
//            WB_Triangle tri=new WB_Triangle(new WB_Point(width/2,height/2,0),p,p_nxt);
//            tris.add(tri);
//        }
//        HEC_FromTriangles hec_t=new HEC_FromTriangles().setTriangles(tris);
//        mesh=new HE_Mesh(hec_t);
//        createMeshes();
//        mesh=tri_mesh;
        copy=mesh.copy();
        for(HE_Vertex vertex:copy.getVertices()){
            vertex.setX(vertex.xf()+80000);
        }
        for(HE_Vertex v:mesh.getVertices()){
            if(v.getVertexDegree()>4){
                ver=v;
            }
        }
//        for(int i=0;i<5;i++){
//            reduceEdges.add(ver.getHalfedgeStar().get(i));
//        }
//
//        HE_Halfedge he2=reduceEdges.get(reduceEdges.size()-1).getNextInVertex();
//        HE_Halfedge he3=reduceEdges.get(0).getPrevInVertex();
//        oppBound.add(he2);
//        oppBound.add(he3);
        calculateFeatures(mesh);

//        println(2440.5%500);
    }

    public void draw(){
        background(255);
        stroke(0);
        if(singF!=null){
            pushStyle();
            Color color=new Color(139, 213, 233,30);
            fill(color.getRGB());
//                render.drawEdge(e);
//                render.drawPoint2D(e.getEndVertex(),4);
            render.drawPolygonEdges(singF.getPolygon());


            popStyle();
        }
        if(copy!=null){
            render.drawFaces(copy);
        }
        if(display) {
            displayHalfEdges(mesh);
        }
        render.drawFaces(mesh);
//        if(he!=null){
//            pushStyle();
//            stroke(0,255,0);
//            strokeWeight(2);
//            render.drawEdge(he);
//            popStyle();
//        }
        if(reduceEdges!=null){
            pushStyle();
            Color color=new Color(0,255,0);
            for(HE_Halfedge e:reduceEdges){
//                render.drawEdge(e);
//                render.drawPoint2D(e.getEndVertex(),4);
                if(display) {
                    displaySingleHalfEdge(e, color);
                }
            }

            popStyle();
        }

        if(singH!=null){
            pushStyle();
            Color color=new Color(255, 179,0);
//                render.drawEdge(e);
//                render.drawPoint2D(e.getEndVertex(),4);
                if(display) {
                    displaySingleHalfEdge(singH, color);
                }


            popStyle();
        }

        if(oppBound!=null){
            pushStyle();
            Color color=new Color(0,0,255);
            for(HE_Halfedge e:oppBound){
                if(display) {
                    displaySingleHalfEdge(e, color);
                }
            }

            popStyle();
        }
        if(vn!=null){
            pushStyle();
            fill(255,0,0,20);
            render.drawPoint(vn,400);
            popStyle();
        }
        if(edge!=null){
            pushStyle();
            Color color=new Color(255,0,0);
            strokeWeight(5);
            displaySingleHalfEdge(edge,color);

            popStyle();
        }
        if(vMax!=null){
            pushStyle();
            fill(255,255,0);
            render.drawPoint(vMax,400);
            popStyle();
        }
//        if(test!=null){
//            pushStyle();
//            Color color=new Color(248, 77, 188);
//            for(HE_Halfedge e:test){
////                render.drawEdge(e);
////                render.drawPoint2D(e.getEndVertex(),4);
//                if(display) {
//                    displaySingleHalfEdge(e, color);
//                }
//            }
//
//            popStyle();
//        }
        pushStyle();
        fill(255,0,0);
        for(int i=0;i<mesh.getVertices().size();i++){
            HE_Vertex ver=mesh.getVertices().get(i);
            WB_Vector dir=new WB_Vector(ver.xf()-ver.getHalfedge().getEndVertex().xf(),ver.yf()-ver.getHalfedge().getEndVertex().yf());
            WB_Vector norm=dir.getOrthoNormal2D();
            WB_Vector p=WB_Vector.add(ver,norm.scaleSelf(8));
            textAlign(CENTER);
            text(String.valueOf(ver.getVertexDegree()),p.xf(),p.yf());
        }
        popStyle();
    }
    boolean display=false;
    int index=0;
    public void keyReleased(){
        if(key=='s'){
            index=(index+1)%ver.getVertexDegree();
            he=ver.getHalfedgeStar().get(index);
        }
        if(key=='a'){
            println("Reduce Vertex Degree!*************");
//            mesh.triangulate();

//            println(vn.getVertexDegree());
            while (most>7){
                reduceVertexDegree(mesh,1500);
                calculateFeatures(mesh);
            }
//            oppBound.clear();
//            HE_Halfedge he2=reduceEdges.get(reduceEdges.size()-1).getNextInVertex();
//            HE_Halfedge he3=reduceEdges.get(0).getPrevInVertex();
//            oppBound.add(he2);
//            oppBound.add(he3);

        }
        if(key=='d'){
            display=!display;
        }
        if(key=='t'){
            cam.top();
        }
        if(key=='p'){
            cam.perspective();
        }
        if(key=='s'){
            HET_Export.saveAsOBJ(mesh,"E://", "hsy");
            println("OBJ Saved!*************");
        }
        if(key=='c'){
            println("Collapse closer vertices!*************");
            collapseVertex(mesh,1000);
            calculateFeatures(mesh);
        }
        if (key == 'f') {
            Util.flipEdgeControl(mesh,PI*(2/3.));
            calculateFeatures(mesh);
        }
        if (key == 'l') {
//            Util.SplitAllEdges(mesh);
            Util.SplitAllLongEdges(mesh,3000);
            calculateFeatures(mesh);
        }
        if (key == 'w') {
            List<int[]>types=calculateLengthTypes(mesh,5);
            println("Total Types: "+types.size());
            for(int i=0;i<types.size();i++){
                println("Type Index: "+i+"  Length: "+types.get(i)[0]+"  Type Number: "+types.get(i)[1]);
            }
        }
        if (key == '1') {

        }
    }
    HE_Mesh dual;
    HE_Mesh tri_mesh;
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
                points[index] = new WB_Point(0 + i * 120 + (((i != 0) && (i != 10)) ? random(-20, 20) : 0),
                        120+ j * 40 + (((j != 0) && (j != 10)) ? random(-20, 20) : 0),
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
    public void calculateFeatures(HE_Mesh mesh){
        int most_temp=0;
        for(HE_Vertex vertex:mesh.getVertices()){
            if(vertex.getVertexStar().size()>most_temp){
                most_temp=vertex.getVertexStar().size();
            }
        }
        System.out.println("Post Max Degree: "+most_temp);
        most=most_temp;
        double Min_temp=Double.MAX_VALUE;
        for(HE_Halfedge edge:mesh.getHalfedges()){
            if(edge.getLength()<Min_temp){
                Min_temp=edge.getLength();
            }
        }
        println("Post Min Length: "+Min_temp);
        MinLength=Min_temp;
    }
    public List<int[]> calculateLengthTypes(HE_Mesh mesh,int tolerance){
        List<int[]>types=new ArrayList<>();

        int minLength=Integer.MAX_VALUE;
        int maxLength=0;
        for(HE_Halfedge he:mesh.getHalfedges()){
            if(he.getLength()>maxLength){
                maxLength=(int)he.getLength();

            }
            if(he.getLength()<minLength){
                minLength=(int)he.getLength();
            }
        }
//        int tc=0;
//        for(HE_Halfedge he :mesh.getHalfedges()){
//            if((int)he.getLength()>=minLength&&(int)he.getLength()<minLength+tolerance){
//                tc++;
//            }
//        }
//        if(tc>0){
//            int[]first=new int[2];
//            first[0]=minLength;
//            first[1]=tc/2;
//            types.add(first);
//        }
        minLength=(int)(minLength/500)*500;
        for(int i = minLength; i<maxLength-tolerance; i+=tolerance){
            int nxt=i+tolerance;
            int typeCount=0;
            for(HE_Halfedge he :mesh.getHalfedges()){
                if((int)he.getLength()>=i&&(int)he.getLength()<nxt){
                    typeCount++;
                }
            }
            if(typeCount>0){
                int[] sub_type=new int[2];
                sub_type[0]=i;
                sub_type[1]=typeCount/2;
                types.add(sub_type);
            }
        }

        int tc=0;
        for(HE_Halfedge he :mesh.getHalfedges()){
            if((int)he.getLength()>=(int)((maxLength-minLength)/tolerance)*tolerance+minLength){
                tc++;
            }
        }
        if(tc>0){
            int[]last=new int[2];
            last[0]=(int)((maxLength-minLength)/tolerance)*tolerance+minLength;
            last[1]=tc/2;
            types.add(last);
        }

        int eCount=0;
        for(int[] sub:types){
            eCount+=sub[1];
        }
        println("Total Edges Count:  "+mesh.getNumberOfEdges());
        println("Examing Edges Count:  "+eCount);
        return types;
    }
    public void collapseVertex(HE_Mesh mesh,double minLength){
        for(HE_Halfedge h:mesh.getHalfedges()){
            if(h.getLength()<minLength){
                collapseEdgeBoundaryPreserving(mesh,h,true);
            }
        }

    }

    public static boolean collapseEdgeBoundaryPreserving(HE_Mesh mesh, HE_Halfedge e, boolean strict) {
        if (mesh.contains(e)) {
            HE_Halfedge he = e.isEdge() ? e : e.getPair();
            HE_Halfedge hePair = he.getPair();
            HE_Face f = he.getFace();
            HE_Face fp = hePair.getFace();
            HE_Vertex v = he.getVertex();
            HE_Vertex vp = hePair.getVertex();
            if (v.isBoundary()) {
                if (vp.isBoundary()) {
                    if (!he.isInnerBoundary() || strict) {
                        return false;
                    }
                } else {
                    vp.set(v);
                }
            }
            if(v.getVertexDegree()>vp.getVertexDegree()){
                vp.set(v);
            }

            List<HE_Halfedge> tmp = v.getHalfedgeStar();

            for(int i = 0; i < tmp.size(); ++i) {
                mesh.setVertex((HE_Halfedge)tmp.get(i), vp);
            }

            mesh.setHalfedge(vp, hePair.getNextInVertex());
            HE_Halfedge hen = he.getNextInFace();
            HE_Halfedge hep = he.getPrevInFace();
            HE_Halfedge hePairn = hePair.getNextInFace();
            HE_Halfedge hePairp = hePair.getPrevInFace();
            if (f != null) {
                mesh.setHalfedge(f, hen);
            }

            if (fp != null) {
                mesh.setHalfedge(fp, hePairn);
            }

            mesh.setNext(hep, hen);
            mesh.setNext(hePairp, hePairn);
            mesh.remove(he);
            mesh.remove(hePair);
            mesh.remove(e);
            mesh.remove(v);
            if (f != null) {
                HET_Fixer.deleteTwoEdgeFace(mesh, f);
            }

            if (fp != null) {
                HET_Fixer.deleteTwoEdgeFace(mesh, fp);
            }

            return true;
        } else {
            return false;
        }
    }
    public void reduceVertexDegree(HE_Mesh mesh, double d) {
        HE_Vertex maxDegree=null;
        int Max=0;

        for(HE_Vertex ver:mesh.getVertices()) {
            if(ver.getVertexDegree()>Max){
                Max=ver.getVertexDegree();
                maxDegree=ver;
            }
        }
        if(maxDegree.getVertexDegree()>7) {
            List<HE_Halfedge>reduceEdges=new ArrayList<>();
            int cutCount=(int)(maxDegree.getVertexDegree()/2);
            int cutMid=(int)(cutCount/2);
            for(int i=0;i<cutCount;i++){
                reduceEdges.add(maxDegree.getHalfedgeStar().get(i));
            }

            HE_Halfedge he0 = reduceEdges.get(0);
            HE_Halfedge he1 = reduceEdges.get(reduceEdges.size() - 1);
            HE_Halfedge he2 = he1.getNextInVertex();
            HE_Halfedge he3 = he0.getPrevInVertex();

            HE_Halfedge he0_p = he0.getPair();
            HE_Halfedge he1_p = he1.getPair();
            HE_Halfedge he2_p = he2.getPair();
            HE_Halfedge he3_p = he3.getPair();

            HE_Halfedge he_opp1=he0.getNextInFace();
            HE_Halfedge he_opp2=he1_p.getPrevInFace();

            HE_Vertex v_ori = he0.getVertex();
//            WB_Vector v1 = new WB_Vector(he0.getEndVertex().xf() - he0.getVertex().xf(), he0.getEndVertex().yf() - he0.getVertex().yf());
//            WB_Vector v2 = new WB_Vector(he1.getEndVertex().xf() - he1.getVertex().xf(), he1.getEndVertex().yf() - he1.getVertex().yf());
//            WB_Vector v = WB_Vector.add(v1, v2);
//            v.normalizeSelf();
            WB_Vector v= (WB_Vector) reduceEdges.get(cutMid).getHalfedgeDirection();
            v = v.mul(d);

            HE_Vertex vNew = new HE_Vertex((double) (v_ori.xf() + v.xf()), (double) (v_ori.yf() + v.yf()), (double) (v_ori.zf() + v.zf()));
//                if(WB_GeometryOp.contains2D(vNew,))
            vn = vNew;

            HE_Halfedge henew1 = new HE_Halfedge();
            HE_Halfedge henew2 = new HE_Halfedge();

            HE_Face f_nex = he1_p.getFace();
            HE_Face f_pre = he0.getFace();

            List<HE_Halfedge> v_edges = v_ori.getHalfedgeStar();
            List<HE_Face> v_faces = new ArrayList<>();
            List<HE_Halfedge> prv_edges = new ArrayList<>();
            v_edges.removeAll(reduceEdges);
            for (HE_Halfedge v_edge : v_edges) {
                HE_Face v_face = v_edge.getFace();
                prv_edges.add(v_edge.getPrevInFace());
                v_faces.add(v_face);
            }
            for (HE_Halfedge e : reduceEdges) {
                HE_Halfedge pair=e.getPair();
                HE_Halfedge pre=e.getPrevInFace();
                HE_Halfedge nxt=e.getNextInFace();
                mesh.setVertex(e, vNew);
                mesh.setPair(pair,e);
                mesh.setNext(pre,e);
                mesh.setNext(e,nxt);
            }

            //建立新加点与原先点相互引用的半边
            mesh.setVertex(henew1, v_ori);
            mesh.setVertex(henew2, vNew);

            //将前述半边更新为新加点和原始点的根半边
            mesh.setHalfedge(vNew, henew2);
            mesh.setHalfedge(v_ori, henew1);

            //半边对应用关系更新
            mesh.setPair(henew1, henew2);
            mesh.setPair(he0, he0_p);
            mesh.setPair(he1, he1_p);
            mesh.setPair(he2, he2_p);
            mesh.setPair(he3, he3_p);
            //将新添加的边放进面里
            if (f_nex != null) {
                mesh.setFace(henew1, f_pre);
                mesh.setFace(he3_p,f_pre);
                mesh.setFace(he0,f_pre);
                mesh.setFace(he_opp1,f_pre);
            }
            test.add(henew1);
            test.add(he3_p);
            test.add(he0);
            if (f_pre != null) {
                mesh.setFace(henew2, f_nex);
                mesh.setFace(he_opp2,f_nex);
                mesh.setFace(he1_p,f_nex);
                mesh.setFace(he2,f_nex);

            }
//                test.add(henew2);
//                test.add(he1_p);
//                test.add(he2);
            //更新原先面对原先点的半边的引用
            for (int l = 0; l < v_edges.size(); l++) {
                mesh.setFace(v_edges.get(l), v_faces.get(l));
            }
            //更新原先点半边的上一半边引用，确保通路
            for (int l = 0; l < v_edges.size(); l++) {
                mesh.setVertex(v_edges.get(l), v_ori);
                mesh.setNext(prv_edges.get(l), v_edges.get(l));
            }
            singH = he1;
            singF = f_nex;

            //更新所有需要更新的半边顺序
            mesh.setNext(he3_p, henew1);
            mesh.setNext(henew1, he0);
            mesh.setNext(he0,he_opp1);

            mesh.setNext(he1_p, henew2);
            mesh.setNext(henew2, he2);
            mesh.setNext(he2,he_opp2);

            mesh.setPair(henew1, henew2);
            mesh.setPair(he0, he0_p);
            mesh.setPair(he1, he1_p);
            mesh.setPair(he2, he2_p);
            mesh.setPair(he3, he3_p);

            mesh.addDerivedElement(vNew, new HE_Element[]{he0});
            mesh.addDerivedElement(henew1, new HE_Element[]{f_pre});
            mesh.addDerivedElement(henew2, new HE_Element[]{f_nex});
            if(f_pre!=null){
                println("endVertex: "+he0.getEndVertex());
                HE_MeshOp.splitFace(mesh,f_pre,v_ori,he0.getEndVertex());
            }
            if(f_nex!=null){
                HE_MeshOp.splitFace(mesh,f_nex,vNew,he2.getEndVertex());
            }

        }
    }
    public void displayHalfEdges(HE_Mesh mesh){
        pushStyle();
        Color color=new Color(95, 178, 199);
        stroke(color.getRGB());
        strokeWeight(2);
        for(HE_Halfedge he:mesh.getHalfedges()){
            double offsetDis=he.getLength()/100;
            stroke(color.getRGB());
            strokeWeight(2);
            HE_Face referFace=he.isOuterBoundary()?he.getPair().getFace():he.getFace();

            WB_Vector vec=new WB_Vector(he.getVertex(),he.getEndVertex());
            WB_Vector v= (WB_Vector) he.getHalfedgeDirection();
            WB_Vector v_ortho=v.rotateAboutAxis(90*DEG_TO_RAD,new WB_Point(0,0,0), referFace.getFaceNormal()).mul(offsetDis);
            WB_Point ps=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
            WB_Point pe=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
            WB_Vector v_arrow=v.rotateAboutAxis(150*DEG_TO_RAD, new WB_Point(0,0,0),referFace.getFaceNormal()).mul(he.getLength()/120);

            WB_Point p_arrow=pe.add(v_arrow);
            line(ps.xf(),ps.yf(),ps.zf(),pe.xf(),pe.yf(),pe.zf());
            line(pe.xf(),pe.yf(),pe.zf(),p_arrow.xf(),p_arrow.yf(),p_arrow.zf());
        }

        popStyle();
    }
    public void displaySingleHalfEdge(HE_Halfedge he,Color color){
        double offsetDis=he.getLength()/100;
        pushStyle();;
        stroke(color.getRGB());
        strokeWeight(2);
        HE_Face referFace=he.isOuterBoundary()?he.getPair().getFace():he.getFace();

        WB_Vector vec=new WB_Vector(he.getVertex(),he.getEndVertex());
        WB_Vector v= (WB_Vector) he.getHalfedgeDirection();
        WB_Vector v_ortho=v.rotateAboutAxis(90*DEG_TO_RAD,new WB_Point(0,0,0), referFace.getFaceNormal()).mul(offsetDis);
        WB_Point ps=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
        WB_Point pe=he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
        WB_Vector v_arrow=v.rotateAboutAxis(150*DEG_TO_RAD, new WB_Point(0,0,0),referFace.getFaceNormal()).mul(he.getLength()/120);

        WB_Point p_arrow=pe.add(v_arrow);
        line(ps.xf(),ps.yf(),ps.zf(),pe.xf(),pe.yf(),pe.zf());
        line(pe.xf(),pe.yf(),pe.zf(),p_arrow.xf(),p_arrow.yf(),p_arrow.zf());
        popStyle();
    }
}
