package com.hang.redis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * thread
 *
 * @author Hang W
 */
@SuppressWarnings("all")
public class ThreadPoolUtils {

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolUtils.class);

    private static int CORE_POOL_SIZE = 4;

    private static int MAX_POOL_SIZE = 8;

    private static int KEEP_ALIVE_TIME = 60;

    private static LinkedBlockingQueue<Runnable> LINKEDBLOCKINGQUEUE = new LinkedBlockingQueue<Runnable>();

    private static ExecutorService executorService;

    static {
        executorService = new ThreadPoolExecutor(CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                LINKEDBLOCKINGQUEUE);
    }

    public static void execute(Runnable runnable) {
        logger.info("task count: {}", LINKEDBLOCKINGQUEUE.size());
        executorService.execute(runnable);
    }

}
