package com.brightfunnel.stage.analyze.accounts;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.accounts.AccountsListPage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountsListStageTests extends BaseStageTestCase {

    public final String[] cohorts = {"Q117", "Q416"};
    public final String[] accountFilters = { "all", "has_sales_no_marketing", "has_marketing_no_sales",
        "has_web_activity_no_marketing_or_sales", "no_contacts", "open_opportunities",
        "active_no_open_opportunities", "inactive_accounts"};


    @Test
    public void testAccountsFilterPage_totals() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts)
                    for(String accountFilter : accountFilters){
                            String result = testAccountsFilterPage_dataTable(orgId, cohort, accountFilter);
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

    private String testAccountsFilterPage_dataTable(int orgId, String cohort, String accountFilter) {

        System.out.println("Starting " + AccountsListPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Cohort: " + cohort + ", Account Filter: " + accountFilter);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the accounts list page
        AccountsListPage accountsListPage = new AccountsListPage(driver, Environments.STAGE);
        accountsListPage.navigateTo();
        accountsListPage.setFilters(cohort, accountFilter);
        accountsListPage.sortByColumnHeader(1);



        String headerXpath = "id('bottom-right-bottom')//div/table/thead/tr/th";
        String rowsXpath = "id('bottom-right-bottom')//div/table/tbody/tr";
        // get column headers for data table
        Map<String,Object> columnHeaderMap = getDataHeaderMap(headerXpath);
        Map<String,Map> stageDataMap = getTableDataMap(accountsListPage, rowsXpath);

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        accountsListPage = new AccountsListPage(driver, Environments.PROD);
        accountsListPage.navigateTo();
        accountsListPage.setFilters(cohort,accountFilter);
        accountsListPage.sortByColumnHeader(1);

        // get column headers for data table
        Map<String,Map> prodDataMap = getTableDataMap(accountsListPage, rowsXpath);

        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);


        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        return comparisonResult.toString();

    }


}
