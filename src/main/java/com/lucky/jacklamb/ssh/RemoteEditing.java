package com.lucky.jacklamb.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@FunctionalInterface
public interface RemoteEditing {

    /**
     * 远程编辑
     * @param in 旧的数据
     * @param out 新的数据
     */
    public void editor(InputStream in, OutputStream out) throws IOException;
}
