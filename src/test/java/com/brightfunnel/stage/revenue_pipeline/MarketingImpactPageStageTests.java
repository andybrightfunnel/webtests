package com.brightfunnel.stage.revenue_pipeline;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.revenue_pipeline.MarketingAnalyticsPage;
import com.brightfunnel.stage.BaseStageTest;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is for stage testing the Marketing Impact Page
 */
public class MarketingImpactPageStageTests extends BaseStageTest {


    public static final int NUM_ROWS = 5;
    public static String USER_NAME;
    public static String PASSWORD;
    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 1_000;

    private WebDriver driver;
    String[] periods = { "quarter", "monthly"};
    String[] opptyTypes = { "deal", "oppty"};
    String[] modelTypes = { "sourced", "last", "even", "custom"};

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
     * Logs into stage, goes to marketing impact page, and for each combination of period, oppty types, etc, it will
     * pull the data for the first NUM_ROWS rows and compare each to prod. It will fail if the differences between
     * the two environments is > ACCEPTABLE_AMOUNT
     *
     * @throws Exception
     */
    public void testMarketingImpact() throws Exception{

        int[] orgIds = {12};
        List<String> failedOrgs = new ArrayList<>();

        for(int i=0; i < orgIds.length; i++){
            int orgId = orgIds[i];
            try{
                for(String periodType : periods){
                    for(String opptyType : opptyTypes){
                        for(String modelType : modelTypes){
                            String result = testMarketingImpactTotalsMultiTab(orgId, periodType, opptyType, modelType);
                            if(result.length() > 0)
                                failedOrgs.add("[Org: " + orgId + "] - result: " + result);
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                failedOrgs.add("[Org: " + orgId + "] - error: " + e.getMessage());
            }


        }

        assertTrue("Marketing Impact totals differ for at least one org. Results[" + listToString(failedOrgs),
                failedOrgs.isEmpty());

    }

    private String testMarketingImpactTotals(int orgId, String periodType, String opptyType, String modelType) {

        System.out.println("Starting stage test for marketing analytics page totals for orgId: " + orgId +
            ", period: " + periodType + ", opptyType: " + opptyType + ", modelType: " + modelType);
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        MarketingAnalyticsPage marketingAnalyticsPage = new MarketingAnalyticsPage(driver, Environments.STAGE);
        marketingAnalyticsPage.navigateTo();

        // change attribution model to single-touch first touch
        marketingAnalyticsPage.changeAttributionModel(periodType, opptyType, modelType);

        // pull marketing impact data for a few of the first rows
        List<MIDataRow> stageData = new LinkedList<>();
        for(int row =1; row < NUM_ROWS; row++){
            String xpath = String.format("id('totalRevenueTable')//tr[%s]/td[%s]", row, 2);
            String quarter = driver.findElement(By.xpath(xpath)).getText();
            BigInteger stagePipeLineTotal = marketingAnalyticsPage.getRowData(row, 3 );
            BigInteger stageOpptysCreatedTotal = marketingAnalyticsPage.getRowData(row, 4 );
            BigInteger stageMIPipelineTotal = marketingAnalyticsPage.getRowData(row, 5 );
            BigInteger stageMOpptyInfluencedTotal = marketingAnalyticsPage.getRowData(row, 6 );
            stageData.add(new MIDataRow(quarter,stagePipeLineTotal, stageOpptysCreatedTotal,
                    stageMIPipelineTotal, stageMOpptyInfluencedTotal));
        }

        // open up homepage on prod
        homePage.logout();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        // go to the marketing analytics page
        marketingAnalyticsPage = new MarketingAnalyticsPage(driver, Environments.PROD);
        marketingAnalyticsPage.navigateTo();

        // change attribution model to single-touch first touch
        marketingAnalyticsPage.changeAttributionModel(periodType, opptyType, modelType);

        List<MIDataRow> prodData = new LinkedList<>();
        for(int row = 1; row < NUM_ROWS; row++){
            String xpath = String.format("id('totalRevenueTable')//tr[%s]/td[%s]", row, 2);
            String quarter = driver.findElement(By.xpath(xpath)).getText();
            BigInteger stagePipeLineTotal = marketingAnalyticsPage.getRowData(row, 3 );
            BigInteger stageOpptysCreatedTotal = marketingAnalyticsPage.getRowData(row, 4 );
            BigInteger stageMIPipelineTotal = marketingAnalyticsPage.getRowData(row, 5 );
            BigInteger stageMOpptyInfluencedTotal = marketingAnalyticsPage.getRowData(row, 6 );
            prodData.add(new MIDataRow(quarter,stagePipeLineTotal, stageOpptysCreatedTotal,
                    stageMIPipelineTotal, stageMOpptyInfluencedTotal));
        }


        // go through each row and compare prod vs stage
        StringBuffer comparisonResult = new StringBuffer();
        for(int i=0; i < NUM_ROWS; i++){
            MIDataRow stage = stageData.get(0);
            MIDataRow prod = prodData.get(0);

            String rowCompare =  stage.compareTo(prod);
            if(rowCompare.length() > 0)
                comparisonResult.append("\t").append(rowCompare).append("\n");

        }

        return comparisonResult.toString();
    }


    private String testMarketingImpactTotalsMultiTab(int orgId, String periodType, String opptyType, String modelType) {

        System.out.println("Starting stage test for marketing analytics page totals for orgId: " + orgId +
            ", period: " + periodType + ", opptyType: " + opptyType + ", modelType: " + modelType);
        HomePage homePage = new HomePage(driver, Environments.STAGE);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        MarketingAnalyticsPage marketingAnalyticsPage = new MarketingAnalyticsPage(driver, Environments.STAGE);
        marketingAnalyticsPage.navigateTo();

        // change attribution model to single-touch first touch
        marketingAnalyticsPage.changeAttributionModel(periodType, opptyType, modelType);

        // pull marketing impact data for a few of the first rows
        List<MIDataRow> stageData = new LinkedList<>();
        for(int row =1; row < NUM_ROWS; row++){
            String xpath = String.format("id('totalRevenueTable')//tr[%s]/td[%s]", row, 2);
            String quarter = driver.findElement(By.xpath(xpath)).getText();
            BigInteger stagePipeLineTotal = marketingAnalyticsPage.getRowData(row, 3 );
            BigInteger stageOpptysCreatedTotal = marketingAnalyticsPage.getRowData(row, 4 );
            BigInteger stageMIPipelineTotal = marketingAnalyticsPage.getRowData(row, 5 );
            BigInteger stageMOpptyInfluencedTotal = marketingAnalyticsPage.getRowData(row, 6 );
            stageData.add(new MIDataRow(quarter,stagePipeLineTotal, stageOpptysCreatedTotal,
                    stageMIPipelineTotal, stageMOpptyInfluencedTotal));
        }

        // open up homepage on prod
        homePage.logout();
        homePage.openNewTab();
        homePage.switchToNewTab();
        homePage = new HomePage(driver, Environments.PROD);
        homePage.navigateTo();
        homePage.login(USER_NAME, PASSWORD);
        homePage.loginAsOrg(orgId);

        // go to the marketing analytics page
        marketingAnalyticsPage = new MarketingAnalyticsPage(driver, Environments.PROD);
        marketingAnalyticsPage.navigateTo();

        // change attribution model to single-touch first touch
        marketingAnalyticsPage.changeAttributionModel(periodType, opptyType, modelType);

        List<MIDataRow> prodData = new LinkedList<>();
        for(int row = 1; row < NUM_ROWS; row++){
            String xpath = String.format("id('totalRevenueTable')//tr[%s]/td[%s]", row, 2);
            String quarter = driver.findElement(By.xpath(xpath)).getText();
            BigInteger stagePipeLineTotal = marketingAnalyticsPage.getRowData(row, 3 );
            BigInteger stageOpptysCreatedTotal = marketingAnalyticsPage.getRowData(row, 4 );
            BigInteger stageMIPipelineTotal = marketingAnalyticsPage.getRowData(row, 5 );
            BigInteger stageMOpptyInfluencedTotal = marketingAnalyticsPage.getRowData(row, 6 );
            prodData.add(new MIDataRow(quarter,stagePipeLineTotal, stageOpptysCreatedTotal,
                    stageMIPipelineTotal, stageMOpptyInfluencedTotal));
        }

        homePage.logout();
        homePage.closeNewTab();
        
        // go through each row and compare prod vs stage
        StringBuffer comparisonResult = new StringBuffer();
        for(int i=0; i < NUM_ROWS; i++){
            MIDataRow stage = stageData.get(0);
            MIDataRow prod = prodData.get(0);

            String rowCompare =  stage.compareTo(prod);
            if(rowCompare.length() > 0)
                comparisonResult.append("\t").append(rowCompare).append("\n");

        }

        return comparisonResult.toString();
    }


    @After
    public void tearDown() throws Exception {
        driver.quit();
    }


    class MIDataRow{
        String quarter;
        BigInteger revenue;
        BigInteger opptysCreatedTotal;
        BigInteger miPipelineTotal;
        BigInteger marketingOpptyInfluencedTotal;

        public MIDataRow(String quarter, BigInteger revenue, BigInteger stageOpptysCreatedTotal,
                         BigInteger stageMIPipelineTotal, BigInteger stageMOpptyInfluencedTotal) {
            this.quarter = quarter;
            this.revenue = revenue;
            this.opptysCreatedTotal = stageOpptysCreatedTotal;
            this.miPipelineTotal = stageMIPipelineTotal;
            this.marketingOpptyInfluencedTotal = stageMOpptyInfluencedTotal;
        }

        public String compareTo(MIDataRow otherRow){
            StringBuffer comparisonResult = new StringBuffer();

            if(!this.quarter.equals(otherRow.getQuarter()))
                return "Quarters do not match. [" + quarter + ", " + otherRow.getQuarter() + "]\n";

            BigDecimal pipelineTotalDiff = new BigDecimal(
                    otherRow.getRevenue().doubleValue() - revenue.doubleValue());
            BigDecimal opptysCreatedDiff = new BigDecimal(
                    otherRow.getOpptysCreatedTotal().doubleValue() - opptysCreatedTotal.doubleValue());
            BigDecimal miPipelineDiff = new BigDecimal(
                    otherRow.getMiPipelineTotal().doubleValue() - miPipelineTotal.doubleValue());
            BigDecimal opptysInfluencedDiff = new BigDecimal(
                    otherRow.getMarketingOpptyInfluencedTotal().doubleValue()
                            - marketingOpptyInfluencedTotal.doubleValue());
            String outputTemplate = "%s totals differ by > %s. Stage: %s, Prod: %s\n";
            if(pipelineTotalDiff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT)
                comparisonResult.append(String.format(outputTemplate, "Pipeline",
                        ACCEPTABLE_DIFFERENCE_AMOUNT, revenue, otherRow.revenue));


            if(opptysCreatedDiff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT)
                comparisonResult.append(String.format(outputTemplate, "Opptys Created",
                        ACCEPTABLE_DIFFERENCE_AMOUNT, opptysCreatedTotal, otherRow.getOpptysCreatedTotal()));


            if(miPipelineDiff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT)
                comparisonResult.append(String.format(outputTemplate, "Marketing Influence Pipeline",
                        ACCEPTABLE_DIFFERENCE_AMOUNT, miPipelineTotal, otherRow.getMiPipelineTotal()));


            if(opptysInfluencedDiff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT)
                comparisonResult.append(String.format(outputTemplate, ACCEPTABLE_DIFFERENCE_AMOUNT,
                        marketingOpptyInfluencedTotal, otherRow.getMarketingOpptyInfluencedTotal()));


            return comparisonResult.toString();
        }

        public BigInteger getRevenue() {
            return revenue;
        }

        public void setRevenue(BigInteger revenue) {
            this.revenue = revenue;
        }

        public BigInteger getOpptysCreatedTotal() {
            return opptysCreatedTotal;
        }

        public void setOpptysCreatedTotal(BigInteger opptysCreatedTotal) {
            this.opptysCreatedTotal = opptysCreatedTotal;
        }

        public BigInteger getMiPipelineTotal() {
            return miPipelineTotal;
        }

        public void setMiPipelineTotal(BigInteger miPipelineTotal) {
            this.miPipelineTotal = miPipelineTotal;
        }

        public BigInteger getMarketingOpptyInfluencedTotal() {
            return marketingOpptyInfluencedTotal;
        }

        public void setMarketingOpptyInfluencedTotal(BigInteger marketingOpptyInfluencedTotal) {
            this.marketingOpptyInfluencedTotal = marketingOpptyInfluencedTotal;
        }

        public String getQuarter() {
            return quarter;
        }

        public void setQuarter(String quarter) {
            this.quarter = quarter;
        }

    }


}
