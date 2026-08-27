package es.NTTEnterprise.RIntellix.ms_risk_engine.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 *
 * This class defines the base OpenAPI specification and binds the OAuth2
 * security scheme (Keycloak) to the exposed endpoints. A separate Keycloak
 * client ('rintellix-swagger') without PKCE is utilized to decouple Swagger UI
 * security from the primary frontend client.
 *
 * @author Lucía Fernández Mancebo
 * @date 08/08/2026
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Risk Engine API", version = "v1", description = "Risk Engine Microservice for RIntellix"), security = @SecurityRequirement(name = "keycloak"))
@SecurityScheme(name = "keycloak", type = SecuritySchemeType.OAUTH2, flows = @OAuthFlows(authorizationCode = @OAuthFlow(authorizationUrl = "http://localhost:8180/realms/rintellix/protocol/openid-connect/auth", tokenUrl = "http://localhost:8180/realms/rintellix/protocol/openid-connect/token", scopes = {
                @OAuthScope(name = "openid", description = "OpenID Connect scope")
})))
public class OpenApiConfig {
}
