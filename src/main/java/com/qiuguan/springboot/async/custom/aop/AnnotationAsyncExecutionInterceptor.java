package com.qiuguan.springboot.async.custom.aop;

import com.qiuguan.springboot.async.custom.ann.MyAsync;
import com.sun.xml.internal.ws.util.CompletedFuture;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author created by qiuguan on 2021/12/15 17:05
 */
public class AnnotationAsyncExecutionInterceptor implements MethodInterceptor, BeanFactoryAware {


    private static final Logger logger = LoggerFactory.getLogger(AnnotationAsyncExecutionInterceptor.class);

    private SingletonSupplier<Executor> defaultExecutor;

    private AsyncUncaughtExceptionHandler handler;

    private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(32);

    private BeanFactory beanFactory;


    public void configure(Executor executor, AsyncUncaughtExceptionHandler handler) {
        //这里可以看下spring的写法，它保存的是一个lambda 表达式，并不是直接去创建默认的执行器，而是
        //当调用 get() 方法时才会去调用 getDefaultExecutor
        this.defaultExecutor = new SingletonSupplier<>(executor, () -> getDefaultExecutor(beanFactory));
        this.handler = handler == null ? new SimpleAsyncUncaughtExceptionHandler() : handler;
    }


    protected AsyncTaskExecutor determineAsyncExecutor(Method method) {
        AsyncTaskExecutor executor = this.executors.get(method);
        if (executor == null) {
            Executor targetExecutor;
            String executorName = getExecutorQualifier(method);
            if (StringUtils.hasLength(executorName)) {
                targetExecutor = getQualifierExecutor(this.beanFactory, executorName);
            } else {
                targetExecutor = defaultExecutor.get();
            }

            if (targetExecutor == null) {
                return null;
            }

            executor = (targetExecutor instanceof AsyncListenableTaskExecutor ?
                    (AsyncListenableTaskExecutor) targetExecutor : new TaskExecutorAdapter(targetExecutor));
            this.executors.put(method, executor);
        }

        return executor;
    }

    private Executor getQualifierExecutor(BeanFactory beanFactory, String executorName) {
        if(beanFactory == null) {
            throw new RuntimeException("beanFactory must be not null");
        }

        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(beanFactory, Executor.class, executorName);
    }


    protected String getExecutorQualifier(Method method) {
        MyAsync async = AnnotatedElementUtils.findMergedAnnotation(method, MyAsync.class);
        if(async == null) {
            async = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), MyAsync.class);
        }

        return async == null ? null :async.value();
    }

    protected Executor getDefaultExecutor(BeanFactory beanFactory) {
        if (beanFactory != null) {
            try {
                return beanFactory.getBean(TaskExecutor.class);

            } catch (NoUniqueBeanDefinitionException e) {
                logger.debug("cloud not find unique TaskExecutor bean", e);

                try {
                    return beanFactory.getBean("taskExecutor", TaskExecutor.class);
                } catch (NoSuchBeanDefinitionException ex) {
                    if (logger.isInfoEnabled()) {
                        logger.info("More than one TaskExecutor bean found within the context, and none is named " +
                                "'taskExecutor'. Mark one of them as primary or name it 'taskExecutor' (possibly " +
                                "as an alias) in order to use it for async processing: " + e.getBeanNamesFound());
                    }
                }

            } catch (NoSuchBeanDefinitionException e) {
                logger.debug("cloud not find default TaskExecutor", e);

                try {
                    return beanFactory.getBean("taskExecutor", TaskExecutor.class);
                } catch (NoSuchBeanDefinitionException ex) {
                    logger.info("No task executor bean found for async processing: " +
                            "no bean of type TaskExecutor and no bean named 'taskExecutor' either");
                }
            }
        }

        return  null;
    }


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
        Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
        final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        AsyncTaskExecutor executor = determineAsyncExecutor(userDeclaredMethod);

        if(executor == null) {
            throw new IllegalStateException("No executor specified and no default executor set on AsyncExecutionInterceptor either");
        }

        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    Object result = invocation.proceed();
                    if(result instanceof Future) {
                        return ((Future<?>)result).get();
                    }
                } catch (Throwable ex) {
                    handleError(ex, userDeclaredMethod, invocation.getArguments());
                }

                return null;
            }
        };


        /**
         * 当执行到147行时：
         * 147 行就是一个普通的匿名函数，并没有去执行里面的call 方法
         *
         * 这里是主线程，一般来说优先执行，这里的task 是一个匿名函数
         * doSubmit 进去之后，就进行判断，然后出发异步任务，然后回到上面的 call() 方法
         */
        return doSubmit(task, executor, invocation.getMethod().getReturnType());
    }

    private Object doSubmit(Callable<Object> task, AsyncTaskExecutor executor, Class<?> returnType) {
        if(CompletedFuture.class.isAssignableFrom(returnType)) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return task.call();
                } catch (Exception ex) {
                    throw new CompletionException(ex);
                }
            }, executor);
        } else if(ListenableFuture.class.isAssignableFrom(returnType)) {
            ((AsyncListenableTaskExecutor)executor).submitListenable(task);
        } else if(Future.class.isAssignableFrom(returnType)) {
            executor.submit(task);
        } else {
            executor.submit(task);
        }

        return null;
    }

    private void handleError(Throwable ex, Method method, Object[] arguments)  throws Exception {
        if (Future.class.isAssignableFrom(method.getReturnType())) {
            ReflectionUtils.rethrowException(ex);
        } else {

            try {
                this.handler.handleUncaughtException(ex, method, arguments);

            } catch (Exception ex2) {
                logger.warn("Exception handler for async method '" + method.toGenericString() +
                        "' threw unexpected exception itself", ex2);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
       this.beanFactory = beanFactory;
    }
}
