package com.mvargas.service_triage.mappers;

import com.mvargas.service_triage.models.request.PatientRq;
import com.mvargas.service_triage.models.response.PatientRs;
import com.mvargas.service_triage.models.entities.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PatientMapper {

    PatientRs toResponse(Patient product);

    @Mapping(target = "id", ignore = true)
    Patient toEntity(PatientRq request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(PatientRq request, @MappingTarget Patient product);
}
