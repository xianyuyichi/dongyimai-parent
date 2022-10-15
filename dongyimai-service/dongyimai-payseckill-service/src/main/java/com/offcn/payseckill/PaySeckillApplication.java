package com.offcn.payseckill;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableRabbit
public class PaySeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaySeckillApplication.class,args);
    }

    @Autowired
    private Environment env;


    //创建秒杀队列
    @Bean(name="seckillQueue")
    public Queue createSeckillQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
    }

    //创建秒杀交换机

    @Bean(name="seckillExchanage")
    public DirectExchange basicSeckillExchanage(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"));
    }

    //绑定秒杀
    @Bean(name="SeckillBinding")
    public Binding basicSeckillBinding(){
        return  BindingBuilder.bind(createSeckillQueue()).to(basicSeckillExchanage()).with(env.getProperty("mq.pay.routing.seckillkey"));
    }
}
