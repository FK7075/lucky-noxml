package com.lucky.jacklamb.sqlcore.abstractionlayer.cache;

/**
 *
 * @author fk7075
 * @version 1.0
 * @date 2020/9/1 14:23
 */
public interface Cache<Key,Value> {

    Value get(Key key);

    Value put(Key key,Value value);

    boolean containsKey(Key key);

    void clear();
}
