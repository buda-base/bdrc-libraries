package io.bdrc.libraries;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailNotification {

    Message message;

    public EmailNotification(String content, String fromEmail, String fromName, String subject, Properties prop, ArrayList<String> recipients) throws AddressException, MessagingException {

        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(prop.getProperty("emailUser"), prop.getProperty("emailPassword"));
            }
        });
        String rList = "";
        for (String ad : recipients) {
            rList = rList + ad + ",";
        }
        rList = rList.substring(0, rList.length() - 1);
        this.message = new MimeMessage(session);
        this.message.setFrom(new InternetAddress(fromEmail));
        this.message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rList));
        this.message.setSubject(subject);
        this.message.setText(content);
    }

    public void send() throws MessagingException {
        Transport.send(this.message);
    }

}
