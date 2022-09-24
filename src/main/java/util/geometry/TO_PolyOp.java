package util.geometry;

//import c_MLZ_new.M4_4.Lmath;
//import c_MLZ_new.M4_4.Ltest;


/**
 * this class is made to get information of WB_Polygon and convert between JTS
 * Polygon and HE_Mesh WB_Polygon hemesh version 2019.04.23
 * 
 * @author XiaoJianZi
 *
 */
public class TO_PolyOp {
//	public static final WB_GeometryFactory gf = WB_GeometryFactory.instance();
//	public static final GeometryFactory JTSgf = new GeometryFactory();
//
//	/**
//	 *
//	 * @param coords
//	 * @return
//	 */
//	public static Coordinate[] addFirst2Last(Coordinate... coords) {
//		Coordinate[] cs = new Coordinate[coords.length + 1];
//		int i = 0;
//		for (; i < coords.length; i++) {
//			cs[i] = coords[i];
//		}
//		cs[i] = coords[0];
//		return cs;
//	}
//
//	/**
//	 * when use Polygon.getCoordinates, use this method to remove the overlap Coordinate
//	 * of start and end point
//	 *
//	 * @param coords
//	 * @return
//	 */
//	public static Coordinate[] subLast(Coordinate... coords) {
//		Coordinate[] cs = new Coordinate[coords.length - 1];
//		int i = 0;
//		for (; i < coords.length - 1; i++) {
//			cs[i] = coords[i];
//			cs[i].z = 0;
//		}
//		return cs;
//	}
//
//	/**
//	 * get shell points of WB_Poygon in CCW
//	 *
//	 * @param poly
//	 * @return
//	 */
//	public static WB_Coord[] getShellPts(WB_Polygon poly) {
//		if (poly.getNumberOfContours() == 1)
//			return poly.getPoints().toArray();
//		int numOut = poly.getNumberOfShellPoints();
//		WB_Point[] out = new WB_Point[numOut];
//		for (int i = 0; i < numOut; i++) {
//			out[i] = poly.getPoint(i);
//		}
//		return out;
//	}
//
//	/**
//	 * get inner points of WB_Poygon in CW
//	 *
//	 * @param poly
//	 * @return
//	 */
//	public static WB_Point[][] getInnerPts(WB_Polygon poly) {
//		if (poly.getNumberOfContours() == 1)
//			return null;
//		WB_Point[][] in = new WB_Point[poly.getNumberOfHoles()][];
//		int[] num = poly.getNumberOfPointsPerContour();
//		int count = num[0];
//		for (int i = 0; i < in.length; i++) {
//			WB_Point[] pts = new WB_Point[num[i + 1]];
//			for (int j = 0; j < pts.length; j++) {
//				pts[j] = poly.getPoint(count + j);
//			}
//			in[i] = pts;
//			count += pts.length;
//		}
//		return in;
//	}
//
//	public static WB_Coord[] getReverse(WB_Coord[] coords) {
//		WB_Coord[] buffer = new WB_Point[coords.length];
//		for (int i = 0; i < coords.length; i++) {
//			buffer[i] = coords[coords.length - 1 - i];
//		}
//		return buffer;
//	}
//
//	public static WB_Coord[][] getReverse(WB_Coord[][] coords) {
//		WB_Coord[][] buffer = new WB_Point[coords.length][];
//
//		for (int i = 0; i < coords.length; i++) {
//			buffer[i] = getReverse(coords[i]);
//		}
//		return buffer;
//	}
//
//	/**
//	 * out points in CountorClockWise, inner points in ClockWise
//	 *
//	 * @param out
//	 * @param in
//	 * @return
//	 */
//	public static WB_Polygon createWB_PolyWithHole(WB_Polygon out, WB_Polygon... in) {
//		WB_Coord[] outPts = getShellPts(out);
//		WB_Coord[][] ptsIn = new WB_Point[in.length][];
//
//		for (int i = 0; i < in.length; i++) {
//			List<WB_Coord> pts = in[i].getPoints().toList();
//			ptsIn[i] = new WB_Point[pts.size()];
//			for (int j = 0; j < pts.size(); j++) {
//				ptsIn[i][j] = new WB_Point(pts.get(pts.size() - 1 - j));
//			}
//		}
//		return new WB_Polygon(outPts, ptsIn);
//	}
//
//	/**
//	 * to convert a JTS Polygon to WB_Polygon
//	 *
//	 * @return a new WB_Polygon
//	 */
//	public static WB_Polygon toWB_Polygon(Polygon p) {
//
//		Coordinate[] coordOut = p.getExteriorRing().getCoordinates();
//		coordOut = subLast(coordOut);
//		WB_Point[] outPt = new WB_Point[coordOut.length];
//		for (int i = 0; i < coordOut.length; i++) {
//			outPt[i] = new WB_Point(coordOut[i].x, coordOut[i].y, coordOut[i].z);
//		}
//		int num = p.getNumInteriorRing();
//
//		if (num == 0) {
//			return new WB_Polygon(outPt);
//		} else {
//			WB_Point[][] ptsIn = new WB_Point[num][];
//			for (int i = 0; i < num; i++) {
//				Coordinate[] coords = p.getInteriorRingN(i).getCoordinates();
//				/**
//				 * LineString also needs to remove the last coordinate which is same with the
//				 * fist coordinate
//				 */
//
//				WB_Point[] pts = new WB_Point[coords.length];
//				for (int j = 0; j < coords.length; j++) {
//					double z = coords[i].z;
//					if (Double.isNaN(z))
//						z = 0;
//					pts[j] = new WB_Point(coords[j].x, coords[j].y, z);
//				}
//				ptsIn[i] = pts;
//			}
//			return new WB_Polygon(outPt, ptsIn);
//		}
//	}
//
//	/**
//	 * get a copy of a polygon, also can be used<br>
//	 * with polygon with hole<br>
//	 *
//	 * @param poly
//	 * @return
//	 */
//	public static WB_Polygon polyDup(WB_Polygon poly) {
//		WB_Coord[] out = getShellPts(poly);
//		WB_Coord[][] in = getInnerPts(poly);
//		return new WB_Polygon(out, in);
//	}
//
//	/**
//	 * convert simple WB_Polygon to JTS Polygon<br>
//	 * to be finished if there is a inner ring<br>
//	 *
//	 * @param poly
//	 * @return JTS Polygon
//	 */
//	public static Polygon toJTSPolygonSimple(WB_Polygon poly) {
//		Coordinate[] coord = new Coordinate[poly.getNumberOfPoints()];
//		for (int i = 0; i < poly.getNumberOfPoints(); i++) {
//			WB_Point p = poly.getPoint(i);
//			Coordinate c = new Coordinate(p.xd(), p.yd(), p.zd());
//			coord[i] = c;
//		}
//		LinearRing ring = JTSgf.createLinearRing(addFirst2Last(coord));
//		return JTSgf.createPolygon(ring);
//	}
//
//	public static Polygon toJTSPolygon(WB_Polygon poly) {
//		int num = poly.getNumberOfContours();
//		if (num == 1)
//			return toJTSPolygonSimple(poly);
//
//		int numOut = poly.getNumberOfShellPoints();
//		Coordinate[] outPts = new Coordinate[numOut];
//
//		for (int i = 0; i < numOut; i++) {
//			WB_Point wbPt = poly.getPoint(i);
//			outPts[i] = new Coordinate(wbPt.xd(), wbPt.yd(), wbPt.zd());
//		}
//
//		outPts = addFirst2Last(outPts);
//		LinearRing outRing = JTSgf.createLinearRing(outPts);
//
//		LinearRing[] holeRings = new LinearRing[poly.getNumberOfHoles()];
//
//		int[] ptsNumPerHole = poly.getNumberOfPointsPerContour();// start form exterior ring
//		int count = ptsNumPerHole[0];
//
//		for (int i = 0; i < holeRings.length; i++) {
//			Coordinate[] pts = new Coordinate[ptsNumPerHole[i + 1]];
//			for (int j = 0; j < pts.length; j++) {
//				WB_Point wbPt = poly.getPoint(count + j);
//				pts[j] = new Coordinate(wbPt.xd(), wbPt.yd(), wbPt.zd());
//			}
//			pts = addFirst2Last(pts);
//			holeRings[i] = JTSgf.createLinearRing(pts);
//			count += pts.length - 1;
//		}
//		return JTSgf.createPolygon(outRing, holeRings);
//	}
//
//	public static WB_Polygon filpPoly(WB_Polygon oriPoly) {
//		WB_Coord[] out = getReverse(getShellPts(oriPoly));
//		if (oriPoly.getNumberOfContours() == 1)
//			return new WB_Polygon(out);
//		WB_Coord[][] in = getReverse(getInnerPts(oriPoly));
//		return new WB_Polygon(out, in);
//	}
//
//	public static List<WB_Polygon> filpPolys(List<WB_Polygon> oriPolys) {
//		List<WB_Polygon> buffer = new ArrayList<>();
//		for (WB_Polygon p : oriPolys) {
//			buffer.add(filpPoly(p));
//		}
//		return buffer;
//	}
//
//	/**
//	 * transform from polygon plane to XY plane with origin point at (0,0,0)
//	 *
//	 * @param poly
//	 * @return
//	 */
//	public static WB_Transform3D toWCSOri(WB_Polygon poly) {
//		return new WB_Transform3D(poly.getCenter(), poly.getNormal(), WB_Vector.ORIGIN(), WB_Vector.Z());
//	}
//
//	public static boolean pointOnPolygon(WB_Point n, Polygon poly){
//		Point p = GeomFactory.gf_jts.createPoint(new Coordinate(n.xf(),n.yf()));
//		Coordinate[] cs = DistanceOp.nearestPoints(poly.getBoundary(),p);
//		if (cs[0].distance(cs[1])<Epsilon.deviation){
//			return true;
//		}else {
//			return false;
//		}
//	}
//	public static double point2Polygon(WB_Point n, Polygon poly){
//		Point p = GeomFactory.gf_jts.createPoint(new Coordinate(n.xf(),n.yf()));
//		if (poly.contains(p)){
//			Coordinate[] cs = DistanceOp.nearestPoints(poly.getBoundary(),p);
//			return cs[0].distance(cs[1]);
//		}else{
//			return -1;
//		}
//	}
//
//
//	public static ArrayList<WB_Point> getRandomPts(float disturb,float averDist, WB_Polygon bound){
//		int num = (int) (Math.abs(bound.getSignedArea()) / averDist / averDist / 2f);
//
//		WB_Transform3D T = new WB_Transform3D(bound.getPoint(0), bound.getNormal(), new WB_Point(0, 0, 0),
//				new WB_Vector(0, 0, 1));
//		WB_Polygon flatbound = new WB_Polygon(bound.apply(T).getPoints());
//		WB_AABB ab = flatbound.getAABB();
//		ArrayList<WB_Point> flatpts = new ArrayList<>();
//
//		for (int i = 0; i < num;) {
//			WB_Point testp = new WB_Point(Lmath.random(ab.getMinX(), ab.getMaxX()),
//					Lmath.random(ab.getMinY(), ab.getMaxY()), 0);
//			if (!Ltest.IsContain(flatbound, testp))
//				continue;
//			boolean flag = true;
//			for (WB_Point refp : flatpts)
//				if (refp.getDistance3D(testp) < disturb) {
//					flag = false;
//					break;
//				}
//			if (flag) {
//				flatpts.add(testp);
//				i++;
//			}
//		}
//		T.inverse();
//		ArrayList<WB_Point> pts = new ArrayList<>();
//		for (WB_Point fp : flatpts)
//			pts.add(fp.apply(T));
//		return pts;
//	}

}
