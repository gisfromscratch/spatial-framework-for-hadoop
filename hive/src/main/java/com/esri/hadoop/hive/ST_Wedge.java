package com.esri.hadoop.hive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.ogc.OGCGeometry;

@Description(
		name = "ST_Wedge",
		value = "_FUNC_(x,y,bearing,distance,linearunit,left_bearing,right_bearing) - return the wedge polygon.\n",
		extended = "Example:\n" +
		"  SELECT _FUNC_(1,2,17.05,150,'NM',7.12,27.79) FROM onerow; -- return the wedge polygon from POINT (1 2) having a bearing line with a azimuth of 17.05, a length of 150 nautical miles, a left bearing of 7.12 and a right bearing of 27.79.\n"
		)
public class ST_Wedge extends ST_Geometry {

	static final Log LOG = LogFactory.getLog(ST_Wedge.class.getName());
	static final GeodesicFactory GEODESIC_FACTORY = new GeodesicFactory();
	
	private final double fromNauticalMilesToMetersConversionFactor = 1852;
	
	public BytesWritable evaluate(DoubleWritable x, DoubleWritable y, DoubleWritable bearing, DoubleWritable distance, Text linearUnit,DoubleWritable leftBearing, DoubleWritable rightBearing) {
		if (null == x || null == y || null == bearing || null == distance || null == linearUnit || null == leftBearing || null == rightBearing) {
			LogUtils.Log_ArgumentsNull(LOG);
			return null;
		}
		
		double distanceInMeters = distance.get();
		if (0 == "NM".compareToIgnoreCase(linearUnit.toString())) {
			distanceInMeters *= fromNauticalMilesToMetersConversionFactor;
		}
		
		// Construct the wedge
		Point fromPoint = new Point(x.get(), y.get());
		Polygon wedge = GEODESIC_FACTORY.createWedge(fromPoint, bearing.get(), distanceInMeters, leftBearing.get(), rightBearing.get());
		BytesWritable wedgeAsWritable = GeometryUtils.geometryToEsriShapeBytesWritable(OGCGeometry.createFromEsriGeometry(wedge, null));
		return wedgeAsWritable;
	}
}
