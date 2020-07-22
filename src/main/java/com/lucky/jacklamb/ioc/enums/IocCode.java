package com.lucky.jacklamb.ioc.enums;

public enum IocCode {

    /** 普通IOC组件
     * -@Component,
     * -@Configuration,
     * -@ControllerExceptionHandler,
     * -@LuckyServlet,@LuckyFilter,
     * -@LuckyListener,@Conversion
     */
    COMPONENT,

    /**
     * Service组件
     * -@Service
     */
    SERVICE,

    /**
     *Controller组件
     * -@Controller,
     * -@CallController,
     * -@LuckyClient,
     */
    CONTROLLER,

    /**
     * Repository组件
     * -@Repository,
     * -@Mapper
     */
    REPOSITORY,

    /**
     * 自动建表机制相关的组件
     * -@Table
     */
    TABLE,

    /**
     * AOP组件
     * -@Aspect
     */
    ASPECT,

    /**
     * WebSocket组件
     * -@ServerEndpoint
     */
    WEBSOCKET

}