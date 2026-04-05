package com.finance.dashboard.service;

import com.finance.dashboard.dto.CreateRecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.dto.UpdateRecordRequest;
import com.finance.dashboard.exception.ApiException;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.specification.FinancialRecordSpecification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;

    public FinancialRecordService(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    public RecordResponse createRecord(CreateRecordRequest request) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.amount());
        record.setType(request.type());
        record.setCategory(request.category().trim());
        record.setTransactionDate(request.transactionDate());
        record.setNotes(cleanNotes(request.notes()));
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return toResponse(financialRecordRepository.save(record));
    }

    public List<RecordResponse> listRecords(java.time.LocalDate startDate, java.time.LocalDate endDate, String category,
                                            com.finance.dashboard.model.RecordType type) {
        return financialRecordRepository.findAll(
                        FinancialRecordSpecification.withFilters(startDate, endDate, category, type),
                        Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RecordResponse getRecord(Long id) {
        return financialRecordRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Record not found"));
    }

    public RecordResponse updateRecord(Long id, UpdateRecordRequest request) {
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Record not found"));

        record.setAmount(request.amount());
        record.setType(request.type());
        record.setCategory(request.category().trim());
        record.setTransactionDate(request.transactionDate());
        record.setNotes(cleanNotes(request.notes()));
        record.setUpdatedAt(LocalDateTime.now());
        return toResponse(financialRecordRepository.save(record));
    }

    public void deleteRecord(Long id) {
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Record not found"));
        financialRecordRepository.delete(record);
    }

    public List<RecordResponse> getRecentActivity() {
        return financialRecordRepository.findTop5ByOrderByTransactionDateDescCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String cleanNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }

    public RecordResponse toResponse(FinancialRecord record) {
        return new RecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getTransactionDate(),
                record.getNotes(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
