package com.mvargas.service_triage.controllers;

import com.mvargas.models.response.HourlyOperationReportRs;
import com.mvargas.service_triage.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/triage/reportes")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/operacion")
    public Flux<HourlyOperationReportRs> operationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta) {
        return reportService.operationReport(desde, hasta);
    }
}
