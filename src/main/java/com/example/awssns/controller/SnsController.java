package com.example.awssns.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.awssns.service.SesService;
import com.example.awssns.service.SnsService;


@RestController
@RequestMapping("/api")
public class SnsController {

	@Autowired
	SnsService snsService;

	@Autowired
	SesService sesService;

	@RequestMapping("/createTopic")
	public String createTopic(@RequestParam("topicName") String topicName) throws URISyntaxException {
		return snsService.createTopic(topicName);
	}

	@RequestMapping("/addSubscribers")
	public String addSubscriberToTopic(@RequestParam("arn") String arn, @RequestParam("email") String email)
			throws URISyntaxException {
		return snsService.addSubscriberToTopic(arn, email);
	}

	@RequestMapping("/sendEmail")
	public String sendEmail(@RequestParam("arn") String arn) throws URISyntaxException {
		return sesService.sendEmail(arn);
	}

	@RequestMapping("/sendEmailWithAttachment")
	public String sendEmailWithAttachment(@RequestParam("arn") String arn, @RequestParam("from") String fromEmail,
			@RequestParam("to") String toEmail) throws URISyntaxException, MessagingException, IOException,
			AddressException, javax.mail.MessagingException {

		return sesService.sendEmailWithAttachment(arn, fromEmail, toEmail);
	}

	@RequestMapping("/sendSMS")
	public String sendSMS(@RequestParam("phone") String phone) throws URISyntaxException {
		return snsService.sendSMS(phone);
	}

	@RequestMapping("/sendBulkSMS")
	public String sendBulkSMS(@RequestParam("arn") String arn) throws URISyntaxException {
		return snsService.sendBulkSMS(arn);
	}
}
