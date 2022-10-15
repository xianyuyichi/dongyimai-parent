package com.offcn.seckill.timer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.sellergoods.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class SeckillGoodsPushTask {

    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 存放进行秒杀的商品:
     *   key: seckillGoods_2022072610  属性名: 4  value: seckillGoods(小米)
     *   key: SeckillGoods_12:00  属性名: 秒杀商品的编号  value: seckillGoods
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void method(){
        List<Date> dateMenus = DateUtil.getDateMenus();
        //26 10:00   26 12:00
        for (Date date : dateMenus) {
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(SeckillGoods::getStartTime,date);
            wrapper.lt(SeckillGoods::getEndTime,DateUtil.addDateHour(date,2));
            wrapper.eq(SeckillGoods::getStatus,"1");
            wrapper.gt(SeckillGoods::getStockCount,0);
            //获取redis中已经存放的秒杀商品(所有的秒杀商品编号)
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + DateUtil.date2Str(date)).keys(); //4
            if(!CollectionUtils.isEmpty(keys)){
                wrapper.notIn(SeckillGoods::getId,keys);
            }
            //查询到满足条件的秒杀商品
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(wrapper);
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps("SeckillGoods_"+DateUtil.date2Str(date)).put(seckillGood.getId(),seckillGood);
                Long [] ids = this.getSecKillGoodsIds(seckillGoods.size(),seckillGood.getId());
                //存放秒杀商品的id到redis中
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGood.getId()).leftPushAll(ids);

                //记录某一个秒杀商品的库存数(防止多线程安全问题)
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGood.getId(),seckillGood.getStockCount());
            }


            //设置秒杀商品在redis中存放有效时间
            redisTemplate.boundHashOps("SeckillGoods_"+DateUtil.date2Str(date)).expire(2, TimeUnit.HOURS);
        }
    }

    /**
     * 定义一个存放秒杀商品的id数组
     */
    private Long[] getSecKillGoodsIds(int len,Long seckillGoodsId){
        Long[] ids = new Long[len];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = seckillGoodsId;
        }
        return ids;
    }
}
