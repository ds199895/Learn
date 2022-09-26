package Remesh;

import com.sun.org.apache.regexp.internal.RE;
import javafx.scene.shape.Mesh;
import org.eclipse.collections.impl.list.mutable.FastList;
import processing.core.PApplet;
import util.geometry.GeomFactory;
import util.render.HE_Render;
import wblut.geom.*;
import wblut.hemesh.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.zip.CheckedOutputStream;

public class Util {
    PApplet app;
    public Util(PApplet app) {
        this.app = app;
    }

    public void displayHalfEdge(HE_Mesh mesh, double offsetDis) {
        app.pushStyle();
        Color color = new Color(95, 178, 199);
        app.stroke(color.getRGB());
        app.strokeWeight(2);
        for (HE_Halfedge he : mesh.getHalfedges()) {

            app.stroke(color.getRGB());
            app.strokeWeight(2);
            HE_Face referFace = he.isOuterBoundary() ? he.getPair().getFace() : he.getFace();

            WB_Vector vec = new WB_Vector(he.getVertex(), he.getEndVertex());
            WB_Vector v = (WB_Vector) he.getHalfedgeDirection();
            WB_Vector v_ortho = v.rotateAboutAxis(90 * app.DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(offsetDis);
            WB_Point ps = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
            WB_Point pe = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
            WB_Vector v_arrow = v.rotateAboutAxis(150 * app.DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(he.getLength() / 20);

            WB_Point p_arrow = pe.add(v_arrow);
            app.line(ps.xf(), ps.yf(), ps.zf(), pe.xf(), pe.yf(), pe.zf());
            app.line(pe.xf(), pe.yf(), pe.zf(), p_arrow.xf(), p_arrow.yf(), p_arrow.zf());
        }

        app.popStyle();
    }

    public void displaySingleHalfEdge(HE_Halfedge he, double offsetDis, Color color) {
        app.pushStyle();
        ;
        app.stroke(color.getRGB());
        app.strokeWeight(2);
        HE_Face referFace = he.isOuterBoundary() ? he.getPair().getFace() : he.getFace();

        WB_Vector vec = new WB_Vector(he.getVertex(), he.getEndVertex());
        WB_Vector v = (WB_Vector) he.getHalfedgeDirection();
        WB_Vector v_ortho = v.rotateAboutAxis(90 * app.DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(offsetDis);
        WB_Point ps = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.25));
        WB_Point pe = he.getVertex().getPosition().add(v_ortho).add(vec.mul(0.75));
        WB_Vector v_arrow = v.rotateAboutAxis(150 * app.DEG_TO_RAD, new WB_Point(0, 0, 0), referFace.getFaceNormal()).mul(he.getLength() / 20);

        WB_Point p_arrow = pe.add(v_arrow);
        app.line(ps.xf(), ps.yf(), ps.zf(), pe.xf(), pe.yf(), pe.zf());
        app.line(pe.xf(), pe.yf(), pe.zf(), p_arrow.xf(), p_arrow.yf(), p_arrow.zf());
        app.popStyle();
    }

    public static void applyFlip(HE_Mesh mesh) {
        int devpre = 0;
        int devpost = 0;

        for (int r = 0; r < 2; ++r) {
            List<HE_Halfedge> edges = new FastList();
            edges.addAll(mesh.getEdges());
            Collections.shuffle(edges);
            Iterator var11 = edges.iterator();

            while (var11.hasNext()) {
                HE_Halfedge e = (HE_Halfedge) var11.next();
                if (!e.isInnerBoundary()) {
                    HE_Vertex a = e.getVertex();
                    HE_Vertex b = e.getEndVertex();
                    HE_Vertex c = e.getNextInFace().getEndVertex();
                    HE_Vertex d = e.getPair().getNextInFace().getEndVertex();
                    devpre = Math.abs((a.isBoundary() ? 4 : 6) - a.getVertexDegree());
                    devpre += Math.abs((b.isBoundary() ? 4 : 6) - b.getVertexDegree());
                    devpre += Math.abs((c.isBoundary() ? 4 : 6) - c.getVertexDegree());
                    devpre += Math.abs((d.isBoundary() ? 4 : 6) - d.getVertexDegree());
                    if (devpre > 0) {
                        flipEdge(mesh, e);
                        devpost = Math.abs((a.isBoundary() ? 4 : 6) - a.getVertexDegree());
                        devpost += Math.abs((b.isBoundary() ? 4 : 6) - b.getVertexDegree());
                        devpost += Math.abs((b.isBoundary() ? 4 : 6) - c.getVertexDegree());
                        devpost += Math.abs((b.isBoundary() ? 4 : 6) - d.getVertexDegree());
                        if (devpre <= devpost) {
                            flipEdge(mesh, e);
                        }
                    }
                }
            }
        }
    }

    public static void controlLength(HE_Mesh mesh, double gap) {
        boolean done = false;
        while (!done) {
            done = true;
            for (HE_Halfedge he : mesh.getHalfedges()) {
                double length = he.getLength();
                if (length > 4 / 3. * gap) {
                    // edge 长度大于 gap 则在该 edge 中间加入新 HE_Vertex
                    done = false;
                    HE_MeshOp.splitEdge(mesh, he);
                    break;
                }
//                if (length <0.2*gap) { // gap 需要动态确定*******
//                    // edge 长度大于 小于 gap 的 1/10 则端点合并 (删除该边)
//                    done = false;
//                    HE_MeshOp.collapseEdge(mesh, he);
//                }
            }
        }
    }

    public List<WB_Polygon> differentialOffset(HE_Mesh mesh, HE_Render render) {
        List<WB_Polygon> newPolys = new ArrayList<>();
        List<WB_Segment> segmentsAll = new ArrayList<>();
        for (HE_Face f : mesh.getFaces()) {
            List<WB_Segment> segments = new ArrayList<>();
            for (int i = 0; i < f.getFaceHalfedges().size(); i++) {
                HE_Halfedge he = f.getFaceHalfedges().get(i);
                double offsetDis = he.getLength() / 20.;

                WB_Vector vec = new WB_Vector(he.getVertex(), he.getEndVertex());
                WB_Vector v = (WB_Vector) he.getHalfedgeDirection();
                WB_Vector v_ortho = v.rotateAboutAxis(90 * Math.PI / 180, new WB_Point(0, 0, 0), f.getFaceNormal()).mul(offsetDis);
                WB_Point ps = he.getVertex().getPosition().add(v_ortho).add(vec.mul(-0.5f));
                WB_Point pe = he.getVertex().getPosition().add(v_ortho).add(vec.mul(1.5f));

                WB_Segment segment = new WB_Segment(ps, pe);
                segments.add(segment);
            }
            segmentsAll.addAll(segments);
            List<WB_Point> offsetPts = new ArrayList<>();
            List<WB_Point> outerPoints = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                List<WB_Point> pts = new ArrayList<>();
                outerPoints.add(f.getFaceHalfedges().get(i).getVertex().getPosition());
                int pre = (i - 1 + segments.size()) % segments.size();
                int nxt = (i + 1) % segments.size();
//                System.out.println(WB_GeometryOp.getIntersection3D(segments.get(i), segments.get(nxt)).object);
                WB_Point ps = (WB_Point) WB_GeometryOp.getIntersection3D(segments.get(i), segments.get(pre)).object;
                WB_Point pe = (WB_Point) WB_GeometryOp.getIntersection3D(segments.get(i), segments.get(nxt)).object;
                pts.add(f.getFaceHalfedges().get(i).getVertex().getPosition());
                pts.add(f.getFaceHalfedges().get(i).getEndVertex().getPosition());
                pts.add(pe);
                pts.add(ps);
//                render.drawSegment((WB_Segment) WB_GeometryOp.getIntersection3D(segments.get(i), segments.get(nxt)).object);
//                app.ellipse(WB_GeometryOp.getIntersection3D(segments.get(i), segments.get(nxt)).object.get);
                WB_Polygon polygon = WB_GeometryFactory.instance().createSimplePolygon(pts);
                newPolys.add(polygon);
            }
//            WB_Polygon polygonWithHole=WB_GeometryFactory.instance().createPolygonWithHole(outerPoints,offsetPts);
//            WB_Polygon polygonWithHole=WB_GeometryFactory.instance().createSimplePolygon(offsetPts);


        }
        HEC_FromPolygons hecp = new HEC_FromPolygons(newPolys);
        mesh.set(new HE_Mesh(hecp));
        return newPolys;
    }

    //negative
    public WB_Point findOffsetPoint(HE_Halfedge he) {
        HE_Halfedge pre = he.getPrevInFace();
        double d1 = he.getLength() / 5.;
        double d2 = pre.getLength() / 5.;
        double alpha = HE_MeshOp.getAngle(he);
        double x1 = (d2 + d1 * Math.cos(alpha)) / Math.sin(alpha);
        double x2 = (d1 + d2 * Math.cos(alpha)) / Math.sin(alpha);

        double length = Math.sqrt(x1 * x1 + d1 * d1);

        double alpha1 = Math.acos(x1 / length);

        WB_Vector vec = (WB_Vector) he.getHalfedgeDirection();
        WB_Vector v_ortho = vec.rotateAboutAxis(alpha1, new WB_Point(0, 0, 0), he.getFace().getFaceNormal()).mul(length);
        WB_Point offPt = he.getVertex().getPosition().add(v_ortho);
        return offPt;
    }

    public WB_Point findOffsetPoint(HE_Vertex v, double d1, double d2, double alpha, WB_Vector vec, WB_Vector faceNormal) {
        double x1 = (d2 + d1 * Math.cos(alpha)) / Math.sin(alpha);
        double x2 = (d1 + d2 * Math.cos(alpha)) / Math.sin(alpha);

        double length = Math.sqrt(x1 * x1 + d1 * d1);

        double alpha1 = Math.acos(x1 / length);

        WB_Vector v_ortho = vec.rotateAboutAxis(alpha1, new WB_Point(0, 0, 0), faceNormal).mul(length);
        WB_Point offPt = v.getPosition().add(v_ortho);
        return offPt;
    }

    public double calculateThreshold(HE_Halfedge he) {
        HE_Halfedge pre = he.getPrevInFace();
        HE_Halfedge nxt = he.getNextInFace();

//                pts.add(new WB_Point(he.getVertex()));
//                pts.add(new WB_Point(nxt.getVertex()));
        double d1 = he.getLength() / 5.;
        double d2 = pre.getLength() / 5.;
        double alpha = HE_MeshOp.getAngle(he);

        double d1_n = nxt.getLength() / 5.;
        double d2_n = he.getLength() / 5.;
        double alpha_n = HE_MeshOp.getAngle(nxt);

        double threshold = (he.getLength() * Math.sin(alpha) * Math.sin(alpha_n) - d2 * Math.sin(alpha_n) - d1_n * Math.sin(alpha)) / Math.sin(alpha + alpha_n);

        return threshold;

    }

    public boolean[] checkThreshold(HE_Mesh mesh, HashMap<Long, Double> distance) {
        boolean vertexValid = true;
        boolean globalValid = true;
        HE_Face f = mesh.getFaceWithIndex(0);
        System.out.println("face vertices num: " + f.getFaceVertices().size());
        List<WB_Point> newPolyPts = new ArrayList<>();

        int count = 0;
        for (int i = 0; i < f.getFaceVertices().size(); i++) {
            newPolyPts.add(f.getFaceVertices().get(i).getPosition());
        }
        for (int i = 0; i < f.getFaceVertices().size(); i++) {
            HE_Vertex cen = f.getFaceVertices().get(i);

            HE_Halfedge he = cen.getHalfedge(f);
            HE_Halfedge pre = he.getPrevInFace();
            HE_Halfedge nxt = he.getNextInFace();

            double d1 = distance.get(cen.getKey());
            double d2 = distance.get(pre.getVertex().getKey());
            double alpha = HE_MeshOp.getAngle(he);

            double d1_n = distance.get(nxt.getVertex().getKey());
            double alpha_n = HE_MeshOp.getAngle(nxt);

            double threshold = (he.getLength() * Math.sin(alpha) * Math.sin(alpha_n) - d2 * Math.sin(alpha_n) - d1_n * Math.sin(alpha)) / Math.sin(alpha + alpha_n);

            System.out.println("Index:  " + i + "  d1:  " + d1);
            System.out.println("Index:  " + i + "  threshold:  " + threshold);

            if (alpha < Math.PI / 2 && alpha_n < Math.PI / 2) {
                if (d1 > threshold) {
                    System.out.println("failed!******");
                    globalValid = false;
                }

            } else{
                if (f.getFaceVertices().size() > 3) {
                    double d1_r = d1_n;
                    double d2_r = d2;

                    double alpha_r = alpha + alpha_n - Math.PI;
                    double x1_r = (d2_r + d1_r * Math.cos(alpha_r)) / Math.sin(alpha_r);
                    double x2_r = (d1_r + d2_r * Math.cos(alpha_r)) / Math.sin(alpha_r);

                    double L_1 = Math.sin(Math.PI - alpha) * he.getLength() / Math.sin(alpha_r);
                    System.out.println("L_1:  " + L_1);
                    if (L_1 > 0) {
                        if (d1 < threshold) {
                            System.out.println("less!");
                            count++;
                            System.out.println("not OK!");
                            System.out.println("find new Vertex!");
                            WB_Vector vec_n = (WB_Vector) nxt.getHalfedgeDirection();
                            WB_Vector vectorOpp = vec_n.mul(-1).mul(L_1);
                            WB_Point newVer = nxt.getVertex().getPosition().add(vectorOpp);
                            nxt.getVertex().set(newVer);
                            collapseEdgeBoundary(mesh, he, true);
                            System.out.println("new Vertex:  " + newVer);
                        }

                    }else{
                        if (d1 > threshold) {
                            System.out.println("failed!******");
                            globalValid = false;
                        }
                    }
                }

            }
        }

        if (count == 0) {
            vertexValid = true;
            System.out.println("vertexValid:  " + vertexValid);
        } else {
            System.out.println("vertexValid:  " + vertexValid);
            System.out.println("count:  " + count);
        }
        boolean[] valid = new boolean[2];
        valid[0] = vertexValid;
        valid[1] = globalValid;
        System.out.println("globalValid:  " + globalValid);

        return valid;
    }

    public static boolean collapseEdgeBoundary(HE_Mesh mesh, HE_Halfedge e, boolean strict) {
        if (mesh.contains(e)) {
            HE_Halfedge he = e.isEdge() ? e : e.getPair();
            HE_Halfedge hePair = he.getPair();
            HE_Face f = he.getFace();
            HE_Face fp = hePair.getFace();
            HE_Vertex v = he.getVertex();
            HE_Vertex vp = hePair.getVertex();

            List<HE_Halfedge> tmp = v.getHalfedgeStar();

            for (int i = 0; i < tmp.size(); ++i) {
                mesh.setVertex((HE_Halfedge) tmp.get(i), vp);
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


    public List<WB_Polygon> diffOffset(HE_Mesh mesh, HE_Render render) {
        List<WB_Polygon> newPolys = new ArrayList<>();

        for (HE_Face f : mesh.getFaces()) {
            boolean vertexValid = false;
            boolean globalValid = true;
            List<WB_Point> pts = new ArrayList<>();

            List<WB_Polygon> poly_temp = new ArrayList<>();
            poly_temp.add(f.getPolygon());
            HEC_FromPolygons hecp_temp = new HEC_FromPolygons(poly_temp);
            HE_Mesh mesh_temp = new HE_Mesh(hecp_temp);
            HashMap<Long, Double> distance = new HashMap<>();
            WB_Point face_cen=f.getPolygon().getCenter();

            for (int i = 0; i < mesh_temp.getVertices().size(); i++) {
                HE_Vertex ver=mesh_temp.getVertexWithIndex(i);
                HE_Halfedge halfedge=ver.getHalfedge();
//                double dis=halfedge.getLength() /5.;
                double dis=Math.min(halfedge.getLength() /5.,0.8*WB_GeometryOp.getDistance3D(face_cen,new WB_Segment(halfedge.getVertex().getPosition(),halfedge.getEndVertex().getPosition())));
                System.out.println("distance:********"+dis);
                distance.put(ver.getKey(), dis);
            }

            while (!vertexValid && globalValid) {
//            for(int i=0;i<1;i++){
                System.out.println("init global valid:  " + globalValid);
                boolean[] bs = checkThreshold(mesh_temp, distance);
                vertexValid = bs[0];
                globalValid = bs[1];
            }
            System.out.println("global valid:  " + globalValid);
//            newPolys.add(mesh_temp.getPolygonList().get(0));

            for (HE_Face face : mesh_temp.getFaces()) {
                for (int i = 0; i < face.getFaceVertices().size(); i++) {
                    HE_Vertex cen = face.getFaceVertices().get(i);

                    HE_Halfedge he = cen.getHalfedge(face);
                    HE_Halfedge pre = he.getPrevInFace();

                    double d1 = distance.get(cen.getKey());
                    double d2 = distance.get(pre.getVertex().getKey());
                    double alpha = HE_MeshOp.getAngle(he);

                    WB_Point offPt = findOffsetPoint(he.getVertex(), d1, d2, alpha, (WB_Vector) he.getHalfedgeDirection(), (WB_Vector) f.getFaceNormal());
                    pts.add(offPt);
                }
            }

            if(globalValid){
//                for(List<WB_Point>pts:allPts){
                    WB_Polygon poly = WB_GeometryFactory.instance().createSimplePolygon(pts);
                    newPolys.add(poly);
//                 }
            }
        }
//        HEC_FromPolygons hecp=new HEC_FromPolygons(newPolys);
//        mesh.set(new HE_Mesh(hecp));

        return newPolys;
    }

    public static List<Integer> getSplitDistance(HE_Mesh mesh) {
        List<Integer> targets = new ArrayList<>();
        int minLength = Integer.MAX_VALUE;
        int maxLength = 0;
        for (HE_Halfedge e : mesh.getHalfedges()) {
            if (e.getLength() > maxLength) {
                maxLength = (int) e.getLength();

            }
            if (e.getLength() < minLength) {
                minLength = (int) e.getLength();
            }
        }
        int target_Length = 0;
        for (int i = 0; i < mesh.getHalfedges().size(); i++) {
            for (int j = 1000; j < maxLength; j += 500) {
                if ((mesh.getHalfedges().get(i).getLength() - j) >= 0 && (mesh.getHalfedges().get(i).getLength() - j) < 500) {
                    targets.add(j);
                }
            }
        }

        return targets;
    }

    public static void SplitAllLongEdges(HE_Mesh mesh, double target) {
        double low = 4 / 5. * target;
        double high = 4 / 3. * target;
        boolean done = false;
        while (!done) {
            done = true;
            for (HE_Halfedge he : mesh.getHalfedges()) {
                if (he.getLength() > high) {
                    if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
                        if (he.getLength() > he.getNextInFace().getLength()
                                && he.getLength() > he.getPrevInFace().getLength()
                                && he.getPair().getLength() > he.getPair().getNextInFace().getLength()
                                && he.getPair().getLength() > he.getPair().getPrevInFace().getLength()) {

                            HE_Face f1 = he.getFace();             //f1：半边的相邻面
                            HE_Face f2 = he.getPair().getFace();             //f2：相邻半边的相邻面

                            HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点
                            HE_Vertex v2_opp = he.getPair().getPrevInFace().getVertex();      // v2_opp：半边相邻三角面分裂边的另一个顶点
                            WB_Vector dir = (WB_Vector) he.getHalfedgeDirection();
                            HE_Vertex lerp_distance = new HE_Vertex(he.getStartPosition().add(dir.mul(target)));
                            HE_MeshOp.splitEdge(mesh, he, lerp_distance);        //长边中分割出目标长度
                            HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1

                            HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                            HE_MeshOp.splitFace(mesh, f2, v1, v2_opp);
                        }
                    } else if (he.isInnerBoundary()) {
                        if (he.getLength() > he.getNextInFace().getLength() && he.getLength() > he.getPrevInFace().getLength()) {
                            HE_Face f1 = he.getFace();             //f1：半边的相邻面

                            HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点

                            WB_Vector dir = (WB_Vector) he.getHalfedgeDirection();
                            HE_Vertex lerp_distance = new HE_Vertex(he.getStartPosition().add(dir.mul(target)));
                            HE_MeshOp.splitEdge(mesh, he, lerp_distance);        //长边中分割出目标长度

                            HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1
                            HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                        }
                    } else if (he.isOuterBoundary()) {
                        continue;
                    }

                }
            }

            for (HE_Halfedge he : mesh.getHalfedges()) {
                if (he.getLength() > high) {
                    done = false;
                }
            }
        }
    }

    public static void SplitAllEdges(HE_Mesh mesh) {
        List<Integer> targets = getSplitDistance(mesh);

//        boolean done=false;
//
//        while (!done) {
//            done=true;
        for (HE_Halfedge he : mesh.getHalfedges()) {
            int times = (int) (he.getLength() / 500);
            int target = (times - 1) * 500;
            if (he.getLength() > target) {
                if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
                    if (he.getLength() > he.getNextInFace().getLength()
                            && he.getLength() > he.getPrevInFace().getLength()
                            && he.getPair().getLength() > he.getPair().getNextInFace().getLength()
                            && he.getPair().getLength() > he.getPair().getPrevInFace().getLength()) {

                        HE_Face f1 = he.getFace();             //f1：半边的相邻面
                        HE_Face f2 = he.getPair().getFace();             //f2：相邻半边的相邻面

                        HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点
                        HE_Vertex v2_opp = he.getPair().getPrevInFace().getVertex();      // v2_opp：半边相邻三角面分裂边的另一个顶点
                        WB_Vector dir = (WB_Vector) he.getHalfedgeDirection();
                        HE_Vertex lerp_distance = new HE_Vertex(he.getStartPosition().add(dir.mul(target)));
                        HE_MeshOp.splitEdge(mesh, he, lerp_distance);        //长边中分割出目标长度
                        HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1

                        HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                        HE_MeshOp.splitFace(mesh, f2, v1, v2_opp);
                    }
                } else if (he.isInnerBoundary()) {
                    if (he.getLength() > he.getNextInFace().getLength() && he.getLength() > he.getPrevInFace().getLength()) {
                        HE_Face f1 = he.getFace();             //f1：半边的相邻面

                        HE_Vertex v1_opp = he.getPrevInFace().getVertex();              // v1_opp：半边相邻三角面分裂边的另一个顶点

                        WB_Vector dir = (WB_Vector) he.getHalfedgeDirection();
                        HE_Vertex lerp_distance = new HE_Vertex(he.getStartPosition().add(dir.mul(target)));
                        HE_MeshOp.splitEdge(mesh, he, lerp_distance);        //长边中分割出目标长度

                        HE_Vertex v1 = he.getEndVertex();           //新的端点（半边的中点）位置更新,注意不能写成简单 HE_Vertex v1=new HE_Vertex(mid);得把网格中的vertex赋予v1
                        HE_MeshOp.splitFace(mesh, f1, v1, v1_opp);
                    }
                } else if (he.isOuterBoundary()) {
                    continue;
                }

            }
        }
//            for(int i=0;i<mesh.getHalfedges().size();i++){
//                if(mesh.getHalfedges().get(i).getLength()>targets.get(i)){
//                    done=false;
//                }
//            }
    }

    //    }
//    public static void flipEdgeControl(HE_Mesh mesh,int Max) {
////        for(HE_Vertex v:mesh.getVertices()){
////            if(!v.isBoundary()) {
////                List<HE_Halfedge> edges = v.getHalfedgeStar();
////                double MaxAngle = 0;
////                HE_Halfedge edge_to_flip = null;
////                for (HE_Halfedge he : edges) {
////                    if (MaxAngle < getHalfEdgeAngle(he)) {
////                        MaxAngle = getHalfEdgeAngle(he);
////                        edge_to_flip = he;
////                    }
////                }
////                HE_Vertex op_ver1 = edge_to_flip.getNextInFace().getEndVertex();
////                HE_Vertex op_ver2 = edge_to_flip.getPair().getNextInFace().getEndVertex();
////                if (op_ver1.getHalfedgeStar().size() < 5 && op_ver2.getHalfedgeStar().size() < 5) {
////                    flipEdge(mesh, edge_to_flip);
////                }
////            }
////        }
////        for(HE_Vertex v:mesh.getVertices()){
////            if(v.getHalfedgeStar().size()>5) {
////                if (!v.isBoundary()) {
//////                System.out.println(v);
////                    List<HE_Halfedge> edges = v.getHalfedgeStar();
//////                    double MaxAngle = 0;
//////                    HE_Halfedge edge_to_flip = null;
//////                    for (HE_Halfedge he : edges) {
//////                        if (MaxAngle < getHalfEdgeAngle(he)) {
//////                            MaxAngle = getHalfEdgeAngle(he);
//////                            edge_to_flip = he;
//////                        }
//////                    }
////                    for(HE_Halfedge he:edges) {
////                        HE_Vertex op_ver1 = he.getNextInFace().getEndVertex();
////                        HE_Vertex op_ver2 = he.getPair().getNextInFace().getEndVertex();
////                        double angle1 = getHalfEdgeAngle(he);    //半边对应角度
////                        double angle2 = getHalfEdgeAngle(he.getPair());     //半边对边对应角度
////                        double angle3 = getHalfEdgeAngle(he.getPair().getNextInFace());
////                        double angle4 = getHalfEdgeAngle(he.getPair().getNextInFace().getNextInFace());
////                        if(angle1>0.5*Math.PI&&angle2>0.5*Math.PI) {
////                            if (op_ver1.getHalfedgeStar().size() < Max - 1 && op_ver2.getHalfedgeStar().size() < Max - 1) {
////                                flipEdge(mesh, he);
////                            }
////                        }
////                    }
////                }
////            }
////        }
//
//        for (HE_Halfedge he : mesh.getHalfedges()) {
//            double angle1 = getHalfEdgeAngle(he);    //半边对应角度
//            double angle2 = getHalfEdgeAngle(he.getPair());     //半边对边对应角度
//            double angle3 = getHalfEdgeAngle(he.getPair().getNextInFace());
//            double angle4 = getHalfEdgeAngle(he.getPair().getNextInFace().getNextInFace());
//            if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
//                if (angle1>threshold&&angle2>threshold){
//                    flipEdge(mesh,he);
//                }else if(angle1>threshold && angle2<threshold && angle3<threshold&& angle4<threshold){
//                    flipEdge(mesh,he);
//                }
//            }
//        }
//    }
    public static void flipEdgeControl(HE_Mesh mesh, double threshold) {
//        for(HE_Vertex v:mesh.getVertices()){
//            if(!v.isBoundary()) {
//                List<HE_Halfedge> edges = v.getHalfedgeStar();
//                double MaxAngle = 0;
//                HE_Halfedge edge_to_flip = null;
//                for (HE_Halfedge he : edges) {
//                    if (MaxAngle < getHalfEdgeAngle(he)) {
//                        MaxAngle = getHalfEdgeAngle(he);
//                        edge_to_flip = he;
//                    }
//                }
//                HE_Vertex op_ver1 = edge_to_flip.getNextInFace().getEndVertex();
//                HE_Vertex op_ver2 = edge_to_flip.getPair().getNextInFace().getEndVertex();
//                if (op_ver1.getHalfedgeStar().size() < 5 && op_ver2.getHalfedgeStar().size() < 5) {
//                    flipEdge(mesh, edge_to_flip);
//                }
//            }
//        }
//        for(HE_Vertex v:mesh.getVertices()){
//            if(v.getHalfedgeStar().size()>5) {
//                if (!v.isBoundary()) {
////                System.out.println(v);
//                    List<HE_Halfedge> edges = v.getHalfedgeStar();
////                    double MaxAngle = 0;
////                    HE_Halfedge edge_to_flip = null;
////                    for (HE_Halfedge he : edges) {
////                        if (MaxAngle < getHalfEdgeAngle(he)) {
////                            MaxAngle = getHalfEdgeAngle(he);
////                            edge_to_flip = he;
////                        }
////                    }
//                    for(HE_Halfedge he:edges) {
//                        HE_Vertex op_ver1 = he.getNextInFace().getEndVertex();
//                        HE_Vertex op_ver2 = he.getPair().getNextInFace().getEndVertex();
//                        double angle1 = getHalfEdgeAngle(he);    //半边对应角度
//                        double angle2 = getHalfEdgeAngle(he.getPair());     //半边对边对应角度
//                        double angle3 = getHalfEdgeAngle(he.getPair().getNextInFace());
//                        double angle4 = getHalfEdgeAngle(he.getPair().getNextInFace().getNextInFace());
//                        if(angle1>0.5*Math.PI&&angle2>0.5*Math.PI) {
//                            if (op_ver1.getHalfedgeStar().size() < Max - 1 && op_ver2.getHalfedgeStar().size() < Max - 1) {
//                                flipEdge(mesh, he);
//                            }
//                        }
//                    }
//                }
//            }
//        }

        for (HE_Halfedge he : mesh.getHalfedges()) {
            double angle1 = getHalfEdgeAngle(he);    //半边对应角度
            double angle2 = getHalfEdgeAngle(he.getPair());     //半边对边对应角度
            double angle3 = getHalfEdgeAngle(he.getPair().getNextInFace());
            double angle4 = getHalfEdgeAngle(he.getPair().getNextInFace().getNextInFace());
            HE_Vertex op_ver1 = he.getNextInFace().getEndVertex();
            HE_Vertex op_ver2 = he.getPair().getNextInFace().getEndVertex();
            if (op_ver1.getVertexDegree() < 7 && op_ver2.getVertexDegree() < 7) {
                if (!he.isOuterBoundary() && !he.isInnerBoundary()) {      //判断半边是否是边界
                    if (angle1 > threshold && angle2 > threshold) {
                        flipEdge(mesh, he);
                    } else if (angle1 > threshold && angle2 < threshold && angle3 < threshold && angle4 < threshold) {
                        flipEdge(mesh, he);
                    }
                }
            }
        }
    }

    public static double getHalfEdgeAngle(HE_Halfedge _he) {
        WB_Vector v1 = _he.getNextInFace().getPosition();  //下一条半边顶点
        WB_Vector v2 = _he.getNextInFace().getEndPosition();  //下一条半边终点

        WB_Vector v3 = _he.getNextInFace().getNextInFace().getPosition();  //下下一条半边顶点
        WB_Vector v4 = _he.getNextInFace().getNextInFace().getEndPosition();  //下下一条半边终点

        WB_Vector vP = WB_Vector.sub(v1, v2);
        WB_Vector vQ = WB_Vector.sub(v4, v3);
        double angle = WB_Vector.getAngle(vP, vQ);    //半边对应角度

        return angle;
    }

    public static void flipEdge(HE_Mesh mesh, HE_Halfedge h0) {
        HE_Halfedge h1 = h0.getNextInFace();
        HE_Halfedge h2 = h1.getNextInFace();
        HE_Halfedge h3 = h0.getPair();
        HE_Halfedge h4 = h3.getNextInFace();
        HE_Halfedge h5 = h4.getNextInFace();
        HE_Halfedge h6 = h1.getPair();
        HE_Halfedge h7 = h2.getPair();
        HE_Halfedge h8 = h4.getPair();
        HE_Halfedge h9 = h5.getPair();

        HE_Vertex v0 = h0.getVertex();
        HE_Vertex v1 = h3.getVertex();
        HE_Vertex v2 = h8.getVertex();
        HE_Vertex v3 = h6.getVertex();

        HE_Face f0 = h0.getFace();
        HE_Face f1 = h3.getFace();


        //重新设置翻转半边起点
        mesh.setVertex(h0, v2);
        mesh.setVertex(h3, v3);

        mesh.setVertex(h5, v2);
        mesh.setVertex(h4, v0);
        mesh.setVertex(h2, v3);
        mesh.setVertex(h1, v1);

        mesh.setVertex(h6, v3);
        mesh.setVertex(h9, v1);
        mesh.setVertex(h8, v2);
        mesh.setVertex(h7, v0);


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


        mesh.setHalfedge(v0, h7);
        mesh.setHalfedge(v1, h9);
        mesh.setHalfedge(v2, h8);
        mesh.setHalfedge(v3, h6);
    }
}
