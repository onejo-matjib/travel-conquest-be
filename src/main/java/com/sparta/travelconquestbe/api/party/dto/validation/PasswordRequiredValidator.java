package com.sparta.travelconquestbe.api.party.dto.validation;

import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.common.annotation.PasswordRequired;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordRequiredValidator implements
    ConstraintValidator<PasswordRequired, PartyCreateRequest> {

  @Override
  public boolean isValid(PartyCreateRequest request, ConstraintValidatorContext context) {
    if (request.isPasswordStatus()) { // 비밀번호 활성화 여부 확인
      // 비밀번호가 null이거나 공백이면 유효하지 않음
      return request.getPassword() != null && !request.getPassword().isBlank();
    }
    return true; // 비밀번호 비활성화일 경우 유효
  }
}