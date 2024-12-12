package com.sparta.travelconquestbe.domain.route.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.sparta.travelconquestbe.api.route.dto.response.RouteSearchAllResponse;
import com.sparta.travelconquestbe.domain.bookmark.entity.QBookmark;
import com.sparta.travelconquestbe.domain.review.entity.QReview;
import com.sparta.travelconquestbe.domain.route.entity.QRoute;
import com.sparta.travelconquestbe.domain.route.enums.RouteSort;
import com.sparta.travelconquestbe.domain.routelocation.entity.QRouteLocation;
import com.sparta.travelconquestbe.domain.user.entity.QUser;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RouteRepositoryQueryDslImpl implements RouteRepositoryQueryDsl {
  private final EntityManager entityManager;
  private final QRoute route = QRoute.route;
  private final QReview review = QReview.review;
  private final QBookmark bookmark = QBookmark.bookmark;
  private final QUser user = QUser.user;
  private final QRouteLocation location = QRouteLocation.routeLocation;

  @Override
  @Transactional(readOnly = true)
  public Page<RouteSearchAllResponse> routeSearchAll(Pageable pageable, RouteSort sort) {
    JPAQuery<RouteSearchAllResponse> query = new JPAQuery<>(entityManager);
    query
        .select(
            Projections.fields(
                RouteSearchAllResponse.class,
                route.id.as("id"),
                route.title.as("title"),
                route.description.as("description"),
                user.nickname.as("creator"),
                route.updatedAt.as("updatedAt"),
                location.mediaUrl.max().as("mediaUrl"),
                route.locations.size().longValue().as("locationCount"),
                review.countDistinct().longValue().as("reviewCount"),
                bookmark.countDistinct().longValue().as("bookmarkCount")))
        .from(route)
        .leftJoin(route.reviews, review)
        .leftJoin(route.bookmarks, bookmark)
        .leftJoin(route.user, user)
        .leftJoin(route.locations, location)
        .groupBy(route.id)
        .distinct();

    // 동적 정렬 컬럼 처리
    switch (sort) {
      case REVIEW_COUNT -> query.orderBy(review.count().desc(), route.updatedAt.desc());
      case BOOKMARK_COUNT -> query.orderBy(bookmark.count().desc(), route.updatedAt.desc());
      default -> query.orderBy(route.updatedAt.desc());
    }

    query.offset(pageable.getOffset());
    query.limit(pageable.getPageSize());
    List<RouteSearchAllResponse> result = query.fetch();

    long total = query.fetchCount();

    return new PageImpl<>(result, pageable, total);
  }
}
