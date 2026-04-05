package com.finance.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record SummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        List<CategoryTotal> categoryTotals,
        List<TrendPoint> monthlyTrends,
        List<RecordResponse> recentActivity
) {

    public record CategoryTotal(String category, BigDecimal total) {
    }

    public record TrendPoint(String period, BigDecimal income, BigDecimal expense) {
    }
}
