package com.brightfunnel.pages.discover.stage_progression;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Calendar;
import java.util.Date;


public class StagesSnapshotPage extends BasePage {

    public static final String PAGE_NAME = "Stages Snapshot Page";

    String basePath = "#/discover/stage-progression/stages-snapshot?cohort=Q217&startDate=1501803215902&endDate=1502408015902";

    public StagesSnapshotPage(WebDriver newDriver) {
        super(newDriver);
    }

    public StagesSnapshotPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        String baseUrl = getCurrentUrlBase();

        driver.get(baseUrl + basePath);
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.id("heading")));

        // todo: add asserts to verify page loads correctly
    }

    public void setFilters(String cohort) {

        Date startTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date endTime = cal.getTime();


        String baseUrl = getCurrentUrlBase();
        String targetPath = String.format("#/discover/stage-progression/stages-snapshot?cohort=%s&startDate=%s&endDate=%s",
                cohort, startTime.getTime(), endTime.getTime());

        driver.get(baseUrl + targetPath);

        waitForHeadingToLoad();
    }
}
