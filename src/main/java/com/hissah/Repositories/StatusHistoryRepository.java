package com.hissah.Repositories;

import com.hissah.Entities.StatusHistory;
import com.hissah.Enums.HistoryEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByEntityTypeAndEntityIdOrderByChangedAtDesc(
            HistoryEntityType entityType, Long entityId);
    List<StatusHistory> findTop20ByOrderByChangedAtDesc();
}
