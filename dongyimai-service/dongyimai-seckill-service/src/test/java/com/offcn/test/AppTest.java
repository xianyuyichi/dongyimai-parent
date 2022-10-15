package com.offcn.test;

import com.offcn.sellergoods.utils.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataUnit;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppTest {

    @Test
    public void method(){
       /* List<Date> dateMenus = DateUtil.getDateMenus();
        dateMenus.forEach(d->{
            System.out.println("开始的日期时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d));
            Date date = DateUtil.addDateHour(d, 2);
            System.out.println("结束的日期时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        });*/

       /* new Thread(()->{
            for (int i = 0; i < 50; i++) {
                System.out.println(Thread.currentThread().getName()+":::::::"+i);
            }
        },"thread001").start();

        new Thread(()->{
            for (int i = 0; i < 20; i++) {
                System.out.println(Thread.currentThread().getName()+"======>"+i);
            }
        },"thread002").start();*/

    }

    @Test
    public void add() {
        List<Date> dateMenus = DateUtil.getDateMenus();
        dateMenus.forEach(d -> {
            System.out.println("开始时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d));
            Date date = DateUtil.addDateHour(d, 2);
            System.out.println("结束时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        });
    }

    @Test
    public void kkkk(){
        //需要过滤的集合
        ArrayList<Dao> newList2 = new ArrayList();
        //参照集合
        ArrayList<Dao> newList1 = new ArrayList();

        newList1.add(new Dao("小黄", 1));
        newList1.add(new Dao("小校", 2));
        newList1.add(new Dao("小米", 3));
        newList2.add(new Dao("小黄", 1));
        newList2.add(new Dao("小黄", 2));

        System.out.println("-----过滤前------");
        newList1.forEach(list -> System.out.println(list.toString()));

        System.out.println("-----参照集合------");
        newList2.forEach(list -> System.out.println(list.toString()));

        System.out.println("-----过滤后------");
        List<Dao> testList = newList1.stream().filter(dao1 -> newList2.stream()
                .noneMatch(all -> Objects.equals(dao1.getName(),all.getName()) && Objects.equals(dao1.getAge(),all.getAge()))).collect(Collectors.toList());
        testList.stream().forEach(dao -> System.out.println(dao.toString()));
    }
}

class Dao {
    private String name;
    private Integer age;

    public Dao(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Dao{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}