import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    
    private Parent main;
    private double xOffset, yOffset;
    private Scene scene;
    private FXMLLoader loader;

    @Override
    public void start(Stage primaryStage) throws Exception {

        try {
            loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("Views/Main.fxml"));

            main = (Parent) loader.load();
            
            scene = new Scene(main);

            primaryStage.initStyle(StageStyle.TRANSPARENT);

            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add("Views/application.css");

            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(App.class.getResourceAsStream( "sme.png" )));
            primaryStage.setTitle("SME");
            primaryStage.show();

            // Make the window draggable
            main.setOnMousePressed(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });

            main.setOnMouseDragged(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);
                }
            });

            

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        launch(args);
    }
}
