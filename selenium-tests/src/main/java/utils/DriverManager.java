package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Locale;

public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final int TIMEOUT_SECONDS = 60;

    private DriverManager() {}

    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            String browser = System.getProperty("browser", System.getenv("BROWSER"));
            if (browser == null || browser.isBlank()) {
                browser = "chrome";
            }
            browser = browser.toLowerCase(Locale.ROOT);
            boolean headless = "true".equalsIgnoreCase(System.getenv("HEADLESS"))
                    || "true".equalsIgnoreCase(System.getProperty("headless"));

            WebDriver driver;
            switch (browser) {
                case "firefox" -> driver = createFirefoxDriver(headless);
                case "edge" -> driver = createEdgeDriver(headless);
                default -> driver = createChromeDriver(headless);
            }

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUT_SECONDS));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(TIMEOUT_SECONDS));
            driver.manage().window().maximize();

            DRIVER.set(driver);
        }
        return DRIVER.get();
    }

    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1280,900");
        }
        options.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.NORMAL);
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless", "--width=1280", "--height=900");
        }
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--window-size=1280,900");
        }
        options.addArguments("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        return new EdgeDriver(options);
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }
}
