package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.LoginPage;
import utils.DriverManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@ExtendWith(AllureJunit5.class)
class LoginTest {

    private static final String DEMO_USER = "alice@findit.demo";
    private static final String DEMO_PASS = "TestPass123!";

    @BeforeEach
    void setUp() {
        // driver created on first LoginPage use
    }

    @AfterEach
    void tearDown() {
        DriverManager.quitDriver();
    }

    private void attachScreenshotOnFailure(Runnable body) {
        try {
            body.run();
        } catch (AssertionError | Exception e) {
            captureScreenshot("failure");
            throw e;
        }
    }

    private void captureScreenshot(String name) {
        try {
            var driver = DriverManager.getDriver();
            if (driver instanceof TakesScreenshot) {
                TakesScreenshot ts = (TakesScreenshot) driver;
                byte[] png = ts.getScreenshotAs(OutputType.BYTES);
                Path tmp = Files.createTempFile("shot-", ".png");
                Files.write(tmp, png);
                Allure.addAttachment(name, Files.newInputStream(tmp));
            }
        } catch (Exception ignored) {
            // best-effort
        }
    }

    @Test
    void TC_LOGIN_01_pageLoadsWithExpectedTitle() {
        var page = new LoginPage();
        page.open();
        Assertions.assertTrue(page.title().toLowerCase().contains("login"));
    }


    @Test
    void TC_LOGIN_02_invalidPasswordShowsError() {
    var page = new LoginPage();
    page.open();
    page.loginAs(DEMO_USER, "WrongPass!!!", true);  // or false, doesn't matter
    // Add wait for error to appear
    WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(5));
    wait.until(driver -> page.isGlobalErrorVisible());
    Assertions.assertTrue(page.isGlobalErrorVisible());
}

    @Test
    void TC_LOGIN_03_emptyEmailStaysOnLogin() {
        var page = new LoginPage();
        page.open();
        page.loginAs("", DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_04_emptyPasswordStaysOnLogin() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "", false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_05_malformedEmailRejectedClientSide() {
        var page = new LoginPage();
        page.open();
        page.loginAs("not-an-email", DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_06_sqlInjectionInEmailFieldDoesNotCrash() {
        var page = new LoginPage();
        page.open();
        page.loginAs("admin' OR '1'='1", DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_07_sqlInjectionInPasswordFieldDoesNotAuthenticate() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "' OR '1'='1", false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_08_unicodeInPasswordHandled() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "密码密码密码密码", false);
        Assertions.assertFalse(page.isDashboard());
    }


    @Test
    void TC_LOGIN_09_whitespaceOnlyPasswordFails() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "        ", false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_10_passwordLengthSevenBoundaryInvalid() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "Short7!", false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_11_extremelyLongEmailDoesNotAuthenticate() {
        var page = new LoginPage();
        page.open();
        String longLocal = "a".repeat(200);
        page.loginAs(longLocal + "@x.com", DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_12_caseSensitivePasswordFailure() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "testpass123!", false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_13_htmlInEmailFieldSanitizedByValidation() {
        var page = new LoginPage();
        page.open();
        page.loginAs("<b>x@x.com</b>", DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }


    @Test
    void TC_LOGIN_14_registerLinkPresent() {
        var page = new LoginPage();
        page.open();
        var driver = DriverManager.getDriver();
        Assertions.assertTrue(driver.findElements(org.openqa.selenium.By.id("login-link-register")).size() > 0);
    }

    @Test
    void TC_LOGIN_15_dualInvalidCredentials() {
        var page = new LoginPage();
        page.open();
        page.loginAs("x@y.z", "nope", false);
        Assertions.assertTrue(page.isGlobalErrorVisible() || !page.isDashboard());
    }

    @Test
    void TC_LOGIN_16_passwordExactlyEightCharsWhenWrongStillFails() {
        var page = new LoginPage();
        page.open();
        page.loginAs(DEMO_USER, "12345678", false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_17_leadingSpaceInEmailHandled() {
        var page = new LoginPage();
        page.open();
        page.loginAs(" " + DEMO_USER, DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }

    @Test
    void TC_LOGIN_18_loginFormIdsStableForAutomation() {
        var page = new LoginPage();
        page.open();
        var d = DriverManager.getDriver();
        Assertions.assertAll(
                () -> Assertions.assertTrue(d.findElements(LoginPage.EMAIL).size() == 1),
                () -> Assertions.assertTrue(d.findElements(LoginPage.PASSWORD).size() == 1),
                () -> Assertions.assertTrue(d.findElements(LoginPage.SUBMIT).size() == 1)
        );
    }

    @Test
    void TC_LOGIN_19_javascriptProtocolInEmailRejected() {
        var page = new LoginPage();
        page.open();
        page.loginAs("javascript:alert(1)@x.com", DEMO_PASS, false);
        Assertions.assertFalse(page.isDashboard());
    }


    @Test
    void TC_LOGIN_20_screenshotAttachmentSmoke() {
        var page = new LoginPage();
        page.open();
        captureScreenshot("login-loaded");
        Assertions.assertTrue(DriverManager.getDriver().getCurrentUrl().contains("login"));
    }
}
