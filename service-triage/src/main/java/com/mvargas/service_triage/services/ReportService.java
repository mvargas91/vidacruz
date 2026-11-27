package com.mvargas.service_triage.services;

import com.mvargas.service_triage.models.response.HourlyOperationReportRs;
import com.mvargas.service_triage.repositories.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public Flux<HourlyOperationReportRs> operationReport(final OffsetDateTime fromDate,
                                                         final OffsetDateTime toDate) {
        if (!fromDate.isBefore(toDate)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "'desde' debe ser anterior a 'hasta'"));
        }
        return reportRepository.findHourlyReport(fromDate, toDate);
    }
}
