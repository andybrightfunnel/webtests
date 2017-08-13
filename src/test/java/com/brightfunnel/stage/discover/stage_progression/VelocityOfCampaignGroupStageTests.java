package com.brightfunnel.stage.discover.stage_progression;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.pages.discover.stage_progression.VelocityOfCampaignGroupPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VelocityOfCampaignGroupStageTests extends BaseStageTestCase{


    private String[] cohorts = {"Q117", "Q416", "Q316"};

    /**
     * Logs into stage, goes to target page, and for each combination of period, oppty types, etc, it will
     * pull the data and compare each to prod. It will fail if the differences between
     * the two environments
     *
     * @throws Exception
     */
    @Test
    public void testVelocityOfCampaignGroupPage_totals() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts){
                    String result = testVelocityOfCampaignGroupPage_dataTotals(orgId, cohort);
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

    private String testVelocityOfCampaignGroupPage_dataTotals(int orgId, String cohort) {
        System.out.println("Starting " + VelocityOfCampaignGroupPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Cohort: " + cohort);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the conversion trends page
        VelocityOfCampaignGroupPage velocityOfCampaignGroupPage = new VelocityOfCampaignGroupPage(driver, Environments.STAGE);
        velocityOfCampaignGroupPage.navigateTo();

        velocityOfCampaignGroupPage.changeCohort(cohort);

        // get column headers for data table
        Map<String,Object> columnHeaderMap = velocityOfCampaignGroupPage.getDataHeaderMap();
        String datRowXPath = "id('vlcTable')/tbody/tr";

        Map<String,Map> stageDataMap = getTableDataMap(velocityOfCampaignGroupPage, datRowXPath);

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        velocityOfCampaignGroupPage = new VelocityOfCampaignGroupPage(driver, Environments.PROD);
        velocityOfCampaignGroupPage.navigateTo();
        velocityOfCampaignGroupPage.changeCohort(cohort);

        // get column headers for data table
        Map<String,Map> prodDataMap = getTableDataMap(velocityOfCampaignGroupPage, datRowXPath);

        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        return comparisonResult.toString();

    }

}
