package com.brightfunnel.stage.discover.stage_progression;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.discover.stage_progression.ConversionTrendsPage;
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

public class ConversionTrendsStageTests extends BaseStageTestCase {


    String []startingStages = { "MQL", "Opportunity"};
    String [] endStages = {"Closed%20Won", "Demo", "MQL", "Opportunity"};



    /**
     * Logs into stage, goes to conversion trends page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    @Test
    public void testConversionTrendsPage_totals() throws Exception{

        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String startingStage : startingStages)
                    for(String endingStage : endStages){
                        String result = testConversionTrendsPage_dataTotals(orgId, startingStage, endingStage);
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

    private String testConversionTrendsPage_dataTotals(int orgId, String startingStage, String endingStage) {

        System.out.println("Starting " + ConversionTrendsPage.PAGE_NAME + " tests. OrgId: " +
                orgId + ", Starting stage: " + startingStage + ", Ending Stage: " + endingStage);

        // log into stage
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        // go to the conversion trends page
        ConversionTrendsPage conversionTrendsPage = new ConversionTrendsPage(driver, Environments.STAGE);
        conversionTrendsPage.navigateTo();

        conversionTrendsPage.setStageSettings(startingStage, endingStage);

        String headerXpath = "id('rev-waterfall-table')/table/thead/tr/th";
        String rowsXpath = "id('rev-waterfall-table')/tbody/tr";
        // get column headers for data table
        Map<String,Object> columnHeaderMap = conversionTrendsPage.getDataHeaderMap(headerXpath);
        Map<String,Map> stageDataMap = new HashMap<>();
        List<WebElement> dataRows = driver.findElements(By.xpath(rowsXpath));

        for(WebElement row : dataRows){
            Map rowData = conversionTrendsPage.getDataRowMap(row);
            String key = (String) rowData.get(COL_1);

            stageDataMap.put(key, rowData);
        }

        // log into prod
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(bfUsername, bfPassword);
        homePage.loginAsOrg(orgId);

        conversionTrendsPage = new ConversionTrendsPage(driver, Environments.PROD);
        conversionTrendsPage.navigateTo();

        conversionTrendsPage.setStageSettings(startingStage, endingStage);

        // get column headers for data table
        Map<String,Map> prodDataMap = new HashMap<>();
        dataRows = driver.findElements(By.xpath(rowsXpath));

        for(WebElement row : dataRows){
            Map rowData = conversionTrendsPage.getDataRowMap(row);
            String key = (String) rowData.get(COL_1);

            prodDataMap.put(key, rowData);
        }

        homePage.logout();
        homePage.closeNewTab();
        homePage.logout();

        // go through both data sets and compare
        StringBuffer comparisonResult = new StringBuffer();
        String messageTemplate = "%s - %s match fail for %s.- [%s]";
        for(String key : stageDataMap.keySet()){
            Map stageRowData = stageDataMap.get(key);
            Map prodRowData = prodDataMap.get(key);
            String columnHeader = (String) columnHeaderMap.get(COL_1);
            String rowName = (String) stageRowData.get(COL_1);
            if(prodDataMap == null){
                System.out.println("Missing " + columnHeader + " in production");
                continue;
            }

            String result = compareDataRows(columnHeaderMap, stageRowData, prodRowData);
            if(result.length() > 0)
                comparisonResult.append(comparisonResult.append(
                        String.format(messageTemplate, orgId, columnHeader, rowName, result)));
        }


        return comparisonResult.toString();

    }

}
