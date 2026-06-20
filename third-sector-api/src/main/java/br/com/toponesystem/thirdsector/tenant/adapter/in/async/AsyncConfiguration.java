package br.com.toponesystem.thirdsector.tenant.adapter.in.async;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
class AsyncConfiguration implements AsyncConfigurer {

    private final TenantContextPropagationTaskDecorator taskDecorator;

    @Override
    public Executor getAsyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(taskDecorator);
        executor.setThreadNamePrefix("async-tenant-");
        executor.initialize();
        return executor;
    }
}
