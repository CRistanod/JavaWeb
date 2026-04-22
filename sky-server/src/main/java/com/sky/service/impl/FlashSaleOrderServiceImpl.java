package com.sky.service.impl;

import com.sky.constant.FlashSaleRabbitConstant;
import com.sky.constant.FlashSaleRedisConstant;
import com.sky.entity.AddressBook;
import com.sky.entity.Dish;
import com.sky.entity.FlashSaleActivity;
import com.sky.entity.FlashSaleOrder;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.exception.FlashSaleBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlashSaleActivityMapper;
import com.sky.mapper.FlashSaleOrderMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.message.FlashSaleOrderMessage;
import com.sky.message.FlashSaleTimeoutMessage;
import com.sky.service.FlashSaleOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;

/**
 * 特卖订单服务。
 * <p>
 * 负责处理“抢购资格通过后”的后续动作：
 * 1. 创建正式订单和订单明细
 * 2. 写入特卖记录表
 * 3. 发送超时检查消息
 * 4. 支付成功时更新特卖记录状态
 * 5. 超时取消/人工取消时回补库存和限购资格
 */
@Service
@Slf4j
public class FlashSaleOrderServiceImpl implements FlashSaleOrderService {

    private static final long REQUEST_RESULT_TTL_DAYS = 7L;
    private static final String TIMEOUT_MILLIS = "600000";

    @Autowired
    private FlashSaleActivityMapper flashSaleActivityMapper;
    @Autowired
    private FlashSaleOrderMapper flashSaleOrderMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建特卖订单的统一入口。
     * <p>
     * 这里包一层 try-catch 的原因是：
     * - MQ 重复消费时，需要按 requestId 做幂等兜底
     * - 建单过程中任一环节失败，都要把 Redis 已经占掉的资格回滚
     */
    @Override
    public void createFlashSaleOrder(FlashSaleOrderMessage message) {
        try {
            doCreateFlashSaleOrder(message);
        } catch (DuplicateKeyException e) {
            log.warn("特卖订单重复消费，requestId={}", message.getRequestId());
            FlashSaleOrder flashSaleOrder = flashSaleOrderMapper.getByRequestId(message.getRequestId());
            if (flashSaleOrder != null) {
                setRequestResult(message.getRequestId(), FlashSaleRedisConstant.REQUEST_SUCCESS_PREFIX + flashSaleOrder.getId());
                return;
            }
            setFailResultAndRollbackQuota(message.getActivityId(), message.getUserId(), message.getRequestId(), "订单创建重复，请稍后重试");
        } catch (Exception e) {
            log.error("异步创建特卖订单失败，requestId={}", message.getRequestId(), e);
            setFailResultAndRollbackQuota(message.getActivityId(), message.getUserId(), message.getRequestId(), e.getMessage());
        }
    }

    /**
     * 真正执行建单逻辑。
     * <p>
     * 关键顺序：
     * 1. requestId 幂等校验
     * 2. 活动/地址/菜品/用户有效性校验
     * 3. 数据库库存扣减，形成最终落库
     * 4. 创建 orders 和 order_detail
     * 5. 创建 flash_sale_order
     * 6. 写入 SUCCESS 结果，并发送超时检查消息
     */
    @Transactional
    public void doCreateFlashSaleOrder(FlashSaleOrderMessage message) {
        // 第一道幂等：如果该 requestId 已经建过单，直接返回成功结果。
        FlashSaleOrder existingOrder = flashSaleOrderMapper.getByRequestId(message.getRequestId());
        if (existingOrder != null) {
            setRequestResult(message.getRequestId(), FlashSaleRedisConstant.REQUEST_SUCCESS_PREFIX + existingOrder.getId());
            return;
        }

        // 活动必须仍然存在且为启用状态，避免活动下线后消息还继续消费。
        FlashSaleActivity activity = flashSaleActivityMapper.getById(message.getActivityId());
        if (activity == null || !FlashSaleActivity.ENABLED.equals(activity.getStatus())) {
            throw new FlashSaleBusinessException("活动不可用");
        }

        // 再次校验地址归属，防止前置参数被绕过或消息数据不一致。
        AddressBook addressBook = addressBookMapper.getById(message.getAddressBookId());
        if (addressBook == null || !message.getUserId().equals(addressBook.getUserId())) {
            throw new FlashSaleBusinessException("收货地址不存在");
        }

        // 活动挂载的菜品必须仍然处于上架状态。
        Dish dish = dishMapper.getById(activity.getDishId());
        if (dish == null || !Integer.valueOf(1).equals(dish.getStatus())) {
            throw new FlashSaleBusinessException("活动菜品不可售");
        }

        // 用户信息用于补齐正式订单的用户名等字段。
        User user = userMapper.getById(message.getUserId());
        if (user == null) {
            throw new FlashSaleBusinessException("用户不存在");
        }

        // Redis 中的库存扣减只是准入控制；这里再扣数据库库存，保证最终数据一致。
        int affectedRows = flashSaleActivityMapper.decrementAvailableStock(activity.getId(), 1);
        if (affectedRows == 0) {
            throw new FlashSaleBusinessException("活动库存不足");
        }

        // 复用现有 orders 表创建正式订单，不额外造一套“特卖正式订单”体系。
        Orders order = Orders.builder()
                .number(generateOrderNumber(message.getUserId()))
                .status(Orders.PENDING_PAYMENT)
                .userId(message.getUserId())
                .addressBookId(message.getAddressBookId())
                .orderTime(LocalDateTime.now())
                .payStatus(Orders.UN_PAID)
                .amount(activity.getSalePrice())
                .remark(message.getRemark())
                .userName(user.getName())
                .phone(addressBook.getPhone())
                .address(addressBook.getDetail())
                .consignee(addressBook.getConsignee())
                .packAmount(0)
                .tablewareNumber(0)
                .tablewareStatus(0)
                .deliveryStatus(1)
                .build();
        orderMapper.insert(order);

        // 当前一期只支持单菜品特卖，因此订单明细只写 1 条。
        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(order.getId())
                .dishId(dish.getId())
                .name(dish.getName())
                .image(dish.getImage())
                .number(1)
                .amount(activity.getSalePrice())
                .build();
        orderDetailMapper.insertBatch(Collections.singletonList(orderDetail));

        // 特卖记录表用于记录活动和正式订单的关联关系，同时承担幂等和状态流转职责。
        FlashSaleOrder flashSaleOrder = FlashSaleOrder.builder()
                .activityId(activity.getId())
                .userId(message.getUserId())
                .orderId(order.getId())
                .requestId(message.getRequestId())
                .status(FlashSaleOrder.CREATED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        flashSaleOrderMapper.insert(flashSaleOrder);

        // 前端轮询结果时，最终就是从这里读取 SUCCESS:flashSaleOrderId。
        setRequestResult(message.getRequestId(), FlashSaleRedisConstant.REQUEST_SUCCESS_PREFIX + flashSaleOrder.getId());

        // 建单成功后发送延迟检查消息，后续用于处理超时未支付订单。
        sendTimeoutMessage(flashSaleOrder.getId());
    }

    /**
     * 处理超时检查。
     * <p>
     * 消息到达这里只代表“检查时机到了”，不代表订单一定超时。
     * 还需要再次查特卖记录和正式订单状态后再决定是否取消。
     */
    @Override
    @Transactional
    public void handleTimeout(Long flashSaleOrderId) {
        FlashSaleOrder flashSaleOrder = flashSaleOrderMapper.getById(flashSaleOrderId);
        if (flashSaleOrder == null || !FlashSaleOrder.CREATED.equals(flashSaleOrder.getStatus())) {
            return;
        }

        Orders orders = orderMapper.getById(flashSaleOrder.getOrderId());
        if (orders == null) {
            // 正式订单异常缺失时，也要关闭特卖记录并回补资格，避免库存被吃死。
            updateFlashSaleOrderStatus(flashSaleOrder, FlashSaleOrder.CANCELLED, "订单不存在，自动取消");
            flashSaleActivityMapper.incrementAvailableStock(flashSaleOrder.getActivityId(), 1);
            restoreQuota(flashSaleOrder.getActivityId(), flashSaleOrder.getUserId());
            setRequestResult(flashSaleOrder.getRequestId(), FlashSaleRedisConstant.REQUEST_FAIL_PREFIX + "订单不存在，已自动取消");
            return;
        }

        // 如果订单已经支付，就把特卖记录补标为已支付，不再继续取消。
        if (Orders.PAID.equals(orders.getPayStatus())) {
            updateFlashSaleOrderStatus(flashSaleOrder, FlashSaleOrder.PAID, null);
            return;
        }

        // 订单仍未支付，则执行正式取消，并回补库存与限购资格。
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("特卖订单超时未支付，自动取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

        updateFlashSaleOrderStatus(flashSaleOrder, FlashSaleOrder.CANCELLED, "特卖订单超时未支付，自动取消");
        flashSaleActivityMapper.incrementAvailableStock(flashSaleOrder.getActivityId(), 1);
        restoreQuota(flashSaleOrder.getActivityId(), flashSaleOrder.getUserId());
        setRequestResult(flashSaleOrder.getRequestId(), FlashSaleRedisConstant.REQUEST_FAIL_PREFIX + "订单超时未支付，已自动取消");
    }

    /**
     * 普通支付回调成功后，联动更新特卖记录状态。
     */
    @Override
    @Transactional
    public void markPaidByOrderId(Long orderId) {
        FlashSaleOrder flashSaleOrder = flashSaleOrderMapper.getByOrderId(orderId);
        if (flashSaleOrder == null
                || FlashSaleOrder.PAID.equals(flashSaleOrder.getStatus())
                || FlashSaleOrder.CANCELLED.equals(flashSaleOrder.getStatus())) {
            return;
        }
        updateFlashSaleOrderStatus(flashSaleOrder, FlashSaleOrder.PAID, null);
        setRequestResult(flashSaleOrder.getRequestId(), FlashSaleRedisConstant.REQUEST_SUCCESS_PREFIX + flashSaleOrder.getId());
    }

    /**
     * 处理人工取消场景。
     * 包括：用户取消、商家拒单、管理端取消。
     */
    @Override
    @Transactional
    public void handleOrderCancellation(Long orderId, String reason) {
        FlashSaleOrder flashSaleOrder = flashSaleOrderMapper.getByOrderId(orderId);
        if (flashSaleOrder == null || !FlashSaleOrder.CREATED.equals(flashSaleOrder.getStatus())) {
            return;
        }

        // 只要订单还处于 CREATED 阶段，被取消后都要回补活动库存和用户资格。
        updateFlashSaleOrderStatus(flashSaleOrder, FlashSaleOrder.CANCELLED, normalizeMessage(reason));
        flashSaleActivityMapper.incrementAvailableStock(flashSaleOrder.getActivityId(), 1);
        restoreQuota(flashSaleOrder.getActivityId(), flashSaleOrder.getUserId());
        setRequestResult(flashSaleOrder.getRequestId(), FlashSaleRedisConstant.REQUEST_FAIL_PREFIX + normalizeMessage(reason));
    }

    /**
     * 建单失败时的统一兜底：
     * 回滚 Redis 预占配额，并写入失败结果。
     */
    private void setFailResultAndRollbackQuota(Long activityId, Long userId, String requestId, String message) {
        restoreQuota(activityId, userId);
        setRequestResult(requestId, FlashSaleRedisConstant.REQUEST_FAIL_PREFIX + normalizeMessage(message));
    }

    /**
     * 回补 Redis 中的运行时配额。
     * 这里回滚的是抢购入口阶段预占的：
     * 1. 库存
     * 2. 用户已购次数
     */
    private void restoreQuota(Long activityId, Long userId) {
        String stockKey = buildActivityStockKey(activityId);
        String userCountKey = buildActivityUserCountKey(activityId);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
            stringRedisTemplate.opsForValue().increment(stockKey);
        }
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        Long count = hashOperations.increment(userCountKey, String.valueOf(userId), -1L);
        if (count != null && count <= 0) {
            hashOperations.delete(userCountKey, String.valueOf(userId));
        }
    }

    /**
     * 更新特卖记录状态的公共方法。
     */
    private void updateFlashSaleOrderStatus(FlashSaleOrder flashSaleOrder, Integer status, String failReason) {
        flashSaleOrder.setStatus(status);
        flashSaleOrder.setFailReason(failReason);
        flashSaleOrder.setUpdateTime(LocalDateTime.now());
        flashSaleOrderMapper.update(flashSaleOrder);
    }

    /**
     * 发送超时检查消息。
     * 这里通过 TTL + 死信队列实现延迟效果。
     */
    private void sendTimeoutMessage(Long flashSaleOrderId) {
        FlashSaleTimeoutMessage timeoutMessage = FlashSaleTimeoutMessage.builder()
                .flashSaleOrderId(flashSaleOrderId)
                .build();
        rabbitTemplate.convertAndSend(
                FlashSaleRabbitConstant.TIMEOUT_EXCHANGE,
                FlashSaleRabbitConstant.TIMEOUT_ROUTING_KEY,
                timeoutMessage,
                msg -> {
                    msg.getMessageProperties().setExpiration(TIMEOUT_MILLIS);
                    return msg;
                }
        );
    }

    /**
     * 生成正式订单号。
     */
    private String generateOrderNumber(Long userId) {
        return System.currentTimeMillis() + String.format("%03d", Math.abs(userId.intValue() % 1000)) + (100 + new Random().nextInt(900));
    }

    /**
     * 统一写入抢购请求结果，供前端轮询查询。
     */
    private void setRequestResult(String requestId, String value) {
        String key = buildRequestKey(requestId);
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofDays(REQUEST_RESULT_TTL_DAYS));
    }

    /**
     * 统一处理空异常信息，避免前端收到空字符串。
     */
    private String normalizeMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "订单创建失败";
        }
        return message;
    }

    private String buildActivityStockKey(Long activityId) {
        return String.format(FlashSaleRedisConstant.ACTIVITY_STOCK_KEY, activityId);
    }

    private String buildActivityUserCountKey(Long activityId) {
        return String.format(FlashSaleRedisConstant.ACTIVITY_USER_COUNT_KEY, activityId);
    }

    private String buildRequestKey(String requestId) {
        return String.format(FlashSaleRedisConstant.REQUEST_RESULT_KEY, requestId);
    }
}
