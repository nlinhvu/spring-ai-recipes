package dev.linhvu.example.typesafe_simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.Choice;
import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.Score;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.SystemOneResponse;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TypeSafeSimpleApplication {

	private static final Logger log = LoggerFactory.getLogger(TypeSafeSimpleApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TypeSafeSimpleApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(TypeSafeClient typeSafeClient) {
		return args -> {
			SystemOneResponse response = typeSafeClient.systemOne(SystemOneRequest.builder()
					.state("Help! My payouts have been failing for 3 days.")
					.question("is_urgent", Noul.builder()
							.instructions("Does this convey urgency?")
							.whenTrue("Explicitly time-sensitive")
							.whenFalse("No urgency expressed")
							.build())
					.question("department", Choice.builder()
							.instructions("Which team should handle this?")
							.option("billing", "Payments, invoicing, refunds")
							.option("technical", "Bugs, outages, integrations")
							.option("sales", "Pricing, upgrades, new accounts")
							.build())
					.question("frustration", Score.of("How frustrated is the customer?",
							"Calm", "Frustrated", "Very angry"))
					.build());

			log.info("");
			log.info("urgent      : {}", response.noulValue("is_urgent"));
			log.info("department  : {} (confidence {}) {}", response.choiceValue("department"),
					response.choice("department").confidence(), response.choice("department").probabilities());
			log.info("frustration : {} -> {} (confidence {})", response.scoreValue("frustration"),
					response.score("frustration").nearestLabel(), response.score("frustration").confidence());
			log.info("tokens      : {} in, {} out", response.usage().inputTokens(),
					response.usage().outputTokens());
			log.info("request id  : {}", response.requestId());


			if (response.choice("department").confidence() >= 0.8d) {
				log.info("=> routing automatically to {}", response.choiceValue("department"));
			}
			else {
				log.info("=> confidence too low to route automatically, sending to a human");
			}
		};
	}
}
