package com.jun.redpacket.base;

/**
 * @Company: ****科技有限公司
 * @Class: StateEnum
 * @Description:
 * @Author: HSJ
 * @Version:
 * @Date: 2017/1/10 18:33
 * @E-mail: mr.ajun@foxmail.com
 */
public enum StateEnum {

    /**
     * 获取红包结束
     */
    fetched(0),

    /**
     * 正在获取红包
     */
    fetching(1),

    /**
     * 正在开红包
     */
    opening(2),

    /**
     * 已拆开
     */
    opened(3);

    StateEnum(int state){

    }

}
