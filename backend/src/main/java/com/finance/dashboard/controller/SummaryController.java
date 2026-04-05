package com.finance.dashboard.controller;

import com.finance.dashboard.dto.SummaryResponse;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.security.AccessControlService;
import com.finance.dashboard.service.SummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class SummaryController {

    private final SummaryService summaryService;
    private final AccessControlService accessControlService;

    public SummaryController(SummaryService summaryService, AccessControlService accessControlService) {
        this.summaryService = summaryService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/summary")
    public SummaryResponse getSummary() {
        accessControlService.requireAnyRole(Role.VIEWER, Role.ANALYST, Role.ADMIN);
        return summaryService.getDashboardSummary();
    }
}
