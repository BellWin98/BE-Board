package com.beboard.service;

import com.beboard.dto.AuthDto;
import com.beboard.dto.UserDto;
import com.beboard.entity.User;
import com.beboard.repository.UserRepository;
import com.beboard.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService implements UserDetailsService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    /**
     * 로그인 처리
     * @param loginRequest 로그인 요청 DTO
     * @return 인증 응답 DTO (토큰 및 사용자 정보)
     */
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
        String accessToken = jwtTokenProvider.generateToken(user);

        log.info("사용자 로그인 성공: {}", user.getEmail());

        // 응답 생성
        UserDto.Response userResponse = UserDto.Response.from(user);

        return AuthDto.LoginResponse.builder()
                .token(accessToken)
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }
}
