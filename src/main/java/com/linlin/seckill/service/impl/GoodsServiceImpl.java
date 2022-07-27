package com.linlin.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linlin.seckill.mapper.GoodsMapper;
import com.linlin.seckill.pojo.Goods;
import com.linlin.seckill.service.IGoodsService;
import com.linlin.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 啊哈哈哈哈
 * @author zhoubin

 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {


    @Autowired
    private GoodsMapper goodsMapper;

    //获取商品列表
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }


    //获取商品详情
    @Override
    public GoodsVo findGoodsVobyGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
