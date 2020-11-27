package com.zjyz.designer.controller.compent;

import com.sun.javafx.application.PlatformImpl;
import com.zjyz.designer.helper.loading.ProgressStage;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Tab;

import java.util.concurrent.CountDownLatch;

/**
 * 带有加载功能的Tab页
 */
public class LoadingTab extends Tab {
    private Tab tab;
    private CountDownLatch countDownLatch;
    private Node node;

    public LoadingTab(String tabName, Node node) {
        tab = new Tab(tabName);
        countDownLatch = new CountDownLatch(1);
        this.node = node;
    }


    public void sleep() {
        ProgressStage.of(
                tab,
                new UnitTab(countDownLatch),
                "正在加载，请稍候..."
        ).show();
    }

    public void over() {
        countDownLatch.countDown();
    }

    /**
     * 单个tab也的异步绘制
     */
    class UnitTab extends Task<Void> {
        CountDownLatch countDownLatch;

        UnitTab(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        protected Void call() throws Exception {
            countDownLatch.await();
            PlatformImpl.runLater(() -> tab.setContent(node));
            return null;
        }
    }

    public Tab getTab() {
        return tab;
    }

}
