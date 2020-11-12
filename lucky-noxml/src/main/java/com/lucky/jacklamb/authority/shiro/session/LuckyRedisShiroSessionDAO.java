package com.lucky.jacklamb.authority.shiro.session;

import com.lucky.jacklamb.authority.shiro.cache.LuckyRedisShiroCacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/11/11 7:55 下午
 */
public class LuckyRedisShiroSessionDAO extends CachingSessionDAO {

    public LuckyRedisShiroSessionDAO(){
        setCacheManager(new LuckyRedisShiroCacheManager());
    }

    @Override
    protected void doUpdate(Session session) {

    }

    @Override
    protected void doDelete(Session session) {

    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = "LUCKY"+UUID.randomUUID().toString().toUpperCase().replaceAll("-","");
        assignSessionId(session, sessionId);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        return null;
    }
}
