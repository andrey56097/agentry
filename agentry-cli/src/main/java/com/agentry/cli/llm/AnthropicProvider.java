package com.agentry.cli.llm;

import com.agentry.core.llm.LLMResponse;
import com.agentry.core.llm.ModelProvider;
import com.agentry.core.llm.ProviderType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@Service
public class AnthropicProvider implements ModelProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicProvider.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final BigDecimal INPUT_COST_PER_TOKEN = new BigDecimal("0.000003");
    private static final BigDecimal OUTPUT_COST_PER_TOKEN = new BigDecimal("0.000015");

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public AnthropicProvider(
            @Value("${agentry.llm.api-key:}") String apiKey,
            @Value("${agentry.llm.model:claude-sonnet-4-20250514}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.mapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No Anthropic API key configured. Set AGENTRY_LLM_API_KEY or agentry.llm.api-key");
        }
    }

    @Override
    public ProviderType getType() {
        return ProviderType.ANTHROPIC;
    }

    @Override
    public LLMResponse complete(String systemPrompt, String userMessage, double temperature) {
        try {
            return doComplete(systemPrompt, userMessage, temperature);
        } catch (IOException e) {
            throw new RuntimeException("LLM call to " + getType() + " failed: " + e.getMessage(), e);
        }
    }

    private LLMResponse doComplete(String systemPrompt, String userMessage, double temperature)
            throws IOException {

        long start = System.currentTimeMillis();

        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", 4096);
        body.put("temperature", temperature);
        body.put("system", systemPrompt);

        ArrayNode messages = body.putArray("messages");
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", API_VERSION)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            long latencyMs = System.currentTimeMillis() - start;
            String responseBody = response.body() != null ? response.body().string() : "{}";

            if (!response.isSuccessful()) {
                throw new RuntimeException("Anthropic API error " + response.code()
                        + ": " + responseBody);
            }

            JsonNode json = mapper.readTree(responseBody);

            StringBuilder text = new StringBuilder();
            JsonNode content = json.get("content");
            if (content != null && content.isArray()) {
                for (JsonNode block : content) {
                    JsonNode blockText = block.get("text");
                    if (blockText != null) {
                        text.append(blockText.asText());
                    }
                }
            }

            JsonNode usage = json.get("usage");
            int inputTokens = usage != null ? usage.get("input_tokens").asInt(0) : 0;
            int outputTokens = usage != null ? usage.get("output_tokens").asInt(0) : 0;

            BigDecimal cost = calculateCost(inputTokens, outputTokens);

            log.debug("LLM [{}]: {} in/{} out, ${}, {}ms",
                    model, inputTokens, outputTokens, cost, latencyMs);

            return new LLMResponse(text.toString(), inputTokens, outputTokens, cost, latencyMs);
        }
    }

    private BigDecimal calculateCost(int inputTokens, int outputTokens) {
        return INPUT_COST_PER_TOKEN.multiply(BigDecimal.valueOf(inputTokens))
                .add(OUTPUT_COST_PER_TOKEN.multiply(BigDecimal.valueOf(outputTokens)))
                .setScale(6, RoundingMode.HALF_UP);
    }
}
