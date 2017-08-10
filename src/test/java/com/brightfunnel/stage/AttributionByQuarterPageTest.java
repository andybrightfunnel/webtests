package com.brightfunnel.stage;

import com.brightfunnel.pages.AttributionByQuarterPage;
import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Stage tests for the Attribution By Quarter Page.
 */
public class AttributionByQuarterPageTest extends BaseStageTest {

    public static final int NUM_ROWS = 5;
    public static String USER_NAME;
    public static String PASSWORD;
    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 1_000;

    private WebDriver driver;
    String[] revenueTypes = { "booked", "pipeline"};
    String[] cohorts = { "Q316", "Q416", "Q117"};


    @Before
    public void setUp() throws Exception {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        USER_NAME = System.getenv("BF_USERNAME");
        PASSWORD = System.getenv("BF_PASSWORD");
        assertNotNull(USER_NAME, "Unable to retrieve username from system environment variable: BF_USERNAME");
        assertNotNull(PASSWORD, "Unable to retrieve password from system environment variable: BF_PASSWORD");

    }

    /**
     * Logs into stage, goes to attribution by quarter page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    public void testAttributionByQuarterPage() throws Exception{

        int[] orgIds = {12};
        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String revenueType : revenueTypes)
                    for(String cohort : cohorts){
                        String result = testAttributioneByQuarterPage(orgId, revenueType, cohort);
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

    private String testAttributioneByQuarterPage(int orgId, String revenueType, String cohort) {
        System.out.println("Starting stage test for attribution by quarter page totals for orgId: " + orgId +
                ", revenueType: " + revenueType + ", cohort: " + cohort);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);


        // go to attribution by quarter page
        AttributionByQuarterPage attributionByQuarterPage = new AttributionByQuarterPage(driver, Environments.STAGE);
        attributionByQuarterPage.navigateTo();

        attributionByQuarterPage.changeAttributionModel(revenueType, cohort);

        Map columnHeaderMap = attributionByQuarterPage.getDataColumnHeaderMap();

        // sort by campaign group asc
//        attributionByQuarterPage.sortByHeader(3);

        // pull data for first few rows
        List<Map<String,Object>> stageData = new LinkedList<>();
        int rowsOnPage [] = {1, 3, 5, 7, 9, 11};

        for(int i=0; i < rowsOnPage.length; i++){
            int row = rowsOnPage[i];

            Map rowData = attributionByQuarterPage.getDataMapForRow(row);
            stageData.add(rowData);
        }

        // open up homepage on prod
       // homePage.logout();
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);


        StringBuffer comparisonResult = new StringBuffer();

        // go to attribution by quarter page on prod
        attributionByQuarterPage = new AttributionByQuarterPage(driver, Environments.PROD);
        attributionByQuarterPage.navigateTo();

        attributionByQuarterPage.changeAttributionModel(revenueType, cohort);
        List<Map<String,Object>> prodData = new LinkedList<>();

        for(int i=0; i < rowsOnPage.length; i++){
            int row = rowsOnPage[i];

            Map rowData = attributionByQuarterPage.getDataMapForRow(row);
            prodData.add(rowData);
        }

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        // go through both sets of data and compare results
        String messageTemplate = "\t[OrgId: %s] - revenueType: %s, cohort: %s - %s\n";
        for(int i=0; i < prodData.size(); i++){
           Map stageRowData = stageData.get(i);
           Map prodRowData = prodData.get(i);
           String result = compareDataRows(columnHeaderMap, stageRowData, prodRowData);
           if(result.length() > 0)
               comparisonResult.append(comparisonResult.append(
                       String.format(messageTemplate, orgId, revenueType, cohort, result)));
        }
        return comparisonResult.toString();

    }

    private String compareDataRows(Map columnHeaderMap, Map stageRowData, Map prodRowData) {

        String columns [] = AttributionByQuarterPage.DATA_COLS;

        StringBuffer results = new StringBuffer();

        String messageTemplate = "\tFAILED[ %s does not match. Stage: %s, Prod: %s.]\n";
        for(String col : columns){
            String header = (String)columnHeaderMap.get(col);

            if(AttributionByQuarterPage.COL_1.equals(col)){
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

}
