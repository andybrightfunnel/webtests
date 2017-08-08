package com.brightfunnel.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class BasePage {

    WebDriver driver;
    public final String DEV_BASE_URL = "https://dev.brightfunnel.com";
    public final String STAGE_BASE_URL = "https://stage.brightfunnel.com";
    public final String PROD_BASE_URL = "https://app.brightfunnel.com";
    public static final int TIME_OUT_IN_SECONDS = 120;

    protected final String baseUrl;

    public BasePage(WebDriver newDriver){
        this.baseUrl = DEV_BASE_URL;
        this.driver = newDriver;
    }

    public BasePage(WebDriver newDriver, Environments environment) {
        this.driver = newDriver;

        // set the appropriate base url based on the target environment
        switch(environment){
            case STAGE:
                this.baseUrl = STAGE_BASE_URL;
                break;
            case PROD:
                this.baseUrl = PROD_BASE_URL;
                break;
            default:
                this.baseUrl = DEV_BASE_URL;
        }
    }

    public String getCurrentUrlBase() {
        String url = driver.getCurrentUrl();
        url = url.substring(0, url.indexOf("#"));
        return url;
    }

    public void openNewTab(){
        driver.findElement(By.tagName("body")).sendKeys(Keys.chord(Keys.CONTROL,"t"));
    }
}
