package com.brightfunnel.pages.discover.revenue_pipeline;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributionTrendsPage extends BasePage {

    String basePath = "#/discover/revenue-and-pipeline/attribution-trends?metric=value&type=revenue&model=sourced";

    public static final String PAGE_NAME = "Attribution Trends Page";
    public static final String DATA_COLUMNS_KEYS[] =
            {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11, COL_12};

    public AttributionTrendsPage(WebDriver newDriver) {
        super(newDriver);
    }

    public AttributionTrendsPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){

        driver.findElement(By.xpath("id('Combined-Shape')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Attribution Trends')]"))
                .click();
        waitForHeadingToLoad();

    }


    public void changeAttributionModel(String revenueType, String attributionModel) {
        String baseUrl = getCurrentUrlBase();
        String targetPath = String.format("#/discover/revenue-and-pipeline/attribution-trends?metric=value&type=%s&model=%s",
                revenueType, attributionModel);

        driver.get(baseUrl + targetPath);

        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));
    }

    public Map<String, Object> getDataTableHeaders() {
        Map<String,Object> headerMap = new HashMap<>();

        String col1Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[4]")).getText();
        String col2Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[5]")).getText();
        String col3Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[6]")).getText();
        String col4Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[7]")).getText();
        String col5Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[8]")).getText();
        String col6Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[9]")).getText();
        String col7Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[10]")).getText();
        String col8Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[11]")).getText();
        String col9Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[12]")).getText();
        String col10Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[13]")).getText();
        String col11Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[14]")).getText();
        String col12Header = driver.findElement(By.xpath("id('revenueByChannelAcrossQtrsTable')/thead/tr/th[15]")).getText();

        headerMap.put(COL_1, col1Header);
        headerMap.put(COL_2, col2Header);
        headerMap.put(COL_3, col3Header);
        headerMap.put(COL_4, col4Header);
        headerMap.put(COL_5, col5Header);
        headerMap.put(COL_6, col6Header);
        headerMap.put(COL_7, col7Header);
        headerMap.put(COL_8, col8Header);
        headerMap.put(COL_9, col9Header);
        headerMap.put(COL_10, col10Header);
        headerMap.put(COL_11, col11Header);
        headerMap.put(COL_12, col12Header);

        return headerMap;
    }

}
