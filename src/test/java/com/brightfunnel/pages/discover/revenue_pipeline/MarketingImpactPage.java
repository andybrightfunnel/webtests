package com.brightfunnel.pages.discover.revenue_pipeline;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigInteger;

public class MarketingImpactPage extends BasePage {

    String basePath = "#/discover/revenue-and-pipeline/marketing-impact";
    public final static String PAGE_NAME = "Marketing Analytics Page";

    public MarketingImpactPage(WebDriver driver){
        super(driver);
    }
    public MarketingImpactPage(WebDriver driver, Environments environment){
        super(driver, environment);
    }

    public void navigateTo(){
        driver.findElement(By.xpath("id('Combined-Shape')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Marketing Impact')]"))
                .click();
        waitForHeadingToLoad();
    }

    public void changeAttributionModel(String period, String opptyType, String modelType){

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.getCurrentUrlBase());

        String url = String.format("%s#/discover/revenue-and-pipeline/marketing-impact?period=%s&type=actual&opptyType=%s&modelType=%s",
                this.getCurrentUrlBase(), period, opptyType, modelType );

        driver.get(url);

        try {
            WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                    until(ExpectedConditions.visibilityOfElementLocated(By.xpath("id('totalRevenueTable')/tfoot//td[3]")));

        }catch(TimeoutException te){
            te.printStackTrace();
            driver.get(url);
            WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                    until(ExpectedConditions.visibilityOfElementLocated(By.xpath("id('totalRevenueTable')/tfoot//td[3]")));
        }

    }

    public BigInteger getRowData(int row, int col) {
        String xpath = String.format("id('totalRevenueTable')//tr[%s]/td[%s]", row, col);
        String amtStr = driver.findElement(By.xpath(xpath)).getText();
        return new BigInteger(amtStr.replaceAll("[$,]",""));
    }
}
