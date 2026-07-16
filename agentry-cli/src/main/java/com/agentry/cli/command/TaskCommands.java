package com.agentry.cli.command;

import com.agentry.cli.service.PipelineOrchestrator;
import com.agentry.core.model.TaskStatus;
import com.agentry.persistence.entity.TaskEntity;
import com.agentry.persistence.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@ShellComponent
public class TaskCommands {

    private static final Logger log = LoggerFactory.getLogger(TaskCommands.class);

    private final PipelineOrchestrator orchestrator;
    private final TaskRepository taskRepository;

    public TaskCommands(PipelineOrchestrator orchestrator, TaskRepository taskRepository) {
        this.orchestrator = orchestrator;
        this.taskRepository = taskRepository;
    }

    @ShellMethod(key = "create-task", value = "Create and execute a new task")
    public String createTask(
            @ShellOption(value = "--description", help = "Task description") String description,
            @ShellOption(value = "--budget", defaultValue = "4000", help = "Token budget limit") int budgetLimit
    ) {
        log.info("Starting task: {} (budget: {} tokens)", description, budgetLimit);
        long start = System.currentTimeMillis();

        TaskEntity task = orchestrator.executeTask(description, budgetLimit);

        long elapsed = System.currentTimeMillis() - start;
        return String.format("""
                ✅ Task completed!
                   ID: %s
                   Status: %s
                   Budget: %d/%d tokens
                   Score: %s
                   Time: %ds
                """,
                task.getId(), task.getStatus(),
                task.getBudgetSpent(), task.getBudgetLimit(),
                task.getQualityGateScore() != null ? task.getQualityGateScore() : "N/A",
                elapsed / 1000
        );
    }

    @ShellMethod(key = "list-tasks", value = "List all tasks")
    public String listTasks() {
        List<TaskEntity> tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        if (tasks.isEmpty()) {
            return "No tasks found.";
        }

        StringBuilder sb = new StringBuilder("Tasks:\n");
        sb.append(String.format("%-38s %-15s %-10s %-8s %s\n",
                "ID", "Status", "Score", "Budget", "Created"));
        sb.append("-".repeat(90)).append("\n");

        for (TaskEntity task : tasks) {
            sb.append(String.format("%-38s %-15s %-10s %-8s %s\n",
                    task.getId(),
                    task.getStatus(),
                    task.getQualityGateScore() != null ? task.getQualityGateScore() : "-",
                    task.getBudgetSpent() + "/" + task.getBudgetLimit(),
                    formatDuration(task.getCreatedAt())
            ));
        }
        return sb.toString();
    }

    @ShellMethod(key = "task-status", value = "Show status of a specific task")
    public String taskStatus(
            @ShellOption(help = "Task ID") String taskId
    ) {
        var task = taskRepository.findById(java.util.UUID.fromString(taskId));
        if (task.isEmpty()) {
            return "Task not found: " + taskId;
        }
        var t = task.get();
        return String.format("""
                Task: %s
                Description: %s
                Status: %s
                Budget: %d/%d
                Score: %s
                Created: %s
                """,
                t.getId(), t.getDescription(), t.getStatus(),
                t.getBudgetSpent(), t.getBudgetLimit(),
                t.getQualityGateScore() != null ? t.getQualityGateScore() : "N/A",
                t.getCreatedAt()
        );
    }

    private String formatDuration(Instant instant) {
        Duration duration = Duration.between(instant, Instant.now());
        if (duration.toMinutes() < 1) return "just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + "m ago";
        if (duration.toHours() < 24) return duration.toHours() + "h ago";
        return duration.toDays() + "d ago";
    }
}
