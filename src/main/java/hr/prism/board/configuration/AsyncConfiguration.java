package hr.prism.board.configuration;

import com.google.common.base.Joiner;
import hr.prism.board.exception.BoardException;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static hr.prism.board.exception.ExceptionCode.PROBLEM;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    private final Executor asyncExecutor;

    @Inject
    public AsyncConfiguration(Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable throwable, Method method, Object... params) -> {
            String message = "Error calling method: " + method.getName() + " in class: " + method.getDeclaringClass()
                .getCanonicalName();
            if (isNotEmpty(params)) {
                message += " with parameters: " + Joiner.on(", ").join(params);
            }

            throw new BoardException(PROBLEM, message, throwable);
        };
    }

}
