package com.finance.dashboard.dto;

import com.finance.dashboard.model.RecordType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecordResponse(
        Long id,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate transactionDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
