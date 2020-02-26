package contacts.singletons;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import contacts.exceptions.ContactsRuntimeException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SecretsManager {

  private static final int MAX_CACHE_RETRIES = 3;

  private static final String CONTACTS_APP_BASE_PATH = "AWS/Secret/Path/Prefix";
  private static final String SERVICE_AUTH_BODY = "/authorizations";

  private AWSSecretsManager awsSecretsManager;
  private LoadingCache<String, JsonObject> secrets;

  private static SecretsManager instance;

  private SecretsManager() {
    awsSecretsManager = AWSSecretsManagerClientBuilder.standard()
      .withRegion(Regions.US_EAST_1)
      .build();

    // cache all secrets to avoid network calls
    secrets = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(
        new CacheLoader<String, JsonObject>() {
          public JsonObject load(String key) {
            return loadSecret(key);
          }
        });
  }

  public static SecretsManager getInstance() {
    if (instance == null) {
      instance = new SecretsManager();
    }
    return instance;
  }

  private JsonObject loadSecret(String key) {
    GetSecretValueResult result = awsSecretsManager
      .getSecretValue(new GetSecretValueRequest().withSecretId(key));
    return new Gson().fromJson(result.getSecretString(), JsonObject.class);
  }

  private JsonObject getSecretPath(String secretNameBody) throws ContactsRuntimeException {
    int numRetries = 0;

    while (numRetries < MAX_CACHE_RETRIES) {
      try {
        return secrets.get(CONTACTS_APP_BASE_PATH + secretNameBody);
      } catch (ExecutionException e) {
        numRetries++;
      }
    }

    throw new ContactsRuntimeException("Failed to get SecretsManager data");
  }

  public String verifyServiceToken(String serviceToken) throws BadCredentialsException {
    JsonObject serviceTokens = getSecretPath(SERVICE_AUTH_BODY);
    Set<Map.Entry<String, JsonElement>> entries = serviceTokens.entrySet();

    for(Map.Entry<String, JsonElement> entry : entries) {
      if(entry.getValue().getAsString().equals(serviceToken)) {
        return entry.getKey();
      }
    }

    throw new BadCredentialsException(String.format("Secret value '%s' not found in Contact Authorizations secrets set", serviceToken));
  }

}

