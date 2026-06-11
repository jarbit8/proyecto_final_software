package com.finance.project.functional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("functional")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SwaggerUiSeleniumTest {

    @LocalServerPort
    private int port;

    private static WebDriver driver;

    @BeforeAll
    public static void setUpDriver() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,1000");

        // selenium.remote.url permite usar un contenedor selenium/standalone-chrome en CI
        String remoteUrl = System.getProperty("selenium.remote.url", "");
        if (remoteUrl.isEmpty()) {
            driver = new ChromeDriver(options);
        } else {
            driver = new RemoteWebDriver(new URL(remoteUrl), options);
        }
    }

    @AfterAll
    public static void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Swagger UI carga y muestra la API documentada")
    public void swaggerUiShowsApiTitle() {

        driver.get("http://localhost:" + port + "/swagger-ui/index.html");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.title")));

        assertEquals("Swagger UI", driver.getTitle());
        assertTrue(title.getText().contains("Personal Finance Management API"));
    }

    @Test
    @DisplayName("La consola H2 responde como pagina web")
    public void h2ConsoleLoads() {

        driver.get("http://localhost:" + port + "/h2-console");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(d -> !d.getTitle().isEmpty());

        assertEquals("H2 Console", driver.getTitle());
    }
}
