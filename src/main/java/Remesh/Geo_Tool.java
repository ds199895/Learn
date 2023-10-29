//package Remesh;
//
//import wblut.geom.WB_Line;
//import wblut.geom.WB_Point;
//import wblut.math.WB_Math;
//
///***
// * @description //todo
// * @author HeSiyuan
// * @version 1.0
// * @date 2023/10/27 21:59
// * Inst. AAA, S-ARCH, Southeast University
// */
//public class Geo_Tool {
//
//    /**
//     * get the common endPoint, intersection as well,
//     * if two lines intersect at one of the endpoints
//     */
//
//    private static WB_Point getOnlyCommonEndPoint(WB_Line line1, WB_Line line2 ) {
//        if ( line1.startPoint.equalsXAndY( line2.endPoint ) ) return line1.startPoint;
//
//        return line1.endPoint;
//    }
//
//    /**
//     * if the two lines Overlap But Have Common EndPoint,
//     * but note that may only have one common endPoint.
//     */
//
//    private static
//    boolean isOverlapButHavingCommonEndPoint( Line line1, Line line2 ) {
//        return Vector.sortByX( line2.startPoint, line2.endPoint ) > 0 &&
//                line1.startPoint.equalsXAndY( line2.endPoint ) ||
//                line1.endPoint.equalsXAndY( line2.startPoint );
//    }
//
//    /**
//     * toLeft test to check if two lines intersect
//     */
//
//    private static
//    boolean ifLinesIntersect( Line line1, Line line2,
//                              double res1, double res2 ) {
//        // parallel cases:
//        // case 1: overlap or on the same line.
//        if (WB_Math.fastAbs( res1 )==0 &&
//                WB_Math.fastAbs( res2)==0 )
//            return isOverlapButHavingCommonEndPoint( line1, line2 );
//        // case 2: parallel on the right side.
//        if ( res1 < 0 && res2 < 0 )
//            return false;
//        // case 3: parallel on the left side.
//        if ( res1 > 0 && res2 > 0 )
//            return false;
//
//        // intersecting cases: either intersect at
//        // a common point other than endpoints,
//        // or at one of the endpoints.
//        return true;
//    }
//
//    /**
//     * line1 and line2 intersects?
//     */
//
//    public static
//    boolean ifLinesIntersect( WB_Line line1, WB_Line line2 ) {
//        if ( line1 == null || line2 == null ) return false;
//
//        // to left test based on line1.
//        double res1 = Triangle.areaTwo( line1.endPoint, line1.startPoint, line2.endPoint );
//        double res2 = Triangle.areaTwo( line1.endPoint, line1.startPoint, line2.startPoint );
//        // to left test based on line2.
//        double res3 = Triangle.areaTwo( line2.endPoint, line2.startPoint, line1.endPoint );
//        double res4 = Triangle.areaTwo( line2.endPoint, line2.startPoint, line1.startPoint );
//
//        // have intersection if and only if
//        // two endpoints of one line are
//        // at the opposite side of the other line.
//        boolean finalRes1 = ifLinesIntersect( line1, line2, res1, res2 );
//        boolean finalRes2 = ifLinesIntersect( line1, line2, res3, res4 );
//        return finalRes1 && finalRes2;
//    }
//
//}