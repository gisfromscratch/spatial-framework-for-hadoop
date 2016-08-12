package com.esri.hadoop.hive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.io.BytesWritable;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.ogc.OGCGeometry;

@Description(
		name = "ST_BearingLine",
		value = "_FUNC_(x,y,bearing,distance) - return the bearing line.\n",
		extended = "Example:\n" +
		"  SELECT _FUNC_(1,2,45.1,150) FROM onerow; -- return the bearing line from POINT (1 2) having a bearing of 45.1 and a length of 150 meters.\n"
		)
public class ST_BearingLine extends ST_Geometry {

	static final Log LOG = LogFactory.getLog(ST_BearingLine.class.getName());
	static final GeodesicFactory GEODESIC_FACTORY = new GeodesicFactory();
	
	public BytesWritable evaluate(DoubleWritable x, DoubleWritable y, DoubleWritable bearing, DoubleWritable distance) {
		if (null == x || null == y || null == bearing || null == distance) {
			LogUtils.Log_ArgumentsNull(LOG);
			return null;
		}
		
		// Construct the bearing line
		Point fromPoint = new Point(x.get(), y.get());
		Polyline bearingLine = GEODESIC_FACTORY.createSpike(fromPoint, bearing.get(), distance.get());
		BytesWritable lineAsWritable = GeometryUtils.geometryToEsriShapeBytesWritable(OGCGeometry.createFromEsriGeometry(bearingLine, null));
		return lineAsWritable;
	}
}
