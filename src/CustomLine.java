import javafx.scene.shape.Line;

public class CustomLine extends Line {
    public CustomLine(CustomCircle circle1, CustomCircle circle2) {
        super(circle1.getCenterX(), circle1.getCenterY(), circle2.getCenterX(),
                circle2.getCenterY());

        strokeWidthProperty().set(3);
        setDisable(true);
    }
}