package com.beboard.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 성능 측정 Aspect
 * 서비스 메서드의 실행 시간을 측정하여 성능 모니터링
 * 캐시 적용 전후의 성능 차이를 확인할 수 있음
 */
@Aspect
@Component
@Slf4j
public class PerformanceMetrics {

    @Around("execution(* com.beboard.service..*(..))")
    public Object measureServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable{
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 100ms 이상 걸리는 메서드만 로깅
            if (executionTime > 100) {
                log.info("성능 측정 - Service Method.{}: {}ms", methodName, executionTime);
            } else {
                log.debug("성능 측정 - Service Method.{}: {}ms", methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("예외 발생 - CategoryService.{}: {}ms, error: {}",
                    methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}
