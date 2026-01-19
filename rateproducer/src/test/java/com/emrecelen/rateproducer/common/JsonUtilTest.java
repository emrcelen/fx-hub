package com.emrecelen.rateproducer.common;

import com.emrecelen.rateproducer.domain.model.RawRate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonUtilTest {

    @Test
    void should_convert_object_to_json_node() {
        RawRate rawRate = new RawRate(
                "anySource",
                "EUR/USD",
                1,
                "anybid",
                "anyask",
                1
        );
        JsonNode node = JsonUtil.toJson(rawRate);
        assertThat(node).isNotNull();
        assertThat(node.get("pair").asText()).isEqualTo("EUR/USD");
    }

    @Test
    void should_return_null_node_when_input_is_null() {
        JsonNode node = JsonUtil.toJson(null);

        assertThat(node).isNotNull();
        assertThat(node.isNull()).isTrue();
    }

    @Test
    void should_wrap_exception_into_illegal_state_exception() {
        try (MockedStatic<JsonUtil> mocked = Mockito.mockStatic(JsonUtil.class)) {

            ObjectMapper brokenMapper = mock(ObjectMapper.class);
            when(brokenMapper.valueToTree(any()))
                    .thenThrow(new RuntimeException("boom"));

            mocked.when(() -> JsonUtil.toJson(any()))
                    .thenCallRealMethod();

            assertThatThrownBy(() -> JsonUtil.toJson(new Object()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("JSON serialization failed");
        }
    }
}
