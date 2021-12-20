package com.qiuguan.springboot.async.custom.aop;

import org.springframework.aop.MethodMatcher;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author created by qiuguan on 2021/12/20 11:08
 */
public class AnnotationMethodMatcher implements MethodMatcher {

    private final Class<? extends Annotation> annotationType;

    private final boolean checkInherited;


    public AnnotationMethodMatcher(Class<? extends Annotation> annotationType, boolean checkInherited) {
        this.annotationType = annotationType;
        this.checkInherited = checkInherited;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (matchesMethod(method)){
            return true;
        }

        if (Proxy.isProxyClass(targetClass)){
            return false;
        }

        Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return (mostSpecificMethod != method) && matchesMethod(method);
    }


    private boolean matchesMethod(Method method) {
        return checkInherited ? AnnotatedElementUtils.hasAnnotation(method, annotationType) :
                method.isAnnotationPresent(annotationType);
    }

    @Override
    public boolean isRuntime() {
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, Object... args) {
        throw new UnsupportedOperationException("not support");
    }
}
