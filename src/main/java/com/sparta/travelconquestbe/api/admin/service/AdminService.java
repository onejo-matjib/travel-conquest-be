package com.sparta.travelconquestbe.api.admin.service;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.AdminSignUpRequest;
import com.sparta.travelconquestbe.api.admin.dto.respones.AdminUpdateUserResponse;
import com.sparta.travelconquestbe.api.coupon.dto.request.CouponCreateRequest;
import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponCreateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final JwtHelper jwtHelper;
  private final PasswordEncoder passwordEncoder;
  private final CouponRepository couponRepository;

  public void signUp(AdminSignUpRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new CustomException("ADMIN#4_001", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
    }

    User adminUser = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .birth(request.getBirth())
        .nickname(request.getNickname())
        .type(UserType.ADMIN)
        .title(Title.CONQUEROR)
        .providerType("LOCAL")
        .build();

    userRepository.save(adminUser);
  }

  public String login(AdminLoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(
            () -> new CustomException("ADMIN#3_001", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND));

    if (!UserType.ADMIN.equals(user.getType())) {
      throw new CustomException("ADMIN#2_002", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException("ADMIN#1_001", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);
    }

    return jwtHelper.createToken(user);
  }

  @Transactional
  public AdminUpdateUserResponse banUser(AuthUserInfo admin, Long userId) {
    verifyAdmin(admin);

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("ADMIN#3_003", "해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    String deletedNickname = "delete_" + user.getNickname();
    user.changeNickname(deletedNickname);
    user.delete();

    userRepository.save(user);

    return mapToResponse(user);
  }

  @Transactional
  public AdminUpdateUserResponse updateUserLevel(AuthUserInfo admin, Long userId) {
    verifyAdmin(admin);

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("ADMIN#3_002", "해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (user.getType() != UserType.USER) {
      throw new CustomException("ADMIN#5_002", "이미 등급이 업그레이드된 사용자입니다.", HttpStatus.BAD_REQUEST);
    }

    user.updateUserType();
    userRepository.save(user);

    return mapToResponse(user);
  }

  private void verifyAdmin(AuthUserInfo admin) {
    if (!(admin.getType() == UserType.ADMIN)) {
      throw new CustomException("ADMIN#2_003", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
  }

  private AdminUpdateUserResponse mapToResponse(User user) {
    return AdminUpdateUserResponse.builder()
        .userId(user.getId())
        .name(user.getName())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .providerType(user.getProviderType())
        .birth(user.getBirth())
        .userType(user.getType())
        .title(user.getTitle())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .deletedAt(user.getDeletedAt())
        .build();
  }

  public CouponCreateResponse createCoupon(CouponCreateRequest request, AuthUserInfo userInfo) {

    if (!(userInfo.getType().equals(UserType.ADMIN))) {
      throw new CustomException("COUPON#3_001",
          "해당 리소스에 접근할 권한이 없습니다.",
          HttpStatus.FORBIDDEN);
    }

    CouponType requestCouponType = CouponType.valueOf(request.getType());
    Coupon coupon = Coupon.builder()
        .name(request.getName())
        .description(request.getDescription())
        .type(requestCouponType)
        .discountAmount(request.getDiscountAmount())
        .validUntil(request.getValidUntil())
        .count(request.getCount())
        .build();
    couponRepository.save(coupon);

    return CouponCreateResponse.builder()
        .id(coupon.getId())
        .name(coupon.getName())
        .description(coupon.getDescription())
        .type(coupon.getType())
        .discountAmount(coupon.getDiscountAmount())
        .validUntil(coupon.getValidUntil())
        .count(coupon.getCount())
        .createdAt(coupon.getCreatedAt())
        .updatedAt(coupon.getUpdatedAt())
        .build();
  }

  @Transactional
  public void deleteCoupon(Long id, AuthUserInfo userInfo) {
    if (!(userInfo.getType().equals(UserType.ADMIN))) {
      throw new CustomException("COUPON#3_003",
          "해당 리소스에 접근할 권한이 없습니다.",
          HttpStatus.FORBIDDEN);
    }

    Coupon coupon = couponRepository.findById(id).orElseThrow
        (() -> new CustomException("COUPON#2_003",
            "해당 쿠폰이 존재하지 않습니다.",
            HttpStatus.NOT_FOUND));
    couponRepository.delete(coupon);
  }
}
