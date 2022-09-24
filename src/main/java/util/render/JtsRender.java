package util.render;


import org.locationtech.jts.geom.*;
import processing.core.PApplet;

public class JtsRender {
    public static GeometryFactory gf = new GeometryFactory();

    PApplet app;

    public JtsRender(PApplet app) {
        this.app = app;
    }

    public void draw(Geometry geo) {
        String type = geo.getGeometryType();
//        System.out.println("type  "+type);
        if (type == "Point") {
            drawPoint(geo);
        } else if (type == "LineString") {
            drawLineString(geo);
        } else if (type == "LinearRing") {
            drawLinearRing(geo);
        } else if (type == "Polygon") {
            drawPolygon(geo);
        } else {
            app.println("a new type... "+geo.getGeometryType());
        }

    }

    private void drawPoint(Geometry geo) {
        Point point = (Point) geo;
        app.ellipse((float) point.getX(), (float) point.getY(), 10, 10);
    }
    public void draw(LineString ls, int color){

        Coordinate[] vs = ls.getCoordinates();

        if(vs.length==2) {
            app.stroke(color);
            app.line((float)vs[0].x,(float)vs[0].y,(float)vs[1].x,(float)vs[1].y);
//            app.fill(0);
//            app.ellipse((float)vs[0].x,(float)vs[0].y,5,5);
        }else {
//            app.beginShape();
//            for (Coordinate v : vs) {
//                app.vertex((float) v.x, (float) v.y);
//            }
//            app.endShape(app.OPEN);
            for(int i=0;i<vs.length-1;i++) {
                app.stroke(color);
                app.line((float)vs[i].x, (float)vs[i].y, (float)vs[(i+1)%vs.length].x, (float)vs[(i+1)%vs.length].y);
//			_app.textSize(50);
//			_app.fill(0, 102, 153, 204);
//			_app.text(i, n1.xf(), n1.yf());
            }
        }

    }

    private void drawLineString(Geometry geo) {

        LineString ls = (LineString) geo;
        Coordinate[] vs = ls.getCoordinates();

        if(vs.length==2) {
            app.line((float)vs[0].x,(float)vs[0].y,(float)vs[1].x,(float)vs[1].y);
//            app.fill(0);
//            app.ellipse((float)vs[0].x,(float)vs[0].y,5,5);
        }else {
            this.app.beginShape();
            Coordinate[] var4 = vs;
            int var5 = vs.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Coordinate v = var4[var6];
                this.app.vertex((float)v.x, (float)v.y);
            }

            PApplet var10001 = this.app;
            this.app.endShape(1);

//            for(int i=0;i<vs.length-1;i++) {
//                app.stroke(255,0,     0);
//                app.line((float)vs[i].x, (float)vs[i].y, (float)vs[(i+1)%vs.length].x, (float)vs[(i+1)%vs.length].y);
////			_app.textSize(50);
////			_app.fill(0, 102, 153, 204);
////			_app.text(i, n1.xf(), n1.yf());
            }

    }

    private void drawLinearRing(Geometry geo) {
        LinearRing lr = (LinearRing) geo;
        Coordinate[] vs = lr.getCoordinates();
        app.beginShape();
        for (Coordinate v : vs) {
            app.vertex((float) v.x, (float) v.y);
        }
        app.endShape(app.OPEN);
    }

    private void drawPolygon(Geometry geo) {
        Polygon poly = (Polygon) geo;
        app.beginShape();
        app.noFill();
        LineString shell = poly.getExteriorRing();
        Coordinate[] coord_shell = shell.getCoordinates();
        for (Coordinate c_s : coord_shell) {
            app.vertex((float) c_s.x, (float) c_s.y);
        }

        int interNum = poly.getNumInteriorRing();

        for (int i = 0; i < interNum; i++) {
            LineString in_poly = poly.getInteriorRingN(i);
            Coordinate[] in_coord = in_poly.getCoordinates();
            app.beginContour();
            for (int j = 0; j < in_coord.length; j++) {
                app.vertex((float) in_coord[j].x, (float) in_coord[j].y);
            }
            app.endContour();
        }

        app.endShape();

        //test to draw  points of the shell
//        LineString test = poly.getExteriorRing();
//        Coordinate[] coord_test = test.getCoordinates();
//        for (int j = 0; j < coord_test.length; j++) {
//            app.pushStyle();
//            app.fill(0);
//            app.ellipse((float) coord_test[j].x, (float) coord_test[j].y,5,5);
//            app.popStyle();
//        }
    }
    public void drawPolygon_Color(Geometry geo,int color,int alpha) {
        Polygon poly = (Polygon) geo;
        app.beginShape();
        if (color!=255){
            app.fill(color,alpha);
        }else {
            app.fill(color);
        }
        LineString shell = poly.getExteriorRing();
        Coordinate[] coord_shell = shell.getCoordinates();
        for (Coordinate c_s : coord_shell) {
            app.vertex((float) c_s.x, (float) c_s.y);
        }

        int interNum = poly.getNumInteriorRing();

        for (int i = 0; i < interNum; i++) {
            LineString in_poly = poly.getInteriorRingN(i);
            Coordinate[] in_coord = in_poly.getCoordinates();
            app.beginContour();
            for (int j = 0; j < in_coord.length; j++) {
                app.vertex((float) in_coord[j].x, (float) in_coord[j].y);
            }
            app.endContour();
        }

        app.endShape();
    }

    /**
     * @author zqy
     * @description draw3D
     * @param: geo
     * @return
    */
    public void draw3D(Geometry geo) {
        String type = geo.getGeometryType();
//        System.out.println("type  "+type);
        if (type == "Point") {
            drawPoint3D(geo);
        } else if (type == "LineString") {
            drawLineString3D(geo);
        } else if (type == "LinearRing") {
            drawLinearRing(geo);
        } else if (type == "Polygon") {
            drawPolygon(geo);
        } else {
            app.println("a new type... "+geo.getGeometryType());
        }

    }
    public void drawPoint3D( Geometry geo){
            Point point = (Point) geo;
            app.pushStyle();
            app.pushMatrix();
            app.fill(255,0,0);
            app.translate((float) point.getX(),(float) point.getY(),(float) point.getCoordinate().z);
            app.sphere(3);
            app.popMatrix();
            app.popStyle();
    }
    private void drawLineString3D(Geometry geo) {

        LineString ls = (LineString) geo;
        Coordinate[] vs = ls.getCoordinates();

        if(vs.length==2) {
            app.line((float)vs[0].x,(float)vs[0].y,(float)vs[0].z,(float)vs[1].x,(float)vs[1].y,(float)vs[1].z);
//            app.fill(0);
//            app.ellipse((float)vs[0].x,(float)vs[0].y,5,5);
        }else {
            this.app.beginShape();
            Coordinate[] var4 = vs;
            int var5 = vs.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Coordinate v = var4[var6];
                this.app.vertex((float)v.x, (float)v.y,(float)v.z);
            }

            PApplet var10001 = this.app;
            this.app.endShape(1);

        }

    }

	/*
	public void drawDelaunayTriangle(ConformingDelaunayTriangulationBuilder delaunayBuilder) {
		Geometry triangles = delaunayBuilder.getTriangles(gf);
		int num = triangles.getNumGeometries();
		for (int i = 0; i < num; i++) {
			this.draw(triangles.getGeometryN(i));
		}
	}

	public void drawVoronoi(VoronoiDiagramBuilder voronoiBuilder) {
		Geometry voronois = voronoiBuilder.getDiagram(gf);
		int num = voronois.getNumGeometries();
		for (int i = 0; i < num; i++) {
			this.draw(voronois.getGeometryN(i));
		}
	}
	*/

    public PApplet getApp() {
        return this.app;
    }
}
