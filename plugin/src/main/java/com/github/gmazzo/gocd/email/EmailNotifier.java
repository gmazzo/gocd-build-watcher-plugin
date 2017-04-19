package com.github.gmazzo.gocd.email;

import com.github.gmazzo.gocd.Notifier;
import com.github.gmazzo.gocd.model.Message;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import static com.github.gmazzo.utils.StringUtils.isBlank;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3;

public class EmailNotifier implements Notifier {
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final boolean ssl;
    private final String from;
    private final String cc;

    public EmailNotifier(String hostname, int port, String username, String password, boolean ssl, String from, String cc) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ssl = ssl;
        this.from = from;
        this.cc = cc;
    }

    @Override
    public void sendMessage(String userEmail, Message message) {
        String html = buildHtml(message);

        try {
            Email email = new HtmlEmail();
            email.setHostName(hostname);
            email.setSmtpPort(port);
            if (!isBlank(username)) {
                email.setAuthenticator(new DefaultAuthenticator(username, password));
            }
            email.setSSLOnConnect(ssl);
            email.setFrom(from);
            email.setSubject(message.text);
            email.setMsg(html);
            email.addTo(userEmail);
            if (!isBlank(cc)) {
                for (String cc : this.cc.split("\\s*[,;]\\s*")) {
                    email.addTo(cc);
                }
            }
            email.send();

        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildHtml(Message message) {
        StringBuilder tags = new StringBuilder();

        boolean col2 = false;
        int rowSpan = 2;
        for (Message.Tag tag : message.tags) {
            col2 |= !tag.isShort;
            if (col2) {
                tags.append("</tr><tr>");
                rowSpan++;
                col2 = !tag.isShort;

            } else {
                col2 = true;
            }

            tags.append("<td width=\"100\"><b>");
            tags.append(escapeHtml3(tag.name));
            tags.append("</b><br/>");
            tags.append(escapeHtml3(tag.value));
            tags.append("</td>");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr><td rowSpan=\"");
        sb.append(rowSpan);
        sb.append("\" style=\"background-color: ");
        sb.append(message.type == Message.Type.GOOD ? "#36a64f" :
                message.type == Message.Type.BAD ? "#d00000" : "#E8E8E8");
        sb.append(";\" width=\"1\">&nbsp;</td><td colSpan=\"2\">");
        if (message.link != null) {
            sb.append("<a href=\"");
            sb.append(message.link);
            sb.append("\">");
            sb.append(escapeHtml3(message.text));
            sb.append("</a>");
        } else {
            sb.append(escapeHtml3(message.text));
        }
        sb.append("</td></tr>");
        if (tags.length() > 0) {
            sb.append("<tr>");
            sb.append(tags);
            sb.append("<tr/>");
        }
        sb.append("</table>");
        return sb.toString();
    }

}
