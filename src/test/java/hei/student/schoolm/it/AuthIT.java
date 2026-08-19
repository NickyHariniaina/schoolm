package hei.student.schoolm.it;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hei.student.schoolm.conf.FacadeIT;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

class AuthIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient = HttpClient.newHttpClient();

  private static final String ADMIN_EMAIL = "admin@hei.school";
  private static final String ADMIN_PASSWORD = "password";

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private HttpResponse<String> postLogin(String email, String password) throws Exception {
    var body =
        objectMapper.writeValueAsString(new hei.student.schoolm.dto.LoginRequest(email, password));
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl() + "/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> get(String path, String token) throws Exception {
    var builder = HttpRequest.newBuilder().uri(URI.create(baseUrl() + path)).GET();
    if (token != null) {
      builder.header("Authorization", "Bearer " + token);
    }
    return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void ping_is_public() throws Exception {
    assertThat(get("/ping", null).statusCode()).isEqualTo(200);
  }

  @Test
  void login_returns_token_and_user() throws Exception {
    var response = postLogin(ADMIN_EMAIL, ADMIN_PASSWORD);

    assertThat(response.statusCode()).isEqualTo(200);
    var json = objectMapper.readValue(response.body(), JsonNode.class);
    assertThat(json.get("token").asText()).isNotBlank();
    assertThat(json.get("user").get("email").asText()).isEqualTo(ADMIN_EMAIL);
    assertThat(json.get("user").get("role").asText()).isEqualTo("ADMIN");
  }

  @Test
  void login_rejects_wrong_password() throws Exception {
    var response = postLogin(ADMIN_EMAIL, "wrong-password");

    assertThat(response.statusCode()).isEqualTo(401);
  }

  @Test
  void protected_endpoint_requires_token() throws Exception {
    assertThat(get("/courses", null).statusCode()).isEqualTo(401);
  }

  @Test
  void bearer_token_grants_access() throws Exception {
    var login =
        objectMapper.readValue(postLogin(ADMIN_EMAIL, ADMIN_PASSWORD).body(), JsonNode.class);
    var token = login.get("token").asText();

    assertThat(get("/courses", token).statusCode()).isEqualTo(200);
  }

  @Test
  void unknown_endpoint_returns_404_not_401() throws Exception {
    assertThat(get("/nope/never-existed", null).statusCode()).isEqualTo(404);
  }
}
