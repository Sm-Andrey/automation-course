package courseplayw;

import base.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class GitHubSearchInterceptionTest extends BaseTest {
  // Перехват запроса
  @BeforeEach
  void testSetUp() {
//    context.route("**/search**", route -> {
//      String originalUrl = route.request().url();
//      System.out.println("Intercepted URL: " + originalUrl);

//      1 вариант
//      String modifyUrl = originalUrl.contains("q=")
//              ? originalUrl.replaceAll("q=[^&]+", "q=stars%3A%3E10000")
//              : originalUrl + (originalUrl.contains("?") ? "&" : "?") + "q=stars%3A%3E10000";
//      System.out.println("Modified URL: " + modifyUrl);
//      route.resume(new Route.ResumeOptions().setUrl(modifyUrl));

//      2 вариант
//      // Проверяем, что это поисковый запрос (может быть несколько запросов к /search/)
//      if (originalUrl.contains("/search?") && originalUrl.contains("q=")) {
//        String modifyUrl = originalUrl.replaceAll("q=[^&]+", "q=stars%3A%3E10000");
//        System.out.println("Modified URL: " + modifyUrl);
//        route.resume(new Route.ResumeOptions().setUrl(modifyUrl));
//      } else {
//        // Для остальных запросов просто продолжаем
//        route.resume();
//      }
//    });

//    Были еще ни суть, остановился на этом

    // Внедряем скрипт для перехвата клиентского роутинга.
    // из всех вариантов сработал только этот вариант, помог deepseek. остальные варианты меняли путь в Request Headers
    // но не меняли Request URL. Ни в варианте с переходом сразу на https://github.com/search?q=java, ни на
    // https://github.com/search с последующем вводом java в поисковой строке.
    page.addInitScript("""
    (function() {
      // Сохраняем оригинальные методы
      const originalPushState = history.pushState;
      const originalReplaceState = history.replaceState;
      
      // Переопределяем pushState
      history.pushState = function(state, title, url) {
        console.log('pushState called with URL:', url);
        
        if (url && typeof url === 'string' && 
            url.includes('/search?') && url.includes('q=')) {
          // Модифицируем URL
          const modifiedUrl = url.replace(/q=[^&]+/, 'q=stars%3A%3E10000');
          console.log('Modified URL for pushState:', modifiedUrl);
          return originalPushState.call(this, state, title, modifiedUrl);
        }
        
        return originalPushState.apply(this, arguments);
      };
      
      // Аналогично для replaceState
      history.replaceState = function(state, title, url) {
        if (url && typeof url === 'string' && 
            url.includes('/search?') && url.includes('q=')) {
          const modifiedUrl = url.replace(/q=[^&]+/, 'q=stars%3A%3E10000');
          return originalReplaceState.call(this, state, title, modifiedUrl);
        }
        return originalReplaceState.apply(this, arguments);
      };
      
      // Также перехватываем изменение location.hash если нужно
      const originalLocationAssign = window.location.assign;
      window.location.assign = function(url) {
        if (url && url.includes('/search?') && url.includes('q=')) {
          url = url.replace(/q=[^&]+/, 'q=stars%3A%3E10000');
        }
        return originalLocationAssign.call(this, url);
      };
    })();
  """);


  }

  @Test
  public void testSearchModification() {
    // Переходим на страницу поиска
    page.navigate("https://github.com/search");

    // Вводим поисковый запрос
    Locator inputSearch = page.locator("//input[@data-component='input']");
    inputSearch.fill("java");
    inputSearch.press("Enter");

    // Ожидаем появления результатов
    page.locator("//div[@data-testid=\"search-sub-header\"]").waitFor();

    // Проверяем запрос
    assertThat(page.locator("//span[@data-target='qbsearch-input.inputButtonText']"))
            .containsText("stars:>10000");
  }
}
