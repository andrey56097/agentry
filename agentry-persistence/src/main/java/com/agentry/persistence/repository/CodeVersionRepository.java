package com.agentry.persistence.repository;

import com.agentry.persistence.entity.CodeVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeVersionRepository extends JpaRepository<CodeVersionEntity, UUID> {

    List<CodeVersionEntity> findByTaskIdOrderByRoundAsc(UUID taskId);
}
