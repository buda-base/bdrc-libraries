package io.bdrc.libraries;

import java.util.ArrayList;
import java.util.Properties;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.MailBuilder;

public class EmailNotification {

    private Mail mail;

    public EmailNotification(String content, String fromEmail, String fromName, String subject, Properties mailProps, ArrayList<String> recipients) {
        // TODO Auto-generated constructor stub
        Configuration configuration = new Configuration().domain(mailProps.getProperty("mail.domain")).apiKey(mailProps.getProperty("mail.key"));
        MailBuilder build = Mail.using(configuration);
        for (String s : recipients) {
            build = build.to(s);
        }
        build.from(fromName, fromEmail).replyTo(fromEmail).subject(subject).text(content);
        this.mail = build.build();
    }

    public void send() {
        this.mail.send();
    }

}
