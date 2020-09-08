package com.lucky.jacklamb.email;

import org.apache.commons.mail.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/9/5 4:46 上午
 */
public class LEmail {


    /** 接收人邮箱 */
    private List<String> to =new ArrayList<>();

    /** 抄送人邮箱*/
    private List<String> cc =new ArrayList<>();

    /** 秘密抄送人邮箱 */
    private List<String> bcc =new ArrayList<>();

    private static EmailConfig emailConfig=EmailConfig.getEmailConfig();

    public LEmail(){}

    public LEmail(String toEmail) {
        to.add(toEmail);
    }

    public LEmail(String...toEmails) {
        to.addAll(Arrays.asList(toEmails));
    }

    public LEmail(List<String> toEmails) {
        to=toEmails;
    }

    public LEmail(List<String> to, List<String> cc, List<String> bcc) {
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
    }

    public LEmail addTo(String...toEmails){
        to.addAll(Arrays.asList(toEmails));
        return this;
    }

    public LEmail addCc(String...ccEmails){
        cc.addAll(Arrays.asList(ccEmails));
        return this;
    }

    public LEmail addBcc(String...bccEmails){
        bcc.addAll(Arrays.asList(bccEmails));
        return this;
    }

    private void init(org.apache.commons.mail.Email email, String subject) throws EmailException {
        email.setHostName(emailConfig.getSmtpHost());
        email.setSmtpPort(emailConfig.getSmtpPort());
        email.setAuthenticator(new DefaultAuthenticator(emailConfig.getEmail(), emailConfig.getPassword()));
        email.setSSLOnConnect(true);
        email.setCharset("UTF-8");
        if(emailConfig.getUsername()==null){
            email.setFrom(emailConfig.getEmail());
        }else {
            email.setFrom(emailConfig.getEmail(),emailConfig.getUsername());
        }
        String[] toArray=new String[to.size()];
        to.toArray(toArray);
        email.addTo(toArray);
        if(!cc.isEmpty()){
            String[] ccArray=new String[cc.size()];
            cc.toArray(ccArray);
            email.addCc(ccArray);
        }
        if(!bcc.isEmpty()){
            String[] bccArray=new String[bcc.size()];
            bcc.toArray(bccArray);
            email.addCc(bccArray);
        }
        email.setSubject(subject);
    }

    /**
     * 发送一封简单的邮件
     * @param subject 主题
     * @param message 内容
     * @throws EmailException
     */
    public void sendSimpleEmail(String subject, String message) throws EmailException {
        org.apache.commons.mail.Email email = new SimpleEmail();
        init(email,subject);
        email.setMsg(message);
        email.send();
    }

    /**
     * 发送一封带附件的邮件
     * @param subject 主题
     * @param message 内容
     * @param attFile 附件(附件为本地资源)
     * @throws EmailException
     */
    public void sendAttachmentEmail(String subject, String message, File...attFile) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        init(email,subject);
        email.setMsg(message);
        // add the attachment
        for (File file : attFile) {
            email.attach(file);
        }
        // send the email
        email.send();
    }

    /**
     * 发送一封带附件的邮件
     * @param subject 主题
     * @param message 内容
     * @param files 附件（本地资源）
     * @throws EmailException
     */
    public void sendAttachmentEmail(String subject, String message, List<File> files) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        init(email,subject);
        email.setMsg(message);
        // add the attachment
        for (File file : files) {
            email.attach(file);
        }
        // send the email
        email.send();
    }

    /**
     * 发送一封带附件的邮件
     * @param subject 主题
     * @param message 内容
     * @param attachment 附件内容（本地资源和网络资源）
     * @throws EmailException
     */
    public void sendAttachmentEmail(String subject, String message, Attachment attachment) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        init(email,subject);
        email.setMsg(message);
        List<File> fileList = attachment.getFileList();
        for (File file : fileList) {
            email.attach(file);
        }
        Map<String, URL> urlMap = attachment.getUrlMap();
        for(Map.Entry<String,URL> en:urlMap.entrySet()){
            email.attach(en.getValue(),en.getKey(),"");
        }
        email.send();
    }

    /**
     * 发送一封HTML格式的邮件
     * @param subject 主题
     * @param htmlMsg Html内容
     * @param alternativeMessage Html内容无法显示时替代的文本内容
     * @throws EmailException
     */
    public void sendHtmlEmail(String subject,HtmlMsg htmlMsg,String alternativeMessage) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        init(email,subject);
        HtmlTemp htmlTemp=new HtmlTemp();
        htmlMsg.setHtmlMsg(email,htmlTemp);
        email.setHtmlMsg(htmlTemp.getHtml());
        email.setTextMsg(alternativeMessage);
        email.send();
    }

    /**
     * 发送一封HTML格式的邮件
     * @param subject 主题
     * @param htmlMsg Html内容
     * @throws EmailException
     */
    public void sendHtmlEmail(String subject,HtmlMsg htmlMsg) throws EmailException {
        sendHtmlEmail(subject,htmlMsg,"Your email client does not support HTML messages");
    }

    private void sendHtmlEmail1(String subject, String message) throws EmailException, MalformedURLException {

        // Create the email message
        HtmlEmail email = new HtmlEmail();
        init(email,subject);

        // embed the image and get the content id
        URL url = new URL("http://www.apache.org/images/asf_logo_wide.gif");
        String cid = email.embed(url, "Apache logo");

        // set the html message
        email.setHtmlMsg("<html>The apache logo - <img src=\"cid:"+cid+"\"></html>");

        // set the alternative message
        email.setTextMsg(message);

        // send the email
        email.send();
    }




}
