package com.emrecelen.rateproducer.infra;

import com.emrecelen.rateproducer.common.Constants;
import com.emrecelen.rateproducer.common.JsonUtil;
import com.emrecelen.rateproducer.infra.rabbit.RateRabbitPublisher;
import com.emrecelen.rateproducer.model.OutboxEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RateRabbitPublisher.class)
class RateRabbitPublisherTest {

    @MockitoBean
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RateRabbitPublisher publisher;

    @Test
    void should_return_rate_event_type() {
        assertThat(publisher.eventType())
                .isEqualTo(Constants.OutboxType.RATE_EVENT.name());
    }

    @Test
    void should_publish_event_with_headers() {
        // given
        JsonNode payload = JsonNodeFactory.instance.objectNode()
                .put("pair", "EUR/USD");

        OutboxEvent event = mock(OutboxEvent.class);
        when(event.getPayload()).thenReturn(payload);
        when(event.getEventKey()).thenReturn("FX:EUR/USD:10");
        when(event.getSchemaVersion()).thenReturn(1);
        when(event.getEventType()).thenReturn(Constants.OutboxType.RATE_EVENT);

        ArgumentCaptor<MessagePostProcessor> mppCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);

        // when
        publisher.publish(event);

        // then
        verify(rabbitTemplate).convertAndSend(
                eq("rate.exchange"),
                eq("rate.update"),
                eq(JsonUtil.toJsonString(payload)),
                mppCaptor.capture()
        );

        Message message = MessageBuilder.withBody(new byte[0]).build();
        Message processed = mppCaptor.getValue().postProcessMessage(message);

        MessageProperties props = processed.getMessageProperties();

        assertThat(props.getHeaders())
                .containsEntry("eventKey", "FX:EUR/USD:10")
                .containsEntry("schemaVersion", 1)
                .containsEntry("eventType", Constants.OutboxType.RATE_EVENT.name());
    }

}
