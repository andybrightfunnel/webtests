package com.brightfunnel.stage.analyze.campaign;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.campaign.CampaignListPage;
import com.brightfunnel.pages.analyze.campaign.HistoricalAveragesPage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.pages.discover.stage_progression.VelocityOfCampaignGroupPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brightfunnel.pages.BasePage.COL_1;

public class HistoricalAveragesStageTests extends BaseStageTestCase {

    private String[] cohorts = { "Q117", "Q416", "Q316"};
    /**
     * Logs into stage, goes to target page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    @Test
    public void testHistoricalPage_dataTable() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts){

                            String result = testHistoricalPage_dataTable(orgId, cohort);
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

    private String testHistoricalPage_dataTable(int orgId, String cohort) {
        System.out.println("Starting " + HistoricalAveragesPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Cohort: " + cohort);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the historical averages page
        HistoricalAveragesPage historicalAveragesPage = new HistoricalAveragesPage(driver, Environments.STAGE);
        historicalAveragesPage.navigateTo();

        historicalAveragesPage.changeCohort(cohort);

        // get column headers for data table
        String headXPath = "id('bottom-right-bottom')/div/div/div[2]/div/table/thead/tr/th";
        Map<String,Object> columnHeaderMap = getDataHeaderMap(headXPath);

        String dataXPath = "id('bottom-right-bottom')/div/div/div[2]/div/table/tbody/tr";

        Map<String,Map> stageDataMap = getTableDataMap(historicalAveragesPage, dataXPath);

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        historicalAveragesPage = new HistoricalAveragesPage(driver, Environments.PROD);
        historicalAveragesPage.navigateTo();

        historicalAveragesPage.changeCohort(cohort);

        // get column headers for data table
        Map<String,Map> prodDataMap = getTableDataMap(historicalAveragesPage, dataXPath);


        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();


        return comparisonResult.toString();

    }

}
