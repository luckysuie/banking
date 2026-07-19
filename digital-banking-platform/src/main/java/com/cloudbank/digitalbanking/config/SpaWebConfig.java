package com.cloudbank.digitalbanking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.List;

/**
 * Serves the React single-page application (built into {@code classpath:/static})
 * from the same Spring Boot JAR that hosts the REST API.
 *
 * <p>Requests for real static files (JS/CSS/images under {@code /assets/**}, etc.)
 * are served as-is. Requests for client-side routes that have no matching static
 * file (e.g. {@code /login}, {@code /dashboard}, {@code /accounts}) fall back to
 * {@code index.html} so React Router can take over on full page loads and refreshes.
 *
 * <p>{@code /api/**}, {@code /swagger-ui/**}, {@code /v3/api-docs/**} and
 * {@code /actuator/**} are always handled by their own controllers/endpoints
 * (which are matched before this resource handler); if one of those paths has no
 * real handler, this resolver deliberately returns no resource instead of
 * forwarding to {@code index.html}, so callers get a proper 404 rather than HTML.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "api/", "swagger-ui/", "v3/api-docs/", "actuator/");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaFallbackResourceResolver());
    }

    private static final class SpaFallbackResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requestedResource = location.createRelative(resourcePath);
            if (requestedResource.exists() && requestedResource.isReadable()) {
                return requestedResource;
            }
            if (isExcluded(resourcePath)) {
                return null;
            }
            return new ClassPathResource("/static/index.html");
        }

        private boolean isExcluded(String resourcePath) {
            return EXCLUDED_PREFIXES.stream().anyMatch(resourcePath::startsWith);
        }
    }
}
