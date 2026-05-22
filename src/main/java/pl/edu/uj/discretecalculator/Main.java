package pl.edu.uj.discretecalculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import pl.edu.uj.discretecalculator.view.Theme;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/fxml/main_view.fxml")));
        Scene scene = new Scene(root);
        Theme.LIGHT.applyTo(scene);
        primaryStage.setTitle("DiscreteCalculator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}