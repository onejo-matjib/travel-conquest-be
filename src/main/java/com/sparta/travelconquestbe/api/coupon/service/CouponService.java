package com.sparta.travelconquestbe.api.coupon.service;

import com.sparta.travelconquestbe.api.coupon.dto.respones.CouponSearchResponse;
import com.sparta.travelconquestbe.common.exception.CustomException;
import com.sparta.travelconquestbe.domain.coupon.repository.CouponRepository;
import com.sparta.travelconquestbe.domain.mycoupon.repository.MyCouponRepository;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final MyCouponRepository myCouponRepository;
    private final UserRepository userRepository;

    private static final int MAX_LIMIT = 50; // 페이지 크기 최대값

    @Transactional(readOnly = true)
    public Page<CouponSearchResponse> searchAllCoupons(int page, int limit) {
        // 유효성 검증
        if (page < 1) {
            throw new CustomException("COMMON_002", "페이지 번호는 1 이상이어야 합니다.", BAD_REQUEST);
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            throw new CustomException("COMMON_003", "페이지 크기는 1 이상" + MAX_LIMIT + " 이하로 설정해야 합니다.",
                    BAD_REQUEST);
        }

        return couponRepository.searchAllCoupons(PageRequest.of(page - 1, limit));
    }
}

