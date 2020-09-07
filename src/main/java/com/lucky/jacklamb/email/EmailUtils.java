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
public class EmailUtils {

    private String[] toEmailArray;

    private static EmailConfig emailConfig=EmailConfig.getEmailConfig();

    public EmailUtils(String toEmail) {
        toEmailArray=new String[]{toEmail};
    }

    public EmailUtils(String...toEmails) {
        toEmailArray=toEmails;
    }

    public EmailUtils(List<String> toEmails) {
        toEmailArray=new String[toEmails.size()];
        toEmails.toArray(toEmailArray);
    }

    private void init(Email email,String[] ccEmails, String[] bccEmail, String subject) throws EmailException {
        email.setHostName(emailConfig.getSmtpHost());
        email.setSmtpPort(emailConfig.getSmtpPort());
        email.setAuthenticator(new DefaultAuthenticator(emailConfig.getUsername(), emailConfig.getPassword()));
        email.setSSLOnConnect(true);
        email.setFrom(emailConfig.getUsername());
        email.addTo(toEmailArray);
        if(ccEmails!=null&&ccEmails.length!=0){
            email.addCc(ccEmails);
        }
        if(bccEmail!=null&&bccEmail.length!=0){
            email.addBcc(bccEmail);
        }
        email.setSubject(subject);
    }

    public void sendSimpleEmail(String subject,String message) throws EmailException {
        Email email = new SimpleEmail();
        init(email,null,null,subject);
        email.setMsg(message);
        email.send();
    }

    public void sendSimpleEmail(String[] ccEmails,String[] bccEmail, String subject, String message) throws EmailException {
        Email email = new SimpleEmail();
        init(email,ccEmails,bccEmail,subject);
        email.setMsg(message);
        email.send();
    }

    public void sendAttachmentEmail(String subject, String message, File...attFile) throws EmailException {
        sendAttachmentEmail(null,null,subject,message,attFile);
    }

    public void sendAttachmentEmail(String toEmail, String subject, String message,  List<File> files) throws EmailException {
        sendAttachmentEmail(null,null,subject,message,files);
    }

    public void sendAttachmentEmail(String toEmail, String subject, String message,  Attachment attachment) throws EmailException {
        sendAttachmentEmail(null,null,subject,message,attachment);
    }

    public void sendAttachmentEmail(String[] ccEmails,String[] bccEmail,String subject, String message, File...attFile) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        init(email,ccEmails,bccEmail,subject);
        email.setMsg(message);
        // add the attachment
        for (File file : attFile) {
            email.attach(file);
        }
        // send the email
        email.send();
    }

    public void sendAttachmentEmail(String[] ccEmails,String[] bccEmail,String subject, String message, Attachment attachment) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        init(email,ccEmails,bccEmail,subject);
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

    public void sendAttachmentEmail(String[] ccEmails,String[] bccEmail,String subject, String message, List<File> files) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        init(email,ccEmails,bccEmail,subject);
        email.setMsg(message);
        // add the attachment
        for (File file : files) {
            email.attach(file);
        }
        // send the email
        email.send();
    }

    public void sendHtmlEmail(String subject,HtmlMsg htmlMsg) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        init(email,null,null,subject);
        email.setHtmlMsg(htmlMsg.getHtmlMsg(email));
        email.send();
    }

    public void sendHtmlEmail1(String subject, String message) throws EmailException, MalformedURLException {

        // Create the email message
        HtmlEmail email = new HtmlEmail();
        init(email,null,null,subject);

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
