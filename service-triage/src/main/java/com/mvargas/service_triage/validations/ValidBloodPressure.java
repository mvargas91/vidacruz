package com.mvargas.service_triage.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BloodPressureValidator.class)
public @interface ValidBloodPressure {
    String message() default "Diastolic pressure must be lower than systolic pressure.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
