package com.sparta.travelconquestbe.domain.admin.entity;

import com.sparta.travelconquestbe.common.entity.TimeStampAll;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends TimeStampAll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;
}
