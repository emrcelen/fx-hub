package com.emrecelen.rateproducer.infra.rabbit;

import com.emrecelen.rateproducer.common.Constants.OutboxType;
import com.emrecelen.rateproducer.common.JsonUtil;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.emrecelen.rateproducer.outbox.publisher.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RateRabbitPublisher implements EventPublisher {

    private final RabbitTemplate rabbit;

    public RateRabbitPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public String eventType() {
        return OutboxType.RATE_EVENT.name();
    }

    @Override
    public void publish(OutboxEvent event) {
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
    }
}
