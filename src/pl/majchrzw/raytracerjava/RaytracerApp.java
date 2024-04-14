package pl.majchrzw.raytracerjava;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pl.majchrzw.raytracerjava.light.AmbientLight;
import pl.majchrzw.raytracerjava.light.DirectionalLight;
import pl.majchrzw.raytracerjava.light.Light;
import pl.majchrzw.raytracerjava.light.PointLight;

import java.util.List;

public class RaytracerApp extends Application {
	
	private final int WIDTH = 900;
	private final int HEIGHT = 900;
	
	private final Raytracer raytracer;
	
	private double[] camera_position = {0d, 0d, 0d};
	
	private double[] viewAngles = {0, 0, 0};
	
	public RaytracerApp() {
		List<Sphere> spheres = List.of(
				new Sphere(new double[]{0, -.5, 3}, 1, Color.SILVER, 200, 0.5, .9, 0.4, 0.4), // metal
				new Sphere(new double[]{2, 0, 4}, 1, Color.AQUAMARINE, 40, 0.001, .05, .5, .2), // ściana
				new Sphere(new double[]{-2, 1, 4}, 1, Color.LIGHTGRAY, 80, 0.003, .2,0.8, .6), // plastik
				new Sphere(new double[]{1, 3, 8}, 2, Color.rgb(161,102,47), 10, .001, .1, .7, .2), // drewno
				new Sphere(new double[]{0, -5001, 0}, 5000, Color.YELLOW, 60, 0.05, .1, .7, .2) // podłoże
		);
		List<Light> lights = List.of(
				new AmbientLight(0.05d, 1),
				new PointLight(1.25, 2, new double[]{2, 1, 0})
				//new DirectionalLight(0.4d, 3, new double[]{1, 4, 4})
		);
		raytracer = new Raytracer(spheres, lights);
	}
	
	@Override
	public void start(Stage stage) {
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		StackPane root = new StackPane(canvas);
		Scene scene = new Scene(root, WIDTH + 20, HEIGHT + 20);
		scene.setOnKeyPressed(this::handleKeyPressed);
		
		stage.setTitle("raytracer in java!");
		stage.setScene(scene);
		stage.show();
		
		new AnimationTimer() {
			private long lastUpdate = 0;
			
			@Override
			public void handle(long now) {
				long elapsed = now - lastUpdate;
				double fps = 20;
				double frameTime = 1.0 / fps * 1_000_000_000;
				if (elapsed >= frameTime) {
					handleRendering(gc);
					lastUpdate = now;
				}
			}
		}.start();
	}
	
	public static void main(String[] args) {
		launch();
	}
	
	private void handleKeyPressed(KeyEvent event) {
		double angleChange = Math.PI / 12;
		double positionChange = 1;
		switch (event.getCode()) {
			case A -> viewAngles[1] -= angleChange;
			case D -> viewAngles[1] += angleChange;
			case W -> viewAngles[0] -= angleChange;
			case S -> viewAngles[0] += angleChange;
			case Q -> viewAngles[2] -= angleChange;
			case E -> viewAngles[2] += angleChange;
			case M -> camera_position[2] += positionChange;
			case N -> camera_position[2] -= positionChange;
			case LEFT -> camera_position[0] += positionChange;
			case RIGHT -> camera_position[0] -= positionChange;
			case DOWN -> camera_position[1] += positionChange;
			case UP -> camera_position[1] -= positionChange;
		}
	}
	
	private void handleRendering(GraphicsContext gc) {
		PixelWriter writer = gc.getPixelWriter();
		double[] rotationMatrix = getRotationMatrix();
		for (int x = -WIDTH / 2; x < WIDTH / 2; x++) {
			for (int y = -HEIGHT / 2; y < HEIGHT / 2; y++) {
				double[] direction = toViewport(x, y);
				direction = multiplyMatrixWithVector(rotationMatrix, direction);
				int recursionDepth = 2;
				int[] colorInBytes = clamp(raytracer.traceRay(camera_position, direction, 1.0d, Double.MAX_VALUE, recursionDepth));
				Color color = Color.rgb(colorInBytes[0], colorInBytes[1], colorInBytes[2]);
				colorPixel(writer, x, y, color);
			}
		}
	}
	
	private double[] toViewport(int x, int y) {
		double viewPortSize = 1.0d;
		int projectionPlane = 1;
		return new double[]{(x * (viewPortSize / HEIGHT)), (y * (viewPortSize / WIDTH)), projectionPlane};
	}
	
	private double[] getRotationMatrix() {
		double[] rotationCameraMatrixX = {1, 0, 0, 0, Math.cos(viewAngles[0]), -Math.sin(viewAngles[0]), 0, Math.sin(viewAngles[0]), Math.cos(viewAngles[0])};
		double[] rotationCameraMatrixY = {Math.cos(viewAngles[1]), 0, Math.sin(viewAngles[1]), 0, 1, 0, -Math.sin(viewAngles[1]), 0, Math.cos(viewAngles[1])};
		double[] rotationCameraMatrixZ = {Math.cos(viewAngles[2]), -Math.sin(viewAngles[2]), 0, Math.sin(viewAngles[2]), Math.cos(viewAngles[2]), 0, 0, 0, 1};
		
		return multiplyMatrices(multiplyMatrices(rotationCameraMatrixX, rotationCameraMatrixY), rotationCameraMatrixZ);
	}
	
	private void colorPixel(PixelWriter writer, int x, int y, Color color) {
		x = WIDTH / 2 + x;
		y = HEIGHT / 2 - y - 1;
		writer.setColor(x, y, color);
	}
	
	private int[] clamp(int[] v) {
		return new int[]{
				Math.min(255, Math.max(0, v[0])),
				Math.min(255, Math.max(0, v[1])),
				Math.min(255, Math.max(0, v[2]))
		};
	}
	
	private double[] multiplyMatrixWithVector(double[] m, double[] k) {
		double[] result = new double[3];
		for (int i = 0; i < 3; i++) {
			double sum = 0;
			for (int j = 0; j < 3; j++) {
				sum += m[i * 3 + j] * k[j];
			}
			result[i] = sum;
		}
		return result;
	}
	
	private double[] multiplyMatrices(double[] m, double[] n) {
		double[] result = new double[9];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				double sum = 0;
				for (int k = 0; k < 3; k++) {
					sum += m[i * 3 + k] * n[k * 3 + j];
				}
				result[i * 3 + j] = sum;
			}
		}
		return result;
	}
}