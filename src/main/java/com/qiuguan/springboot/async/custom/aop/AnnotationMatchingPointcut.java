package com.qiuguan.springboot.async.custom.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

import java.lang.annotation.Annotation;

/**
 * @author created by qiuguan on 2021/12/15 17:14
 */
public class AnnotationMatchingPointcut implements Pointcut {

    private final ClassFilter classFilter;

    private final MethodMatcher methodMatcher;


    public AnnotationMatchingPointcut(Class<? extends Annotation> annotationType){
        this.classFilter = new AnnotationCandidateClassFilter(annotationType);
        this.methodMatcher = new AnnotationMethodMatcher(annotationType, true);
    }

    @Override
    public ClassFilter getClassFilter() {
        return classFilter;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

}
