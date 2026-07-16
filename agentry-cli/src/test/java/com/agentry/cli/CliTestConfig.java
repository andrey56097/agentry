package com.agentry.cli;

import com.agentry.cli.llm.ProviderRegistry;
import com.agentry.core.llm.ModelProvider;
import com.agentry.core.llm.ProviderType;
import com.agentry.persistence.repository.TaskRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class CliTestConfig {

    @Bean
    public TaskRepository taskRepository() {
        return mock(TaskRepository.class);
    }

    @Bean
    public ModelProvider mockModelProvider() {
        var mock = mock(ModelProvider.class);
        return mock;
    }

    @Bean
    public ProviderRegistry providerRegistry() {
        return new ProviderRegistry(java.util.List.of(mockModelProvider()));
    }
}
