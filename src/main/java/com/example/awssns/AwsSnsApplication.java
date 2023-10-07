package com.example.awssns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.context.annotation.Bean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;

@SpringBootApplication
public class AwsSnsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsSnsApplication.class, args);
	}

	@Bean
	AmazonSNS sns() {
		AWSCredentials awsCredentials = new BasicAWSCredentials("AKIA57VYUOSVVBPDMK4O",
                "uzq1sOJhjyBaWNrzDyf6BJYVsBS7x07wCmgphd1s");

		return AmazonSNSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
				.withRegion(Regions.SA_EAST_1).build();
	}

	@Bean
	NotificationMessagingTemplate notificationMessagingTemplate(AmazonSNS amazonSNS) {
		return new NotificationMessagingTemplate(amazonSNS);
	}
}
