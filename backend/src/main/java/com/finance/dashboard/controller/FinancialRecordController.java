package com.finance.dashboard.controller;

import com.finance.dashboard.dto.CreateRecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.dto.UpdateRecordRequest;
import com.finance.dashboard.model.RecordType;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.security.AccessControlService;
import com.finance.dashboard.service.FinancialRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;
    private final AccessControlService accessControlService;

    public FinancialRecordController(FinancialRecordService financialRecordService, AccessControlService accessControlService) {
        this.financialRecordService = financialRecordService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<RecordResponse> getRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) RecordType type
    ) {
        accessControlService.requireAnyRole(Role.VIEWER, Role.ANALYST, Role.ADMIN);
        return financialRecordService.listRecords(startDate, endDate, category, type);
    }

    @GetMapping("/{id}")
    public RecordResponse getRecord(@PathVariable Long id) {
        accessControlService.requireAnyRole(Role.VIEWER, Role.ANALYST, Role.ADMIN);
        return financialRecordService.getRecord(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecordResponse createRecord(@Valid @RequestBody CreateRecordRequest createRecordRequest) {
        accessControlService.requireAdmin();
        return financialRecordService.createRecord(createRecordRequest);
    }

    @PutMapping("/{id}")
    public RecordResponse updateRecord(@PathVariable Long id,
                                       @Valid @RequestBody UpdateRecordRequest updateRecordRequest) {
        accessControlService.requireAdmin();
        return financialRecordService.updateRecord(id, updateRecordRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@PathVariable Long id) {
        accessControlService.requireAdmin();
        financialRecordService.deleteRecord(id);
    }
}
