package com.offcn.seckill.task;

import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.dao.SeckillOrderMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.seckill.pojo.SeckillStatus;
import com.offcn.sellergoods.entity.StatusCode;
import com.offcn.sellergoods.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    /***
     * 多线程下单操作
     */
    @Async
    public void createOrder(){
       

        //从队列中获取秒杀商品的状态对象
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
        if(seckillStatus != null) {
            String time = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();
            String username = seckillStatus.getUsername();

            //从redis中获取秒杀商品的id
            Object obj = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).rightPop();

            if(obj == null){
                //删除用户秒杀商品的状态信息
                redisTemplate.boundHashOps("UserQueueStatus").delete(username);
                //上传用户点击的次数
                redisTemplate.boundHashOps("UserQueueCount").delete(username);

                return;
            }

            //根据秒杀商品的编号,查询redis中存放的秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {
                throw new RuntimeException("该商品已经售完");
            }
            //创建秒杀订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(seckillGoods.getId());
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            seckillOrder.setStatus("0");//未支付
            seckillOrder.setUserId(username);
            //保存秒杀订单
            seckillOrderMapper.insert(seckillOrder);

            //将秒杀订单存放到redis中
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            //从redis中记录的秒杀商品的库存中扣减数量(原子性)
            Long count = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1);

            //重新设置库存
            seckillGoods.setStockCount(count.intValue());

            //库存为0,到数据库中更新秒杀商品的库存,将redis中存放的这个秒杀商品进行删除
            if (seckillGoods.getStockCount() == 0) {
                seckillGoodsMapper.updateById(seckillGoods);
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
            } else {
                //重新将最新的秒杀商品设置到缓存中
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, seckillGoods);
            }

            seckillStatus.setStatus(2);//等待支付
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(seckillOrder.getMoney().floatValue());
            //重新设置用户所抢购商品的状态
            redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);
        }
    }
}
