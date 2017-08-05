package com.brightfunnel.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigInteger;

public class MarketingAnalyticsPage extends BasePage {

    String basePath = "#/discover/revenue-and-pipeline/marketing-impact";

    public MarketingAnalyticsPage(WebDriver driver){
        super(driver);
    }
    public MarketingAnalyticsPage(WebDriver driver, Environments environment){
        super(driver, environment);
    }

    public void navigateTo(){
        String baseUrl = getCurrentUrlBase();

        driver.get(baseUrl + basePath);
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));

        // todo: add asserts to verify page loads correctly
    }



    public void changeAttributionModel(){
        // TODO: parameterize this method to allow for different settings
        driver.get(getCurrentUrlBase() +
                "#/discover/revenue-and-pipeline/marketing-impact?period=month&type=actual&modelType=even&opptyType=oppty");

    }

    public BigInteger getPipelineTotal() {
        String amtStr = driver.findElement(By.xpath("id('totalRevenueTable')/tfoot//td[3]")).getText();
        return new BigInteger(amtStr.replaceAll("[$,]",""));
    }

    public BigInteger getOpptysCreatedTotal() {
        String amtStr = driver.findElement(By.xpath("id('totalRevenueTable')/tfoot//td[4]")).getText();
        return new BigInteger(amtStr.replaceAll("[$,]",""));
    }

    public BigInteger getMIPipelineTotal() {
        String amtStr = driver.findElement(By.xpath("id('totalRevenueTable')/tfoot//td[5]")).getText();
        return new BigInteger(amtStr.replaceAll("[$,]",""));
    }

    public BigInteger getOpptysInfluencedTotal() {
        String amtStr = driver.findElement(By.xpath("id('totalRevenueTable')/tfoot//td[6]")).getText();
        return new BigInteger(amtStr.replaceAll("[$,]",""));
    }
}
