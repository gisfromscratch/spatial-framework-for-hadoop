package com.esri.hadoop.hive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.junit.Test;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.ogc.OGCGeometry;

public class TestStBearingLine {
	
	private final static double Epsilon = 0.0001;

	@Test
	public void testBearingLine() {
		final double latitude = 27.668196685574735;
		final double longitude = -158.41586225679893;
		final double bearing = 17.05277505911702;
		final double distance = 123.95472310931031;
			
		ST_BearingLine bearingLine = new ST_BearingLine();
		Text linearUnit = new Text("NM");
		BytesWritable lineAsWritable = bearingLine.evaluate(new DoubleWritable(longitude), new DoubleWritable(latitude), new DoubleWritable(bearing), new DoubleWritable(distance), linearUnit);
		assertNotNull("The line writable must not be null!", lineAsWritable);
		
		OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(lineAsWritable);
		Geometry esriGeometry = ogcGeometry.getEsriGeometry();
		assertTrue("A line geometry was expected!", esriGeometry instanceof Polyline);
		
		Polyline esriLineGeometry = (Polyline) esriGeometry;
		assertFalse("The line geometry must not be empty!", esriLineGeometry.isEmpty());
		
		Point fromPoint = esriLineGeometry.getPoint(0);
		assertEquals("Longitude is different!", longitude, fromPoint.getX(), Epsilon);
		assertEquals("Latitude is different!", latitude, fromPoint.getY(), Epsilon);
	}
}
