package com.brightfunnel.pages.discover.stage_progression;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class VelocityOfCampaignGroupPage extends BasePage {

    public final static String PAGE_NAME = "Conversion Trends Page";

    String basePath = "#/discover/stage-progression/trending?cohort=Q317&quarters=4&startStage=MQL&endStage=Closed%20Won";

    public VelocityOfCampaignGroupPage(WebDriver newDriver) {
        super(newDriver);
    }

    public VelocityOfCampaignGroupPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        String baseUrl = getCurrentUrlBase();

        driver.get(baseUrl + basePath);
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));

        // todo: add asserts to verify page loads correctly
    }

    public void changeCohort(String cohort) {
        String baseUrl = getCurrentUrlBase();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, -7);

        Date endDate = cal.getTime();
        Date startDate = new Date();

        String targetPath =
                String.format("#/discover/stage-progression/velocity-campaign-group?cohort=%s&startDate=%s&endDate=%s&groupType=campaign",
                        cohort, startDate, endDate);
        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();
    }

    /**
     * Looks up the column header values for the displayed data table
     * @return
     */
    public Map<String, Object> getDataHeaderMap() {

        Map<String,Object> headerMap = new HashMap<>();

        headerMap.put(COL_1, driver.findElement(By.xpath("id('vlcTable')/thead/tr/th[2]")).getText());
        headerMap.put(COL_2, driver.findElement(By.xpath("id('vlcTable')/thead/tr/th[3]")).getText());
        headerMap.put(COL_3, driver.findElement(By.xpath("id('vlcTable')/thead/tr/th[4]")).getText());
        headerMap.put(COL_4, driver.findElement(By.xpath("id('vlcTable')/thead/tr/th[5]")).getText());
        headerMap.put(COL_5, driver.findElement(By.xpath("id('vlcTable')/thead/tr/th[6]")).getText());
        return headerMap;
    }

    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        List<WebElement> cols = row.findElements(By.tagName("td"));

        rowData.put(COL_1, cols.get(1).getText());
        rowData.put(COL_2, getDecimalDataValue(cols.get(2).getText()));
        rowData.put(COL_3, getDecimalDataValue(cols.get(3).getText()));
        rowData.put(COL_4, getDecimalDataValue(cols.get(4).getText()));
        rowData.put(COL_5, getDecimalDataValue(cols.get(5).getText()));


        return rowData;

    }

}
