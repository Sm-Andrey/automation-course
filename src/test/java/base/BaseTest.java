package base;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.awt.*;
import java.util.List;

public class BaseTest {
    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    public Page page;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        // Получаем реальный размер экрана
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false));
        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(width, height)
        );
        page = context.newPage();
    }



    @AfterEach
    void tearDown() {
        playwright.close();
    }
}