package com.sparta.travelconquestbe.common.annotation;

import com.sparta.travelconquestbe.api.party.dto.validation.PasswordRequiredValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordRequiredValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordRequired {

  String message() default "비밀번호 활성화 시 비밀번호는 필수입니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}