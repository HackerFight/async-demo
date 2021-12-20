package com.qiuguan.springboot.async.custom.aop;

import org.springframework.lang.Nullable;

import java.util.function.Supplier;

/**
 * @author created by qiuguan on 2021/12/20 16:33
 */
public class SingletonSupplier<T> implements Supplier<T> {

    private final Supplier<? extends T> instanceSupplier;

    private final Supplier<? extends T> defaultSupplier;

    @Nullable
    private volatile T singletonInstance;


    public SingletonSupplier(@Nullable T instance, Supplier<? extends T> defaultSupplier) {
        this.instanceSupplier = null;
        this.defaultSupplier = defaultSupplier;
        this.singletonInstance = instance;
    }


    @Override
    public T get() {
        T instance = this.singletonInstance;
        if (instance == null) {
            synchronized (this) {
                instance = this.singletonInstance;
                if (instance == null) {
                    if (this.instanceSupplier != null) {
                        instance = this.instanceSupplier.get();
                    }
                    if (instance == null && this.defaultSupplier != null) {
                        instance = this.defaultSupplier.get();
                    }
                    this.singletonInstance = instance;
                }
            }
        }
        return instance;
    }
}
