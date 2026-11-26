package com.mvargas.clinical_assistant.mappers;

import com.mvargas.clinical_assistant.models.entities.Patient;
import com.mvargas.clinical_assistant.models.request.PatientRq;
import com.mvargas.clinical_assistant.models.response.PatientRs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    @Mapping(target = "level", source = "slaLevel.level")
    @Mapping(target = "slaMinutes", source = "slaLevel.minutes")
    @Mapping(target = "status", source = "patientStatus")
    PatientRs toResponse(Patient product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slaLevel", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "patientStatus", ignore = true)
    Patient toEntity(PatientRq request);
}
