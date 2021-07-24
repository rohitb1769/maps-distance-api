package loc;

public class Point {
	private double x;
	private double y;
	Point(double d, double e){
		this.x=d;
		this.y=e;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return "{x: " + x + " ,y: " + y + " }";
	}
	
}
