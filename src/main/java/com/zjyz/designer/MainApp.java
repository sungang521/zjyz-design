package com.zjyz.designer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


/**
 * @Author libingsi
 * @Description 启动类
 * @Date 2020/11/13 10:35
 * @Version 1.0
 **/
@SpringBootApplication
public class MainApp extends Application {

    private static ApplicationContext applicationContext;


    public static FXMLLoader loadFxml(String fxmlPath){
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(MainApp.class.getResource(fxmlPath));
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        return fxmlLoader;
    }
    public static void main(String[] args) {
        applicationContext = SpringApplication.run(MainApp.class, args);
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root = loadFxml("/fxml/main.fxml").load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("/style/power.png"));
        primaryStage.setTitle("PowerDesigner Client");
        primaryStage.show();
    }


}
