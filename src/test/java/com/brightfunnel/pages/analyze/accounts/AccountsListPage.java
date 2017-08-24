package com.brightfunnel.pages.analyze.accounts;

import com.brightfunnel.pages.BasePage;
import com.brightfunnel.pages.Environments;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Calendar;
import java.util.Date;

public class AccountsListPage extends BasePage {

    public static final String PAGE_NAME = "Accounts List Page";
    public final String basePath = "#/analyze/accounts/list-analysis?cohort=quarter2Date&account_filter=all&metric=percentage";

    public AccountsListPage(WebDriver newDriver) {
        super(newDriver);
    }

    public AccountsListPage(WebDriver newDriver, Environments environment) {
        super(newDriver, environment);
    }

    public void navigateTo(){
        driver.findElement(By.xpath("id('analyze-svg')")).click();
        driver.findElement(
                By.xpath("id('inner-nav-body')//div/ul/li/ng-include//span/a[contains(., 'Accounts List')]"))
                .click();
        waitForHeadingToLoad();
    }

    public void setFilters(String cohort, String accountFilter) {

        String baseUrl = getCurrentUrlBase();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date startDate = cal.getTime();
        Date endDate = new Date();

        String dateParams = String.format("&startDate=%s&endDate=%s",startDate.getTime(), endDate.getTime());

        String targetPath = String.format("#/analyze/accounts/list-analysis?cohort=%s&account_filter=%s&metric=percentage",
                cohort, accountFilter);

        driver.get(baseUrl + targetPath + dateParams);

        waitForHeadingToLoad();
    }

    public void sortByColumnHeader(int headerCol) {

        String xpath = String.format(
                "id('bottom-right-bottom')//div/table/thead/tr/th[%s]",
                headerCol);

        driver.findElement(By.xpath(xpath)).click();

        String footerXpath = "id('bottom-right-bottom')/div//table/tfoot/tr[1]/td";
        WebElement element = (new WebDriverWait(driver, TIME_OUT_IN_SECONDS)).
                until(ExpectedConditions.visibilityOfElementLocated(By.xpath(footerXpath)));
    }
}
