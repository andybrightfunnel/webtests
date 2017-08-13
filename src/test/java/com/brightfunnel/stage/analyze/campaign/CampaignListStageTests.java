package com.brightfunnel.stage.analyze.campaign;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.campaign.CampaignListPage;
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
            String output = StagesSnapshotPage.PAGE_NAME + " totals differ for at least one org. Results[" +
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
        Map<String,Object> columnHeaderMap = campaignListPage.getDataHeaderMap();
        Map<String,Map> stageDataMap = new HashMap<>();
        List<WebElement> dataRows = driver.findElements(By.xpath("id('campaign-groups-tab')/table/tbody/tr"));

        for(WebElement row : dataRows){
            Map rowData = campaignListPage.getDataRowMap(row);
            String key = (String) rowData.get(COL_1);

            if(key.length() == 0)
                continue;
            stageDataMap.put(key, rowData);
        }

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
        Map<String,Map> prodDataMap = new HashMap<>();
        dataRows = driver.findElements(By.xpath("id('campaign-groups-tab')/table/tbody/tr"));

        for(WebElement row : dataRows){
            Map rowData = campaignListPage.getDataRowMap(row);
            String key = (String) rowData.get(COL_1);

            if(key.length() == 0)
                continue;
            prodDataMap.put(key, rowData);
        }

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        // go through both data sets and compare
        StringBuffer comparisonResult = new StringBuffer();
        String messageTemplate = "%s - %s match fail for %s.- [%s]";
        for(String key : stageDataMap.keySet()){
            String result = "";

            Map stageRowData = stageDataMap.get(key);

            Map prodRowData = prodDataMap.get(key);
            String columnHeader = (String) columnHeaderMap.get(COL_1);
            String rowName = (String) stageRowData.get(COL_1);
            if(prodRowData == null){
                System.out.println("Missing campaign group:" + key + " in production");
                continue;
            }

            result = compareDataRows(columnHeaderMap, stageRowData, prodRowData);
            if(result.length() > 0)
                comparisonResult.append(comparisonResult.append(
                        String.format(messageTemplate, orgId, columnHeader, rowName, result)));
        }


        return comparisonResult.toString();


    }


}
