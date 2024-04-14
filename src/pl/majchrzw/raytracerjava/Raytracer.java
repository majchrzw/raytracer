package pl.majchrzw.raytracerjava;

import javafx.scene.paint.Color;
import pl.majchrzw.raytracerjava.light.AmbientLight;
import pl.majchrzw.raytracerjava.light.DirectionalLight;
import pl.majchrzw.raytracerjava.light.Light;
import pl.majchrzw.raytracerjava.light.PointLight;

import java.util.List;

public class Raytracer {
	
	private final List<Sphere> spheres;
	private final List<Light> lights;
	
	public Raytracer(List<Sphere> spheres, List<Light> lights) {
		this.spheres = spheres;
		this.lights = lights;
	}
	
	public int[] traceRay(double[] origin, double[] direction, double minT, double maxT, int recursionDepth) {
		Closest closest = closestIntersection(origin, direction, minT, maxT);
		if (closest.closestSphere == null) {
			return colorToVector(Color.WHITE);
		}
		var closestSphere = closest.closestSphere;
		double[] point = add(origin, multiply(closest.closestIntersection, direction));
		double[] normal = subtract(point, closestSphere.center());
		normal = multiply(1.0d / length(normal), normal);
		
		double[] view = multiply(-1, direction);
		double lighting = computeLighting(point, normal, view, closestSphere.alpha(), closestSphere.k_a(), closestSphere.k_s(), closestSphere.k_d());
		int[] localColor = multiply(lighting, closestSphere.getColorAsVector());
		
		if (closestSphere.reflective() <= 0 || recursionDepth <= 0) {
			return localColor;
		}
		double r = closestSphere.reflective();
		double[] reflectedRay = reflectRay(view, normal);
		int[] reflectedColor = traceRay(point, reflectedRay, 0.001, Double.MAX_VALUE, recursionDepth - 1);
		return add(multiply(1d - r, localColor), multiply(r, reflectedColor));
	}
	
	private double[] reflectRay(double[] v1, double[] v2) {
		return subtract(multiply(2 * dot(v1, v2), v2), v1);
	}
	
	private double computeLighting(double[] point, double[] normal, double[] vector, double alpha, double k_a, double k_s, double k_d) {
		double intensity = 0.0d;
		double normalLength = length(normal);
		double vectorLength = length(vector);
		for (Light light : lights) {
			if (light instanceof AmbientLight) {
				intensity += light.getIntensity() * k_a;
			} else {
				double[] vectorL = new double[3];
				double tMax = Double.MAX_VALUE;
				if (light instanceof PointLight) {
					vectorL = subtract(((PointLight) light).getPosition(), point);
					tMax = 1;
				} else if (light instanceof DirectionalLight) {
					vectorL = ((DirectionalLight) light).getPosition();
				}
				Closest closest = closestIntersection(point, vectorL, 0.001, tMax);
				if (closest.closestSphere != null) {
					continue;
				}
				double n_dot_l = dot(normal, vectorL);
				if (n_dot_l > 0) {
					intensity += ((light.getIntensity() * k_d * n_dot_l) / (normalLength * length(vectorL)));
				}
				if (alpha != -1) {
					double[] vectorR = reflectRay(vectorL, normal);
					double r_dot_v = dot(vectorR, vector);
					if (r_dot_v > 0) {
						intensity += (light.getIntensity() * k_s) * Math.pow(r_dot_v / (length(vectorR) * vectorLength), alpha);
					}
				}
			}
		}
		return intensity;
	}
	
	private Closest closestIntersection(double[] origin, double[] direction, double minT, double maxT) {
		double closestT = Double.MAX_VALUE;
		Sphere closestSphere = null;
		for (Sphere sphere : spheres) {
			double[] ts = intersectRay(origin, direction, sphere);
			if (closestT > ts[0] && ts[0] > minT && ts[0] < maxT) {
				closestT = ts[0];
				closestSphere = sphere;
			}
			if (closestT > ts[1] && ts[1] > minT && ts[1] < maxT) {
				closestT = ts[1];
				closestSphere = sphere;
			}
		}
		return new Closest(closestSphere, closestT);
	}
	
	private double[] intersectRay(double[] origin, double[] destination, Sphere sphere) {
		double[] oc = subtract(origin, sphere.center());
		
		double k1 = dot(destination, destination);
		double k2 = 2 * dot(oc, destination);
		double k3 = dot(oc, oc) - sphere.radius() * sphere.radius();
		
		double discriminant = (k2 * k2) - (4 * k1 * k3);
		if (discriminant <= 0) {
			return new double[]{Double.MAX_VALUE, Double.MAX_VALUE};
		}
		double t1 = (-k2 + Math.sqrt(discriminant)) / (2 * k1);
		double t2 = (-k2 - Math.sqrt(discriminant)) / (2 * k1);
		return new double[]{t1, t2};
	}
	
	record Closest(Sphere closestSphere, double closestIntersection) {
	}
	
	private double[] add(double[] v1, double[] v2) {
		return new double[]{v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2]};
	}
	
	private int[] add(int[] v1, int[] v2) {
		return new int[]{v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2]};
	}
	
	private double dot(double[] v1, double[] v2) {
		return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
	}
	
	private double length(double[] v) {
		return Math.sqrt(dot(v, v));
	}
	
	private double[] subtract(double[] v1, double[] v2) {
		return new double[]{v1[0] - v2[0], v1[1] - v2[1], v1[2] - v2[2]};
	}
	
	private double[] multiply(double k, double[] v) {
		return new double[]{
				v[0] * k,
				v[1] * k,
				v[2] * k
		};
	}
	
	private int[] multiply(double k, int[] v) {
		return new int[]{
				(int) (v[0] * k),
				(int) (v[1] * k),
				(int) (v[2] * k)
		};
	}
	private int[] colorToVector(Color color) {
		double red = color.getRed();
		double green = color.getGreen();
		double blue = color.getBlue();
		
		return new int[]{
				(int) (red * 255),
				(int) (green * 255),
				(int) (blue * 255)
		};
	}
}
