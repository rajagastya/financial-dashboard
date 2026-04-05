package com.finance.dashboard.specification;

import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class FinancialRecordSpecification {

    private FinancialRecordSpecification() {
    }

    public static Specification<FinancialRecord> withFilters(LocalDate startDate, LocalDate endDate, String category, RecordType type) {
        return Specification.where(hasStartDate(startDate))
                .and(hasEndDate(endDate))
                .and(hasCategory(category))
                .and(hasType(type));
    }

    private static Specification<FinancialRecord> hasStartDate(LocalDate startDate) {
        return (root, query, builder) -> startDate == null
                ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.get("transactionDate"), startDate);
    }

    private static Specification<FinancialRecord> hasEndDate(LocalDate endDate) {
        return (root, query, builder) -> endDate == null
                ? builder.conjunction()
                : builder.lessThanOrEqualTo(root.get("transactionDate"), endDate);
    }

    private static Specification<FinancialRecord> hasCategory(String category) {
        return (root, query, builder) -> category == null || category.isBlank()
                ? builder.conjunction()
                : builder.equal(builder.lower(root.get("category")), category.trim().toLowerCase());
    }

    private static Specification<FinancialRecord> hasType(RecordType type) {
        return (root, query, builder) -> type == null
                ? builder.conjunction()
                : builder.equal(root.get("type"), type);
    }
}
