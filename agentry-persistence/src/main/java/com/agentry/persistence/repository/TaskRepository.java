package com.agentry.persistence.repository;

import com.agentry.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    List<TaskEntity> findAllByOrderByCreatedAtDesc();
}
