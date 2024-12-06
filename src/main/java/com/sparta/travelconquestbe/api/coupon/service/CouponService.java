package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sparta.travelconquestbe.domain.coupon.enums.CouponType.PRIMIUM;
import static com.sparta.travelconquestbe.domain.mycoupon.enums.UseStatus.AVAILABLE;
import static com.sparta.travelconquestbe.domain.user.enums.Title.CONQUEROR;
import static com.sparta.travelconquestbe.domain.user.enums.UserType.ADMIN;
import static com.sparta.travelconquestbe.domain.user.enums.UserType.USER;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final MyCouponRepository myCouponRepository;

    private static final int MAX_LIMIT = 50; // 페이지 크기 최대값

    @Transactional(readOnly = true)
    public Page<CouponSearchResponse> searchAllCoupons(int page, int limit) {
        // 유효성 검증
        if (page < 1) {
            throw new CustomException("COMMON_002", "페이지 번호는 1 이상이어야 합니다.", BAD_REQUEST);
        }
    public Page<CouponSearchResponse> searchAllCoupons(int page, int limit) {

        return couponRepository.searchAllCoupons(PageRequest.of(page - 1, limit));
    }

    @Transactional
    public CouponSaveResponse saveCoupon(Long couponId, Long userId) {
        // 등업하지 않은 유저가 쿠폰을 등록하는 경우
        User user = qualifyUser(couponId, userId);

        // 쿠폰 자격사항 확인
        Coupon coupon = qualifyCoupon(couponId, userId);


        // 해당 쿠폰이 소진될 경우
        if (coupon.getCount() == 0) {
            throw new CustomException("COUPON_005", "해당 쿠폰이 소진되었습니다.", CONFLICT);
        }

        MyCoupon myCoupon = MyCoupon.builder()
                .status(AVAILABLE)
                .user(user)
                .coupon(coupon)
                .build();

        myCouponRepository.save(myCoupon);

        CouponSaveResponse response = CouponSaveResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .code(coupon.getCode())
                .status(AVAILABLE)
                .discountAmount(coupon.getDiscountAmount())
                .validUntil(coupon.getValidUntil())
                .createdAt(myCoupon.getCreatedAt())
                .build();

        return response;
    }

    // 유저 자격사항 확인 메서드
    public User qualifyUser(Long couponId, Long userId) {
        User user = userRepository.findByuserId(userId)
                .orElseThow(() -> new CustomException("USER_002", "사용자를 찾을 수 없습니다", NOT_FOUND));

        if (user.getType().equals(USER)) {
            throw new CustomException("COUPON_008", "등업된 사용자가 아닙니다.", FORBIDDEN);
        }

        return user;
    }

    // 쿠폰 자격사항 확인 메서드
    public Coupon qualifyCoupon(Long couponId, Long userId) {
        User user = userRepository.findByuserId(userId)
                .orElseThow(() -> new CustomException("USER_002", "사용자를 찾을 수 없습니다", NOT_FOUND));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow(()
                -> new CustomException("COUPON_002", "해당 쿠폰이 존재하지 않습니다.", NOT_FOUND));

        UserType userType = user.getType();

        /* 쿠폰 등급 확인
         * 프리미엄 쿠폰일 경우 정복자만 이용할 수 있습니다.
         * */
        if (coupon.getType().equals(PRIMIUM)) {
            if (!(user.getTitle().equals(CONQUEROR) || (userType.equals(ADMIN)))) {

        if (limit < 1 || limit > MAX_LIMIT) {
            throw new CustomException("COMMON_003", "페이지 크기는 1 이상" + MAX_LIMIT + " 이하로 설정해야 합니다.",
                    BAD_REQUEST);
        }

        return couponRepository.searchAllCoupons(PageRequest.of(page - 1, limit));
                throw new CustomException("COUPON_009", "정복자 등급만 등록할 수 있는 쿠폰입니다.", CONFLICT);
            }
        }
        return coupon;
    }
}
