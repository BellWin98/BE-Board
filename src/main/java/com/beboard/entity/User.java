package com.beboard.entity;

import com.beboard.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_nickname", columnList = "nickname"),
        @Index(name = "idx_user_status", columnList = "status"),
        @Index(name = "idx_user_last_login", columnList = "lastLoginAt"),
        @Index(name = "idx_user_created", columnList = "createdAt"),
        @Index(name = "idx_user_active_status", columnList = "deleted, status, lastLoginAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"password"})
public class User extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private String profileImage;

    // 관리자 권한 부여/제거
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * 사용자 상태 - 라이프사이클 관리
     * 사용자는 정적인 존재가 아닙니다. 가입부터 탈퇴까지
     * 다양한 상태를 거치며, 각 상태마다 다른 권한과
     * 서비스 접근 레벨을 가집니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    // 계정 활성화/비활성화
    @Setter
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    /**
     * 로그인 실패 횟수 - 보안 강화
     * 브루트 포스 공격을 방어하기 위한 카운터입니다.
     * 일정 횟수 이상 실패 시 계정을 임시 잠금하여
     * 보안을 강화할 수 있습니다.
     */
    private Integer failedLoginAttempts = 0;

    /**
     * 마지막 로그인 시간 - 활성도의 핵심 지표
     * 휴면 사용자 식별, 리텐션 분석, 개인화 서비스 제공
     */
    private LocalDateTime lastLoginAt;

    /**
     * 마지막 활동 시간 - 더 세밀한 참여도 추적
     * 로그인과 실제 활동은 다릅니다. 로그인 후 즉시 떠나는 사용자와
     * 오랫동안 활발하게 참여하는 사용자를 구분하는 것이 중요합니다.
     */
    private LocalDateTime lastActivityAt;
    private LocalDateTime deletedAt;

    /**
     * 계정 잠금 해제 시간
     * 보안 정책에 따라 계정이 잠긴 경우, 언제 자동으로
     * 해제될지를 기록합니다. 관리자 개입 없이도
     * 자동화된 보안 관리가 가능합니다.
     */
    private LocalDateTime accountLockedUntil;

    /**
     * 사용자 프로필 - 확장된 정보
     * 핵심 인증 정보와 부가적인 프로필 정보를 분리하여
     * 성능과 유지보수성을 높입니다. 프로필 정보는 선택적으로
     * 로딩할 수 있어 메모리 효율성도 개선됩니다.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    // 생성자: 사용자 생성 시 필수 정보 초기화
    @Builder
    public User(String email, String nickname, String password, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role != null ? role : Role.USER;
    }

    // =========================== UserDetails 구현 ===========================

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
        return accountLockedUntil == null || accountLockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !deleted && status == UserStatus.ACTIVE;
    }

    // =========================== 비즈니스 로직 메서드 ===========================

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

    /**
     * 소프트 삭제 처리
     * 물리적 삭제가 아닌 논리적 삭제를 수행합니다.
     */
    public void softDelete() {
        this.deleted = true;
        this.active = false;
        this.deletedAt = LocalDateTime.now();
        this.status = UserStatus.DELETED;

        // 개인정보 마스킹 (필요에 따라)
//        this.email = "deleted_" + this.id + "@example.com";
//        this.nickname = "deleted_user_" + this.id;
    }

    /**
     * 휴면 상태 확인
     * 특정 기간 동안 활동이 없는 사용자를 식별합니다.
     */
    public boolean isDormant(int daysThreshold) {
        if (lastActivityAt == null) {
            return this.getCreatedAt().isBefore(LocalDateTime.now().minusDays(daysThreshold));
        }
        return lastActivityAt.isBefore(LocalDateTime.now().minusDays(daysThreshold));
    }

    /**
     * 로그인 성공 처리
     * 단순히 시간만 업데이트하는 것이 아니라, 관련된
     * 보안 정보도 함께 리셋합니다.
     */
    public void markLoginSuccess() {
        this.lastLoginAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * 로그인 실패 처리
     * 보안 정책에 따라 계정 잠금 여부를 결정합니다.
     */
    public void markLoginFailure() {
        this.failedLoginAttempts++;

        // 5회 실패 시 30분간 계정 잠금
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * 활동 시간 업데이트
     * 사용자가 의미있는 활동을 할 때마다 호출됩니다.
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}
