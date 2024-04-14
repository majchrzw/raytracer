package pl.majchrzw.raytracerjava.light;

public class PointLight extends Light {
	
	private double[] position;
	
	public double[] getPosition() {
		return position;
	}
	
	public PointLight(double intensity, int id,  double[] position) {
		super(intensity, id);
		this.position = position;
	}
}
