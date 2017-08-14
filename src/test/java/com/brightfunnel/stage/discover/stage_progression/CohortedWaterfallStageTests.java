package com.brightfunnel.stage.discover.stage_progression;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.stage_progression.CohortedWaterfallPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CohortedWaterfallStageTests extends BaseStageTestCase {


    String[] cohorts = { "Q217", "Q416" };
    int [] startStageSequences = { 1, 2, 3, 4 };


    /**
     * Logs into stage, goes to attribution trends page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    @Test
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
            String output = CohortedWaterfallPage.PAGE_NAME + " totals differ for at least one org. Results[" +
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
        String rowExpath = "id('rev-waterfall-data')//tr[@class='ng-scope collaps-init pointer']";

        Map<String,Map> stageDataMap = getTableDataMap(cohortedWaterfallPage, rowExpath);

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the CohortedWaterfall page on stage
        cohortedWaterfallPage = new CohortedWaterfallPage(driver, Environments.PROD);
        cohortedWaterfallPage.navigateTo();
        cohortedWaterfallPage.changeCohortAndSequence(cohort, startingStageSequence);

        Map<String,Map> prodDataMap = getTableDataMap(cohortedWaterfallPage, rowExpath);

        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();


        return comparisonResult.toString();
    }
}
