package com.neutroware.ebaysyncserver.longjob;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LongJobRespository extends JpaRepository<LongJob, Long> {
    @Query("SELECT lj FROM LongJob lj WHERE lj.userId = :userId ORDER BY lj.createdDate DESC")
    List<LongJob> findByUserId(@Param("userId") String userId, Pageable pageable);
}
