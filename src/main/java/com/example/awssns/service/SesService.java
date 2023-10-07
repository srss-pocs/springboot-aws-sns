package com.example.awssns.service;

import static com.example.awssns.config.AwsSnsConfig.getAwsCredentials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import software.amazon.awssdk.services.ses.model.SendRawEmailResponse;

@Component
@Service
@Slf4j
public class SesService {

	@Autowired
	NotificationMessagingTemplate notificationMessagingTemplate;

	public String sendEmail(String arn) throws URISyntaxException {
		final String msg = "This AWS SNS Demo email works!";
		this.notificationMessagingTemplate.sendNotification(arn, msg, "AWS SNS Demo");
		return "Email sent to subscribers";
	}

	public String sendEmailWithAttachment(String arn, String fromEmail,
			String toEmail) throws URISyntaxException, MessagingException, IOException,
			AddressException, javax.mail.MessagingException {

		String subject = "AWS SES Demo";

		String attachment = "C:\\Users\\PC\\Pictures\\1.jpg";

		String body = "<html>" + "<body>" + "<h1>Hello!</h1>" + "<p>Please check your email for attachment" + "</body>"
				+ "</html>";

		Session session = Session.getDefaultInstance(new Properties(), null);

		MimeMessage message = new MimeMessage(session);

		message.setSubject(subject, "UTF-8");
		message.setFrom(new InternetAddress(fromEmail)); // you aws account email
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail)); // recipient email

		MimeMultipart msg_body = new MimeMultipart("alternative");
		MimeBodyPart wrap = new MimeBodyPart();

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(body, "text/html; charset=UTF-8");
		msg_body.addBodyPart(htmlPart);
		wrap.setContent(msg_body);

		MimeMultipart msg = new MimeMultipart("mixed");

		message.setContent(msg);

		msg.addBodyPart(wrap);

		MimeBodyPart att = new MimeBodyPart();
		DataSource fds = new FileDataSource(attachment);
		att.setDataHandler(new DataHandler(fds));
		att.setFileName(fds.getName());

		msg.addBodyPart(att);

		SesClient sesClient = SesClient.builder().credentialsProvider(getAwsCredentials("Access Key ID", "Secret Key"))
				.region(Region.US_EAST_1) // Set your selected region
				.build();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		message.writeTo(outputStream);

		RawMessage rawMessage = RawMessage.builder().data(SdkBytes.fromByteArray(outputStream.toByteArray())).build();

		SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder().rawMessage(rawMessage).build();

		SendRawEmailResponse sendRawEmailResponse = sesClient.sendRawEmail(rawEmailRequest);

		if (sendRawEmailResponse.sdkHttpResponse().isSuccessful()) {
			log.info("Message publishing successful");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					sendRawEmailResponse.sdkHttpResponse().statusText().get());
		}

		return "Email sent to subscribers. Message ID: " + sendRawEmailResponse.messageId();
	}

}
