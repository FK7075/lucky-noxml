package com.lucky.jacklamb.start;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.ioc.config.ServerConfig;

public class LuckyShutdown {
	
	private final ServerConfig serverCfg=AppConfig.getAppConfig().getServerConfig();
	
	public void shutdown() throws IOException {
        Socket socket = new Socket("localhost", serverCfg.getClosePort());
        OutputStream stream = socket.getOutputStream();
        for(int i = 0;i < serverCfg.getShutdown().length();i++)
            stream.write(serverCfg.getShutdown().charAt(i));
        stream.flush();
        stream.close();
        socket.close();
	}
	
	public void shutdown(String host,int port,String command) throws IOException {
        Socket socket = new Socket(host, port);
        OutputStream stream = socket.getOutputStream();
        for(int i = 0;i < command.length();i++)
            stream.write(command.charAt(i));
        stream.flush();
        stream.close();
        socket.close();
	}

}
