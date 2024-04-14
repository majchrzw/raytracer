package pl.majchrzw.raytracerjava;


import javafx.scene.paint.Color;

record Sphere(
		double[] center,
		int radius,
		Color color,
		int alpha,
		double reflective,
		double k_s,
		double k_d,
		double k_a
) {
	public int[] getColorAsVector() {
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
