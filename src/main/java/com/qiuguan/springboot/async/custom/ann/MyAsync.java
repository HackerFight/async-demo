package com.qiuguan.springboot.async.custom.ann;

import java.lang.annotation.*;

/**
 * @author created by qiuguan on 2021/12/15 17:10
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAsync {

    String value() default "";
}
