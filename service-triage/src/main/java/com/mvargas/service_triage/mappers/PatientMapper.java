package com.mvargas.service_triage.mappers;

import com.mvargas.models.entities.Patient;
import com.mvargas.models.request.PatientRq;
import com.mvargas.models.response.PatientRegisterRs;
import com.mvargas.models.response.PatientRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "level", source = "slaLevel.level")
    @Mapping(target = "slaMinutes", source = "slaLevel.minutes")
    @Mapping(target = "state", source = "patientStatus")
    PatientRegisterRs toResponse(Patient response);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slaLevel", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "patientStatus", ignore = true)
    @Mapping(target = "escalated", ignore = true)
    @Mapping(target = "escalatedDate", ignore = true)
    Patient toEntity(PatientRq request);
}
