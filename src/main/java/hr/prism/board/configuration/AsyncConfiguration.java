package hr.prism.board.configuration;

import com.google.common.base.Joiner;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import hr.prism.board.exception.BoardException;
import hr.prism.board.exception.ExceptionCode;

@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, Method method, Object... params) -> {
            String message = "Error calling method: " + method.getName() + " in class: " + method.getDeclaringClass()
                .getCanonicalName();
            if (ArrayUtils.isNotEmpty(params)) {
                message += " with parameters: " + Joiner.on(", ").join(params);
            }

            throw new BoardException(ExceptionCode.PROBLEM, message, throwable);
        };
    }

}
