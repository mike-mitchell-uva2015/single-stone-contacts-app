package contacts.configurations;

import contacts.security.ApiKeyAuthFilter;
import contacts.singletons.SecretsManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@Order(1)
@Profile("prod")
public class ProdWebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("Authorization")
  private String principleRequestHeader;

  /**
   * require UI to pass a header with an authorization token to be verified against a
   * mapping of allowed entities stored in aws secrets manager
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter(principleRequestHeader);

    filter.setAuthenticationManager(authentication -> {
      String serviceToken = (String) authentication.getPrincipal();
      if(serviceToken == null || serviceToken.isEmpty()) {
        throw new BadCredentialsException("No value given for 'Authorization' header");
      }

      //will throw is this token is now found in our secret that stores permitted tokens
      SecretsManager.getInstance().verifyServiceToken(serviceToken);

      authentication.setAuthenticated(true);
      return authentication;
    });

    http.antMatcher("/v1/**")
          .csrf().disable()
          .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          .and()
          .addFilter(filter).authorizeRequests().anyRequest().authenticated();
  }

}
