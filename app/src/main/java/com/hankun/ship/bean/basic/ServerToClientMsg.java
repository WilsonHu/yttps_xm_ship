package com.hankun.ship.bean.basic;

/**
 * 服务端发给App的消息格式
 */
public class ServerToClientMsg {

    public static class MsgType {
        ///改单
        public static final Integer ORDER_CHANGE = 1;
        ///拆单
        public static final Integer ORDER_SPLIT = 2;
        ///取消
        public static final Integer ORDER_CANCEL = 3;
    }

    /**
     * 需求单编号
     */
    private String orderNum;

    /**
     * 机器编号
     */
    private String nameplate;

    /**
     * 消息类别
     */
    private Integer type;

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getNameplate() {
        return nameplate;
    }

    public void setNameplate(String nameplate) {
        this.nameplate = nameplate;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
