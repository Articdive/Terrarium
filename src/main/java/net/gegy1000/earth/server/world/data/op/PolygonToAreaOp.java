package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import net.gegy1000.earth.server.world.data.AreaData;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.rasterization.PolygonShapeProducer;

import java.awt.geom.Area;

public final class PolygonToAreaOp {
    public static DataOp<AreaData> apply(DataOp<PolygonData> polygons, CoordinateState coordinateState) {
        return polygons.map((polygonData, engine, view) -> {
            Area area = new Area();

            for (MultiPolygon polygon : polygonData.getPolygons()) {
                for (int i = 0; i < polygon.getNumGeometries(); i++) {
                    Geometry geometry = polygon.getGeometryN(i);
                    if (geometry instanceof Polygon) {
                        area.add(PolygonShapeProducer.toShape((Polygon) geometry, coordinateState));
                    }
                }
            }

            return new AreaData(area);
        });
    }
}
