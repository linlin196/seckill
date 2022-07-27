package com.linlin.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linlin.seckill.pojo.Goods;
import com.linlin.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
public interface IGoodsService extends IService<Goods> {


    //获取商品列表
    List<GoodsVo> findGoodsVo();


    //获取商品详情
    GoodsVo findGoodsVobyGoodsId(Long goodsId);
}
