package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends BaseTimeEntity {

    @Id
    private Long id; // User와 동일한 ID 사용

    /**
     * 사용자와의 일대일 관계
     *
     * @MapsId 를 사용하여 User의 ID를 그대로 사용합니다.
     * 이는 별도의 외래키 없이도 강한 연결관계를 보장하며,
     * 조인 성능도 최적화됩니다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // 실명 - 선택적 공개 정보
    @Column(length = 100)
    private String realName;

    // 한 줄 소개
    @Column(length = 200)
    private String bio;


}
