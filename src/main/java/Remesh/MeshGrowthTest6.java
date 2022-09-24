package Remesh;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PConstants;
import wblut.geom.*;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;


/**
 * 更新于2022.5.15
 * 更新了网格分裂的方法，加入了flipEdge方法平衡细分时的拓扑关系
 * 采用了热量扩散的方法拟合计算测地线距离
 */

public class MeshGrowthTest6 extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Remesh.MeshGrowthTest6");
    }

    HE_Mesh mesh;
    WB_Render render;


    PeasyCam camera;

    int MaxVertexCount = 10000;
    double CollisionDistance = 100;     //最小碰撞距离
    double MaxSplitDistance = 90;    //分裂距离，线段长度大于分裂距离时会分裂出新节点
    double MinSplitDistance = 20;
    double FinalLength = 100;

    double CollisionWeight = 20000;     //碰撞因子权重
    double EdgeLengthConstraintWeight = 1000;      //线段收缩力权重
    double BendingResistanceWeight = 300000;    //抗弯力因子权重
    double ControlEdgeAngleWeight = 600000;  //边缘角度控制权重

    int count=0;
    ArrayList<WB_Vector> totalWeightedMoves;             //总移动向量
    ArrayList<Double> totalWeights;        //碰撞次数
    ArrayList<Double> GrowthIntensity;        //生长因子
    ArrayList<HE_Vertex> vertices;        //生长因子
    WB_KDTreeInteger<WB_Point> tree;     //KD树形结构
    WB_KDTreeInteger.WB_KDEntryInteger<WB_Point>[] inRange;

    public void setup() {
        size(1600, 1000, P3D);
//        smooth(8);
//        frameCount=5;
        camera = new PeasyCam(this, 0.5 * width, 0.5 * height, 50, 50);

        int count = 5;
        WB_Point[] points = new WB_Point[count * count];
        int index = 0;

        for (int j = 0; j < count; j++) {
            for (int i = 0; i < count; i++) {
//                points[index] = new WB_Point(i * 40, j * 40, sin(TWO_PI / 20 * i) * 40 + cos(TWO_PI / 10 * j) * 40);
                points[index]=new WB_Point(-200+ i * 40+(((i!=0)&&(i!=10))?random(-20, 20):0),-200+j * 40+(((j!=0)&&(j!=10))?random(-20, 20):0),sin(TWO_PI/20*i)*100+cos(TWO_PI/10*j)*100);
                index++;
            }
        }

        //create triangles from point grid
        WB_Triangle[] tris = new WB_Triangle[2 * (count - 1) * (count - 1)];

        for (int i = 0; i < (count - 1); i++) {
            for (int j = 0; j < (count - 1); j++) {
                tris[2 * (i + (count - 1) * j)] = new WB_Triangle(points[i + count * j], points[i + 1 + count * j], points[i + count * j + count]);
                tris[2 * (i + (count - 1) * j) + 1] = new WB_Triangle(points[i + 1 + count * j], points[i + count * j + count + 1], points[i + count * j + count]);
            }
        }

        HEC_FromTriangles creator = new HEC_FromTriangles();

        creator.setTriangles(tris);
        //alternatively tris can be any Collection<WB_Triangle>
        mesh = new HE_Mesh(creator);

//        mesh = HET_Import.readFromObjFile("G:\\obj\\111.obj");

        render = new WB_Render(this);

        totalWeightedMoves = new ArrayList<WB_Vector>();        //记录总移动向量
        totalWeights = new ArrayList<Double>();          //记录碰撞次数
        GrowthIntensity = new ArrayList<Double>();
        vertices = new ArrayList<HE_Vertex>();     //记录顶点数组

        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            totalWeightedMoves.add(new WB_Vector(0, 0, 0));
            totalWeights.add(0.0);
            GrowthIntensity.add(0.0);
            vertices.add(mesh.getVertexWithIndex(i));
        }
        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            if (mesh.getVertexWithIndex(i).isBoundary()) {
                GrowthIntensity.set(i, 1.0);
            }
        }
        getGrowthIntensity();

    }
    boolean drawStroke=true;

    public void draw() {
        background(0);
//        directionalLight(255, 255, 255, 1, 1, -1);
//        directionalLight(127, 127, 127, -1, -1, 1);
        lights();
        translate(width / 2, height / 2);
//        rotateY(mouseX*1.0f/width*TWO_PI);
//        rotateX(mouseY*1.0f/height*TWO_PI);


        if (record) {
            beginRecord("nervoussystem.obj.OBJExport", "G:\\objdc\\output.obj");
        }
        if(drawStroke) {
            strokeWeight(1);
            stroke(0);
            render.drawEdges(mesh);

            noStroke();
            fill(255, 0, 0);
            render.drawFaces(mesh);
//            wb_simpleMesh = mesh.toFacelistMesh();
//            pappletDrawMesh(this, wb_simpleMesh);

        }else{
            noStroke();
            fill(255,0,0);
            for(HE_Face f:mesh.getFaces()){
                render.drawFace(f);
            }
        }
        stroke(0, 255, 0);
        strokeWeight(4);
        render.drawEdge(mesh.getHalfedgeWithIndex(count % mesh.getNumberOfHalfedges()));

        if(mesh.getHalfedgeWithIndex(count%mesh.getNumberOfHalfedges())==null){
            print("null");
        }

//        render.drawFaces(mesh);

//        for(int i=0;i<mesh.getNumberOfVertices();i++){
//            pushMatrix();
//            translate(mesh.getPoints().get(i).xf(),mesh.getPoints().get(i).yf(),mesh.getPoints().get(i).zf());
//            sphere((float) (0.5*CollisionDistance));
//            popMatrix();
//        }

//        HE_Vertex v1=mesh.getVertexWithIndex(3);
//        for (HE_Vertex ver :v1.getNeighborVertices()){
//            render.drawVertex(ver,3);
//        }


        for (int i = 0; i < 1; i++) {
            if (growth) {
                update();
            }
        }

//        for(int i=0;i<mesh.getNumberOfHalfedges();i++){
//            System.out.println("半边"+i+"的长度"+mesh.getHalfedgeWithIndex(i).getLength());
//        }

        if (record) {                //记录结束
            endRecord();
            record = false;
        }
    }

//    private void pappletDrawMesh(PApplet app, WB_SimpleMesh m) {
//        if (m == null) {
//            return;
//        }
//        if (m.getNumberOfVertices() == 0) {
//            return;
//        }
//        for (final int[] face : m.getFacesAsInt()) {
//            pappletDrawPolygon(app, face, m.getPoints());
//        }
//    }

    private void pappletDrawPolygon(PApplet app, final int[] indices, final WB_CoordCollection points) {
        if (points != null && indices != null) {
            app.beginShape(PConstants.POLYGON);
            for (final int indice : indices) {
                app.vertex(points.get(indice).xf(), points.get(indice).yf(), points.get(indice).zf());
            }
            app.endShape(PConstants.CLOSE);
        }
    }

    boolean growth = false;
    boolean record = false;

    public void keyPressed() {
        if (key == 's') {
            growth = !growth;
        }
        if (key == 'p') {
            System.out.println("SplitPreMesh=............"+mesh.getNumberOfFaces());
            SplitAllLongEdges(20);
            System.out.println("SplitAfterMesh=............"+mesh.getNumberOfFaces());
        }
        if (key == 'm') {
            mesh.smooth(2);
        }
        if (key == 'r') {
            record = true;
        }
        if (key == 'c') {
            System.out.println("FlipPreMesh=............"+mesh.getNumberOfHalfedges());
            flipEdgeControl();
            System.out.println("FlipAfterMesh=............"+mesh.getNumberOfHalfedges());
        }
        if (key == 't') {
            getGrowthIntensity();
        }
        if (key == 'a') {
            count+=1;
        }
    }

    public void update() {

        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {                   //更新生长因子
//            System.out.println("....第" + i + "个顶点生长因子是" + GrowthIntensity.get(i));
            render.drawVertex(mesh.getVertexWithIndex(i), 5 * GrowthIntensity.get(i));
        }

        SplitAllLongEdges(20);   //分裂较长的边缘
//        adaptiveSubdivision();
        flipEdgeControl();

        totalWeightedMoves = new ArrayList<WB_Vector>();        //记录总移动向量
        totalWeights = new ArrayList<Double>();          //记录碰撞次数
        vertices = new ArrayList<HE_Vertex>();     //记录顶点数组
        GrowthIntensity = new ArrayList<Double>();  //记录生长因子


        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            totalWeightedMoves.add(new WB_Vector(0, 0, 0));
            totalWeights.add(0.0);
            vertices.add(mesh.getVertexWithIndex(i));
        }

        getKDTree();//运用KD树形结构计算

        for (int i = 0; i < 5; i++) {
            getGrowthIntensity();    //获取生长因子,扩散次数为5
        }

        ProcessCollisionUsingKDTree();     //计算碰撞
        EdgeLengthConstraintUsingKDTree();      //计算边长收缩，控制每条边长尽量相同
        BendingResistanceUsingKDTree();     //计算抗弯折力
        ControlEdgeAngleUsingKDTree();     //边缘角度控制


        updateVertexPosition();
    }

    public void getKDTree() {
        tree = new WB_KDTreeInteger<WB_Point>();
        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            tree.add(mesh.getPositionWithIndex(i), i);
        }
    }


    public void ProcessCollisionUsingKDTree() {
        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            //将每个顶点碰撞范围内的点记录到inRange数组中
            inRange = tree.getRange(mesh.getPositionWithIndex(i), CollisionDistance);
//            System.out.println("......................."+inRange.length);

            //对于每一个顶点遍历其inRange数组中的每个点
            for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> j : inRange) {
                if (i > j.value || i == j.value) continue;
//                System.out.println(j.value+"   i="+i);

                WB_Vector move = WB_Vector.sub(mesh.getPositionWithIndex(i), mesh.getPositionWithIndex(j.value));
                double currentDistance = move.getLength();
                move = WB_Vector.mul(move, 0.5 * (CollisionDistance - currentDistance) / currentDistance);

                totalWeightedMoves.get(i).set(WB_Vector.add(totalWeightedMoves.get(i), (WB_Vector.mul(move, CollisionWeight))));
                totalWeightedMoves.get(j.value).set(WB_Vector.sub(totalWeightedMoves.get(j.value), (WB_Vector.mul(move, CollisionWeight))));
                totalWeights.set(i, totalWeights.get(i) + CollisionWeight);
                totalWeights.set(j.value, totalWeights.get(j.value) + CollisionWeight);
            }
        }

    }

    public void getGrowthIntensity() {
        ArrayList next = new ArrayList<Double>();          //记录下次迭代的生长因子

        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            next.add(0.0);
            if (i > GrowthIntensity.size() || i == GrowthIntensity.size()) {   //新分裂出的顶点加入数组后面，生长因子为0
                GrowthIntensity.add(0.0);
            }
        }

        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {                       //计算出next的生长因子
//            System.out.println("第"+i+"个顶点生长因子是"+GrowthIntensity.get(i));
            if (mesh.getVertexWithIndex(i).isBoundary()) {
                next.set(i, 1.0);
            } else {
                HE_Vertex v1 = mesh.getVertexWithIndex(i);

                double all = GrowthIntensity.get(i);

                for (HE_Vertex ver : v1.getNeighborVertices()) {
                    int index = vertices.indexOf(ver);   //获取周围点的序号
                    all = GrowthIntensity.get(index) + all;
                }
                next.set(i, all / (v1.getNeighborVertices().size() + 1));
            }
        }

        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {                   //更新生长因子
            double n1 = (double) next.get(i);
            GrowthIntensity.set(i, n1);
//            System.out.println("....第"+i+"个顶点生长因子是"+GrowthIntensity.get(i));
//            System.out.println(GrowthIntensity.size());
//            System.out.println(mesh.getNumberOfVertices());
        }
    }

    public double getSplitDistance(HE_Halfedge he) {
        double splitDistance_;

        HE_Vertex v1 = he.getVertex();
        HE_Vertex v2 = he.getEndVertex();

        int index1 = vertices.indexOf(v1);
        int index2 = vertices.indexOf(v2);

        if (index1 >= 0 && index2 >= 0) {

//        System.out.println("index1="+index1  +"       index2="+index2  );

            double n1 = (float) ((GrowthIntensity.get(index1) + GrowthIntensity.get(index2)) * 0.5);      //两个端点的测地线平均值
            double n2 = map((float) n1, 0, 1.0F, 1.0F, 0);
            splitDistance_ = n2 * (MaxSplitDistance - MinSplitDistance) + MinSplitDistance;
//        System.out.println("n1="+n1);
//            System.out.println(n2);
//            if (vertices.size() < 500) {
//                splitDistance_ = 5f * (float) SplitDistance;
//            } else {
//                splitDistance_ = map((float) n1, 0, 1.0F, 5f * (float) SplitDistance, (float) SplitDistance);
//            }
        } else {
            splitDistance_ = Double.MAX_VALUE;
        }
        return splitDistance_;
    }

    public void SplitAllLongEdges(double target) {
        boolean done=false;
        while(!done){
            done=true;
            for (HE_Halfedge he : mesh.getHalfedges()) {
                if (mesh.getNumberOfVertices() < MaxVertexCount && he.getLength() >4/3.*target) {
                    if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
                        if (he.getLength() > he.getNextInFace().getLength()
                                && he.getLength() > he.getPrevInFace().getLength()
                                && he.getPair().getLength() > he.getPair().getNextInFace().getLength()
                                && he.getPair().getLength() > he.getPair().getPrevInFace().getLength()) {

                            HE_Face f1 = he.getFace();             //f1：半边的相邻面
                            HE_Face f2 = he.getPair().getFace();             //f2：相邻半边的相邻面

                            HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点
                            HE_Vertex v2_opp = he.getPair().getPrevInFace().getVertex();      // v2_opp：半边相邻三角面分裂边的另一个顶点

    //                        println("mesh vertices size: "+mesh.getVertices().size()+", vertices size: "+vertices.size());
                            int index1 = vertices.indexOf(he.getVertex());
                            int index2 = vertices.indexOf(he.getEndVertex());
    //                        System.out.println("index1="+index1+".............index2="+index2);
                            double newVgrowthIntensity = (GrowthIntensity.get(index1) + GrowthIntensity.get(index2)) / 2;
    //                        println(newVgrowthIntensity);

                            HE_MeshOp.splitEdge(mesh, he);        //长边从中点分割一半
                            HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1

                            vertices.add(v1);
                            GrowthIntensity.add(newVgrowthIntensity);

                            HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                            HE_MeshOp.splitFace(mesh, f2, v1, v2_opp);
                        }

                    } else if (he.isInnerBoundary()) {
                        if (he.getLength() > he.getNextInFace().getLength() && he.getLength() > he.getPrevInFace().getLength()) {
                            HE_Face f1 = he.getFace();             //f1：半边的相邻面


                            HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点
    //                        println("mesh vertices size: "+mesh.getVertices().size()+", vertices size: "+vertices.size());
                            int index1 = vertices.indexOf(he.getVertex());
                            int index2 = vertices.indexOf(he.getEndVertex());
    //                        System.out.println("index1="+index1+".............index2="+index2);
                            double newVgrowthIntensity = (GrowthIntensity.get(index1) + GrowthIntensity.get(index2)) / 2;
    //                        println("new vertex growthIntensity: "+newVgrowthIntensity);
                            HE_MeshOp.splitEdge(mesh, he);        //长边从中点分割一半
                            HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1

                            vertices.add(v1);
                            GrowthIntensity.add(newVgrowthIntensity);

                            HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                        }

                    } else if (he.isOuterBoundary()) {
                        continue;
                    }

                }
            }
            for(HE_Halfedge he :mesh.getHalfedges()){
                if(he.getLength()>4/3.*target){
                    done=false;
                }
            }
        }
    }

    public void flipEdgeControl() {
        for (HE_Halfedge he : mesh.getHalfedges()) {
            double angle1 = getHalfEdgeAngle(he);    //半边对应角度
            double angle2 = getHalfEdgeAngle(he.getPair());     //半边对边对应角度
            double angle3 = getHalfEdgeAngle(he.getPair().getNextInFace());
            double angle4 = getHalfEdgeAngle(he.getPair().getNextInFace().getNextInFace());
            if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
                if (angle1>0.5*PI&&angle2>0.5*PI){
                    FlipEdge(mesh,he);
                    HE_MeshOp.flipEdge(mesh,he);
                }else if(angle1>0.6*PI && angle2<0.5*PI && angle3<0.5*PI && angle4<0.5*PI){
                    FlipEdge(mesh,he);
                }
            }
        }
    }

    public double getHalfEdgeAngle(HE_Halfedge _he) {
        WB_Vector v1 = _he.getNextInFace().getPosition();  //下一条半边顶点
        WB_Vector v2 = _he.getNextInFace().getEndPosition();  //下一条半边终点

        WB_Vector v3 = _he.getNextInFace().getNextInFace().getPosition();  //下下一条半边顶点
        WB_Vector v4 = _he.getNextInFace().getNextInFace().getEndPosition();  //下下一条半边终点

        WB_Vector vP = WB_Vector.sub(v1, v2);
        WB_Vector vQ = WB_Vector.sub(v4, v3);
        double angle = WB_Vector.getAngle(vP, vQ);    //半边对应角度

        return angle;
    }
    public void FlipEdge(HE_Mesh mesh, HE_Halfedge h0) {
        HE_Halfedge h1 = h0.getNextInFace();
        HE_Halfedge h2 = h1.getNextInFace();
        HE_Halfedge h3 = h0.getPair();
        HE_Halfedge h4 = h3.getNextInFace();
        HE_Halfedge h5 = h4.getNextInFace();
        HE_Halfedge h6 = h1.getPair();
        HE_Halfedge h7 = h2.getPair();
        HE_Halfedge h8 = h4.getPair();
        HE_Halfedge h9 = h5.getPair();

        HE_Vertex v0=h0.getVertex();
        HE_Vertex v1=h3.getVertex();
        HE_Vertex v2 = h8.getVertex();
        HE_Vertex v3 = h6.getVertex();

        HE_Face f0 = h0.getFace();
        HE_Face f1 = h3.getFace();


        //重新设置翻转半边起点
        mesh.setVertex(h0, v2);
        mesh.setVertex(h3, v3);

        mesh.setVertex(h5,v2);
        mesh.setVertex(h4,v0);
        mesh.setVertex(h2,v3);
        mesh.setVertex(h1,v1);

        mesh.setVertex(h6,v3);
        mesh.setVertex(h9,v1);
        mesh.setVertex(h8,v2);
        mesh.setVertex(h7,v0);


        //重设半边连接顺序关系
        mesh.setNext(h0, h2);
        mesh.setNext(h2, h4);
        mesh.setNext(h4, h0);

        mesh.setNext(h1, h3);
        mesh.setNext(h3, h5);
        mesh.setNext(h5, h1);
        //重新设置半边之间pair关系
        mesh.setPair(h0, h3);
        mesh.setPair(h1, h6);
        mesh.setPair(h5, h9);
        mesh.setPair(h2, h7);
        mesh.setPair(h4, h8);
        //重设半边与面的关系
        mesh.setFace(h4, f0);
        mesh.setFace(h0, f0);
        mesh.setFace(h2, f0);

        mesh.setFace(h1, f1);
        mesh.setFace(h5, f1);
        mesh.setFace(h3, f1);
        //重设半边索引到的面
        mesh.setHalfedge(f0, h0);
        mesh.setHalfedge(f1, h3);


        mesh.setHalfedge(v0,h7);
        mesh.setHalfedge(v1,h9);
        mesh.setHalfedge(v2,h8);
        mesh.setHalfedge(v3,h6);
    }

    private void adaptiveSubdivision() {

        boolean done = false;
        while (done == false) {      //一次性细分到规定距离以内
            done = true;
            System.out.println("11111111111111111111111111111111111111");
            for (HE_Halfedge he : mesh.getHalfedges()) {
                double MySplitDistance = getSplitDistance(he);
//                double MySplitDistance = SplitDistance;

                if (mesh.getNumberOfVertices() < MaxVertexCount && he.getLength() > MySplitDistance) {
                    done = false;

                    if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
                        if (he.getLength() > he.getNextInFace().getLength()
                                && he.getLength() > he.getPrevInFace().getLength()
                                && he.getPair().getLength() > he.getPair().getNextInFace().getLength()
                                && he.getPair().getLength() > he.getPair().getPrevInFace().getLength()) {

                            HE_Face f1 = he.getFace();             //f1：半边的相邻面
                            HE_Face f2 = he.getPair().getFace();             //f2：相邻半边的相邻面

                            HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点
                            HE_Vertex v2_opp = he.getPair().getPrevInFace().getVertex();      // v2_opp：半边相邻三角面分裂边的另一个顶点

                            HE_MeshOp.splitEdge(mesh, he);        //长边从中点分割一半
                            HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1

                            HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                            HE_MeshOp.splitFace(mesh, f2, v1, v2_opp);
                        }

                    } else if (he.isInnerBoundary()) {
                        if (he.getLength() > he.getNextInFace().getLength() && he.getLength() > he.getPrevInFace().getLength()) {
                            HE_Face f1 = he.getFace();             //f1：半边的相邻面

                            HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点

                            HE_MeshOp.splitEdge(mesh, he);        //长边从中点分割一半
                            HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1

                            HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                        }
                        System.out.println("222222222222he=" + "");

                    } else if (he.isOuterBoundary()) {
                        System.out.println(he.getLength());
                        System.out.println(MySplitDistance);
                        System.out.println("1111");
                        System.out.println(done);
                        done = true;
                        System.out.println(done);
                        continue;


                    }
                }
            }

        }
    }


    public void getVertex5CP() {
        HE_Vertex he = mesh.getVertexWithIndex(35);
        mesh.add(new HE_Vertex(300, 300, 0));
        System.out.println(getClosetLengthToBoundary(mesh, he));
    }


    public double getClosetLengthToBoundary(HE_Mesh mesh, HE_Vertex v_start) {
        double MinDistance = Double.MAX_VALUE;
        for (HE_Vertex v : mesh.getAllBoundaryVertices()) {
            if (!v.equals(v_start)) {
                HE_Path path_temp = HE_Path.getShortestPath(v_start, v, mesh);
                double path_tempLength = path_temp.getPathLength();
                if (path_tempLength < MinDistance) {
                    MinDistance = path_tempLength;
                }
            }
        }

        return MinDistance;
    }


    public void EdgeLengthConstraintUsingKDTree() {
        for (HE_Halfedge he : mesh.getHalfedges()) {
            WB_Vector move = WB_Vector.sub(he.getVertex(), he.getEndVertex());

            if (move.getLength() > FinalLength || move.getLength() < FinalLength) {
                move = WB_Vector.mul(move, 0.5 * (move.getLength() - FinalLength) / move.getLength());    //如果两点距离小于碰撞距离则扩张到规定距离

                WB_Point v1 = he.getPosition();      //半边起点位
                WB_Point v2 = he.getEndPosition();      //半边起点位

                inRange = tree.getRange(v1, 1);
                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(v1)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.sub(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move, EdgeLengthConstraintWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + EdgeLengthConstraintWeight);
                    }
                }

                inRange = tree.getRange(v2, 1);
                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(v2)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.add(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move, EdgeLengthConstraintWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + EdgeLengthConstraintWeight);
                    }
                }

            } else {
                continue;
            }
        }
    }


    public void BendingResistanceUsingKDTree() {
        for (HE_Halfedge he : mesh.getHalfedges()) {
            if (!he.isOuterBoundary() && !he.isInnerBoundary()) {

                WB_Vector vI = he.getVertex().getPosition();
                WB_Vector vJ = he.getEndVertex().getPosition();
                WB_Vector vP = he.getPrevInFace().getVertex().getPosition();
                WB_Vector vQ = he.getPair().getPrevInFace().getVertex().getPosition();

                WB_Vector nP = (WB_Vector) he.getFace().getFaceNormal();
                WB_Vector nQ = (WB_Vector) he.getPair().getFace().getFaceNormal();

//                WB_Vector vIJ=WB_Vector.sub(vJ,vI);
//                WB_Vector vIP=WB_Vector.sub(vP,vI);
//                WB_Vector vIQ=WB_Vector.sub(vQ,vI);
//
//                WB_Vector nP = WB_Vector.cross(vIJ, vIP);
//                WB_Vector nQ = WB_Vector.sub(vIQ, vIJ);

                WB_Vector planNormal = WB_Vector.add(nP, nQ);
                WB_Vector planOrigin = vI.add(vJ).add(vP).add(vQ).mul(0.25f);

                WB_Plane plane = new WB_Plane(planOrigin, planNormal);

                WB_Vector vI2 = WB_GeometryOp.getClosestPoint3D(vI, plane);
                WB_Vector vJ2 = WB_GeometryOp.getClosestPoint3D(vJ, plane);
                WB_Vector vP2 = WB_GeometryOp.getClosestPoint3D(vP, plane);
                WB_Vector vQ2 = WB_GeometryOp.getClosestPoint3D(vQ, plane);

                WB_Vector move1 = WB_Vector.sub(vI2, vI);
                WB_Vector move2 = WB_Vector.sub(vJ2, vJ);
                WB_Vector move3 = WB_Vector.sub(vP2, vP);
                WB_Vector move4 = WB_Vector.sub(vQ2, vQ);

                inRange = tree.getRange(vI, 1);
                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(vI)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.add(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move1, BendingResistanceWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + BendingResistanceWeight);
                    }
                }

                inRange = tree.getRange(vJ, 1);
                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(vJ)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.add(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move2, BendingResistanceWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + BendingResistanceWeight);
                    }
                }

                inRange = tree.getRange(vP, 1);
                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(vP)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.add(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move3, BendingResistanceWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + BendingResistanceWeight);
                    }
                }

                inRange = tree.getRange(vQ, 1);
                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(vQ)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.add(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move4, BendingResistanceWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + BendingResistanceWeight);
                    }
                }
            }
        }
    }

    public void ControlEdgeAngleUsingKDTree() {
        for (HE_Halfedge he : mesh.getAllBoundaryHalfedges()) {
            HE_Halfedge henext = he.getNextInVertex();
            while (!henext.isInnerBoundary() && !henext.isOuterBoundary()) {
                henext = henext.getNextInVertex();
            }  //循环找出边缘共顶点的半边

            WB_Vector v1 = he.getPosition();  //半边顶点
            WB_Vector v2 = he.getEndPosition();  //半边终点

            WB_Vector v3 = henext.getEndPosition();  //边界相邻共点半边终点

            WB_Vector vP = WB_Vector.sub(v2, v1);
            WB_Vector vQ = WB_Vector.sub(v3, v1);
            double angle1 = WB_Vector.getAngle(vP, vQ);

            if (angle1 < PI) {
                WB_Vector move = WB_Vector.add(vP, vQ);
                move = WB_Vector.mul(move, 0.5 * (PI - angle1) / angle1);

                inRange = tree.getRange(v1, 1);

                for (WB_KDTreeInteger.WB_KDEntryInteger<WB_Point> ver : inRange) {
                    if (mesh.getPositionWithIndex(ver.value).equals(v1)) {
                        totalWeightedMoves.get(ver.value).set(WB_Vector.add(totalWeightedMoves.get(ver.value), (WB_Vector.mul(move, ControlEdgeAngleWeight))));
                        totalWeights.set(ver.value, totalWeights.get(ver.value) + ControlEdgeAngleWeight);
                    }
                }
            }
        }
    }




    public void updateVertexPosition() {
        for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
            if (totalWeights.get(i) == 0.0) continue;

            WB_Vector move = totalWeightedMoves.get(i).div(totalWeights.get(i));    //每次更新需要移动的向量大小
//            double g1 = GrowthIntensity.get(i);
//            float n1 = map((float) g1, 0, 1, 0.1f, 1);
//            WB_Vector.mul(move, n1);
            WB_Point newPosition = mesh.getPositionWithIndex(i).add(move);

            HE_Vertex v = mesh.getVertexWithIndex(i);   //更新最终的点位置
            v.set(newPosition);

        }
    }


}
