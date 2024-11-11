package moonlit.chill.ownpay.async;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Gawaind
 * @date 2024/11/11 13:45
 */
@Slf4j
public class AsyncFactoryManager {

    /**
     * 异步操作任务调度线程池
     */
    private final ScheduledExecutorService executor = SpringUtil.getBean("scheduledExecutorService");

    private AsyncFactoryManager() {
    }

    private static final AsyncFactoryManager me = new AsyncFactoryManager();

    public static AsyncFactoryManager me() {
        return me;
    }

    public void execute(TimerTask task) {
        //操作延迟10毫秒
        int operateDelayTime = 10;
        executor.schedule(task, operateDelayTime, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                        log.info("线程池未终止");
                    }
                }
            } catch (InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
