package com.brightfunnel.pages.analyze.opportunities;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.WebDriver;

import java.util.Calendar;
import java.util.Date;

public class OpportunitiesListPage extends BasePage {

    public static final String PAGE_NAME = "Opportunities List Page";
    private final String basePath = "#/analyze/opportunities/list-analysis?cohort=quarter2Date&dateType=created&closedType=any&type=pipeline";

    public OpportunitiesListPage(WebDriver newDriver) {
        super(newDriver);
    }

    public OpportunitiesListPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, 7); // 1 week into future
        Date startDate = cal.getTime();
        Date endDate = new Date();

        String extraPath = String.format("&startDate=%s&endDate=%s", startDate.getTime(), endDate.getTime());
        String currentBaseUrl = getCurrentUrlBase();
        String targetUrl = currentBaseUrl + basePath + extraPath;

        int count = 0;
        while(count < 3) {
            try{
                driver.get(targetUrl);

                // todo: add asserts to verify page loads correctly
                waitForHeadingToLoad();
                break;
            }catch(Exception e){
                System.out.println("target url \n[" + targetUrl + "] \ntimed out, retrying up 3 times");
            }
            count++;
        }
    }


    public void setFilters(String cohort, String model, String type) {

        String currentBasePath = getCurrentUrlBase();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, 7); // 1 week into future
        Date startDate = cal.getTime();
        Date endDate = new Date();

        String targetPath = String.format("#/analyze/opportunities/list-analysis?" +
                        "startDate=%s&endDate=%s&cohort=%s&%s&model=%s",
                startDate.getTime(), endDate.getTime(), cohort, type, model);

        driver.get(currentBasePath + targetPath);

        waitForHeadingToLoad();

    }
}
