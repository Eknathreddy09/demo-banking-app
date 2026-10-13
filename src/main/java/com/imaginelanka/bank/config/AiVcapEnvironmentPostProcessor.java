package com.imaginelanka.bank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps the credentials of the bound "ai-models" (genai-service) marketplace instance onto
 * Spring AI's spring.ai.openai.* properties. Broker credential key names vary, so this tries a
 * handful of common variants; added with lowest precedence so a manual `cf set-env
 * SPRING_AI_OPENAI_*` always wins if the guessed mapping is wrong for this broker.
 */
public class AiVcapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final List<String> BASE_URL_KEYS = List.of(
            "api_base", "apiBase", "base_url", "baseUrl", "url", "uri", "endpoint", "api_url");
    private static final List<String> API_KEY_KEYS = List.of(
            "api_key", "apiKey", "key", "token", "access_token", "password");
    private static final List<String> MODEL_KEYS = List.of(
            "model_name", "modelName", "model", "default_model", "model_id");

    private final Log log;

    public AiVcapEnvironmentPostProcessor(DeferredLogFactory logFactory) {
        this.log = logFactory.getLog(AiVcapEnvironmentPostProcessor.class);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String vcapServices = environment.getProperty("VCAP_SERVICES");
        if (vcapServices == null || vcapServices.isBlank()) {
            return;
        }

        try {
            Map<String, Object> credentials = findAiModelsCredentials(vcapServices);
            if (credentials == null) {
                log.info("No 'ai-models' service instance found in VCAP_SERVICES yet; AI chat will use application.yml defaults until bound.");
                return;
            }

            Map<String, Object> mapped = new LinkedHashMap<>();
            findFirst(credentials, API_KEY_KEYS).ifPresent(v -> mapped.put("spring.ai.openai.api-key", v));
            findFirst(credentials, BASE_URL_KEYS).ifPresent(v -> mapped.put("spring.ai.openai.base-url", v));
            findFirst(credentials, MODEL_KEYS).ifPresent(v -> mapped.put("spring.ai.openai.chat.options.model", v));

            if (mapped.isEmpty()) {
                log.warn("Found an 'ai-models' service binding but none of the expected credential keys were present. "
                        + "Run `cf env <app>` to inspect the actual JSON and override via `cf set-env` with "
                        + "SPRING_AI_OPENAI_API_KEY / SPRING_AI_OPENAI_BASE_URL / SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL.");
                return;
            }

            environment.getPropertySources().addLast(new MapPropertySource("vcapAiModelsBinding", mapped));
            log.info("Mapped ai-models service binding to Spring AI properties: " + mapped.keySet());
        } catch (Exception ex) {
            log.warn("Could not parse VCAP_SERVICES for an ai-models binding: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findAiModelsCredentials(String vcapServicesJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(vcapServicesJson, Map.class);

        for (Object groupValue : root.values()) {
            if (!(groupValue instanceof List<?> instances)) {
                continue;
            }
            for (Object instanceObj : instances) {
                if (!(instanceObj instanceof Map<?, ?> instance)) {
                    continue;
                }
                if (looksLikeAiModelsInstance(instance)) {
                    Object creds = instance.get("credentials");
                    if (creds instanceof Map<?, ?> credsMap) {
                        return (Map<String, Object>) credsMap;
                    }
                }
            }
        }
        return null;
    }

    private boolean looksLikeAiModelsInstance(Map<?, ?> instance) {
        Object label = instance.get("label");
        Object name = instance.get("name");
        Object tags = instance.get("tags");

        if ("ai-models".equals(label)) {
            return true;
        }
        if (name != null && name.toString().toLowerCase().contains("ai")) {
            return true;
        }
        if (tags instanceof List<?> tagList) {
            for (Object tag : tagList) {
                String t = String.valueOf(tag).toLowerCase();
                if (t.contains("genai") || t.contains("ai-models") || t.contains("llm")) {
                    return true;
                }
            }
        }
        return false;
    }

    private java.util.Optional<Object> findFirst(Map<String, Object> credentials, List<String> keys) {
        for (String key : keys) {
            for (Map.Entry<String, Object> entry : credentials.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key) && entry.getValue() != null) {
                    return java.util.Optional.of(entry.getValue());
                }
            }
        }
        return java.util.Optional.empty();
    }
}
