package game;

import game.checkers.Unit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (Unit.OPEN) {
            Parent root = FXMLLoader.load(getClass().getResource("interface.fxml"));
            primaryStage.setTitle("Checkers Game");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        }
    }
}
