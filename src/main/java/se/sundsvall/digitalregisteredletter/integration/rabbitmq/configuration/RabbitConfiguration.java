package se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.MethodInvocationRecoverer;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterSendEvent;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterStatusEvent;
import tools.jackson.databind.json.JsonMapper;

import static se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Constants.DIGITAL_REGISTERED_LETTER_EXCHANGE;
import static se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Constants.STATUS_DIGITAL_REGISTERED_LETTER_QUEUE;

@Configuration
public class RabbitConfiguration {

	@Value("${spring.rabbitmq.listener.simple.retry.max-attempts:3}")
	private int maxAttempts;

	@Value("${spring.rabbitmq.listener.simple.retry.initial-interval:10000}")
	private long initialInterval;

	@Value("${spring.rabbitmq.listener.simple.retry.multiplier:2.0}")
	private double multiplier;

	@Value("${spring.rabbitmq.listener.simple.retry.max-interval:100000}")
	private long maxInterval;

	@Bean
	public JacksonJsonMessageConverter messageConverter(final JsonMapper jsonMapper) {
		return new JacksonJsonMessageConverter(jsonMapper);
	}

	/**
	 * Even if the maximum number of retries is exhausted, we want to handle the failed message and send a status event to
	 * the status queue. Also, the message is sent to a dead letter queue using a RabbitMQ policy (Not part of the java
	 * code).
	 */
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(final ConnectionFactory connectionFactory, final JacksonJsonMessageConverter messageConverter, final RabbitTemplate rabbitTemplate) {
		MethodInvocationRecoverer<?> recoverer = (args, cause) -> {
			for (var arg : args) {
				if (arg instanceof org.springframework.amqp.core.Message message) {
					var event = messageConverter.fromMessage(message);
					if (event instanceof DigitalRegisteredLetterSendEvent sendEvent) {
						var statusEvent = new DigitalRegisteredLetterStatusEvent(
							sendEvent.recipientId(),
							null,
							"FAILED",
							cause != null ? cause.getMessage() : "Unknown error after retry exhaustion");
						rabbitTemplate.convertAndSend(DIGITAL_REGISTERED_LETTER_EXCHANGE, STATUS_DIGITAL_REGISTERED_LETTER_QUEUE, statusEvent);
					}
					break;
				}
			}

			throw new AmqpRejectAndDontRequeueException(cause);
		};

		var factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter);
		factory.setAdviceChain(RetryInterceptorBuilder.stateless()
			.maxAttempts(maxAttempts)
			.backOffOptions(initialInterval, multiplier, maxInterval)
			.recoverer(recoverer)
			.build());
		return factory;
	}
}
