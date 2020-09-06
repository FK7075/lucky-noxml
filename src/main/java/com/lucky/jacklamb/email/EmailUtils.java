package com.lucky.jacklamb.email;

import org.apache.commons.mail.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/9/5 4:46 上午
 */
public class EmailUtils {

    private EmailConfig emailConfig=EmailConfig.getEmailConfig();

    public void simpleEmail(String toEmail,String subject,String message) throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName(emailConfig.getSmtpHost());
        email.setSmtpPort(emailConfig.getSmtpPort());
        email.setAuthenticator(new DefaultAuthenticator(emailConfig.getUsername(), emailConfig.getPassword()));
        email.setSSLOnConnect(true);
        email.setFrom(emailConfig.getUsername());
        email.setSubject(subject);
        email.setMsg(message);
        email.addTo(toEmail);
        email.send();
    }

    public void attachmentEmail(String toEmail, String subject, String message, File attFile ) throws EmailException {
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(attFile.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setName(attFile.getName());

        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName(emailConfig.getSmtpHost());
        email.addTo(toEmail);
        email.setFrom(emailConfig.getUsername());
        email.setSubject(subject);
        email.setMsg(message);

        // add the attachment
        email.attach(attachment);

        // send the email
        email.send();
    }

    public void htmlEmail(String toEmail, String subject, String message ) throws EmailException, MalformedURLException {

        // Create the email message
        HtmlEmail email = new HtmlEmail();
        email.setHostName(emailConfig.getSmtpHost());
        email.addTo(toEmail);
        email.setFrom(emailConfig.getUsername());
        email.setSubject(subject);

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
