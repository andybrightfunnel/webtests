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

public class ConversionTrendsPage extends BasePage {

    public final static String PAGE_NAME = "Conversion Trends Page";

    String basePath = "#/discover/stage-progression/trending?cohort=Q317&quarters=4&startStage=MQL&endStage=Closed%20Won";

    public ConversionTrendsPage(WebDriver newDriver) {
        super(newDriver);
    }

    public ConversionTrendsPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        driver.findElement(By.xpath("id('Combined-Shape')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Conversion Trends')]"))
                .click();
        waitForHeadingToLoad();
    }

    public void setStageSettings(String startingStage, String endingStage) {

        String baseUrl = getCurrentUrlBase();

        String targetPath =
                String.format("#/discover/stage-progression/trending?cohort=Q317&quarters=4&startStage=%s&endStage=%s",
                    startingStage, endingStage);
        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();
    }

    /**
     * Looks up the column header values for the displayed data table
     * @return
     */
    public Map<String, Object> getDataHeaderMap(String tableName) {

        Map<String,Object> headerMap = new HashMap<>();

        String xpath = String.format("id('%s')/thead/tr/th", tableName);
        List<WebElement> columnHeaders = driver.findElements(By.xpath(xpath));

        for(int i=0; i < columnHeaders.size(); i++){
            WebElement col = columnHeaders.get(i);
            headerMap.put("col"+(i+1), col.getText());
        }

        return headerMap;
    }

    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        List<WebElement> cols = row.findElements(By.tagName("td"));

        for(int i=0; i < cols.size(); i++){

            WebElement col = cols.get(i);
            Object val = null;
            if(i==0){
                val = col.getText();
            }else
                val = getDecimalDataValue(col.getText());

            rowData.put("col" + (i+1), val);
        }

        return rowData;

    }
}
