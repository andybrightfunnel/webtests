package com.brightfunnel.stage.analyze.accounts;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.accounts.AccountsTrendingPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Stage tests for the Accounts Trending Page
 */
public class AccountsTrendingStageTests extends BaseStageTestCase {

    public final String[] cohorts = {"Q117", "Q416"};
    public final String[] metrics = { "percentage", "activity", "contacts", "engaged", "average"};


    @Test
    public void testAccountsTrendingPage_totals() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts)
                    for(String metric : metrics){
                        String result = testAccountsTrendingPage_dataTable(orgId, cohort, metric);
                        if(result.length() > 0) {

                            String errorMsg = String.format("Org %s, cohort: %s, metric: %s. Result[%s]",
                                    orgId, cohort, metric, result);
                            failedOrgs.add(errorMsg);

                        }
                    }


            }catch(Exception e){
                e.printStackTrace();
                failedOrgs.add("[Org: " + orgId + "] - error: " + e.getMessage());
            }

        }

        if(!failedOrgs.isEmpty()){
            String output = AccountsTrendingPage.PAGE_NAME + " totals differ for at least one org. Results[" +
                    listToString(failedOrgs);
            System.out.println(output);
            fail(output);
        }

    }

    private String testAccountsTrendingPage_dataTable(int orgId, String cohort, String metric) {

        System.out.println("Starting " + AccountsTrendingPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Cohort: " + cohort + ", Metric: " + metric);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the accounts list page
        AccountsTrendingPage accountsTrendingPage = new AccountsTrendingPage(driver, Environments.STAGE);
        accountsTrendingPage.navigateTo();
        accountsTrendingPage.setFilters(cohort, metric);



        String headerXpath = "id('bottom-right-bottom')//div//table/thead/tr[2]/th";
        String rowsXpath = "id('bottom-right-bottom')/div/div/div[2]/div/table/tbody/tr";
        // get column headers for data table
        Map<String,Object> columnHeaderMap = getDataHeaderMap(headerXpath);
        Map<String,Map> stageDataMap = getTableDataMap(accountsTrendingPage, rowsXpath);

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        accountsTrendingPage = new AccountsTrendingPage(driver, Environments.PROD);
        accountsTrendingPage.navigateTo();
        accountsTrendingPage.setFilters(cohort,metric);

        // get column headers for data table
        Map<String,Map> prodDataMap = getTableDataMap(accountsTrendingPage, rowsXpath);

        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);


        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        return comparisonResult.toString();

    }

}
