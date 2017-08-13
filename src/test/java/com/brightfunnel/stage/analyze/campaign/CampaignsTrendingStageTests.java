package com.brightfunnel.stage.analyze.campaign;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.campaign.CampaignsTrendingPage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.brightfunnel.pages.BasePage.COL_1;

public class CampaignsTrendingStageTests extends BaseStageTestCase {


    private String[] cohorts = { "Q416"};
    private String[] dataSets = {"membershipActivity",
            "campaignCreatedDate", "opptyCreatedDate", "opptyCloseDate"};
    private String[] models = { "sourced", "last"};
    private String[] fields = { "opptys", "leads", "deals", "deals_unique", "lead:New"};
    private String[] freqTypes = { "auto", "weeks", "months"};



    @Test
    public void testCampaignsTrending_dataTable() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts)
                    for(String model : models)
                        for(String dataSet : dataSets)
                            for(String field : fields)
                                for(String freqType : freqTypes){

                                String result = testCampaignsTrendingPage_dataTable(orgId, cohort, model, dataSet, field, freqType);
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

    private String testCampaignsTrendingPage_dataTable(int orgId, String cohort, String model,
                                                       String dataSet, String field, String freqType) {
        System.out.println("Starting " + CampaignsTrendingPage.PAGE_NAME +
                " stage test. OrgID: " + orgId + ", cohort: " + cohort + ", model: " + model
                + ", dataSet: " + dataSet + ", Field: " + field + ", freqType: " + freqType);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);


        // go to campaigns trending page
        CampaignsTrendingPage campaignsTrendingPage = new CampaignsTrendingPage(driver, Environments.STAGE);
        campaignsTrendingPage.navigateTo();
        campaignsTrendingPage.changeCohortAndModel(cohort, model, dataSet, field, freqType);

        // get column headers for data table
        String tableName ="bottom-right-bottom";
        Map<String,Object> columnHeaderMap = campaignsTrendingPage.getDataHeaderMap(tableName);
        Map<String,Map> stageDataMap = new HashMap<>();
        List<WebElement> dataRows = driver.findElements(By.xpath("id('bottom-right-bottom')/div/div/div[3]/div[2]/table/tbody/tr"));

        for(WebElement row : dataRows){
            Map rowData = campaignsTrendingPage.getDataRowMap(row);
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

        // go to campaigns trending page
        campaignsTrendingPage = new CampaignsTrendingPage(driver, Environments.PROD);
        campaignsTrendingPage.navigateTo();
        campaignsTrendingPage.changeCohortAndModel(cohort, model, dataSet, field, freqType);

        Map<String,Map> prodDataMap = new HashMap<>();
        dataRows = driver.findElements(By.xpath("id('bottom-right-bottom')/div/div/div[3]/div[2]/table/tbody/tr"));

        for(WebElement row : dataRows){
            Map rowData = campaignsTrendingPage.getDataRowMap(row);
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
                System.out.println("Missing group:" + key + " in production");
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
