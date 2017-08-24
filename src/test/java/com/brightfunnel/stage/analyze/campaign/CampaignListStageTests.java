package com.brightfunnel.stage.analyze.campaign;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.campaign.CampaignListPage;
import com.brightfunnel.pages.discover.stage_progression.VelocityOfCampaignGroupPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

public class CampaignListStageTests extends BaseStageTestCase {

    private String[] cohorts = { "Q117", "Q416", "Q316"};
    private String[] models = { "source", "last", "even", "custom"};
    private String[] dataSets = { "membershipActivity", "campaignCreatedDate", "opptyCreatedDate", "opptyCloseDate" };


    /**
     * Logs into stage, goes to target page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    @Test
    public void testCampaignListPage_dataTable() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts)
                    for(String model : models)
                        for(String dataSet : dataSets){

                            String result = testCampaignListPage_dataTable(orgId, cohort, model, dataSet);
                            if(result.length() > 0)
                                failedOrgs.add(result);
                }


            }catch(Exception e){
                e.printStackTrace();
                failedOrgs.add("[Org: " + orgId + "] - error: " + e.getMessage());
            }

        }

        if(!failedOrgs.isEmpty()){
            String output = CampaignListPage.PAGE_NAME + " totals differ for at least one org. Results[" +
                    listToString(failedOrgs);
            System.out.println(output);
            fail(output);
        }

    }

    private String testCampaignListPage_dataTable(int orgId, String cohort, String model, String dataSet) {
        System.out.println("Starting " + VelocityOfCampaignGroupPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Cohort: " + cohort + ", model: " + model + ", dataSet: " + dataSet);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the conversion trends page
        CampaignListPage campaignListPage = new CampaignListPage(driver, Environments.STAGE);
        campaignListPage.navigateTo();

        campaignListPage.changeCohortAndModel(cohort, model, dataSet);

        // get column headers for data table
        String headerXPath = "id('campaign-groups-tab')/table/tbody/tr";
        String dataXPath = "id('campaign-groups-tab')/table/tbody/tr";

        Map<String,Object> columnHeaderMap = getDataHeaderMap(headerXPath);
        Map<String,Map> stageDataMap = getTableDataMap(campaignListPage, dataXPath);

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        campaignListPage = new CampaignListPage(driver, Environments.PROD);
        campaignListPage.navigateTo();

        campaignListPage.changeCohortAndModel(cohort, model, dataSet);

        // get column headers for data table
        Map<String,Map> prodDataMap = getTableDataMap(campaignListPage, dataXPath);

        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        return comparisonResult.toString();


    }


}
