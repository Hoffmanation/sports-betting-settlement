package com.sportsbetting.integration.kafka;

public class KafkaPublishException extends RuntimeException {

    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
