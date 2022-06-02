import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PathFinder extends Application {
    private static final String SAVE_FILE_NAME = "europa.graph";
    private static final String IMAGE_FILE_NAME = "file:europa.gif";
    private ListGraph<CustomCircle> listGraph = new ListGraph<>();
    private Stage stage;
    private BorderPane root;
    private Pane centerPane = new Pane();
    private boolean unSavedChanges;
    private ImageView imageView;
    private CustomCircle circle1;
    private CustomCircle circle2;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        root = new BorderPane();
        centerPane.setId("outputArea");

        root.setTop(setRootTop());
        root.setCenter(centerPane);

        // ! ------------------ Stage ------------------
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(event -> {
            if (unSavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, continue anyway?");

                Optional<ButtonType> response = alert.showAndWait();

                if (response.isPresent() && response.get().equals(ButtonType.CANCEL))
                    event.consume();
            }
        });

        stage.setTitle("PathFinder");
        stage.show();
    }

    /*
     * ------------------ Top Bar ------------------
     */

    private VBox setRootTop() {
        VBox vbox = new VBox();

        // ! ------------------ Menu ------------------
        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu");
        Menu menu = new Menu("File");
        menu.setId("menuFile");

        MenuItem newMap = new MenuItem("New Map");
        newMap.setId("menuNewMap");
        newMap.setOnAction(event -> {
            if (unSavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, continue anyway?");

                Optional<ButtonType> response = alert.showAndWait();

                if (response.isPresent() && response.get().equals(ButtonType.CANCEL))
                    return;
            }

            unSavedChanges = true;
            root.setCenter(setRootCenter());
        });

        MenuItem open = new MenuItem("Open");
        open.setId("menuOpenFile");
        open.setOnAction(event -> {
            if (unSavedChanges) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, continue anyway?");

                Optional<ButtonType> response = alert.showAndWait();

                if (response.isPresent() && response.get().equals(ButtonType.CANCEL))
                    return;
            }

            unSavedChanges = true;
            root.setCenter(setRootCenter());
            loadSavedGraph();
        });

        MenuItem save = new MenuItem("Save");
        save.setId("menuSaveFile");
        save.setOnAction(event -> {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE_NAME));

                // ! ------------------ Write file name on 1st line ------------------
                writer.write(String.format("file:%s", "europa.gif"));
                writer.newLine();

                // ! ------------------ Write all nodes on 2nd line ------------------
                ArrayList<CustomCircle> nodes = new ArrayList<>(listGraph.getNodes());

                for (int i = 0; i < nodes.size(); i++) {
                    if (i == nodes.size() - 1)
                        writer.write(
                                String.format("%s;%s;%s", nodes.get(i).getName(), Double.toString(nodes.get(i)
                                        .getxPos()),
                                        Double.toString(nodes.get(i).getyPos())));
                    else
                        writer.write(
                                String.format(
                                        "%s;%s;%s;", nodes.get(i).getName(), Double.toString(nodes.get(i)
                                                .getxPos()),
                                        Double.toString(nodes.get(i).getyPos())));
                }
                writer.newLine();

                // ! ------------------ Write all connections from 3rd line ------------------
                for (CustomCircle circle : nodes) {
                    for (Edge<CustomCircle> edge : listGraph.getEdgesFrom(circle)) {
                        writer.write(String.format("%s;%s;%s;%d",
                                circle.getName(), edge.getDestination(), edge.getName(),
                                edge.getWeight()));
                        writer.newLine();
                    }
                }

                writer.close();
                unSavedChanges = false;
            } catch (IOException e) {
            }
        });

        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setId("menuSaveImage");
        saveImage.setOnAction(event -> {
            try {
                WritableImage image = root.getCenter().snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, String.format("IO-fel, %s",
                        e.getMessage()));
            }
        });

        MenuItem exit = new MenuItem("Exit");
        exit.setId("menuExit");
        exit.setOnAction(event -> {
            if (unSavedChanges) {
                Alert alert = new Alert(AlertType.CONFIRMATION);

                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, continue anyway?");

                Optional<ButtonType> response = alert.showAndWait();

                if (response.isPresent() && response.get().equals(ButtonType.CANCEL))
                    return;
            }

            stage.close();
        });

        menu.getItems().addAll(newMap, open, save, saveImage, exit);
        menuBar.getMenus().add(menu);

        // ! ------------------ Buttons ------------------
        FlowPane flowPane = new FlowPane();

        Button findPath = new Button("Find Path");
        findPath.setId("btnFindPath");
        findPath.setOnAction(event -> {
            if (circle1 == null || circle2 == null) {
                errorAlert("Two places must be selected!");
                return;
            }

            List<Edge<CustomCircle>> path = listGraph.getPath(circle1, circle2);

            if (path == null) {
                errorAlert("No path available!");
                return;
            }

            FindPathAlert alert = new FindPathAlert();
            int total = 0;

            for (Edge<CustomCircle> edge : path) {
                alert.addText(edge.toString());
                total += edge.getWeight();
            }

            alert.addText(String.format("Total: %d", total));

            alert.showAndWait();
        });

        Button showConnection = new Button("Show Connection");
        showConnection.setId("btnShowConnection");
        showConnection.setOnAction(event -> {
            if (circle1 == null || circle2 == null) {
                errorAlert("Two places must be selected!");
                return;
            }

            if (listGraph.getEdgeBetween(circle1, circle2) == null) {
                errorAlert("There is no connection between the nodes!");
                return;
            }

            new ShowConnectionAlert().showAndWait();
        });

        Button newPlace = new Button("New Place");
        newPlace.setId("btnNewPlace");
        newPlace.setOnAction(event -> {
            if (centerPane == null) {
                errorAlert("No map available!");
                return;
            }

            root.setCursor(Cursor.CROSSHAIR);
            newPlace.setDisable(true);

            centerPane.setOnMouseClicked(e -> {
                NewPlaceAlert alert = new NewPlaceAlert();

                Optional<ButtonType> response = alert.showAndWait();

                if (response.isPresent() && response.get().equals(ButtonType.CANCEL)) {
                    root.setCursor(Cursor.DEFAULT);
                    newPlace.setDisable(false);
                    return;
                }

                double valueX = e.getX();
                double valueY = e.getY();

                // Node node = new Node(alert.getNameOfPlace(), valueX, valueY);

                CustomCircle circle = new CustomCircle(valueX, valueY, alert.getNameOfPlace());
                circle.setId(circle.getName());
                circle.setOnMouseClicked(new CircleHandler());

                addCircle(circle);
                listGraph.add(circle);

                centerPane.setOnMouseClicked(null);
                root.setCursor(Cursor.DEFAULT);
                newPlace.setDisable(false);
                unSavedChanges = true;
            });
        });

        Button newConnection = new Button("New Connection");
        newConnection.setId("btnNewConnection");
        newConnection.setOnAction(event -> {
            if (circle1 == null || circle2 == null) {
                errorAlert("Two places must be selected!");
                return;
            }

            try {
                if (listGraph.getEdgeBetween(circle1, circle2) != null) {
                    errorAlert("There can only be one edge between nodes!");
                    return;
                }

                NewConnectionAlert alert = new NewConnectionAlert();
                Optional<ButtonType> response = alert.showAndWait();

                if (response.isPresent() && response.get().equals(ButtonType.CANCEL))
                    return;

                String name = alert.getName();
                int time = alert.getTime();

                listGraph.connect(circle1, circle2, name, time);
                CustomLine line = new CustomLine(circle1, circle2);

                centerPane.getChildren().add(line);
                unSavedChanges = true;
            } catch (NoSuchElementException e) {
            }
        });

        Button changeConnection = new Button("Change Connection");
        changeConnection.setId("btnChangeConnection");
        changeConnection.setOnAction(event -> {
            if (circle1 == null || circle2 == null) {
                errorAlert("Two places must be selected!");
                return;
            }

            if (listGraph.getEdgeBetween(circle1, circle2) == null) {
                errorAlert("There is no edge between the selected nodes!");
                return;
            }

            ChangeConnectionAlert alert = new ChangeConnectionAlert();

            Optional<ButtonType> response = alert.showAndWait();

            if (response.isPresent() && response.get().equals(ButtonType.CANCEL)) {
                return;
            }

            int time = alert.getTime();
            listGraph.setConnectionWeight(circle1, circle2, time);

            unSavedChanges = true;
        });

        flowPane.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);
        flowPane.alignmentProperty().setValue(Pos.CENTER);
        flowPane.setHgap(10);

        vbox.getChildren().addAll(menuBar, flowPane);
        return vbox;
    }

    /*
     * ------------------ Functions ------------------
     */

    private Pane setRootCenter() {
        centerPane.getChildren().clear();

        listGraph = new ListGraph<>();
        circle1 = null;
        circle2 = null;

        imageView = new ImageView(IMAGE_FILE_NAME);

        centerPane.getChildren().add(imageView);

        stage.sizeToScene();
        stage.centerOnScreen();

        return centerPane;
    }

    private void loadSavedGraph() {
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(SAVE_FILE_NAME));

            String in;
            HashMap<String, CustomCircle> nodeMap = new HashMap<>();

            // ! Image
            in = reader.readLine();
            imageView.setImage(new Image(in));
            stage.sizeToScene();

            // ! Nodes
            in = reader.readLine();
            String[] stringArray = in.split(";");
            for (int i = 0; i < stringArray.length; i += 3) {
                double positionX = Double.parseDouble(
                        stringArray[i + 1]);
                double positionY = Double.parseDouble(
                        stringArray[i + 2]);

                // ! ------------------ Adding the circles to the graph ------------------
                CustomCircle circle = new CustomCircle(positionX, positionY, stringArray[i]);
                circle.setOnMouseClicked(new CircleHandler());
                addCircle(circle);

                listGraph.add(circle);
                nodeMap.put(stringArray[i], circle);
            }

            while ((in = reader.readLine()) != null) {
                stringArray = in.split(";");

                CustomCircle c1 = nodeMap.get(stringArray[0]);
                CustomCircle c2 = nodeMap.get(stringArray[1]);

                try {
                    listGraph.connect(
                            c1, c2,
                            stringArray[2],
                            Integer.parseInt(stringArray[3]));

                    // ! ------------------ Adding the lines to the graph ------------------
                    CustomLine line = new CustomLine(c1, c2);

                    addLine(line);

                } catch (IllegalStateException e) {
                }
            }

            reader.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void addCircle(CustomCircle circle) {
        centerPane.getChildren().add(circle);
    }

    private void addLine(CustomLine line) {
        centerPane.getChildren().add(line);
    }

    private void errorAlert(String msg) {
        Alert alert = new Alert(AlertType.ERROR);

        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /*
     * ------------------ Classes (Handlers) ------------------
     */

    class CircleHandler implements EventHandler<MouseEvent> {
        public void handle(MouseEvent event) {
            CustomCircle c = (CustomCircle) event.getSource();

            if (circle1 == c) {
                circle1 = null;
                c.setFill(Color.BLUE);
                return;
            } else if (circle2 == c) {
                circle2 = null;
                c.setFill(Color.BLUE);
                return;
            }

            if (circle1 == null) {
                circle1 = c;
                c.setFill(Color.RED);
            } else if (circle2 == null && circle1 != c) {
                circle2 = c;
                c.setFill(Color.RED);
            }
        }
    }

    /*
     * ------------------ Classes (Alerts) ------------------
     */

    class FindPathAlert extends Alert {
        private TextArea textArea = new TextArea();

        FindPathAlert() {
            super(AlertType.INFORMATION);

            GridPane grid = new GridPane();

            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(10);
            grid.setVgap(10);

            grid.addRow(0, textArea);

            textArea.setEditable(false);

            setHeaderText(String.format("The path from %s to %s:", circle1.getName(),
                    circle2.getName()));
            getDialogPane().setContent(grid);
        }

        public void addText(String text) {
            textArea.setText(String.format("%s\n%s", textArea.getText(), text));
        }
    }

    class ChangeConnectionAlert extends Alert {
        private TextField nameField = new TextField();
        private TextField timeField = new TextField();

        ChangeConnectionAlert() {
            super(AlertType.CONFIRMATION);
            GridPane grid = new GridPane();

            CustomCircle n1 = null;
            CustomCircle n2 = null;

            for (CustomCircle circle : listGraph.getNodes()) {
                if (circle.equals(circle1)) {
                    n1 = circle;
                } else if (circle.equals(circle2)) {
                    n2 = circle;
                }
            }

            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(10);
            grid.setVgap(10);

            grid.addRow(0, new Label("Name:"), nameField);
            grid.addRow(1, new Label("Time:"), timeField);

            nameField.setText(listGraph.getEdgeBetween(n1, n2).getName());
            nameField.setEditable(false);

            setHeaderText(String.format("Connection from %s to %s", n1.getName(), n2.getName()));
            getDialogPane().setContent(grid);
        }

        public int getTime() {
            return Integer.parseInt(timeField.getText());
        }
    }

    class ShowConnectionAlert extends Alert {
        private TextField nameField = new TextField();
        private TextField timeField = new TextField();

        ShowConnectionAlert() {
            super(AlertType.CONFIRMATION);
            GridPane grid = new GridPane();

            String nameFrom = null;
            String nameTo = null;

            for (CustomCircle node : listGraph.getNodes()) {
                if (node.equals(circle1)) {
                    nameFrom = node.getName();
                } else if (node.equals(circle2)) {
                    nameTo = node.getName();
                }
            }

            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(10);
            grid.setVgap(10);

            grid.addRow(0, new Label("Name:"), nameField);
            grid.addRow(1, new Label("Time:"), timeField);

            nameField.setText(listGraph.getEdgeBetween(circle1, circle2).getName());
            timeField.setText(Integer.toString(
                    listGraph.getEdgeBetween(circle1, circle2).getWeight()));

            nameField.setEditable(false);
            timeField.setEditable(false);

            setHeaderText(String.format("Connection from %s to %s", nameFrom, nameTo));

            getDialogPane().setContent(grid);
        }
    }

    class NewConnectionAlert extends Alert {
        private TextField nameField = new TextField();
        private TextField timeField = new TextField();

        NewConnectionAlert() {
            super(AlertType.CONFIRMATION);
            GridPane grid = new GridPane();

            String nameFrom = null;
            String nameTo = null;

            for (CustomCircle node : listGraph.getNodes()) {
                if (node.equals(circle1)) {
                    nameFrom = node.getName();
                } else if (node.equals(circle2)) {
                    nameTo = node.getName();
                }
            }

            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(10);
            grid.setVgap(10);

            grid.addRow(0, new Label("Name:"), nameField);
            grid.addRow(1, new Label("Time:"), timeField);

            setHeaderText(String.format("Connection from %s to %s", nameFrom, nameTo));

            getDialogPane().setContent(grid);
        }

        public String getName() {
            return nameField.getText();
        }

        public int getTime() {
            return Integer.parseInt(timeField.getText());
        }
    }

    class NewPlaceAlert extends Alert {
        private TextField nameOfPlace = new TextField();

        NewPlaceAlert() {
            super(AlertType.CONFIRMATION);
            GridPane grid = new GridPane();

            setHeaderText(null);

            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(10);

            grid.addRow(0, new Label("Name of place:"), nameOfPlace);

            getDialogPane().setContent(grid);
        }

        public String getName() {
            return nameOfPlace.getText();
        }

        public String getNameOfPlace() {
            return nameOfPlace.getText();
        }
    }
}
