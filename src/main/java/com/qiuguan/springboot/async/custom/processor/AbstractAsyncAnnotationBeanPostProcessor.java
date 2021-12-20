package com.qiuguan.springboot.async.custom.processor;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author created by qiuguan on 2021/12/20 11:51
 */
public abstract class AbstractAsyncAnnotationBeanPostProcessor extends ProxyProcessorSupport implements BeanPostProcessor, BeanFactoryAware {

    protected Advisor advisor;

    /**
     * 符合条件的bean
     */
    private final Map<Class<?>, Boolean> eligibleBeans = new ConcurrentHashMap<>(256);


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (this.advisor == null) {
            return bean;
        }

        //判断是否符合条件
        if (isEligible(bean, beanName)) {
            //aop 创建代理必不可少的一步
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.copyFrom(this);
            proxyFactory.setTarget(bean);

            if (!proxyFactory.isProxyTargetClass()) {
                evaluateProxyInterfaces(bean.getClass(), proxyFactory);
            }

            proxyFactory.addAdvisor(this.advisor);

            //交给spring创建代理对象，由他去选择是jdk还是cglib
            return proxyFactory.getProxy(getProxyClassLoader());
        }

        return bean;
    }


    protected boolean isEligible(Object bean, String beanName) {
        return isEligible(bean.getClass());
    }

    protected boolean isEligible(Class<?> targetClass) {
        Boolean eligible = this.eligibleBeans.get(targetClass);
        if (eligible != null) {
            return eligible;
        }
        if (this.advisor == null) {
            return false;
        }

        //事务这里也是这样，只不过advisor 不一样而已
        eligible = AopUtils.canApply(this.advisor, targetClass);
        this.eligibleBeans.put(targetClass, eligible);
        return eligible;
    }

}
