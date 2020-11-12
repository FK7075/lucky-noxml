package com.lucky.jacklamb.authority.shiro.mgt;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListenerAdapter;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/11/12 18:44
 */
public class LuckyShiroSessionListener extends SessionListenerAdapter {

    public LuckyShiroSessionListener() {
        super();
    }

    @Override
    public void onStart(Session session) {
        super.onStart(session);
        System.out.println("onStart");
    }

    @Override
    public void onStop(Session session) {
        super.onStop(session);
        System.out.println("onStop");
    }

    @Override
    public void onExpiration(Session session) {
        super.onExpiration(session);
        System.out.println("onExpiration");
        SecurityUtils.getSubject().logout();
    }
}
