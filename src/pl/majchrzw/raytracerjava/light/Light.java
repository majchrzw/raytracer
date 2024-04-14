package pl.majchrzw.raytracerjava.light;

public abstract class Light {
	private double intensity;
	
	private int id;
	
	public double getIntensity() {
		return intensity;
	}
	
	public int getId() {
		return id;
	}
	
	public Light(double intensity, int id) {
		this.intensity = intensity;
		this.id = id;
	}
}
