package loc;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class distance {
	
	private static final String ACCESS_CODE = "AIzaSyAEQvKUVouPDENLkQlCF6AAap1Ze-6zMos";
	private static List<Point> midpoints = new LinkedList<Point>();
	private static List<Point> steps = new LinkedList<Point>();
	private static final int fixDistance = 50;

	public static void main(String[] args) {
		
		double lat1=12.94523;
		double lon1=77.61896;
		double lat2=12.95944;
		double lon2=77.66085;
		Point startPoint = new Point(lat1, lon1);
		steps.add(startPoint);
		String url = "https://maps.googleapis.com/maps/api/directions/json"
				+ "?origin=12.94523,77.61896"
				+ "&destination=12.95944,77.66085"
				+ "&key=AIzaSyAEQvKUVouPDENLkQlCF6AAap1Ze-6zMos";
		execute(url);
		Point endPoint = new Point(lat2, lon2);
		steps.add(endPoint);
		tests();
		midpoints.add(0, startPoint);
		midpoints.add(endPoint);
		System.out.println(midpoints);
	}
	
	private static void tests() {
		for(int i=0;i<steps.size()-1;i++) {
			double lat1 = steps.get(i).getX();
			double lon1 = steps.get(i).getY();
			double lat2 = steps.get(i+1).getX();
			double lon2 = steps.get(i+1).getY();
			double distBtn = distance(lat1, lon1, lat2, lon2);
			int interPoints = (int) (distBtn/fixDistance);
			createPoints(lat1,lon1,lat2,lon2,interPoints);
			int lastIndex = midpoints.size()-1;
			lat1 = midpoints.get(lastIndex).getX();
			lon1 = midpoints.get(lastIndex).getY();
			movePoint(lat1,lon1,lat2,lon2,distBtn%fixDistance);
		}
	}
	
	private static void createPoints(double lat1, double lon1, double lat2, double lon2, int interPoints) {
		if(interPoints<1) {
			return;
		} else {
			double bearing = bearing(lat1, lon1, lat2, lon2);
			movePoint(lat1, lon1, fixDistance, bearing);
			int lastIndex = midpoints.size()-1;
			lat1 = midpoints.get(lastIndex).getX();
			lon1 = midpoints.get(lastIndex).getY();
			interPoints = interPoints-1;
			createPoints(lat1, lon1, lat2, lon2, interPoints);
		}
	}
	
	private static void movePoint(double lat1, double lon1, double lat2, double lon2, double distance) {
		double bearing = bearing(lat1, lon1, lat2, lon2);
		movePoint(lat1, lon1, distance, bearing);
	}
	
	private static double distance(Point A, Point B) {
		return distance(A.getX(), A.getY(), B.getX(), B.getY());
	}
	
	private static double distance(double lat1, double lon1, double lat2, double lon2) {
//		double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//        lat1 = Math.toRadians(lat1);
//        lat2 = Math.toRadians(lat2);
//        double a = Math.pow(Math.sin(dLat / 2), 2) +
//                   Math.pow(Math.sin(dLon / 2), 2) *
//                   Math.cos(lat1) *
//                   Math.cos(lat2);
//        double rad = 6371;
//        double c = 2 * Math.asin(Math.sqrt(a));
//        return rad * c;
		return org.apache.lucene.util.SloppyMath.haversinMeters(lat1, lon1, lat2, lon2);
	}

	private static void movePoint(double latitude, double longitude, double distanceInMetres, double bearing) {
		double brngRad = Math.toRadians(bearing);
	    double latRad = Math.toRadians(latitude);
	    double lonRad = Math.toRadians(longitude);
	    int earthRadiusInMetres = 6371000;
	    double distFrac = distanceInMetres / earthRadiusInMetres;

	    double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
	    double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
	    double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

	    Point point = new Point(Math.toDegrees(latitudeResult), Math.toDegrees(longitudeResult));
	    midpoints.add(point);
	}

	protected static double bearing(double lat1, double lon1, double lat2, double lon2){
		  double longitude1 = lon1;
		  double longitude2 = lon2;
		  double latitude1 = Math.toRadians(lat1);
		  double latitude2 = Math.toRadians(lat2);
		  double longDiff= Math.toRadians(longitude2-longitude1);
		  double y= Math.sin(longDiff)*Math.cos(latitude2);
		  double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

		  return (Math.toDegrees(Math.atan2(y, x))+360)%360;
		}

	private static void execute(String url) {
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpget = new HttpGet(url);
			HttpResponse httpresponse = httpclient.execute(httpget);
			String response = IOUtils.toString(httpresponse.getEntity().getContent());
			updateSteps(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateSteps(String response) {
		JsonObject jsonObject = (JsonObject) JsonParser.parseString(response);
		JsonArray jsonArray = jsonObject.getAsJsonArray("routes").get(0).getAsJsonObject().getAsJsonArray("legs").get(0).getAsJsonObject().getAsJsonArray("steps");
		for(int i=0;i<jsonArray.size();i++) {
			double lat = jsonArray.get(i).getAsJsonObject().getAsJsonObject("end_location").get("lat").getAsDouble();
			double log = jsonArray.get(i).getAsJsonObject().getAsJsonObject("end_location").get("lng").getAsDouble();
			Point step = new Point(lat,log);
			steps.add(step);
		}
	}
	
}
