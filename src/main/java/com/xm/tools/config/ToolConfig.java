package com.xm.tools.config;

import com.xm.tools.service.EmailMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Bean
    ToolCallbackProvider emailTools(EmailMcpTools emailMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(emailMcpTools)
                .build();
    }
}