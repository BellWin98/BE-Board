package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password"})
public class User extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private String profileImage;

    // 관리자 권한 부여/제거
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 계정 활성화/비활성화
    @Setter
    @Column(nullable = false)
    private boolean active = true;

    // 생성자: 사용자 생성 시 필수 정보 초기화
    @Builder
    public User(String email, String nickname, String password, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role != null ? role : Role.USER;
    }

    // 프로필 정보 업데이트
    public void updateProfile(String username, String profileImage) {
        if (username != null && !username.isBlank()) {
            this.nickname = username;
        }
        this.profileImage = profileImage;
    }

    // 비밀번호 업데이트
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // --- UserDetails 구현 메서드 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // UserDetails에서는 username이 식별자이므로 email 반환
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
