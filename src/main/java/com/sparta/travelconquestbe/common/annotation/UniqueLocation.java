package com.sparta.travelconquestbe.common.annotation;

import com.sparta.travelconquestbe.common.validator.UniqueLocationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueLocationValidator.class)
public @interface UniqueLocation {
  String message() default "동일한 좌표가 이미 존재합니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
