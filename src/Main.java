import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Afacad-Regular.ttf"), 14);

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/customWindow.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style-light.css").toExternalForm());

        primaryStage.initStyle(StageStyle.UNDECORATED); // 💡 custom title bar
        primaryStage.setTitle("Library Management System");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/logo.jpeg")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}