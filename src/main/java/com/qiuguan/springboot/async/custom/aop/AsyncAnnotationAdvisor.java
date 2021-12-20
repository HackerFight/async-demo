package com.qiuguan.springboot.async.custom.aop;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;

/**
 * @author created by qiuguan on 2021/12/15 16:56
 */
public class AsyncAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    private final Advice advice;

    private final Pointcut pointcut;

    public AsyncAnnotationAdvisor(Executor executor, AsyncUncaughtExceptionHandler handler, Class<? extends Annotation> asyncAnnotationType){
        this.advice = buildAdvice(executor, handler);
        this.pointcut = buildJoinPoint(asyncAnnotationType);
    }

    private Pointcut buildJoinPoint(Class<? extends Annotation> asyncAnnotationType) {
        return new AnnotationMatchingPointcut(asyncAnnotationType);
    }

    private Advice buildAdvice(Executor executor, AsyncUncaughtExceptionHandler handler) {
        AnnotationAsyncExecutionInterceptor advice = new AnnotationAsyncExecutionInterceptor();
        advice.configure(executor, handler);
        return advice;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
}
