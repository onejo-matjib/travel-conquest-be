package com.sparta.travelconquestbe.domain.tag.repository;

import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByKeyword(String keyword);
}
