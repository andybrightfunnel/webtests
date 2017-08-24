package com.brightfunnel.pages.analyze.campaign;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class CampaignListPage extends BasePage {
    public final static String PAGE_NAME = "Campaign List Page";

    String basePath = "#/analyze/campaigns/list-analysis/campaign-groups?cohort=quarter2Date&dataSet=membershipActivity&chart=ranged.sourced.leads&model=sourced&ty=c&co=alpha&t&grp=no-list";

    public CampaignListPage(WebDriver newDriver) {
        super(newDriver);
    }

    public CampaignListPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        driver.findElement(By.xpath("id('analyze-svg')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Campaigns List')]"))
                .click();
        waitForHeadingToLoad();
    }

    /**
     * Looks up the column header values for the displayed data table
     * @return
     */
    public Map<String, Object> getDataHeaderMap() {

        Map<String,Object> headerMap = new HashMap<>();

        List<WebElement> columns = driver.findElements(By.xpath("id('campaign-groups-tab')/table/thead/tr/th"));
        int colIndex = 1;

        for(WebElement col : columns){
            String val = col.getText();

            headerMap.put("col" + colIndex, col.getText());
            colIndex++;
        }

        return headerMap;
    }

    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        List<WebElement> cols = row.findElements(By.tagName("td"));

        int colIndex = 1;

        for(int i=0; i < cols.size(); i++){
            WebElement col = cols.get(i);
            String val = col.getText();

            if(val.length() == 0 && rowData.isEmpty())
                continue;

            rowData.put("col"+colIndex, getDecimalDataValue(val));
            colIndex++;
        }

        return rowData;

    }

    public void changeCohortAndModel(String cohort, String model, String dataSet) {

        String baseUrl = getCurrentUrlBase();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date endDate = cal.getTime();
        Date startDate = new Date();

        String targetPath =
                String.format("#/analyze/campaigns/list-analysis/campaign-groups?cohort=%s&dataSet=%s&chart=ranged.sourced.leads&model=%s&ty=c&co=alpha&t&grp=no-list&startDate=%s&endDate=%s",
                        cohort, model, dataSet, startDate, endDate);
        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();
    }
}
