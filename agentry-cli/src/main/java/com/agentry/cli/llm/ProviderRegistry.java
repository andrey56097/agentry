package com.agentry.cli.llm;

import com.agentry.core.llm.ModelProvider;
import com.agentry.core.llm.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(ProviderRegistry.class);
    private final Map<ProviderType, ModelProvider> providers = new HashMap<>();

    public ProviderRegistry(List<ModelProvider> providerList) {
        for (ModelProvider provider : providerList) {
            ProviderType type = provider.getType();
            if (providers.put(type, provider) != null) {
                log.warn("Duplicate provider registered for type: {}", type);
            }
            log.info("Registered provider: {}", type);
        }
    }

    public ModelProvider getProvider(ProviderType type) {
        ModelProvider provider = providers.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("No provider registered for type: " + type
                    + ". Available: " + providers.keySet());
        }
        return provider;
    }

    public ModelProvider getDefaultProvider() {
        if (providers.isEmpty()) {
            throw new IllegalStateException("No providers registered");
        }
        // Prefer ANTHROPIC, fallback to first available
        return providers.getOrDefault(ProviderType.ANTHROPIC, providers.values().iterator().next());
    }

    public Optional<ModelProvider> getOptionalProvider(ProviderType type) {
        return Optional.ofNullable(providers.get(type));
    }
}
