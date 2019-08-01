package sample;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        CefWebview authWebview = new CefWebview();
        authWebview.start(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
