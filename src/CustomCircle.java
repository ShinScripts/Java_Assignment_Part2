import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CustomCircle extends Circle {
    private final String name;

    public CustomCircle(double x, double y, String name) {
        super(x, y, 15);
        setFill(Color.BLUE);
        this.name = name;
        setId(name);
    }

    public String getName() {
        return name;
    }

    public double getxPos() {
        return super.getCenterX();
    }

    public double getyPos() {
        return super.getCenterY();
    }

    @Override
    public String toString() {
        return name;
    }
}
