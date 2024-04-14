package pl.majchrzw.raytracerjava.light;

public class DirectionalLight extends Light {
	
	private double[] position;
	
	public double[] getPosition() {
		return position;
	}
	
	public DirectionalLight(double intensity, int id, double[] position) {
		super(intensity, id);
		this.position = position;
	}
}