package com.brightfunnel.stage.discover.revenue_pipeline;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.revenue_pipeline.AttributionTrendsPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Before;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AttributionTrendsPageStageTests extends BaseStageTestCase {

    public static String USER_NAME;
    public static String PASSWORD;
    public static final int NUM_DATA_ROWS_TO_INSPECT = 5;

    String[] revenueTypes = { "revenue", "pipeline"};
    String[] attributionModels = { "sourced", "custom"};


    @Before
    public void setUp() throws Exception {
        initDriver();
        USER_NAME = System.getenv("BF_USERNAME");
        PASSWORD = System.getenv("BF_PASSWORD");
        assertNotNull(USER_NAME, "Unable to retrieve username from system environment variable: BF_USERNAME");
        assertNotNull(PASSWORD, "Unable to retrieve password from system environment variable: BF_PASSWORD");

    }

    /**
     * Logs into stage, goes to attribution trends page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    public void testAttributionTrendsPage() throws Exception{

        int[] orgIds = {12};
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
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        // go to the attribution trending page
        AttributionTrendsPage attributionTrendsPage = new AttributionTrendsPage(driver, Environments.STAGE);
        attributionTrendsPage.navigateTo();
        attributionTrendsPage.changeAttributionModel(revenueType, attributionModel);

        Map<String,Object> dataColumnHeaders = attributionTrendsPage.getDataTableHeaders();
        List<Map> stageData = new LinkedList<>();
        int [] rows = { 1, 3, 5, 7, 9, 11, 13, 15};

        for(int row : rows){
            Map<String,Object> rowData = attributionTrendsPage.getDataMapForRow(row);
            stageData.add(rowData);
        }

        // log into prod in a separate tab
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        // go to same attribution trending page with same attribution model
        attributionTrendsPage = new AttributionTrendsPage(driver, Environments.PROD);
        attributionTrendsPage.navigateTo();
        attributionTrendsPage.changeAttributionModel(revenueType, attributionModel);

        List<Map> prodData = new LinkedList<>();

        for(int row : rows){
            Map<String,Object> rowData = attributionTrendsPage.getDataMapForRow(row);
            prodData.add(rowData);
        }

        // log out of both tabs
        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        // go through both sets of data and compare results
        StringBuffer comparisonResult = new StringBuffer();
        String messageTemplate = "\t[OrgId: %s] - revenueType: %s, cohort: %s - %s\n";
        for(int i=0; i < prodData.size(); i++){
            Map stageRowData = stageData.get(i);
            Map prodRowData = prodData.get(i);
            String result = compareDataRows(dataColumnHeaders, stageRowData, prodRowData);
            if(result.length() > 0)
                comparisonResult.append(comparisonResult.append(
                        String.format(messageTemplate, orgId, revenueType, attributionModel, result)));
        }
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
