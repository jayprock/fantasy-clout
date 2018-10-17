package com.bitbus.fantasyclout.scraper;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.github.bonigarcia.wdm.WebDriverManager;

@Configuration
@Profile("selenium")
public class SeleniumConfiguration {

    @PostConstruct
    public void setup() {
        WebDriverManager.chromedriver().setup();
    }

    @Bean(destroyMethod = "quit")
    public WebDriver getWebDriver() {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        // Disable JS and downloading of images
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.images", 2);
        prefs.put("profile.managed_default_content_settings.javascript", 2);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        return driver;
    }
}
