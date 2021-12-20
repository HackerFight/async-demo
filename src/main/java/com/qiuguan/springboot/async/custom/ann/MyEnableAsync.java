package com.qiuguan.springboot.async.custom.ann;

import com.qiuguan.springboot.async.custom.config.ProxyAsyncConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author created by qiuguan on 2021/12/15 16:49
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ProxyAsyncConfiguration.class)
public @interface MyEnableAsync {

}
