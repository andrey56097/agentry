package com.agentry.cli.command;

import com.agentry.cli.service.PipelineOrchestrator;
import com.agentry.core.model.TaskStatus;
import com.agentry.persistence.entity.TaskEntity;
import com.agentry.persistence.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCommandsTest {

    @Mock
    private PipelineOrchestrator orchestrator;
    @Mock
    private TaskRepository taskRepository;

    private TaskCommands commands;

    @BeforeEach
    void setUp() {
        commands = new TaskCommands(orchestrator, taskRepository);
    }

    @Test
    void shouldCreateTask() {
        UUID taskId = UUID.randomUUID();
        var task = new TaskEntity(taskId, "test task", TaskStatus.DONE, 1000, 500, Instant.now());
        task.setQualityGateScore(80);
        when(orchestrator.executeTask(anyString(), anyInt())).thenReturn(task);

        String result = commands.createTask("test task", 1000);

        assertTrue(result.contains(taskId.toString()));
        assertTrue(result.contains("DONE"));
        assertTrue(result.contains("500/1000"));
        assertTrue(result.contains("80"));
    }

    @Test
    void shouldListTasks() {
        var task1 = new TaskEntity(UUID.randomUUID(), "task 1", TaskStatus.DONE, 1000, 500, Instant.now());
        task1.setQualityGateScore(85);
        var task2 = new TaskEntity(UUID.randomUUID(), "task 2", TaskStatus.IN_PROGRESS, 2000, 300, Instant.now());
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(task1, task2));

        String result = commands.listTasks();

        assertTrue(result.contains("Tasks:"));
        assertTrue(result.contains(task1.getId().toString()));
        assertTrue(result.contains(task2.getId().toString()));
    }

    @Test
    void shouldShowEmptyList() {
        when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        String result = commands.listTasks();

        assertEquals("No tasks found.", result);
    }

    @Test
    void shouldShowTaskStatus() {
        UUID taskId = UUID.randomUUID();
        var task = new TaskEntity(taskId, "test task", TaskStatus.DONE, 1000, 500, Instant.now());
        task.setQualityGateScore(90);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        String result = commands.taskStatus(taskId.toString());

        assertTrue(result.contains(taskId.toString()));
        assertTrue(result.contains("test task"));
        assertTrue(result.contains("DONE"));
        assertTrue(result.contains("500/1000"));
    }

    @Test
    void shouldShowNotFoundForInvalidTask() {
        String result = commands.taskStatus(UUID.randomUUID().toString());
        assertTrue(result.startsWith("Task not found"));
    }
}
