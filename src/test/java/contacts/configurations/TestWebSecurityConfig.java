package contacts.configurations;

import contacts.security.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Order(1)
@Profile("test")
public class TestWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("Authorization")
  private String principleRequestHeader;

  @Value("abc123")
  private String principalRequestValue;

  private static Map<String, String> serviceTokens = getServiceTokens();

  private static Map<String, String> getServiceTokens() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("contact", "abc123");
    return tokens;
  }

  public String verifyServiceToken(String serviceToken) throws BadCredentialsException {
    for(Map.Entry<String, String> entry : serviceTokens.entrySet()) {
      if(entry.getValue().equals(serviceToken)) {
        return entry.getKey();
      }
    }

    throw new BadCredentialsException(String.format("Secret value '%s' not found in Directory ServiceAuthorizations secrets set", serviceToken));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter(principleRequestHeader);

    filter.setAuthenticationManager(authentication -> {
      String serviceToken = (String) authentication.getPrincipal();

      if(serviceToken == null || serviceToken.isEmpty()) {
        throw new BadCredentialsException("No value given for 'Authorization' header");
      }

      verifyServiceToken(serviceToken);

      authentication.setAuthenticated(true);
      return authentication;
    });

    http.antMatcher("/v1/**")
      .csrf().disable()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
  }
}
