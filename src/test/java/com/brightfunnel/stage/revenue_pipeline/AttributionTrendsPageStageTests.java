package com.brightfunnel.stage.revenue_pipeline;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.revenue_pipeline.AttributionTrendsPage;
import com.brightfunnel.stage.BaseStageTest;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.brightfunnel.pages.BasePage.*;
import static com.brightfunnel.pages.BasePage.COL_1;

public class AttributionTrendsPageStageTests extends BaseStageTest {

    public static String USER_NAME;
    public static String PASSWORD;
    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 1_000;
    public static final int NUM_DATA_ROWS_TO_INSPECT = 5;

    private WebDriver driver;
    String[] revenueTypes = { "revenue", "pipeline"};
    String[] attributionModels = { "sourced", "custom"};


    @Before
    public void setUp() throws Exception {
        driver = createDriver(WEBDRIVER_TYPE.CHROME_DRIVER);

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
            System.out.println("Attribution By Quarter totals differ for at least one org. Results[" + listToString(failedOrgs));
            fail("Attribution By Quarter totals differ for at least one org");
        }

    }

    private String testAttributioneTrendingPage(int orgId, String revenueType, String attributionModel) {
        System.out.println("Starting stage test for attribution trendingpage totals for orgId: " + orgId +
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

    private String compareDataRows(Map columnHeaderMap, Map stageRowData, Map prodRowData) {


        String columns [] = {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7, COL_8, COL_9, COL_10, COL_11, COL_12};

        StringBuffer results = new StringBuffer();

        String messageTemplate = "\tFAILED[ %s does not match. Stage: %s, Prod: %s.]\n";
        for(String col : columns){
            String header = (String)columnHeaderMap.get(col);

            if(COL_1.equals(col)){
                String stageVal = (String)stageRowData.get(col);
                String prodVal = (String) prodRowData.get(col);
                if(!stageVal.equals(prodVal)){
                    results.append("Stage campaign Ids don't match. Stage: "  + stageVal +
                            ", prod: " + prodVal );
                }
            }else{
                BigDecimal stageVal = (BigDecimal) stageRowData.get(col);
                BigDecimal prodVal = (BigDecimal) prodRowData.get(col);

                BigDecimal diff = prodVal.subtract(stageVal).abs();
                if(diff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT){
                    results.append(String.format(messageTemplate, header, stageVal, prodVal));
                }
            }

        }
        return results.toString();
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

}
