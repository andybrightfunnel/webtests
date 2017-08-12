package com.brightfunnel.pages.discover.stage_progression;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
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
        String baseUrl = getCurrentUrlBase();

        driver.get(baseUrl + basePath);

        waitForHeadingToLoad();

        // todo: add asserts to verify page loads correctly
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

        headerMap.put(COL_1, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[3]")));
        headerMap.put(COL_2, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[4]")));
        headerMap.put(COL_3, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[5]")));
        headerMap.put(COL_4, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[6]")));
        headerMap.put(COL_5, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[7]")));
        headerMap.put(COL_6, driver.findElement(By.xpath("id('rev-waterfall-table')//thead/tr/th[8]")));

        return headerMap;
    }

    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        rowData.put(COL_1, getStringDataValue(row, 3));
        rowData.put(COL_2, getDecimalDataValue(row, 4));
        rowData.put(COL_3, getDecimalDataValue(row, 5));
        rowData.put(COL_4, getDecimalDataValue(row, 6));
        rowData.put(COL_5, getDecimalDataValue(row, 7));
        rowData.put(COL_6, getDecimalDataValue(row, 8));


        return rowData;
    }
}
