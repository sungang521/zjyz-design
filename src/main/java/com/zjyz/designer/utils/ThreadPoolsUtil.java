package com.zjyz.designer.utils;



import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具类
 */
public class ThreadPoolsUtil {

    private final static int corePoolSize=4;
    private final static int maximumPoolSize=8;
    private final static int maxPoolsSize=32;
    private static final ThreadPoolExecutor threadPool=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(maxPoolsSize));

    /**
     * 提交任务到队列中,无返回值
     * @param runnable
     */
    public  static void execute(Runnable runnable){
        queueAwait();
        threadPool.execute(runnable);
    }
    /**
     * 提交任务到队列中,有返回值
     * @param runnable
     */
    public  static  Future submit(Runnable runnable){
        queueAwait();
        return threadPool.submit(runnable);
    }

    /**
     * 防止队列满了的处理机制,满了就歇一歇
     */
    public static void queueAwait(){
        while(true){
            if(threadPool.getPoolSize()==threadPool.getMaximumPoolSize()&&threadPool.getQueue().size()==maxPoolsSize){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                break;
            }
        }


    }
}
