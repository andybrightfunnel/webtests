package com.brightfunnel.stage.discover.revenue_pipeline;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.revenue_pipeline.AttributionTrendsPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

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

        Map<String,Object> dataColumnHeaders = this.getDataHeaderMap("id('revenueByChannelAcrossQtrsTable')/thead/tr/th");
        String rowsXpath = "id('revenueByChannelAcrossQtrsTable')/tbody/tr";

        Map<String,Map> stageDataMap = getTableDataMap(attributionTrendsPage, rowsXpath);

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

        Map<String,Map> prodDataMap = getTableDataMap(attributionTrendsPage, rowsXpath);


        // go through both sets of data and compare results
        StringBuffer comparisonResult = compareDataSets(orgId, dataColumnHeaders, stageDataMap, prodDataMap);

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
