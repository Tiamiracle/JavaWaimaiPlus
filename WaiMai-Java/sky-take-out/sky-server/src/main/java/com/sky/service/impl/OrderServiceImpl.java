package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.BaseException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.CheckUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.webSocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShopCartMapper shopCartMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CheckUtil checkUtil;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;


    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId= BaseContext.getCurrentId();
        AddressBook addressBook=addressMapper.getAddressById(ordersSubmitDTO.getAddressBookId());
//        校验:1.地址2.商品
        if(ordersSubmitDTO.getAddressBookId() == null||ordersSubmitDTO.getAddressBookId()<=0||addressBook==null){
            throw new BaseException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        List<ShoppingCart> shoppingCartList=shopCartMapper.list(userId);
        if(shoppingCartList==null||shoppingCartList.size()==0){
            throw new BaseException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
//        提交订单数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setUserId(userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getProvinceName()+addressBook.getDistrictName()+addressBook.getDetail());
        orderMapper.insert(orders);
        Long orderId=orders.getId();
//        遍历提交订单详情数据
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for(ShoppingCart shoppingCart:shoppingCartList){
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail, "id");
            orderDetail.setOrderId(orderId);
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insert(orderDetailList);
//        清空购物车
        shopCartMapper.clean(userId);
//        封装返回数据
        OrderSubmitVO orderSubmitVO=new OrderSubmitVO();
        orderSubmitVO.setId(orderId);
        orderSubmitVO.setOrderNumber(orders.getNumber());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        orderSubmitVO.setOrderTime(orders.getOrderTime());
        return orderSubmitVO;
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        校验
        Long userId=BaseContext.getCurrentId();
        checkUtil.checkStr(ordersPaymentDTO.getOrderNumber());
        Orders orders=orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber(),userId);
        if(orders==null){
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus()!=Orders.PENDING_PAYMENT){
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
 //        直接调用支付成功接口
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(orders);
//        给管理端发送来单通知

        Map map=new HashMap<>();
        map.put("type",1);//1表示来单，2表示催单
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+orders.getNumber());
        String content=JSONObject.toJSONString(map);
        webSocketServer.sendNotice(content);
        return null;
//        Long userId=BaseContext.getCurrentId();
//        User user=userMapper.getById(userId);
////        支付流程:1.调用微信接口得到预支付标识2.处理并封装返回
//        JSONObject payJson=weChatPayUtil.pay(ordersPaymentDTO.getOrderNumber(), orders.getAmount(),"外卖订单",user.getOpenid());
//        OrderPaymentVO vo = new OrderPaymentVO();
//        vo.setTimeStamp(payJson.getString("timeStamp"));
//        vo.setNonceStr(payJson.getString("nonceStr"));
//        vo.setPackageStr(payJson.getString("package")); // 工具类key是package，VO属性是packageStr
//        vo.setSignType("RSA");
//        vo.setPaySign(payJson.getString("paySign"));
//        return vo;
    }

    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> result= orderMapper.page(ordersPageQueryDTO);
        PageResult pageResult = new PageResult(result.getTotal(), result.getResult());
        return pageResult;
    }
    private void clearCacheAfterOrderComplete() {
         redisTemplate.delete(redisTemplate.keys("dishCache::*"));
         redisTemplate.delete(redisTemplate.keys("setmealCache::*"));
    }
    @Override
    public OrdersDTO getOrderDetail(Long id) {
        List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(id);
        Orders orders=orderMapper.getByOrderId(id);
        OrdersDTO res=new OrdersDTO();
        BeanUtils.copyProperties(orders,res);
        res.setOrderDetailList(orderDetails);
        return res;
    }

    @Override
    public void confirm(OrderDefult orderDefult) {
//        校验
        Orders orders=orderMapper.getByOrderId(orderDefult.getId());
        if(orders.getStatus()!=Orders.TO_BE_CONFIRMED){
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders=orderMapper.getByOrderId(ordersRejectionDTO.getId());
        if(orders.getStatus()!=Orders.TO_BE_CONFIRMED){
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setPayStatus(Orders.REFUND);
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders=orderMapper.getByOrderId(id);
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus()!=Orders.CONFIRMED){
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    @Override
    public PageResult userPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        Long userId=BaseContext.getCurrentId();
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> orderList= orderMapper.userPage(ordersPageQueryDTO,userId);
        List<OrdersDTO> dtoList = new ArrayList<>();
        for(Orders order:orderList){
            OrdersDTO ordersDTO=new OrdersDTO();
            BeanUtils.copyProperties(order,ordersDTO);
            List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(order.getId());
            ordersDTO.setOrderDetailList(orderDetails);
            dtoList.add(ordersDTO);
        }
        PageResult pageResult = new PageResult(orderList.getTotal(), dtoList);
        return pageResult;
    }

    @Override
    public void cancel(Long id) {
        Orders orders=orderMapper.getByOrderId(id);
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer status = orders.getStatus();
        if (!Orders.PENDING_PAYMENT.equals(status) && !Orders.TO_BE_CONFIRMED.equals(status)) {
            throw new BaseException("当前订单状态不支持手动取消，请联系商家处理");
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
//        orders.setPayStatus(Orders.REFUND);
        orders.setCancelReason("用户自行取消订单");
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders=orderMapper.getByOrderId(id);
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus()!=Orders.DELIVERY_IN_PROGRESS){
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
        clearCacheAfterOrderComplete();
        String key = "dishRecommend::" + orders.getUserId();
        redisTemplate.delete(key);
    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders=orderMapper.getByOrderId(ordersCancelDTO.getId());
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setPayStatus(Orders.REFUND);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders=orderMapper.getByOrderId(id);
        if (orders == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus()!=Orders.TO_BE_CONFIRMED){
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map=new HashMap<>();
        map.put("type",2);//1表示来单，2表示催单
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+orders.getNumber());
        String content=JSONObject.toJSONString(map);
        webSocketServer.sendNotice(content);
    }

    @Override
    @Transactional
    public OrdersDTO repetition(Long id) {
        Orders oldOrder=orderMapper.getByOrderId(id);
        if (oldOrder == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(id);
        OrdersDTO ordersDTO = new OrdersDTO();
        BeanUtils.copyProperties(oldOrder, ordersDTO);
        ordersDTO.setOrderDetailList(orderDetails);
        return ordersDTO;
    }

    /**
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
       Long confirmed=orderMapper.countByStatus(Orders.CONFIRMED,null);
       Long deliveryInProgress=orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS,null);
       Long toBeConfirmed=orderMapper.countByStatus(Orders.TO_BE_CONFIRMED,null);
//       通过builder模式构建返回对象
       return OrderStatisticsVO.builder().confirmed(confirmed).toBeConfirmed(toBeConfirmed).deliveryInProgress(deliveryInProgress).build();

    }
}
