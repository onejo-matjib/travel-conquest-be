package com.sparta.travelconquestbe.domain.route.repository;

import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
                bookmark.countDistinct().longValue().as("bookmarkCount"),
                review.rating.avg().coalesce(0.0).as("reviewAvg")))
        .from(route)
        .leftJoin(route.reviews, review)
        .leftJoin(route.bookmarks, bookmark)
        .leftJoin(route.user, user)
        .leftJoin(route.locations, location)
        .groupBy(route.id)
        .distinct();

    query.orderBy(getSortOrder(sort));
    query.offset(pageable.getOffset());
    query.limit(pageable.getPageSize());
    List<RouteSearchAllResponse> result = query.fetch();

    long total = query.fetchCount();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<RouteSearchAllResponse> routeSearchByKeyword(
      Pageable pageable, RouteSort sort, String keyword) {
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
                bookmark.countDistinct().longValue().as("bookmarkCount"),
                review.rating.avg().coalesce(0.0).as("reviewAvg")))
        .from(route)
        .leftJoin(route.reviews, review)
        .leftJoin(route.bookmarks, bookmark)
        .leftJoin(route.user, user)
        .leftJoin(route.locations, location)
        .where(
            hasTitle(keyword)
                .or(hasDescription(keyword))
                .or(hasAuthor(keyword).or(hasLocation(keyword))))
        .groupBy(route.id)
        .distinct();
    query.orderBy(getSortOrder(sort));
    query.offset(pageable.getOffset());
    query.limit(pageable.getPageSize());
    List<RouteSearchAllResponse> result = query.fetch();

    long total = query.fetchCount();

    return new PageImpl<>(result, pageable, total);
  }

  private OrderSpecifier<?>[] getSortOrder(RouteSort sort) {
    switch (sort) {
      case REVIEW_COUNT -> {
        return new OrderSpecifier[] {review.count().desc(), route.updatedAt.desc()};
      }
      case BOOKMARK_COUNT -> {
        return new OrderSpecifier[] {bookmark.count().desc(), route.updatedAt.desc()};
      }
      case REVIEW_AVG -> {
        return new OrderSpecifier[] {review.rating.avg().desc(), route.updatedAt.desc()};
      }
      default -> {
        return new OrderSpecifier[] {route.updatedAt.desc()};
      }
    }
  }

  private BooleanExpression hasTitle(String keyword) {
    return hasText(keyword) ? QRoute.route.title.containsIgnoreCase(keyword) : null;
  }

  private BooleanExpression hasDescription(String keyword) {
    return hasText(keyword) ? QRoute.route.description.containsIgnoreCase(keyword) : null;
  }

  private BooleanExpression hasAuthor(String keyword) {
    return hasText(keyword) ? QUser.user.nickname.containsIgnoreCase(keyword) : null;
  }

  private BooleanExpression hasLocation(String keyword) {
    return hasText(keyword)
        ? QRouteLocation.routeLocation.locationName.containsIgnoreCase(keyword)
        : null;
  }
}
