package com.brightfunnel.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.List;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.*;

public class BasePage {

    protected WebDriver driver;
    public final String DEV_BASE_URL = "https://dev.brightfunnel.com";
    public final String STAGE_BASE_URL = "https://stage.brightfunnel.com";
    public final String PROD_BASE_URL = "https://app.brightfunnel.com";
    public static final int TIME_OUT_IN_SECONDS = 60;

    // Data column map keys
    public static final String COL_1 = "col1";
    public static final String COL_2 = "col2";
    public static final String COL_3 = "col3";
    public static final String COL_4 = "col4";
    public static final String COL_5 = "col5";
    public static final String COL_6 = "col6";
    public static final String COL_7 = "col7";
    public static final String COL_8 = "col8";
    public static final String COL_9 = "col9";
    public static final String COL_10 = "col10";
    public static final String COL_11 = "col11";
    public static final String COL_12 = "col12";

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
        //driver.findElement(By.tagName("body")).sendKeys(Keys.chord(Keys.CONTROL,"t"));
        ((JavascriptExecutor) driver).executeScript("window.open('', '_blank')");
    }

    public void switchToNewTab(){
        ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
    }

    public void closeNewTab(){
        ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        driver.close();
        driver.switchTo().window(tabs.get(0));
    }

    public void waitForHeadingToLoad() {
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));
    }

    public String getStringDataValue(WebElement row, int dataCol) {
        return row.findElement(By.xpath("//td["+dataCol+"]")).getText();
    }

    public Object getDecimalDataValue(WebElement row, int col) {
        String val = row.findElement(By.xpath("//td["+col+"]")).getText();

        BigDecimal decimalVal = null;
        try{
            decimalVal = new BigDecimal(val.replaceAll("[/\\D/g]", ""));
        }catch(Exception e){
            return val;
        }
        return decimalVal;
    }

    public Object getDecimalDataValue(String val) {

        BigDecimal decimalVal = null;
        try{
            decimalVal = new BigDecimal(val.replaceAll("[$,()]", ""));
        }catch(Exception e){
            return val;
        }
        return decimalVal;
    }


    /**
     * Looks up the column header values for the displayed data table
     * @return
     */
    public Map<String, Object> getDataHeaderMap(String xpath) {

        Map<String,Object> headerMap = new HashMap<>();

        java.util.List<WebElement> columns = driver.findElements(By.xpath(xpath));
        int colIndex = 1;

        for(WebElement col : columns){
            String val = col.getText();

            headerMap.put("col" + colIndex, val);
            colIndex++;
        }

        return headerMap;
    }

    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        java.util.List<WebElement> cols = row.findElements(By.tagName("td"));

        int rowIndex=1;

        for(int i=0; i < cols.size(); i++){

            WebElement col = cols.get(i);
            String val = col.getText();

            if(val.length() == 0 && rowData.isEmpty()) // ignore begining empty cols
                continue;

            rowData.put("col"+(rowIndex), getDecimalDataValue(val));
            rowIndex++;
        }

        return rowData;

    }
}
