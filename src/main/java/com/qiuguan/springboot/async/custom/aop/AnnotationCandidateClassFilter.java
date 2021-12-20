package com.qiuguan.springboot.async.custom.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 * @author created by qiuguan on 2021/12/20 11:14
 */
public class AnnotationCandidateClassFilter implements ClassFilter {

    private final Class<? extends Annotation> annotationType;


    public AnnotationCandidateClassFilter(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        return AnnotationUtils.isCandidateClass(clazz, annotationType);
    }
}
