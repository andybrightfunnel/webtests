package com.brightfunnel.stage.discover.revenue_pipeline;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.revenue_pipeline.AttributionTrendsPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

import static com.brightfunnel.pages.BasePage.COL_1;

public class AttributionTrendsStageTests extends BaseStageTestCase {


    String[] revenueTypes = { "revenue", "pipeline"};
    String[] attributionModels = { "sourced", "custom"};



    /**
     * Logs into stage, goes to attribution trends page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    @Test
    public void testAttributionTrendsPage() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String revenueType : revenueTypes)
                   for(String attributionModel :attributionModels){
                        String result = testAttributioneTrendingPage(orgId, revenueType, attributionModel);
                        if(result.length() > 0)
                            failedOrgs.add(result);
                    }


            }catch(Exception e){
                e.printStackTrace();
                failedOrgs.add("[Org: " + orgId + "] - error: " + e.getMessage());
            }


        }

        if(!failedOrgs.isEmpty()){
            String output = AttributionTrendsPage.PAGE_NAME +
                    " totals differ for at least one org. Results[" + listToString(failedOrgs);
            System.out.println(output);
            fail(output);
        }

    }

    private String testAttributioneTrendingPage(int orgId, String revenueType, String attributionModel) {
        System.out.println("Starting stage " + AttributionTrendsPage.PAGE_NAME + "test for orgId: " + orgId +
                ", revenueType: " + revenueType + ", attributionModel: " + attributionModel);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the attribution trending page
        AttributionTrendsPage attributionTrendsPage = new AttributionTrendsPage(driver, Environments.STAGE);
        attributionTrendsPage.navigateTo();
        attributionTrendsPage.changeAttributionModel(revenueType, attributionModel);

        Map<String,Object> dataColumnHeaders = attributionTrendsPage.getDataTableHeaders();
        Map<String,Map> stageDataMap = new HashMap<>();

        List<WebElement> dataRows = driver.findElements(By.xpath("id('revenueByChannelAcrossQtrsTable')/tbody/tr"));
        for(WebElement dataRow : dataRows){
            if(dataRow.getText().length() == 0)
                continue;

            Map<String,Object> rowData = attributionTrendsPage.getDataMapForRow(dataRow);
            String key = (String)rowData.get(COL_1);
            stageDataMap.put(key, rowData);
        }

        // log into prod in a separate tab
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to same attribution trending page with same attribution model
        attributionTrendsPage = new AttributionTrendsPage(driver, Environments.PROD);
        attributionTrendsPage.navigateTo();
        attributionTrendsPage.changeAttributionModel(revenueType, attributionModel);

        dataRows = driver.findElements(By.xpath("id('revenueByChannelAcrossQtrsTable')/tbody/tr"));
        Map<String,Map> prodDataMap = new HashMap<>();
        for(WebElement row : dataRows){
            if(row.getText().length() == 0)
                continue;
            Map<String,Object> rowData = attributionTrendsPage.getDataMapForRow(row);
            String key = (String)rowData.get(COL_1);
            prodDataMap.put(key, rowData);
        }


        // go through both sets of data and compare results
        StringBuffer comparisonResult = new StringBuffer();
        String messageTemplate = "\t[OrgId: %s] - revenueType: %s, cohort: %s - %s\n";
        for(String key : prodDataMap.keySet()){

            Map stageRowData = stageDataMap.get(key);
            Map prodRowData = prodDataMap.get(key);
            if(stageRowData == null)
                continue;

            String result = compareDataRows(dataColumnHeaders, stageRowData, prodRowData);
            if(result.length() > 0)
                comparisonResult.append(comparisonResult.append(
                        String.format(messageTemplate, orgId, revenueType, attributionModel, result)));
        }

        // log out of both tabs
        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        return comparisonResult.toString();

    }


    /**
     * Overrides base class method to allow for more columns stored in data map
     *
     * @return a list of dataMap keys for each column in the data list section on the page
     */
    public String[] getDataColumnMapKeys() {
        return AttributionTrendsPage.DATA_COLUMNS_KEYS;
    }
}
