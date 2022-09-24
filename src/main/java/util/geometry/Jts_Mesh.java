package util.geometry;


import org.locationtech.jts.geom.GeometryFactory;
import wblut.geom.WB_GeometryFactory;

/**
 * 	JTS与HE_Mesh的2D图元转换类
 * */
public class Jts_Mesh {
    public static GeometryFactory gf_Jts = GeomFactory.gf_jts;
    public static WB_GeometryFactory gf_hemesh = GeomFactory.gf_hemesh;
}
