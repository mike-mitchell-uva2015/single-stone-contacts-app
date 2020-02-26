package contacts.configurations;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import contacts.exceptions.ContactsRuntimeException;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.yaml.snakeyaml.Yaml;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = {"contacts.repositories"})
@EnableTransactionManagement
@Profile("prod")
public class ProdConfig {

  private static final String CONTACTS_APP_BASE_PATH = "AWS/Secret/Path/Prefix";

  /**
   * Set the db credentials either in the environment or use the instance profile to retrieve them
   * from AWS Secrets Manager.
   *
   * <p>The environment variables to set are "DB_DATABASE", "DB_USERNAME", and "DB_PASSWORD".  On
   * the java commandline this can be done via switches like '-DDB_DATABASE=contacts', etc..
   * In IntelliJ's run contacts.configurations these can be set in the 'environment variables' field.
   *
   * <p>Not implemented yet is the ability to specify a different host, port, or database engine.
   * These default to 'localhost', '3306', and 'mysql' respectively.
   *
   * @return Properties object containing database name, username, and password to connect to a
   * database instance
   */
  private static Properties getDbCredentials() {
    Properties credentials = new Properties();

    Map<String, String> env = System.getenv();
    if (env.containsKey("DB_DATABASE")
      && env.containsKey("DB_USERNAME")
      && env.containsKey("DB_PASSWORD")
      && env.containsKey("DB_PORT")
      && env.containsKey("DB_HOST")
      && env.containsKey("DB_ENGINE")) {
      credentials.put("dbname", env.get("DB_DATABASE"));
      credentials.put("username", env.get("DB_USERNAME"));
      credentials.put("password", env.get("DB_PASSWORD"));
      credentials.put("port", env.get("DB_PORT"));
      credentials.put("host", env.get("DB_HOST"));
      credentials.put("engine", env.get("DB_ENGINE"));
    } else {
      String secretName = CONTACTS_APP_BASE_PATH + "/database";

      credentials = parseCredentials(getSecrets(secretName));
    }

    return credentials;
  }

  private static String getSecrets(String secretName) {
    AWSSecretsManager awsClient =
      AWSSecretsManagerClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .build();

    GetSecretValueResult secretResult;
    try {
      secretResult = makeSecretRequest(secretName, awsClient);
    } catch (Exception ex) {
      throw new ContactsRuntimeException("Exception while creating Secrets Manager request", ex);
    }

    return (secretResult != null) ? secretResult.getSecretString() : "";
  }

  private static Properties parseCredentials(String secretString) {
    Properties credentials = new Properties();
    Map<String, Object> secretMap = new Yaml().load(secretString);
    secretMap.forEach((k, v) -> credentials.setProperty(k, String.valueOf(v)));
    return credentials;
  }

  private static GetSecretValueResult makeSecretRequest(String secretName, AWSSecretsManager awsClient) {
    GetSecretValueResult result;

    GetSecretValueRequest secretRequest = new GetSecretValueRequest().withSecretId(secretName);
    try {
      result = awsClient.getSecretValue(secretRequest);
    } catch (Exception ex) {
      throw new ContactsRuntimeException("Exception while retrieving secret value", ex);
    }

    return result;
  }

  /**
   * DataSource bean for production.
   *
   * @return the datasource
   */
  @Bean
  public DataSource dataSource() {

    Properties credentials = getDbCredentials();
    if (credentials.getProperty("engine") == null ||
      credentials.getProperty("host") == null ||
      credentials.getProperty("port") == null) {
      throw new ContactsRuntimeException("Missing 1 or more required database properties: engine, host and port are all required");
    }

    String url = String.format("jdbc:%s://%s:%s/%s",
      credentials.getProperty("engine"),
      credentials.getProperty("host"),
      credentials.getProperty("port"),
      credentials.getProperty("dbname"));

    return DataSourceBuilder.create()
      .driverClassName("com.mysql.cj.jdbc.Driver")
      .url(url)
      .username(credentials.getProperty("username"))
      .password(credentials.getProperty("password"))
      .build();
  }

  /**
   * The Entity Manager Factory bean required for JPA.
   *
   * @return the factory
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdaptor = getHibernateJpaVendorAdapter();
    return getEntityManagerFactory(vendorAdaptor);
  }

  /**
   * The Transaction Manager bean required for JPA.
   *
   * @param entityManagerFactory the Entity Manager Factory
   * @return the factory
   */
  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);

    return transactionManager;
  }

  private HibernateJpaVendorAdapter getHibernateJpaVendorAdapter() {
    HibernateJpaVendorAdapter vendorAdaptor = new HibernateJpaVendorAdapter();
    vendorAdaptor.setGenerateDdl(false);
    return vendorAdaptor;
  }

  private LocalContainerEntityManagerFactoryBean getEntityManagerFactory(
    HibernateJpaVendorAdapter vendorAdaptor) {
    LocalContainerEntityManagerFactoryBean entityManagerFactory =
      new LocalContainerEntityManagerFactoryBean();

    entityManagerFactory.setJpaVendorAdapter(vendorAdaptor);
    entityManagerFactory.setPackagesToScan("contacts");
    entityManagerFactory.setDataSource(dataSource());
    entityManagerFactory.setJpaProperties(jpaProperties());

    return entityManagerFactory;
  }

  private Properties jpaProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
    return properties;
  }

}
