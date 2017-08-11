package com.brightfunnel.stage.discover.stage_progression;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage tests for the Stages Snapshot Page
 */
public class StagesSnapshotPageStageTests extends BaseStageTestCase {

    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 10;

    String[] cohorts = { "Q217", "Q117", "Q416", "Q316"};




    /**
     * Logs into stage, goes to attribution trends page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    public void testStagesSnapshotPage() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts){
                    String result = testStagesSnapshotPageForOrg(orgId, cohort);
                        if(result.length() > 0)
                            failedOrgs.add(result);
                    }


            }catch(Exception e){
                e.printStackTrace();
                failedOrgs.add("[Org: " + orgId + "] - error: " + e.getMessage());
            }

        }

        if(!failedOrgs.isEmpty()){
            String output = StagesSnapshotPage.PAGE_NAME + " totals differ for at least one org. Results[" +
                    listToString(failedOrgs);
            System.out.println(output);
            fail(output);
        }

    }

    private String testStagesSnapshotPageForOrg(int orgId, String cohort) {
        System.out.println("Starting Stage Snapshot Page tests for orgId: " + orgId + " and cohort: " + cohort);


        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        // go to the StagesSnapshot page
        StagesSnapshotPage stagesSnapshotPage = new StagesSnapshotPage(driver, Environments.STAGE);
        stagesSnapshotPage.navigateTo();

        // change cohort
        stagesSnapshotPage.changeCohort(cohort);

        // pull all of the data table rows
        List<WebElement> dataRows =
                driver.findElements(By.xpath("//div[contains(@class, 'panel-group')]//h4/a/span/div\n"));

        Map<String,Object> stageData = new HashMap<>();
        for(WebElement dataRow : dataRows){
            String innerHTML = dataRow.getAttribute("innerHTML");
            String stageName = innerHTML.substring(0, innerHTML.indexOf("<"));
            String stageTotal = dataRow.findElement(By.tagName("span")).getText();
            stageData.put(stageName, new BigDecimal(stageTotal.replaceAll("[$,]","")));

        }

        // log into prod in a separate tab
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        // go to the StagesSnapshot page
        stagesSnapshotPage = new StagesSnapshotPage(driver, Environments.PROD);
        stagesSnapshotPage.navigateTo();

        // change cohort
        stagesSnapshotPage.changeCohort(cohort);

        // pull all of the data table rows
        dataRows = driver.findElements(By.xpath("//div[contains(@class, 'panel-group')]//h4/a/span/div\n"));

        Map<String,Object> prodData = new HashMap<>();
        for(WebElement dataRow : dataRows){
            String innerHTML = dataRow.getAttribute("innerHTML");
            String stageName = innerHTML.substring(0, innerHTML.indexOf("<"));
            String stageTotal = dataRow.findElement(By.tagName("span")).getText();
            prodData.put(stageName, new BigDecimal(stageTotal.replaceAll("[$,]","")));

        }

        // log out of both tabs
        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();


        // compare the data for stage and prod and report and discrepencies
        StringBuffer comparisonResult = new StringBuffer();
        String failMsgTemplate = "\tFail: %s on stage doesn't match stage on prod. Error: %s";
        for(String stageName : stageData.keySet()){

            BigDecimal stageVal = (BigDecimal)stageData.get(stageName);
            BigDecimal prodVal = (BigDecimal)prodData.get(stageName);

            if(prodVal == null){
                comparisonResult.append(String.format(failMsgTemplate, stageName, "No matching stage found on prod"));
                continue;
            }

            BigDecimal diff = prodVal.subtract(stageVal).abs();
            if(diff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT){
                comparisonResult.append(String.format(failMsgTemplate, stageName,
                        "Values differ by too much. Stage:" + stageVal + ", Prod: " + prodVal));
            }
        }

        return comparisonResult.toString();
    }
}
