package com.finance.dashboard.dto;

import com.finance.dashboard.model.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRecordRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,
        @NotNull(message = "Type is required")
        RecordType type,
        @NotBlank(message = "Category is required")
        String category,
        @NotNull(message = "Date is required")
        LocalDate transactionDate,
        @Size(max = 500, message = "Notes must be at most 500 characters")
        String notes
) {
}
