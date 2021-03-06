package hr.prism.board.configuration;

import com.google.common.base.Joiner;
import hr.prism.board.exception.BoardException;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;

import static hr.prism.board.exception.ExceptionCode.UNKNOWN;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean
    @Override
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, Method method, Object... params) -> {
            String message = "Error calling method: " + method.getName() + " in class: " + method.getDeclaringClass()
                .getCanonicalName();
            if (isNotEmpty(params)) {
                message += " with parameters: " + Joiner.on(", ").join(params);
            }

            throw new BoardException(UNKNOWN, message, throwable);
        };
    }

}
