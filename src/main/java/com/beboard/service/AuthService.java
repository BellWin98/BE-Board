package com.beboard.service;

import com.beboard.dto.AuthDto;
import com.beboard.dto.UserDto;
import com.beboard.entity.Role;
import com.beboard.entity.User;
import com.beboard.exception.AlreadyExistsException;
import com.beboard.repository.UserRepository;
import com.beboard.util.ErrorCode;
import com.beboard.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * 사용자 등록
     * @param requestDto 회원가입 요청 DTO
     * @return 등록된 사용자 정보
     */
    @Transactional
    public UserDto.Response register(UserDto.RegisterRequest requestDto) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new AlreadyExistsException(ErrorCode.EMAIL_ALREADY_USED);
        }

        // 사용자명 중복 확인
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new AlreadyExistsException(ErrorCode.NICKNAME_ALREADY_USED);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(requestDto.getEmail())
                .nickname(requestDto.getNickname())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("새 사용자 등록 완료: {}", savedUser.getEmail());

        return UserDto.Response.from(savedUser);
    }

    /**
     * 로그인 처리
     * @param loginRequest 로그인 요청 DTO
     * @return 인증 응답 DTO (토큰 및 사용자 정보)
     */
    @Transactional
    public AuthDto.LoginResponse login(AuthDto.LoginRequest loginRequest) {
        // Spring Security 인증 수행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 사용자 정보 조회
        User user = (User) authentication.getPrincipal();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("사용자 로그인 성공: {}", user.getEmail());

        // 응답 생성
        UserDto.Response userResponse = UserDto.Response.from(user);

        return AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    /**
     * 현재 인증된 사용자 정보 조회
     * @return 사용자 정보
     */
    public UserDto.Response getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return UserDto.Response.from(user);
    }

    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param requestDto 비밀번호 변경 요청 DTO
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public UserDto.Response changePassword(Long userId, UserDto.ChangePasswordRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화 및 업데이트
        String encodedPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.updatePassword(encodedPassword);

        User updatedUser = userRepository.save(user);
        log.info("사용자 비밀번호 변경 완료: {}", updatedUser.getEmail());

        return UserDto.Response.from(updatedUser);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.softDelete();
        userRepository.save(user);

        // 현재 세션 무효화
        SecurityContextHolder.clearContext();

        log.info("사용자 계정 삭제 완료: {}", user.getEmail());
    }

    // 로그아웃
    @Transactional
    public void signOut(String userEmail) {
        User user = userRepository.findByEmailAndDeletedFalse(userEmail)
                .orElseThrow();

//        user.removeRefreshToken();
        userRepository.save(user);
        SecurityContextHolder.clearContext();
    }
}
