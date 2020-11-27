package com.zjyz.designer.helper.loading;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Objects;

public class ProgressStage {

    private Task<?> work;

    private ProgressStage() {
    }

    /**
     * 创建
     *
     * @param tab
     * @param work
     * @param ad
     * @return
     */
    public static ProgressStage of(Tab tab, Task<?> work, String ad) {
        ProgressStage ps = new ProgressStage();
        ps.work = Objects.requireNonNull(work);
        ps.initUI(tab, ad);
        return ps;
    }

    /**
     * 显示
     */
    public void show() {
        new Thread(work).start();
    }

    private void initUI(Tab tab, String ad) {
        // message
        Label adLbl = new Label(ad);
        adLbl.setTextFill(Color.BLUE);

        // progress
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setProgress(-1);
        indicator.progressProperty().bind(work.progressProperty());

        // pack
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setBackground(Background.EMPTY);
        vBox.getChildren().addAll(indicator, adLbl);
        //设定位置

        // close if work finish
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tab.setContent(vBox);
                work.setOnSucceeded(e -> vBox.setVisible(false));
            }
        });

    }
}
