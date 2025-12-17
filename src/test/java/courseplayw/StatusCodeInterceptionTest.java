package courseplayw;

import base.BaseTest;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.Collections;

public class StatusCodeInterceptionTest extends BaseTest {
  // перехват запроса
  @BeforeEach
  void testSetUp() {
    context.route("**/status_codes/404", route -> {
      route.fulfill(new Route.FulfillOptions()
              .setStatus(200)
              .setHeaders(Collections.singletonMap("Content-Type", "text/html"))
              .setBody("<h3>Mocked Success Response</h3>")
      );
    });
  }

  @Test
  public void testMockedStatusCode() {
    // Перейти по ссылки
    page.navigate("https://the-internet.herokuapp.com/status_codes");
    // Клик по ссылке "404"
    page.locator("//a[@href='status_codes/404']").click();
    // Проверка мок-текста
    assertThat(page.locator("//h3")).containsText("Mocked Success Response");
  }
}


