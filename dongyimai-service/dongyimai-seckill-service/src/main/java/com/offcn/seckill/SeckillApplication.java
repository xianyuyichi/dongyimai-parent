package com.offcn.seckill;

import com.offcn.seckill.config.FeignInterceptor;
import com.offcn.sellergoods.utils.IdWorker;
import com.offcn.sellergoods.utils.TokenDecode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableEurekaClient
//@EnableFeignClients
@MapperScan(basePackages = {"com.offcn.seckill.dao"})
@EnableScheduling // 开启定时器
@EnableAsync //开启异步任务
@EnableRabbit
public class SeckillApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }

    @Bean
    public TokenDecode tokenDecode(){
        return new TokenDecode();
    }

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }


    /***
     * 创建秒杀队列
     * @return
     */
    @Bean(name = "queueSeckillOrder")
    public Queue queueSeckillOrder(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"), true);
    }

    //创建秒杀交换机
    @Bean
    public DirectExchange directExchangeOrder(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"));
    }


    /****
     * 队列绑定到交换机上
     * @return
     */
    @Bean
    public Binding basicBindingSeckillOrder(){
        return BindingBuilder
                .bind(queueSeckillOrder())
                .to(directExchangeOrder())
                .with(env.getProperty("mq.pay.routing.seckillkey"));
    }
}
