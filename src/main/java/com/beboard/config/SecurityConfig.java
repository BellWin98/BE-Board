package com.beboard.config;

import com.beboard.service.AuthService;
import com.beboard.service.CustomUserDetailsService;
import com.beboard.util.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://fe-board.vercel.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                // 쿠키와 세션을 사용하는 MVC패턴에서 csrf 공격에 취약하나, REST API는 덜 취약하여 비활성화
                // csrf: 사용자가 자신의 의지와는 무관하게 공격자가 의도한 행위(데이터 수정, 삭제, 등록 등) 을 특정 웹사이트에 요청하게 하는 공격
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 활성화(특정 도메인만 접속할 수 있도록 허용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)

                // 기본 인증 로그인 사용 안하고 JWT 로그인 사용하므로 disable
                .httpBasic(AbstractHttpConfigurer::disable)

                // 세션을 사용하지 않는 설정 추가
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // URL 기반 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 공개 엔드포인트: 인증 없이 접근 가능
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/categories",              // 카테고리 목록 조회
                                "/api/posts",                   // 게시글 목록 조회
                                "/api/posts/{id}",             // 게시글 상세 조회
                                "/api/posts/{postId}/comments", // 댓글 목록 조회
                                "/api/comments/search",         // 댓글 검색
                                "/api/challenges",
                                "/error",                       // 에러 페이지
                                "/actuator/health"              // 헬스 체크
                        ).permitAll()

                        // 관리자 전용 엔드포인트
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
