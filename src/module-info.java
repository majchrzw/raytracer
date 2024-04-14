module pl.majchrzw.raytracerjava {
    requires javafx.controls;
    requires javafx.fxml;
	
    opens pl.majchrzw.raytracerjava to javafx.fxml;
	opens pl.majchrzw.raytracerjava.light to javafx.graphics;
	
	exports pl.majchrzw.raytracerjava to javafx.graphics;
}