package com.example.awssns.service;

import static com.example.awssns.config.AwsSnsConfig.getSnsClient;

import java.net.URISyntaxException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;

@Component
@Service
@Slf4j
public class SnsService {

	public String createTopic(String topicName) throws URISyntaxException {
		// topic name cannot contain spaces
		final CreateTopicRequest topicCreateRequest = CreateTopicRequest.builder().name(topicName).build();

		SnsClient snsClient = getSnsClient();

		final CreateTopicResponse topicCreateResponse = snsClient.createTopic(topicCreateRequest);

		if (topicCreateResponse.sdkHttpResponse().isSuccessful()) {
			log.info("Topic creation successful");
			log.info("Topics: " + snsClient.listTopics());
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					topicCreateResponse.sdkHttpResponse().statusText().get());
		}

		snsClient.close();
		return "Topic ARN: " + topicCreateResponse.topicArn();
	}

	public String addSubscriberToTopic(String arn, String email) throws URISyntaxException {
		SnsClient snsClient = getSnsClient();

		final SubscribeRequest subscribeRequest = SubscribeRequest.builder().topicArn(arn).protocol("email")
				.endpoint(email).build();

		SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);

		if (subscribeResponse.sdkHttpResponse().isSuccessful()) {
			log.info("Subscriber creation successful");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					subscribeResponse.sdkHttpResponse().statusText().get());
		}

		snsClient.close();

		return "Subscription ARN request is pending. To confirm the subscription, check your email.";
	}

	public String sendSMS(String phone) throws URISyntaxException {

		SnsClient snsClient = getSnsClient();

		final PublishRequest publishRequest = PublishRequest.builder().phoneNumber(phone)
				.message("This is AWS SMS Demo").build();

		PublishResponse publishResponse = snsClient.publish(publishRequest);

		if (publishResponse.sdkHttpResponse().isSuccessful()) {
			log.info("Message publishing to phone successful");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					publishResponse.sdkHttpResponse().statusText().get());
		}

		snsClient.close();

		return "SMS sent to " + phone + ". Message ID: " + publishResponse.messageId();
	}

	@RequestMapping("/sendBulkSMS")
	public String sendBulkSMS(String arn) throws URISyntaxException {

		SnsClient snsClient = getSnsClient();

		String[] phoneNumbers = new String[] { "+919010058555", "+919010058555" };

		for (String phoneNumber : phoneNumbers) {
			final SubscribeRequest subscribeRequest = SubscribeRequest.builder().topicArn(arn).protocol("sms")
					.endpoint(phoneNumber).build();

			SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);
			if (subscribeResponse.sdkHttpResponse().isSuccessful()) {
				log.info(phoneNumber + " subscribed to topic " + arn);
			}
		}

		final PublishRequest publishRequest = PublishRequest.builder().topicArn(arn)
				.message("This is AWS SMS Demo").build();

		PublishResponse publishResponse = snsClient.publish(publishRequest);

		if (publishResponse.sdkHttpResponse().isSuccessful()) {
			log.info("Bulk Message sending successful");
			log.info(publishResponse.messageId());
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					publishResponse.sdkHttpResponse().statusText().get());
		}

		snsClient.close();

		return "Done";
	}

}
