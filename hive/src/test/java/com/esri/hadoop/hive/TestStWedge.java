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
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.ogc.OGCGeometry;

public class TestStWedge {

	private final static double Epsilon = 0.0001;

	@Test
	public void testStWedge() {
		final double latitude = 27.668196685574735;
		final double longitude = -158.41586225679893;
		final double bearing = 17.05277505911702;
		final double distance = 123.95472310931031;

		ST_Wedge wedge = new ST_Wedge();
		Text linearUnit = new Text("NM");
		BytesWritable wedgeWritable = wedge.evaluate(new DoubleWritable(longitude), new DoubleWritable(latitude),
				new DoubleWritable(bearing), new DoubleWritable(distance), linearUnit, new DoubleWritable(bearing - 10),
				new DoubleWritable(bearing + 10));
		assertNotNull("The wedge writable must not be null!", wedgeWritable);

		OGCGeometry ogcGeometry = GeometryUtils.geometryFromEsriShape(wedgeWritable);
		Geometry esriGeometry = ogcGeometry.getEsriGeometry();
		assertTrue("A polygon geometry was expected!", esriGeometry instanceof Polygon);

		Polygon esriPolygonGeometry = (Polygon) esriGeometry;
		assertFalse("The polygon geometry must not be empty!", esriPolygonGeometry.isEmpty());

		Point fromPoint = esriPolygonGeometry.getPoint(0);
		assertEquals("Longitude is different!", longitude, fromPoint.getX(), Epsilon);
		assertEquals("Latitude is different!", latitude, fromPoint.getY(), Epsilon);
	}
}
