package com.icbc.codeResolver.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Log4j
@Component
public class LogAspect {

    @Pointcut("@annotation(com.icbc.codeResolver.aop.WebLog)")
    public void pointCut() {
    }

    /**
     * 方法执行前插入，打印方法的方法名、参数、返回值
     */
    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        WebLog webLog = methodSignature.getMethod().getAnnotation(WebLog.class);
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();
        log.info("URL : " + request.getRequestURL().toString());
        log.info("HTTP_METHOD : " + request.getMethod());
        log.info("IP : " + request.getRemoteAddr());
        log.info("DESCRIPTION : " + webLog.value());
        log.info("CLASS : " + joinPoint.getSignature().getDeclaringTypeName());
        log.info("METHOD : " + methodSignature.toLongString());
        log.info("REQUEST ARGS : " + Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(value = "pointCut()",returning = "result")
    public void afterReturning(JoinPoint joinPoint,Object result) {
        log.info("RETURN VALUE: " + result);
    }


}
