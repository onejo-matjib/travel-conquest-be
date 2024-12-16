package com.sparta.travelconquestbe.common.annotation;

import com.sparta.travelconquestbe.common.validator.KoreaLatitudeLongitudeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = KoreaLatitudeLongitudeValidator.class)
public @interface ValidInKorea {
  String message() default "유효하지 않은 대한민국 내 좌표입니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
