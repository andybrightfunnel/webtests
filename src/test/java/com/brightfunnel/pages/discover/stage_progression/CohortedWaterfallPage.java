package com.brightfunnel.pages.discover.stage_progression;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CohortedWaterfallPage extends BasePage {


    public static final String PAGE_NAME = "Stages Snapshot Page";

    String basePath = "#/discover/stage-progression/attribution?cohort=quarter2Date&startStageSequence=1&attModel=all";

    public CohortedWaterfallPage(WebDriver newDriver) {
        super(newDriver);
    }

    public CohortedWaterfallPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        driver.findElement(By.xpath("id('Combined-Shape')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Cohorted Waterfall')]"))
                .click();
        waitForHeadingToLoad();
    }

    public void changeCohortAndSequence(String cohort, int startingStageSequence) {
        String baseUrl = getCurrentUrlBase();

        String targetPath = "#/discover/stage-progression/attribution?cohort="+cohort +
                "&startStageSequence=" + startingStageSequence +"&attModel=all";
        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();

    }


    public Map<String, Object> getDataHeaderMap() {
        Map<String,Object> headerMap = new HashMap<>();

        headerMap.put(COL_1, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[3]")).getText());
        headerMap.put(COL_2, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[4]")).getText());
        headerMap.put(COL_3, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[5]")).getText());
        headerMap.put(COL_4, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[6]")).getText());
        headerMap.put(COL_5, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[7]")).getText());
        headerMap.put(COL_6, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[8]")).getText());

        return headerMap;
    }

    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        List<WebElement> cols = row.findElements(By.tagName("td"));

        rowData.put(COL_1, cols.get(2).getText());
        rowData.put(COL_2, getDecimalDataValue(cols.get(3).getText()));
        rowData.put(COL_3, getDecimalDataValue(cols.get(4).getText()));
        rowData.put(COL_4, getDecimalDataValue(cols.get(5).getText()));
        rowData.put(COL_5, getDecimalDataValue(cols.get(6).getText()));
        rowData.put(COL_6, getDecimalDataValue(cols.get(7).getText()));


        return rowData;
    }
}
