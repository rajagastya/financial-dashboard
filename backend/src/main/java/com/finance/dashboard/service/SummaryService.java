package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.dto.SummaryResponse;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SummaryService {

    private final FinancialRecordRepository financialRecordRepository;
    private final FinancialRecordService financialRecordService;

    public SummaryService(FinancialRecordRepository financialRecordRepository, FinancialRecordService financialRecordService) {
        this.financialRecordRepository = financialRecordRepository;
        this.financialRecordService = financialRecordService;
    }

    public SummaryResponse getDashboardSummary() {
        List<FinancialRecord> records = financialRecordRepository.findAll();

        BigDecimal totalIncome = sumByType(records, RecordType.INCOME);
        BigDecimal totalExpenses = sumByType(records, RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        List<SummaryResponse.CategoryTotal> categoryTotals = buildCategoryTotals(records);
        List<SummaryResponse.TrendPoint> trends = buildMonthlyTrends(records);
        List<RecordResponse> recentActivity = financialRecordService.getRecentActivity();

        return new SummaryResponse(totalIncome, totalExpenses, netBalance, categoryTotals, trends, recentActivity);
    }

    private BigDecimal sumByType(List<FinancialRecord> records, RecordType type) {
        return records.stream()
                .filter(record -> record.getType() == type)
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<SummaryResponse.CategoryTotal> buildCategoryTotals(List<FinancialRecord> records) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        records.stream()
                .filter(record -> record.getType() == RecordType.EXPENSE)
                .forEach(record -> totals.merge(record.getCategory(), record.getAmount(), BigDecimal::add));

        return totals.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> new SummaryResponse.CategoryTotal(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<SummaryResponse.TrendPoint> buildMonthlyTrends(List<FinancialRecord> records) {
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<SummaryResponse.TrendPoint> trends = new ArrayList<>();

        for (int offset = 5; offset >= 0; offset--) {
            YearMonth targetMonth = currentMonth.minusMonths(offset);
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;

            for (FinancialRecord record : records) {
                if (YearMonth.from(record.getTransactionDate()).equals(targetMonth)) {
                    if (record.getType() == RecordType.INCOME) {
                        income = income.add(record.getAmount());
                    } else {
                        expense = expense.add(record.getAmount());
                    }
                }
            }

            trends.add(new SummaryResponse.TrendPoint(targetMonth.format(formatter), income, expense));
        }

        return trends;
    }
}
