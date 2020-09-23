package com.lucky.jacklamb.ssh;

public class Remote {

    private String user="root";

    private String host="localhost";

    private int port=22;

    private String password="";

    private String identity="~/.ssh/id_rsa";

    private String passphrase="";

    private int connectTimeout = 30000;

    private int sessionTimeout = 30000;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connect_timeout) {
        this.connectTimeout = connect_timeout;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int session_timeout) {
        this.sessionTimeout = session_timeout;
    }
}
