package com.qiuguan.springboot.async;

import com.qiuguan.springboot.async.custom.ann.MyEnableAsync;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author created by qiuguan on 2021/12/20 15:04
 */
@MyEnableAsync
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {

        SpringApplication.run(MainApplication.class, args);
    }
}
