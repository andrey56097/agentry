package com.agentry.app;

import com.agentry.cli.llm.AnthropicProvider;
import com.agentry.cli.service.PipelineOrchestrator;
import com.agentry.core.llm.ModelProvider;
import com.agentry.persistence.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration",
        "agentry.llm.api-key=test-key",
        "agentry.llm.model=claude-sonnet-4-20250514"
    }
)
@ActiveProfiles("test")
class AgentryApplicationTest {

    @MockBean
    private ModelProvider modelProvider;

    @MockBean
    private PipelineOrchestrator orchestrator;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
    }
}
