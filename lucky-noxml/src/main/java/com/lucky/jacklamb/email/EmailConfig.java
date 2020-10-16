package com.lucky.jacklamb.email;

import com.lucky.jacklamb.utils.file.ini.IniFilePars;
import com.lucky.jacklamb.tcconversion.typechange.JavaConversion;
import com.lucky.jacklamb.utils.reflect.ClassUtils;
import com.lucky.jacklamb.utils.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/9/5 4:47 上午
 */
public class EmailConfig {

    private static EmailConfig emailCfg = null;

    private String smtpHost;
    private int smtpPort = 25;
    private String popHost;
    private int popPort=110;
    private String email;
    private String username;
    private String password;
    private Boolean smtpAuth;
    private Boolean smtpStarttlsEnable;
    private Boolean smtpStarttlsRequired;


    private EmailConfig() {
    }

    public String getPopHost() {
        return popHost;
    }

    public void setPopHost(String popHost) {
        this.popHost = popHost;
    }

    public int getPopPort() {
        return popPort;
    }

    public void setPopPort(int popPort) {
        this.popPort = popPort;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(Boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public Boolean getSmtpStarttlsEnable() {
        return smtpStarttlsEnable;
    }

    public void setSmtpStarttlsEnable(Boolean smtpStarttlsEnable) {
        this.smtpStarttlsEnable = smtpStarttlsEnable;
    }

    public Boolean getSmtpStarttlsRequired() {
        return smtpStarttlsRequired;
    }

    public void setSmtpStarttlsRequired(Boolean smtpStarttlsRequired) {
        this.smtpStarttlsRequired = smtpStarttlsRequired;
    }

    private static void init() {
        Map<String, String> email = new IniFilePars().getSectionMap("Email");
        Field[] allFields = ClassUtils.getAllFields(EmailConfig.class);
        emailCfg = new EmailConfig();
        for (Field field : allFields) {
            if (email.containsKey(field.getName())) {
                FieldUtils.setValue(emailCfg, field, JavaConversion.strToBasic(email.get(field.getName()), field.getType()));
            }
        }
    }

    public static EmailConfig getEmailConfig() {
        if (emailCfg == null) {
            init();
        }
        return emailCfg;
    }
}
