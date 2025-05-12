package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(100)", unique = true, nullable = false)
    private String email;

    @Column(columnDefinition = "VARCHAR(100)", nullable = false)
    private String password;

    @Column(columnDefinition = "VARCHAR(50)", nullable = false)
    private String username;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String email, String password, String username, Role role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role != null ? role : Role.ROLE_USER;
    }

    // 비밀번호 업데이트
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 계정 활성화/비활성화
    public void setActive(boolean active) {
        this.active = active;
    }

    // 관리자 권한 부여/제거
    public void setRole(Role role) {
        this.role = role;
    }
}
