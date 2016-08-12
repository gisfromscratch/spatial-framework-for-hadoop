package com.esri.hadoop.hive;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicLine;
import net.sf.geographiclib.GeodesicMask;

/**
 * Represents a factory for creating geodesic geometries.
 */
public class GeodesicFactory {

	private final double Epsilon = 1E-5;
	private final double DensifyInMeters = 50000;
	
	/**
	 * Creates a spike geometry using azimuth and distance.
	 * @param fromPoint the start point.
	 * @param bearingInDegree the bearing in degree.
	 * @param distanceInMeters the distance in meters.
	 * @return A new spike geometry.
	 */
	public Polyline createSpike(Point fromPoint, double bearingInDegree, double distanceInMeters) {
		if (null == fromPoint) {
			throw new IllegalArgumentException("The from point must not be null!");
		}
		if (distanceInMeters < Epsilon) {
			throw new IllegalArgumentException("The distance in meters must not be less than 1E-5!");
		}
		
		double azimuth = bearingInDegree / 180.0 * Math.PI;
		 
		Polyline spike = new Polyline();
		GeodesicLine directLine = Geodesic.WGS84.DirectLine(fromPoint.getY(), fromPoint.getX(), azimuth, distanceInMeters);
		spike.startPath(fromPoint);
		for (double distanceAlongSpike = DensifyInMeters; distanceAlongSpike + Epsilon < distanceInMeters; distanceAlongSpike+=DensifyInMeters) {
			GeodesicData positionOnSpike = directLine.Position(distanceAlongSpike, GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
			spike.lineTo(positionOnSpike.lon2, positionOnSpike.lat2);
		}
		GeodesicData positionOnSpike = directLine.Position(distanceInMeters, GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
		spike.lineTo(positionOnSpike.lon2, positionOnSpike.lat2);
		return spike;
	}
	
	/**
	 * Creates a spike geometry using two points.
	 * @param fromPoint the start point.
	 * @param toPoint the end point.
	 * @return A new spike geometry.
	 */
	public Polyline createSpike(Point fromPoint, Point toPoint) {
		if (null == fromPoint) {
			throw new IllegalArgumentException("The from point must not be null!");
		}
		if (null == toPoint) {
			throw new IllegalArgumentException("The to point must not be null!");
		}
		 
		Polyline spike = new Polyline();
		GeodesicLine directLine = Geodesic.WGS84.InverseLine(fromPoint.getY(), fromPoint.getX(), toPoint.getY(), toPoint.getX(), GeodesicMask.DISTANCE_IN | GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
		double distanceInMeters = directLine.Distance();
		spike.startPath(fromPoint);
		for (double distanceAlongSpike = DensifyInMeters; distanceAlongSpike + Epsilon < distanceInMeters; distanceAlongSpike+=DensifyInMeters) {
			GeodesicData positionOnSpike = directLine.Position(distanceAlongSpike, GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
			spike.lineTo(positionOnSpike.lon2, positionOnSpike.lat2);
		}
		GeodesicData positionOnSpike = directLine.Position(distanceInMeters, GeodesicMask.LATITUDE | GeodesicMask.LONGITUDE);
		spike.lineTo(positionOnSpike.lon2, positionOnSpike.lat2);
		return spike;
	}
	
	/**
	 * Creates a wegde geometry.
	 * @param fromPoint the start point.
	 * @param bearingInDegree the bearing in degree.
	 * @param distanceInMeters the distance in meters.
	 * @param leftBearingInDegree the left bearing of the wedge.
	 * @param rightBearingInDegree the right bearing of the wedge.
	 * @return A new wedge geometry.
	 */
	public Polygon createWedge(Point fromPoint, double bearingInDegree, double distanceInMeters, double leftBearingInDegree, double rightBearingInDegree) {
		if (null == fromPoint) {
			throw new IllegalArgumentException("The from point must not be null!");
		}
		if (distanceInMeters < Epsilon) {
			throw new IllegalArgumentException("The distance in meters must not be less than 1E-5!");
		}
		
		Polygon wedge = new Polygon();
		Polyline leftSpike = createSpike(fromPoint, leftBearingInDegree, distanceInMeters);
		Point leftSpikeToPoint = null;
		int leftSpikePointCount = leftSpike.getPointCount();
		if (0 < leftSpikePointCount) {
			wedge.add(leftSpike, false);
			leftSpikeToPoint = leftSpike.getPoint(leftSpikePointCount - 1);
		}
		
		Polyline centerSpike = createSpike(fromPoint, bearingInDegree, distanceInMeters);
		Point centerSpikeToPoint = null;
		int centerSpikePointCount = centerSpike.getPointCount();
		if (0 < centerSpikePointCount) {
			wedge.add(centerSpike, false);
			centerSpikeToPoint = centerSpike.getPoint(centerSpikePointCount - 1);
		}
		
		Polyline rightSpike = createSpike(fromPoint, rightBearingInDegree, distanceInMeters);
		Point rightSpikeToPoint = null;
		int rightSpikePointCount = rightSpike.getPointCount();
		if (0 < rightSpikePointCount) {
			rightSpikeToPoint = rightSpike.getPoint(rightSpikePointCount - 1);
		}
		
		if (null != leftSpikeToPoint && null != centerSpikeToPoint) {
			Polyline leftToCenterSpike = createSpike(leftSpikeToPoint, centerSpikeToPoint);
			int leftToCenterSpikePointCount = leftToCenterSpike.getPointCount();
			if (0 < leftToCenterSpikePointCount) {
				wedge.add(leftToCenterSpike, false);
				Point leftToCenterToPoint = leftToCenterSpike.getPoint(leftToCenterSpikePointCount - 1);
				
				if (null != rightSpikeToPoint) {
					Polyline centerToRightSpike = createSpike(leftToCenterToPoint, rightSpikeToPoint);
					int centerToRightSpikePointCount = centerToRightSpike.getPointCount();
					if (0 < centerToRightSpikePointCount) {
						wedge.add(centerToRightSpike, false);
					}
					
					wedge.add(rightSpike, true);
				}
			}
		}
		return wedge;
	}
}
