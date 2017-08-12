package com.brightfunnel.stage.discover.stage_progression;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.stage_progression.CohortedWaterfallPage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brightfunnel.pages.BasePage.COL_1;

public class CohortedWaterfallPageStageTests extends BaseStageTestCase {

    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 10;

    String[] cohorts = { "Q217", "Q117", "Q416", "Q316"};
    int [] startStageSequences = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };


/*
https://stage.brightfunnel.com/#/discover/stage-progression/attribution?cohort=quarter2Date&startStageSequence=1&attModel=all
cohort=Q217&startStageSequence=1&attModel=all

startStageSequence=1-&attModel=all

 */


    /**
     * Logs into stage, goes to attribution trends page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    public void testCohortedWaterFallPage_totals() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts)
                    for(int startingStageSequence : startStageSequences){
                    String result = testCohortedWaterFallPage(orgId, cohort, startingStageSequence);
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

    private String testCohortedWaterFallPage(int orgId, String cohort, int startingStageSequence) {
        System.out.println("Starting " + CohortedWaterfallPage.PAGE_NAME +
                " tests for orgId: " + orgId + " and cohort: " + cohort +
                " and startingStageSequence: " + startingStageSequence);


        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the CohortedWaterfall page on stage
        CohortedWaterfallPage cohortedWaterfallPage = new CohortedWaterfallPage(driver, Environments.STAGE);
        cohortedWaterfallPage.navigateTo();

        cohortedWaterfallPage.changeCohortAndSequence(cohort, startingStageSequence);

        Map<String,Object> columnHeaderMap = cohortedWaterfallPage.getDataHeaderMap();
        Map<String,Map> stageDataMap = new HashMap<>();
        List<WebElement> dataRows = driver.findElements(By.xpath("id('rev-waterfall-table')/tbody/tr[@class='ng-scope collaps-init pointer']"));

        for(WebElement row : dataRows){
            Map rowData = cohortedWaterfallPage.getDataRowMap(row);
            String key = (String) rowData.get(COL_1);
            stageDataMap.put(key, rowData);
        }

        // log into prod
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the CohortedWaterfall page on stage
        cohortedWaterfallPage = new CohortedWaterfallPage(driver, Environments.PROD);
        cohortedWaterfallPage.navigateTo();

        cohortedWaterfallPage.changeCohortAndSequence(cohort, startingStageSequence);

        Map<String,Map> prodDataMap = new HashMap<>();
        List<WebElement> prodDataRows = driver.findElements(By.xpath("id('rev-waterfall-table')/tbody/tr[@class='ng-scope collaps-init pointer']"));

        for(WebElement row : prodDataRows){
            Map rowData = cohortedWaterfallPage.getDataRowMap(row);
            String key = (String) rowData.get(COL_1);
            prodDataMap.put(key, rowData);
        }

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        // go through both data sets and compare
        StringBuffer comparisonResult = new StringBuffer();
        String messageTemplate = "%s - %s match fail for %s.- [%s]";
        for(String key : stageDataMap.keySet()){
            Map stageRowData = stageDataMap.get(key);
            Map prodRowData = prodDataMap.get(key);
            String columnHeader = (String) columnHeaderMap.get(COL_1);
            String rowName = (String) stageRowData.get(COL_1);
            if(prodDataMap == null){
                System.out.println("Missing " + columnHeader + " in production");
                continue;
            }

            String result = compareDataRows(columnHeaderMap, stageRowData, prodRowData);
            if(result.length() > 0)
                comparisonResult.append(comparisonResult.append(
                        String.format(messageTemplate, orgId, columnHeader, rowName, result)));
        }


        return comparisonResult.toString();
    }
}
