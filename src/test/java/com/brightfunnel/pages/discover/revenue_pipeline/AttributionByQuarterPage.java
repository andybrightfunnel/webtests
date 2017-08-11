package com.brightfunnel.pages.discover.revenue_pipeline;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AttributionByQuarterPage extends BasePage {

    public static final String PAGE_NAME = "Attribution By Quarter Page";
    String basePath = "#/discover/revenue-and-pipeline/attribution-by-quarter?startDate=1501611452734&endDate=1502216252734&cohort=quarter2Date&grp=only&dataSet=opptyCloseDate&revenueType=booked";

    public AttributionByQuarterPage(WebDriver newDriver) {
        super(newDriver);
    }

    public AttributionByQuarterPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){

        String baseUrl = getCurrentUrlBase();

        driver.get(baseUrl + basePath);
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));

    }

    public void changeAttributionModel(String revenueType, String cohort){

        String dataSet = ("booked".equals(revenueType)) ? "opptyCloseDate" : "opptyCreatedDate";

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(this.getCurrentUrlBase());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_MONTH, -3);
        Date startDate = calendar.getTime();
        Date endDate = new Date();

        String url = String.format("%s#/discover/revenue-and-pipeline/attribution-by-quarter?cohort=%s&grp=only&revenueType=%s&dataSet=%s&startDate=%s&endDate=%s",
                this.getCurrentUrlBase(), cohort, revenueType, dataSet, startDate.getTime(), endDate.getTime() );

        driver.get(url);

        try {
            WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                    until(ExpectedConditions.visibilityOfElementLocated(By.xpath("id('attrTable')//tfoot/tr[2]")));

        }catch(TimeoutException te){
            te.printStackTrace();
            driver.get(url);
            WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                    until(ExpectedConditions.visibilityOfElementLocated(By.xpath("id('attrTable')//tfoot/tr[2]")));
        }

    }

    public String getStringDataValue(int dataRow, int dataCol) {
        return driver.findElement(By.xpath("id('attrTable')/table/tbody/tr["+dataRow+"]/td["+dataCol+"]")).getText();
    }

    public BigDecimal getDecimalDataValue(int row, int col) {
        String val = driver.findElement(By.xpath("id('attrTable')/table/tbody/tr["
                +row+"]/td["+col+"]")).getText();

        return new BigDecimal(val.replaceAll("[$,]", ""));
    }

    public Map getDataMapForRow(int row) {
        Map<String,Object> rowData = new HashMap<>();

        String capaignGroup = getStringDataValue(row, 3);
        BigDecimal firstTouchAmt = getDecimalDataValue(row, 4);
        BigDecimal lastTouchAmt = getDecimalDataValue(row, 5);
        BigDecimal multiTouchEvenAMt = getDecimalDataValue(row, 6);
        BigDecimal multiTouchCustomAmt = getDecimalDataValue(row, 7);
        BigDecimal sourcedOpptys = getDecimalDataValue(row, 8);
        BigDecimal touchedOpptys = getDecimalDataValue(row, 9);

        rowData.put(COL_1, capaignGroup);
        rowData.put(COL_2, firstTouchAmt);
        rowData.put(COL_3, lastTouchAmt);
        rowData.put(COL_4, multiTouchEvenAMt);
        rowData.put(COL_5, multiTouchCustomAmt);
        rowData.put(COL_6, sourcedOpptys);
        rowData.put(COL_7, touchedOpptys);

        return rowData;
    }

    public Map getDataColumnHeaderMap() {
        String col1Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[3]")).getText();
        String col2Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[4]")).getText();
        String col3Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[5]")).getText();
        String col4Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[6]")).getText();
        String col5Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[7]")).getText();
        String col6Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[8]")).getText();
        String col7Header = driver.findElement(By.xpath("id('attrTable')/table//tr/th[9]")).getText();

        Map<String,Object> columnHeaderMap = new HashMap<>();
        columnHeaderMap.put(COL_1, col1Header);
        columnHeaderMap.put(COL_2, col2Header);
        columnHeaderMap.put(COL_3, col3Header);
        columnHeaderMap.put(COL_4, col4Header);
        columnHeaderMap.put(COL_5, col5Header);
        columnHeaderMap.put(COL_6, col6Header);
        columnHeaderMap.put(COL_7, col7Header);

        return columnHeaderMap;
    }

    public void sortByHeader(int headerCol) {
        String colXPath = "id('attrTable')/table/thead/tr/th["
                +headerCol+"]";
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.xpath(colXPath)));
        driver.findElement(By.xpath(colXPath)).click();

        element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.xpath(colXPath)));
        driver.findElement(By.xpath(colXPath)).click();

    }
}
