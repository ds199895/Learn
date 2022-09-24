package Remesh;

import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import processing.core.PApplet;
import processing.core.PConstants;
import wblut.geom.WB_Coord;
import wblut.geom.WB_GeometryFactory2D;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Import_dxf {
	static PApplet app;
	public Import_dxf(PApplet app) {
		this.app = app;
	}
	
	public List<List<DXFLine>> input_dxf_line(String path, List<List<DXFLine>> ls) {
		ls = new ArrayList<List<DXFLine>>();
			
		Parser parser = ParserBuilder.createDefaultParser();
		try {
			parser.parse(path, DXFParser.DEFAULT_ENCODING);
			DXFDocument doc = parser.getDocument();
			Iterator it = doc.getDXFLayerIterator();
			while (it.hasNext()) {
				DXFLayer layer = (DXFLayer) (it.next());
				
				if(layer.hasDXFEntities(DXFConstants.ENTITY_TYPE_LINE)) {
					ls.add(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE));
					PApplet.println(layer.getName(),layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE).size());
					System.out.println("ls add " + layer.getName());
				}
			}
			System.out.println("==================================");
			System.out.println("ls size: " + ls.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}
	public HE_Mesh getMesh(String path){
		HE_Mesh mesh;
		List<List<DXFPolyline>> polylines=input_dxf_polyline(path);
		List<WB_Polygon>polygons=new ArrayList<>();

		for(List<DXFPolyline>ps:polylines){
			for(DXFPolyline poly:ps){
				polygons.add(getPolygon(poly));
			}
		}
		HEC_FromPolygons hecp=new HEC_FromPolygons(polygons);
		mesh=new HE_Mesh(hecp);
		return mesh;
	}



	public List<List<DXFInsert>> input_dxf_block(String path, String blockName) {
		ArrayList<List<DXFInsert>> inserts = new ArrayList<List<DXFInsert>>();
		Parser parser = ParserBuilder.createDefaultParser();
		try {
			parser.parse(path, DXFParser.DEFAULT_ENCODING);
			DXFDocument doc = parser.getDocument();
			Iterator it = doc.getDXFLayerIterator();
			while(it.hasNext()) {
				DXFLayer dXFLayer = (DXFLayer) it.next();
				System.out.println("LayerName:  " + dXFLayer.getName());
				if (dXFLayer.isVisible()) {
					if (dXFLayer.hasDXFEntities(DXFConstants.ENTITY_TYPE_INSERT)) {
						inserts.add(dXFLayer.getDXFEntities((DXFConstants.ENTITY_TYPE_INSERT)));
						PApplet.println(dXFLayer.getName(), dXFLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_INSERT).size());
						System.out.println("inserts add " + dXFLayer.getName());
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return inserts;
	}

	public List<List<DXFPolyline>> input_dxf_polyline(String path) {
		ArrayList<List<DXFPolyline>>pls = new ArrayList<List<DXFPolyline>>();

		Parser parser = ParserBuilder.createDefaultParser();
		try {
			parser.parse(path, DXFParser.DEFAULT_ENCODING);
			DXFDocument doc = parser.getDocument();
			Iterator it = doc.getDXFLayerIterator();
			while (it.hasNext()) {
				DXFLayer layer = (DXFLayer) (it.next());
				if(layer.hasDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
					pls.add(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE));
					PApplet.println(layer.getName(),layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE).size());
					System.out.println("pls add " + layer.getName());
				}
			}
			System.out.println("==================================");
			System.out.println("pls size: " + pls.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return pls;
	}
	public void drawDxf_line(List<List<DXFLine>> ls) {
		app.pushStyle();
//		app.scale(1, -1, 1);
		app.colorMode(PConstants.HSB);
		
		for(List<DXFLine> l: ls) {
			app.stroke(30, 255, 255);
			app.noFill();
			for (DXFLine line : l) {
				org.kabeja.dxf.helpers.Point st = line.getStartPoint();
				org.kabeja.dxf.helpers.Point ed = line.getEndPoint();
				app.line(Import_dxf.x(st), -Import_dxf.y(st), Import_dxf.x(ed), -Import_dxf.y(ed));
			}
		}
		
		app.popStyle();
		
	}
	public void drawDxf_polyline(List<List<DXFPolyline>> pls) {
		app.colorMode(PConstants.HSB);
		
		for(List<DXFPolyline> l: pls) {
			app.pushStyle();
			app.stroke(200, 255, 255);
			app.noFill();
			for (DXFPolyline pline : l) {
				app.beginShape();
				for (int i = 0; i < pline.getVertexCount(); i++) {
					DXFVertex v = pline.getVertex(i);
					app.vertex(Import_dxf.x(v), -Import_dxf.y(v));
				}
				app.endShape(PConstants.CLOSE);
			}
			app.popStyle();
		}
	}

	public List<List<WB_Point>> get_block_base_point(List<List<DXFInsert>> inserts) {
		List<List<WB_Point>>pts=new ArrayList<>();
//		app.colorMode(PConstants.HSB);

		for(List<DXFInsert> ins: inserts) {
//			app.pushStyle();
//			app.stroke(200, 255, 255);
//			app.noFill();
			List<WB_Point>ps=new ArrayList<>();
			for (DXFInsert in : ins) {
				Point p=in.getPoint();
				WB_Point wbp=new WB_Point(-p.getX(),-p.getY(),p.getZ());
//				app.pushMatrix();
//				app.translate((float) p.getX(),(float)p.getY(),(float)p.getZ());
//				app.sphere(5);
//
//				app.popMatrix();
				ps.add(wbp);
			}
			pts.add(ps);
//			app.popStyle();
		}

		return pts;
	}

	public static WB_Polygon getPolygon(DXFPolyline pline) {
		WB_GeometryFactory2D wbgf=new WB_GeometryFactory2D();
		List<WB_Coord>wc=new ArrayList<WB_Coord>();
		for (int i = 0; i < pline.getVertexCount(); i++) {
			DXFVertex v = pline.getVertex(i);
			WB_Coord coord=new WB_Point(x(v),-y(v));
			wc.add(coord);
		}
		WB_Polygon poly=wbgf.createSimplePolygon(wc);
		
		return poly;
	}

	public static float x(DXFVertex v) {
		return Math.round(v.getX());
	}
	public static float y(DXFVertex v) {
		return Math.round(v.getY());
	}
	
	public static float x(org.kabeja.dxf.helpers.Point p) {
		return Math.round(p.getX());
	}
	public static float y(org.kabeja.dxf.helpers.Point p) {
		return Math.round(p.getY());
	}

}
