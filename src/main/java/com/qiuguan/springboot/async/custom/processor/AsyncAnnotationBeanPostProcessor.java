package com.qiuguan.springboot.async.custom.processor;

import com.qiuguan.springboot.async.custom.aop.AsyncAnnotationAdvisor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;

/**
 * @author created by qiuguan on 2021/12/15 16:50
 */
public class AsyncAnnotationBeanPostProcessor extends AbstractAsyncAnnotationBeanPostProcessor {

    private Executor executor;

    private AsyncUncaughtExceptionHandler asyncExceptionHandler;

    private Class<? extends Annotation> annotationType;

    private BeanFactory beanFactory;


    public AsyncAnnotationBeanPostProcessor(Executor executor, AsyncUncaughtExceptionHandler handler){
        this.executor = executor;
        this.asyncExceptionHandler = handler;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public AsyncUncaughtExceptionHandler getAsyncExceptionHandler() {
        return asyncExceptionHandler;
    }

    public void setAsyncExceptionHandler(AsyncUncaughtExceptionHandler asyncExceptionHandler) {
        this.asyncExceptionHandler = asyncExceptionHandler;
    }

    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;

        //创建一个增强器
        AsyncAnnotationAdvisor advisor = new AsyncAnnotationAdvisor(this.executor, this.asyncExceptionHandler, this.annotationType);
        advisor.setBeanFactory(beanFactory);
        this.advisor = advisor;
    }
}
