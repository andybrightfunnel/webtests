package com.brightfunnel.stage.analyze.opportunities;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.analyze.opportunities.OpportunitiesListPage;
import com.brightfunnel.pages.discover.stage_progression.StagesSnapshotPage;
import com.brightfunnel.stage.BaseStageTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpportunitiesListStageTests extends BaseStageTestCase {



    private final String[] cohorts = { "Q117", "Q416" };
    private final String[] models = { "last", "even", "custom", "none"};
    private final String[] types = { "dateType=created&closedType=any&type=pipeline",   // created
           "dateType=closed&closedType=any&type=closed",                                // closed
            "dateType=closed&closedType=won&type=revenue",                              // closed won
            "dateType=closed&closedType=open&type=forecast"};                           // forcasted


    @Test
    public void testOpportunitiesListPage_totals() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String cohort : cohorts)
                    for(String model : models)
                        for(String type : types){
                        String result = testOpportunitiesListPage_dataTable(orgId, cohort, model, type);
                        if(result.length() > 0)
                            failedOrgs.add(result);
                    }


            }catch(Exception e){
                e.printStackTrace();
                failedOrgs.add("[Org: " + orgId + "] - error: " + e.getMessage());
            }

        }

        if(!failedOrgs.isEmpty()){
            String output = OpportunitiesListPage.PAGE_NAME + " totals differ for at least one org. Results[" +
                    listToString(failedOrgs);
            System.out.println(output);
            fail(output);
        }

    }

    private String testOpportunitiesListPage_dataTable(int orgId, String cohort, String model, String type) {

        System.out.println("Starting " + OpportunitiesListPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Cohort: " + cohort + ", Model: " + model + ", Type: " + type);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the opportunities list page
        OpportunitiesListPage opportunitiesListPage = new OpportunitiesListPage(driver, Environments.STAGE);
        opportunitiesListPage.navigateTo();

        opportunitiesListPage.setFilters(cohort, model, type);

        String headerXpath = "id('bottom-right-bottom')//div/table/thead/tr/th";
        String rowsXpath = "id('bottom-right-bottom')//div/table/tbody/tr";
        // get column headers for data table
        Map<String,Object> columnHeaderMap = getDataHeaderMap(headerXpath);
        Map<String,Map> stageDataMap = getTableDataMap(opportunitiesListPage, rowsXpath);


        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        opportunitiesListPage = new OpportunitiesListPage(driver, Environments.PROD);
        opportunitiesListPage.navigateTo();
        opportunitiesListPage.setFilters(cohort, model, type);

        // get column headers for data table
        Map<String,Map> prodDataMap = getTableDataMap(opportunitiesListPage, rowsXpath);

        // go through both data sets and compare
        StringBuffer comparisonResult = compareDataSets(orgId, columnHeaderMap, stageDataMap, prodDataMap);

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();


        return comparisonResult.toString();
    }

}
