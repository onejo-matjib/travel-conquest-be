package com.sparta.travelconquestbe.api.mycoupon.service;

import com.sparta.travelconquestbe.api.mycoupon.dto.respones.MyCouponSaveResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.entity.Coupon;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.entity.MyCoupon;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.enums.UserType;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

import static com.sparta.travelconquestbe.domain.coupon.enums.CouponType.PRIMIUM;
import static com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus.AVAILABLE;
import static com.sparta.travelconquestbe.domain.user.enums.Title.CONQUEROR;
import static com.sparta.travelconquestbe.domain.user.enums.UserType.ADMIN;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class MyCouponService {

    private final MyCouponRepository myCouponRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final Clock clock;


    @Transactional
    public MyCouponSaveResponse saveCoupon(Long couponId, Long userId) {
        User user = validateUser(userId);
        Coupon coupon = validateCoupon(couponId, user);

        // 쿠폰 등록 처리
        MyCoupon myCoupon = MyCoupon.builder()
                .status(AVAILABLE)
                .user(user)
                .coupon(coupon)
                .build();

        myCouponRepository.save(myCoupon);
        return buildResponse(myCoupon);
    }

    /**
     * 유저 유효성 검사
     */
    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_002", "사용자를 찾을 수 없습니다.", NOT_FOUND));
    }

    /**
     * 쿠폰 유효성 검사
     */
    private Coupon validateCoupon(Long couponId, User user) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException("COUPON_002", "해당 쿠폰이 존재하지 않습니다.", NOT_FOUND));

        if (user.getType().equals(UserType.USER)) {
            throw new CustomException("COUPON_008", "등업된 사용자가 아닙니다.", FORBIDDEN);
        }

        validatePremiumCouponAccess(coupon, user);
        validateCouponAvailability(coupon);
        validateCouponExpiration(coupon);

        return coupon;
    }

    /**
     * 프리미엄 쿠폰 접근 권한 확인
     */
    private void validatePremiumCouponAccess(Coupon coupon, User user) {
        if (coupon.getType().equals(PRIMIUM) &&
                !(user.getTitle().equals(CONQUEROR) || user.getType().equals(ADMIN))) {
            throw new CustomException("COUPON_009", "정복자 등급만 등록할 수 있는 쿠폰입니다.", CONFLICT);
        }
    }

    /**
     * 쿠폰 재고 확인
     */
    private void validateCouponAvailability(Coupon coupon) {
        if (coupon.getCount() <= 0) {
            throw new CustomException("COUPON_005", "해당 쿠폰이 소진되었습니다.", CONFLICT);
        }
    }

    /**
     * 쿠폰 유효기간 확인
     */
    private void validateCouponExpiration(Coupon coupon) {
        LocalDate currentDate = LocalDate.now(clock);
        if (currentDate.isAfter(coupon.getValidUntil())) {
            throw new CustomException("COUPON_006", "해당 쿠폰의 유효기간이 지났습니다.", BAD_REQUEST);
        }
    }

    /**
     * 응답 생성 메서드
     */
    private MyCouponSaveResponse buildResponse(MyCoupon myCoupon) {
        return MyCouponSaveResponse.builder()
                .id(myCoupon.getId())
                .name(myCoupon.getCoupon().getName())
                .description(myCoupon.getCoupon().getDescription())
                .code(myCoupon.getCoupon().getCode())
                .status(myCoupon.getStatus())
                .discountAmount(myCoupon.getCoupon().getDiscountAmount())
                .validUntil(myCoupon.getCoupon().getValidUntil())
                .createdAt(myCoupon.getCreatedAt())
                .build();
    }
}