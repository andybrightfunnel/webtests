package com.brightfunnel.pages.analyze.campaign;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.*;

public class HistoricalAveragesPage extends BasePage {

    public static final String PAGE_NAME = "Historical Averages Page";
    private final String basePath = "#/analyze/campaigns/historical-averages-campaign-group?cohort=quarter2Date&groupType=campaign";

    public HistoricalAveragesPage(WebDriver newDriver) {
        super(newDriver);
    }

    public HistoricalAveragesPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        String baseUrl = getCurrentUrlBase();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date endDate = cal.getTime();
        Date startDate = new Date();
        String extraPath = String.format("&startDate=%s&endDate=%s", startDate.getTime(), endDate.getTime());
        driver.get(baseUrl + basePath + extraPath);

        // todo: add asserts to verify page loads correctly
        waitForHeadingToLoad();
    }


    public Map getDataRowMap(WebElement row) {
        Map<String, Object> rowData = new HashMap<>();

        List<WebElement> cols = row.findElements(By.tagName("td"));

        for(int i=0; i < cols.size(); i++){
            WebElement col = cols.get(i);
            String val = col.getText();
            rowData.put("col"+i, getDecimalDataValue(cols.get(i).getText()));
        }

        return rowData;

    }


    public void changeCohort(String cohort) {

        String baseUrl = getCurrentUrlBase();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date startDate = cal.getTime();
        Date endDate = new Date();


        String targetPath = String.format("#/analyze/campaigns/historical-averages-campaign-group?cohort=quarter2Date&groupType=campaign?startDate=%s&endDate=%s&" +
                "cohort=%s&groupType=campaign", startDate.getTime(), endDate.getTime(), cohort);

        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();
    }
}
