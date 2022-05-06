
package main;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author h4ck3r
 */
public class ThreadPoolClass {
    private static  ExecutorService objExecutor = null;
    
    private ThreadPoolClass(){}
    
    public static ExecutorService getThreadPool(){
        if (objExecutor == null) {
            objExecutor = Executors.newFixedThreadPool(5);
        }
        return objExecutor;
    }
    
    
}
