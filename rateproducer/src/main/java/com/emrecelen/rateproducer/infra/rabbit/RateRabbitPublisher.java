package com.emrecelen.rateproducer.infra.rabbit;

import com.emrecelen.rateproducer.common.Constants.OutboxType;
import com.emrecelen.rateproducer.common.JsonUtil;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.outbox.publisher.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateRabbitPublisher implements EventPublisher {

    private final RabbitTemplate rabbit;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public RateRabbitPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public String eventType() {
        return OutboxType.RATE_EVENT.name();
    }

    @Override
    public void publish(OutboxEvent event) {
        log.debug(
                "Publishing rate event to RabbitMQ. eventKey={}, schemaVersion={}",
                event.getEventKey(),
                event.getSchemaVersion()
        );

        rabbit.convertAndSend(
                "rate.exchange",
                "rate.update",
                JsonUtil.toJsonString(event.getPayload()),
                msg -> {
                    msg.getMessageProperties().setHeader("eventKey", event.getEventKey());
                    msg.getMessageProperties().setHeader("schemaVersion", event.getSchemaVersion());
                    msg.getMessageProperties().setHeader("eventType", event.getEventType().name());
                    msg.getMessageProperties().setContentType("application/json");
                    return msg;
                }
        );
        log.info(
                "Rate event published successfully. eventKey={}",
                event.getEventKey()
        );
    }
}
