package com.sparta.travelconquestbe.api.party.dto.validation;

import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.api.party.dto.request.PartyUpdateRequest;
import com.sparta.travelconquestbe.common.annotation.PasswordRequired;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordRequiredValidator implements ConstraintValidator<PasswordRequired, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    String password = null;
    Boolean passwordStatus = null;

    // 객체 타입 확인 및 캐스팅
    if (value instanceof PartyCreateRequest request) {
      password = request.getPassword();
      passwordStatus = request.isPasswordStatus();
    } else if (value instanceof PartyUpdateRequest request) {
      password = request.getPassword();
      passwordStatus = request.isPasswordStatus();
    } else {
      // 지원하지 않는 타입
      return false;
    }

    // 공백 문자열 처리
    if (password != null && password.isBlank()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("비밀번호는 공백일 수 없습니다.")
          .addConstraintViolation();
      return false;
    }

    if (Boolean.TRUE.equals(passwordStatus)) {
      // 비밀번호 활성화 상태에서 비밀번호가 없을 경우
      if (password == null) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("비밀번호가 활성화된 상태에서는 비밀번호를 입력해야 합니다.")
            .addConstraintViolation();
        return false;
      }
    } else {
      // 비밀번호 비활성화 상태에서 비밀번호가 존재할 경우
      if (password != null) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("비밀번호 비활성화 상태에서는 비밀번호를 입력할 수 없습니다.")
            .addConstraintViolation();
        return false;
      }
    }

    return true; // 조건을 모두 통과하면 유효
  }
}