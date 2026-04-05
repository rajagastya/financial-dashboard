package com.finance.dashboard.repository;

import com.finance.dashboard.model.FinancialRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    List<FinancialRecord> findTop5ByOrderByTransactionDateDescCreatedAtDesc();

    List<FinancialRecord> findByTransactionDateBetweenOrderByTransactionDateAsc(LocalDate startDate, LocalDate endDate);
}
