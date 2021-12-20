package com.qiuguan.springboot.async.service;

import com.qiuguan.springboot.async.custom.ann.MyAsync;
import org.springframework.stereotype.Component;

/**
 * @author created by qiuguan on 2021/12/20 15:07
 */
@Component
public class MyAsyncTask {

    @MyAsync
    public void asyncTask() {
        System.out.println("异步任务开始执行。。。" + Thread.currentThread().getName());
    }


    @MyAsync
    public String asyncTask2() {
        System.out.println("异步任务开始执行。。。" + Thread.currentThread().getName());
        return "hello async";
    }
}
