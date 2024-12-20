package com.sparta.travelconquestbe.api.admin.service;

import com.sparta.travelconquestbe.api.admin.dto.request.AdminLoginRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.AdminSignUpRequest;
import com.sparta.travelconquestbe.api.admin.dto.request.CouponCreateRequest;
import com.sparta.travelconquestbe.api.admin.dto.respones.AdminUpdateUserResponse;
import com.sparta.travelconquestbe.api.admin.dto.respones.CouponCreateResponse;
import com.sparta.travelconquestbe.api.report.dto.response.ReportSearchResponse;
import com.sparta.travelconquestbe.api.user.dto.respones.UserResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.common.config.jwt.JwtHelper;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.enums.CouponType;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.report.entity.Report;
import com.sparta.travelconquestbe.domain.report.enums.Villain;
import com.sparta.travelconquestbe.domain.report.repository.ReportRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.Title;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  private final ReportRepository reportRepository;
  private static final int MAX_PAGE_SIZE = 100;

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
      throw new CustomException("ADMIN#2_001", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException("ADMIN#1_001", "비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);
    }

    return jwtHelper.createToken(user);
  }

  @Transactional
  public AdminUpdateUserResponse banUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("ADMIN#3_003", "해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    String deletedNickname = "resign_" + user.getNickname();
    user.changeNickname(deletedNickname);
    user.delete();

    userRepository.save(user);

    return mapToResponse(user);
  }

  @Transactional
  public AdminUpdateUserResponse updateUserLevel(Long userId) {
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

  @Transactional
  public void restoreUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new CustomException("ADMIN#3_004", "해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    if (user.getDeletedAt() == null) {
      throw new CustomException("ADMIN#4_002", "이미 활성화된 유저입니다.", HttpStatus.CONFLICT);
    }

    user.restore();

    // 닉네임에서 delete_, resign_ 제거
    String originalNickname = removePrefixFromNickname(user.getNickname());
    user.changeNickname(originalNickname);

    userRepository.save(user);
  }

  public Page<UserResponse> getAllUsers(Pageable pageable) {
    if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
      throw new CustomException("ADMIN#5_003", "페이지 번호와 사이즈는 양수여야 합니다.", HttpStatus.BAD_REQUEST);
    }

    int pageSize = pageable.getPageSize() > MAX_PAGE_SIZE ? MAX_PAGE_SIZE : pageable.getPageSize();
    Pageable limitedPageable = Pageable.ofSize(pageSize).withPage(pageable.getPageNumber());

    Page<User> userPage = userRepository.findAll(limitedPageable);
    List<UserResponse> userResponses = userPage.map(user -> UserResponse.builder()
        .id(user.getId())
        .name(user.getName())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .birth(user.getBirth())
        .title(user.getTitle().name())
        .subscriptionsCount(user.getSubscriptionCount())
        .build()).getContent();
    return new PageImpl<>(userResponses, limitedPageable, userPage.getTotalElements());
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
  public void deleteCoupon(Long couponId, AuthUserInfo userInfo) {
    if (!(userInfo.getType().equals(UserType.ADMIN))) {
      throw new CustomException("COUPON#3_003",
          "해당 리소스에 접근할 권한이 없습니다.",
          HttpStatus.FORBIDDEN);
    }

    Coupon coupon = couponRepository.findById(couponId).orElseThrow
        (() -> new CustomException("COUPON#2_002",
            "해당 쿠폰이 존재하지 않습니다.",
            HttpStatus.NOT_FOUND));
    couponRepository.delete(coupon);
  }

  @Transactional
  public void processReport(Long Id, Villain status, AuthUserInfo userInfo) {
    Report report = reportRepository.findById(Id)
        .orElseThrow(() -> new CustomException("ADMIN#3_005", "해당 신고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

    // 이미 처리된 신고인지 확인
    if (report.getCheckedAt() != null) {
      throw new CustomException("ADMIN#6_001", "이미 처리된 신고입니다.", HttpStatus.BAD_REQUEST);
    }

    // 관리자 ID와 처리 시간 기록
    report.markProcessed(userInfo.getId());

    User targetUser = report.getTargetId();

    // 무죄 처리
    if (status == Villain.SAINT) {
      report.updateStatus(status);
      reportRepository.save(report);
      return;
    }

    // 유죄 처리
    report.updateStatus(status);

    // 유저의 상태를 업데이트
    if (targetUser.getUserType() == UserType.USER && status == Villain.OUTLAW) {
      // OUTLAW 상태로 변경 및 경고 메시지 설정
      targetUser.setUserType(UserType.OUTLAW);
      targetUser.setWarningMessage("당신은 OUTLAW 상태입니다. 추가 위반 시 DEVIL 상태가 됩니다.");
    } else if (targetUser.getUserType() == UserType.OUTLAW && status == Villain.OUTLAW) {
      // OUTLAW 상태에서 OUTLAW 신고를 또 받은 경우, DEVIL 상태로 변경 및 1주일 정지
      targetUser.setUserType(UserType.DEVIL);
      targetUser.changeNickname("tempblock_" + targetUser.getNickname());
      targetUser.deleteWeeks(LocalDateTime.now().plusWeeks(1)); // 1주일 후 시간 설정
    } else if (targetUser.getUserType() == UserType.DEVIL && status == Villain.DEVIL) {
      // DEVIL 상태에서 DEVIL 신고를 받은 경우, 강퇴
      String deletedNickname = "resign_" + targetUser.getNickname();
      targetUser.changeNickname(deletedNickname);
      targetUser.delete();
    } else if (targetUser.getUserType() == UserType.USER && status == Villain.DEVIL){
      // DEVIL 상태로 변경 및 1주일 정지
      targetUser.setUserType(UserType.DEVIL);
      targetUser.changeNickname("tempblock_" + targetUser.getNickname());
      targetUser.deleteWeeks(LocalDateTime.now().plusWeeks(1)); // 1주일 후 시간 설정
    }

    // 상태 및 로그 업데이트
    userRepository.save(targetUser);
    reportRepository.save(report);
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

  private String removePrefixFromNickname(String nickname) {
    if (nickname.startsWith("delete_")) {
      return nickname.substring("delete_".length());
    } else if (nickname.startsWith("resign_")) {
      return nickname.substring("resign_".length());
    }
    return nickname;
  }

  public Page<ReportSearchResponse> searchAllReports(int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page - 1, limit);
    return reportRepository.findAllReports(pageRequest);
  }
}
