package com.mvargas.service_triage.validations;

import com.mvargas.models.request.PatientRq;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BloodPressureValidator implements ConstraintValidator<ValidBloodPressure, PatientRq> {

    @Override
    public boolean isValid(final PatientRq request, final ConstraintValidatorContext context) {
        if (request == null || request.getSystolicPressure() == null || request.getDiastolicPressure() == null) {
            return true;
        }

        boolean isValid = request.getDiastolicPressure() < request.getSystolicPressure();

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("diastolicPressure")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
