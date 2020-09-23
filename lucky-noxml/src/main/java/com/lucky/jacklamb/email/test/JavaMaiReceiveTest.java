package com.lucky.jacklamb.email.test;

import org.apache.commons.mail.util.MimeMessageParser;

import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;

/**
 * @author fk7075
 * @version 1.0
 * @date 2020/9/4 16:17
 */
public class JavaMaiReceiveTest {

    private static final String EMAIL = "1814375626@qq.com";
    private static final String POP = "pop.qq.com";
    private static final String SQM = "amzhoyvcmcxkcagg";

    public static void main(String[] args) throws Exception {
        //创建Session对象
        Properties prop=System.getProperties();
        Session session=Session.getDefaultInstance(prop);
        Store store=session.getStore("pop3");
        store.connect(POP,EMAIL,SQM);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages(1,3);
        for (Message message : messages) {
            MimeMessageParser parser=new MimeMessageParser((MimeMessage) message);
            System.out.println("标题："+parser.getSubject());
            System.out.println("发件人："+parser.getFrom());
            System.out.println("发件时间："+message.getSentDate());
            System.out.println("类型："+message.getContentType());
            if(parser.parse().hasPlainContent()){//文本内容

            }
            if(parser.parse().hasHtmlContent()){//HTML内容


            }
            if(parser.parse().hasAttachments()){//多文件
                List<DataSource> attachmentList = parser.parse().getAttachmentList();
            }
            getContent(message);
            System.out.println("################################\n");
        }
        folder.close(false);
        store.close();
    }

    public static void getContent(Part part) throws Exception {
        if(part.isMimeType("text/plain")){
            System.out.println("内容："+part.getContent());
        }else if(part.isMimeType("text/html")){
            System.out.println("内容："+part.getContent());
        }else if(part.isMimeType("multipart/*")){
            MimeMultipart mp= (MimeMultipart) part.getContent();
            for(int i=0,j=mp.getCount();i<j;i++){
                BodyPart bodyPart = mp.getBodyPart(i);
                getContent(bodyPart);
            }
        }
    }
}
