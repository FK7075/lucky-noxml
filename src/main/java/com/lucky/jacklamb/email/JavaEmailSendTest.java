package com.lucky.jacklamb.email;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/4 11:13
 */
public class JavaEmailSendTest {

    private static final String FROM = "XXX";
    private static final String TO = "fk-7075@qq.com";
    private static final String SMTP = "smtp.qq.com";
    private static final String SQM = "XXX";
    private static final File FJ=new File("D:\\IDEA\\JackLamb\\test-two\\src\\main\\resources\\appconfig.ini");

    public static void main(String[] args) throws GeneralSecurityException, MessagingException {

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", SMTP);

        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties, null);
        session.setDebug(true);
        // 创建默认的 MimeMessage 对象
        MimeMessage message = new MimeMessage(session);

        // Set From: 头部头字段
        message.setFrom(new InternetAddress(FROM));

        // Set To: 头部头字段
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));

        // Set Subject: 头部头字段
        message.setSubject("附件-001");

        Multipart multipart=new MimeMultipart();
        BodyPart bodyPart=new MimeBodyPart();
        bodyPart.setText("带附件的邮件测试...");
        multipart.addBodyPart(bodyPart);
        BodyPart dataPart=new MimeBodyPart();
        DataSource source=new FileDataSource(FJ);
        dataPart.setDataHandler(new DataHandler(source));
        dataPart.setFileName("appconfig.ini");
        multipart.addBodyPart(dataPart);
        // 设置消息体
        message.setContent(multipart);
        message.saveChanges();
        Transport transport = session.getTransport("smtp");
        transport.connect(SMTP, FROM, SQM);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();

    }


}
