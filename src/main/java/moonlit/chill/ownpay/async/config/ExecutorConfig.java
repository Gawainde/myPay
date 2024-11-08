package moonlit.chill.ownpay.async.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 查询线程池
 * @author Gawaind
 * @date 2024/11/8 16:33
 */
@Slf4j
@EnableAsync
@Configuration
public class ExecutorConfig {

    private static final int QUEUE_CAPACITY = 100;
    private static final int KEEP_ALIVE_TIME = 60;

    @Bean(name = "asyncServiceExecutor")
    public Executor asyncServiceExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int threadCount = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(threadCount * 2);
        executor.setMaxPoolSize(threadCount * 2 + 1);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_TIME);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setThreadNamePrefix("pay-");
        executor.initialize();
        return executor;
    }
}
