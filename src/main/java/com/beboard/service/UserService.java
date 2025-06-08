package com.beboard.service;

import com.beboard.dto.UserDto;
import com.beboard.entity.Role;
import com.beboard.entity.User;
import com.beboard.exception.AlreadyExistsException;
import com.beboard.repository.UserRepository;
import com.beboard.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new AlreadyExistsException(ErrorCode.USERNAME_ALREADY_USED);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 사용자 엔티티 생성
        User user = User.builder()
                .email(requestDto.getEmail())
                .username(requestDto.getUsername())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);
        log.info("새 사용자 등록 완료: {}", savedUser.getEmail());

        return UserDto.Response.from(savedUser);
    }

    /**
     * 사용자 프로필 업데이트
     * @param userId 사용자 ID
     * @param requestDto 프로필 업데이트 요청 DTO
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public UserDto.Response updateProfile(Long userId, UserDto.UpdateProfileRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 사용자명 중복 확인 (변경된 경우에만)
        if (requestDto.getEmail() != null && !requestDto.getEmail().equals(user.getDisplayName()) &&
                userRepository.existsByUsername(requestDto.getUsername())) {
            throw new AlreadyExistsException(ErrorCode.USERNAME_ALREADY_USED);
        }

        // 프로필 업데이트
        user.updateProfile(requestDto.getUsername(), requestDto.getProfileImage());
        User updatedUser = userRepository.save(user);
        log.info("사용자 프로필 업데이트 완료: {}", updatedUser.getEmail());

        return UserDto.Response.from(updatedUser);
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
        String encodedPassword = passwordEncoder.encode(requestDto.getCurrentPassword());
        user.updatePassword(encodedPassword);

        User updatedUser = userRepository.save(user);
        log.info("사용자 비밀번호 변경 완료: {}", updatedUser.getEmail());

        return UserDto.Response.from(updatedUser);
    }

    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    public UserDto.Response getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        return UserDto.Response.from(user);
    }

    /**
     * 이메일로 사용자 정보 조회
     * @param email 이메일
     * @return 사용자 정보
     */
    public UserDto.Response getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. 이메일: " + email));

        return UserDto.Response.from(user);
    }

    /**
     * 사용자 목록 조회 (관리자용)
     * @param pageable 페이징 정보
     * @return 사용자 목록
     */
    public Page<UserDto.Response> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDto.Response::from);
    }

    /**
     * 사용자 검색 (관리자용)
     * @param searchTerm 검색어
     * @param pageable 페이징 정보
     * @return 검색된 사용자 목록
     */
    public Page<UserDto.Response> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable)
                .map(UserDto.Response::from);
    }

    /**
     * 사용자 활성화/비활성화 (관리자용)
     * @param userId 사용자 ID
     * @param active 활성화 여부
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public UserDto.Response setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.setActive(active);
        User updatedUser = userRepository.save(user);
        log.info("사용자 {} 활성화 상태 변경: {}", updatedUser.getEmail(), active);

        return UserDto.Response.from(updatedUser);
    }

    /**
     * 사용자 역할 변경 (관리자용)
     * @param userId 사용자 ID
     * @param role 변경할 역할
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public UserDto.Response changeUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.setRole(role);
        User updatedUser = userRepository.save(user);
        log.info("사용자 {} 역할 변경: {}", updatedUser.getEmail(), role);

        return UserDto.Response.from(updatedUser);
    }

    /**
     * 활성 사용자 수 조회
     * @return 활성 사용자 수
     */
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
}
