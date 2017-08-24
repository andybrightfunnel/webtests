package com.brightfunnel.pages.analyze.campaign;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class CampaignsTrendingPage extends BasePage{

    public final static String PAGE_NAME = "Campaigns Trending Page";

    String basePath = "#/analyze/campaigns/trending-analysis?cohort=Q117&dataSet=membershipActivity&model=sourced&field=opptys&grp=no-list&type=benchmark&freqType=auto";
    public CampaignsTrendingPage(WebDriver newDriver) {
        super(newDriver);
    }

    public CampaignsTrendingPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        driver.findElement(By.xpath("id('analyze-svg')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Campaigns Trending')]"))
                .click();
        waitForHeadingToLoad();
    }


    public void changeCohortAndModel(String cohort, String model, String dataSet, String field, String freqType) {

        String baseUrl = getCurrentUrlBase();

        String targetPath =
                String.format("#/analyze/campaigns/trending-analysis?cohort=%s&dataSet=%s&model=%s&field=%s&grp=no-list&type=benchmark&freqType=%s",
                        cohort, dataSet, model, field, freqType);
        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();
    }

    /**
     * Looks up the column header values for the displayed data table
     * @return
     */
    public Map<String, Object> getDataHeaderMap(String tableName) {

        Map<String,Object> headerMap = new HashMap<>();

        String xpath = String.format("id('%s')//table/thead/tr[2]/th", tableName);

        java.util.List<WebElement> columns = driver.findElements(By.xpath(xpath));
        int colIndex = 1;

        for(WebElement col : columns){
            String val = col.getText();

            if(val.length() == 0)
                continue;

            headerMap.put("col" + colIndex, col.getText());
            colIndex++;
        }

        return headerMap;
    }
}

