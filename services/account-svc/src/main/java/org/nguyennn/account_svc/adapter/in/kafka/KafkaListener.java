package org.nguyennn.account_svc.adapter.in.kafka;

import static org.nguyennn.account_svc.common.KafkaTopics.TOPIC_TRANSACTION_INITIATED;

import java.util.UUID;
import java.math.BigDecimal;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.nguyennn.account_svc.application.in.AccountUseCases;
import org.nguyennn.account_svc.common.RetryableException;

import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// TODO: implement DeserializationFailureHandler<JsonObject>

@ApplicationScoped
public class KafkaListener {

    @Inject
    AccountUseCases accountUseCases;

    /**
     * This method listens to the Kafka topic for transaction initiation messages. It processes the
     * message and updates the account balance accordingly.
     *
     * @param message The message received from the Kafka topic.
     * @throws Exception if an error occurs while processing the message.
     *
     * @Transactional ensures that the method is executed within a transaction context.
     */

    // TODO: https://quarkus.io/guides/messaging-virtual-threads#control-the-maximum-concurrency
    @RunOnVirtualThread
    @Transactional
    @Incoming(TOPIC_TRANSACTION_INITIATED)
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    @Retry(maxRetries = 3, delay = 1000, retryOn = {RetryableException.class})
    public void onTransactionInitiated(String message) {
        Log.info("Received message: " + message);

        String[] parts = message.split(",");
        UUID accountId = UUID.fromString(parts[0]);
        BigDecimal amount = new BigDecimal(parts[1]);
        accountUseCases.verifyAccountBalance(accountId, amount);
    }
}
