package com.offcn.test;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class AppTest {

    @Test
    public void method(){
        /*Long[] arrs = new Long [2];
        arrs[0] = 149187842867922L;
        arrs[1] = 149187842867912L;
        String s = JSON.toJSONString(arrs);
        System.out.println(s);*/
      /*  int i = Runtime.getRuntime().availableProcessors();
        System.out.println(i);*/

        CountDownLatch countDownLatch = new CountDownLatch(6);

        for (int i = 1; i < 7; i++) {
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"走了");
                countDownLatch.countDown();
            },String.valueOf(i)).start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("关门.......");

    }

    @Test
    public void method2(){

        CyclicBarrier barrier = new CyclicBarrier(7, () -> {
            System.out.println("成功");
        });

        for (int i = 1; i < 8; i++) {
            int finalI = i;
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"收集了:"+ finalI +"个");
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Test
    public void method3(){

    }

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 1; i < 6; i++) {
            new Thread(()->{
                try {
                    semaphore.acquire();

                    System.out.println(Thread.currentThread().getName()+"抢到");

                    TimeUnit.SECONDS.sleep(2);

                    System.out.println(Thread.currentThread().getName()+"离开");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }

            },String.valueOf(i)).start();
        }
    }
}
