package com.sparta.travelconquestbe.common.aspect;

import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminUserAspect {

  @Before("@annotation(com.sparta.travelconquestbe.common.annotation.AdminUser)")
  public void checkAdminRole(JoinPoint joinPoint) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof AuthUserInfo userInfo) {
      if (userInfo.getType() != UserType.ADMIN) {
        throw new CustomException("AUTH#2_003", "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN);
      }
    } else {
      throw new CustomException("AUTH#1_008", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
    }
  }
}
