package com.linlin.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linlin.seckill.pojo.Goods;
import com.linlin.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
public interface GoodsMapper extends BaseMapper<Goods> {


    //获取商品列表
    List<GoodsVo> findGoodsVo();


    //获取商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
