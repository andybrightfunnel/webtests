package com.brightfunnel.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HomePage extends BasePage{


    public HomePage(WebDriver newDriver){
        super(newDriver);
    }

    public HomePage(WebDriver newDriver, Environments environment){
       super(newDriver, environment);
    }

    public void navigateTo(){
        driver.get(baseUrl);
    }

    public void login(String userName, String password){

        driver.get(baseUrl + "/login/auth");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(userName);
        driver.findElement(By.id("loginButton")).click();

        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));
        assertTrue("Should be on the home dashboard page",
                driver.getCurrentUrl().contains("brightfunnel.com/#/dashboard"));
    }
}
