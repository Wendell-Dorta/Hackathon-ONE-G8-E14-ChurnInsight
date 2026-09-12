package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.RetentionOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RetentionOutcomeRepository extends JpaRepository<RetentionOutcomeEntity, String> {
    Optional<RetentionOutcomeEntity> findByActionId(String actionId);
}
