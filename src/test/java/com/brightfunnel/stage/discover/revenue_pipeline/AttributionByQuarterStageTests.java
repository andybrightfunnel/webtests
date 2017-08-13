package com.brightfunnel.stage.discover.revenue_pipeline;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.revenue_pipeline.AttributionByQuarterPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.brightfunnel.pages.BasePage.*;

/**
 * Stage tests for the Attribution By Quarter Page.
 */
public class AttributionByQuarterStageTests extends BaseStageTestCase {


    String[] revenueTypes = { "booked", "pipeline"};
    String[] cohorts = { "Q117", "Q416" };


    /**
     * Logs into stage, goes to attribution by quarter page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    @Test
    public void testAttributionByQuarterPage() throws Exception{

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
            String output =  AttributionByQuarterPage.PAGE_NAME +
                    "totals differ for at least one org. Results[" + listToString(failedOrgs);
            System.out.println(output);
            fail(output);
        }

    }

    private String testAttributioneByQuarterPage(int orgId, String revenueType, String cohort) {
        System.out.println("Starting stage test for " + AttributionByQuarterPage.PAGE_NAME + " totals for orgId: " + orgId +
                ", revenueType: " + revenueType + ", cohort: " + cohort);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);


        // go to attribution by quarter page
        AttributionByQuarterPage attributionByQuarterPage = new AttributionByQuarterPage(driver, Environments.STAGE);
        attributionByQuarterPage.navigateTo();
        attributionByQuarterPage.changeAttributionModel(revenueType, cohort);

        String headersXpath = "id('attrTable')/table/thead/tr/th";

        Map columnHeaderMap = getDataHeaderMap(headersXpath);
        String rowsXpath = "id('attrTable')/table/tbody/tr";

        // pull data for first few rows
        Map<String,Map> stageDataMap = getTableDataMap(attributionByQuarterPage, rowsXpath);

        // homePage.logout();
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);


        // go to attribution by quarter page on prod
        attributionByQuarterPage = new AttributionByQuarterPage(driver, Environments.PROD);
        attributionByQuarterPage.navigateTo();
        attributionByQuarterPage.changeAttributionModel(revenueType, cohort);

        Map<String,Map> prodDataMap = getTableDataMap(attributionByQuarterPage, rowsXpath);

        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);


        return comparisonResult.toString();

    }

    /*
        Override base class to allow for more columns in the data map
     */
    public String[] getDataColumnMapKeys() {
        return new String[] {COL_1, COL_2, COL_3, COL_4, COL_5, COL_6, COL_7};
    }

}
