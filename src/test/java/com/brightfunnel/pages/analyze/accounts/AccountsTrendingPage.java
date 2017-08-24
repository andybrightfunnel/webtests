package com.brightfunnel.pages.analyze.accounts;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

public class AccountsTrendingPage extends BasePage{

    public static final String PAGE_NAME = "Accounts Trending Page";

    private final String basePath = "#/analyze/accounts/trending?cohort=quarter2Date&account_filter=all&metric=percentage&withSalesActivity";

    private Date startDate;
    private Date endDate;

    public AccountsTrendingPage(WebDriver newDriver) {
        super(newDriver);
    }

    public AccountsTrendingPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        endDate = cal.getTime();
        startDate = new Date();

    }

    public void navigateTo(){

        driver.findElement(By.xpath("id('analyze-svg')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Accounts Trending')]"))
                .click();
        waitForHeadingToLoad();
    }

    public void setFilters(String cohort, String metric) {

        String baseUrl = getCurrentUrlBase();
        String dateParams = String.format("&startDate=%s&endDate=%s",startDate.getTime(), endDate.getTime());

        String targetPath = String.format("#/analyze/accounts/trending?cohort=%s&account_filter=all&metric=%s&withSalesActivity",
                cohort, metric);

        int count = 0;
        while(count < 3) {
            try {
                driver.get(baseUrl + targetPath + dateParams);
                waitForHeadingToLoad();

                Assert.isTrue(driver.getCurrentUrl().contains(String.format("cohort=%s", cohort)));
                Assert.isTrue(driver.getCurrentUrl().contains(String.format("metric=%s", metric)));
                break;
            }catch(Exception e){
                System.out.println("target url not loaded correctly. retrying");
            }
            count++;
        }

    }

}
