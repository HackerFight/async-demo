
package com.qiuguan.springboot.async;

import com.qiuguan.springboot.async.service.MyAsyncTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author created by qiuguan on 2021/12/20 15:08
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MainTest {

    @Autowired
    private MyAsyncTask myAsyncTask;

    @Test
    public void test(){
        for (int i = 0; i < 5; i++) {
            String s = myAsyncTask.asyncTask2();
            System.out.println(s);
        }

    }


    @Test
    public void test2() throws Exception {

        /**
         * 执行到38行时，他就是一个普通的匿名函数，并不会去执行逻辑
         * 当执行到51行时，会出发异步操作，然后调用43行的call 方法
         *
         * 可以分别在 43， 46， 52 行打一个断点，可以看到它的执行顺序
         */
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("Callable thread: " +Thread.currentThread().getName());
                return 100;
            }
        };


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(callable);

    }

}
