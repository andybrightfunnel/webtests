package com.brightfunnel.stage;

import com.brightfunnel.pages.Environments;
import com.brightfunnel.pages.HomePage;
import com.brightfunnel.pages.MarketingAnalyticsPage;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

public class MarketingImpactPageTest extends TestCase {


    public static final String USER_NAME = "andy@brightfunnel.com";
    public static final String PASSWORD = "Time2poop!";
    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 200_000;
    private WebDriver driver;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    public void testMarketingImpactByMonthTotals() throws Exception {
        System.out.println("**");
        // open op homepage on stage
        HomePage stageHomePage = new HomePage(driver, Environments.STAGE);
        stageHomePage.navigateTo();
        stageHomePage.login(USER_NAME, PASSWORD);

        // go to the marketing analytics page
        MarketingAnalyticsPage stageMarketingAnalyticsPage = new MarketingAnalyticsPage(driver, Environments.STAGE);
        stageMarketingAnalyticsPage.navigateTo();

        // change attribution model to single-touch first touch
        stageMarketingAnalyticsPage.changeAttributionModel();

        // pull some totals from the marketing analytics page
        BigInteger stagePipeLineTotal = stageMarketingAnalyticsPage.getPipelineTotal();
        BigInteger stageOpptysCreatedTotal = stageMarketingAnalyticsPage.getOpptysCreatedTotal();
        BigInteger stageMIPipelineTotal = stageMarketingAnalyticsPage.getMIPipelineTotal();
        BigInteger stageMOpptyInfluencedTotal = stageMarketingAnalyticsPage.getOpptysInfluencedTotal();

        // open up homepage on prod
        HomePage prodHomePage = new HomePage(driver, Environments.PROD);
        prodHomePage.openNewTab();
        prodHomePage.navigateTo();
        prodHomePage.login(USER_NAME, PASSWORD);

        // go to the marketing analytics page
        MarketingAnalyticsPage prodMarketingAnalyticsPage = new MarketingAnalyticsPage(driver, Environments.DEV);
        prodMarketingAnalyticsPage.navigateTo();

        // change attribution model to single-touch first touch
        prodMarketingAnalyticsPage.changeAttributionModel();


        // verify the totals section at the bottom
        BigInteger prodPipeLineTotal = prodMarketingAnalyticsPage.getPipelineTotal();
        BigInteger prodOpptysCreatedTotal = prodMarketingAnalyticsPage.getOpptysCreatedTotal();
        BigInteger prodMIPipelineTotal = prodMarketingAnalyticsPage.getMIPipelineTotal();
        BigInteger prodMOpptyInfluencedTotal = prodMarketingAnalyticsPage.getOpptysInfluencedTotal();

        double pipeLineTotalsDiff = Math.abs(stagePipeLineTotal.doubleValue() - prodPipeLineTotal.doubleValue());
        double opptysCreatedTotalsDiff = Math.abs(stageOpptysCreatedTotal.doubleValue()
                - prodOpptysCreatedTotal.doubleValue());
        double miPipelineTotalsDiff = Math.abs(stageMIPipelineTotal.doubleValue() - prodMIPipelineTotal.doubleValue());
        double opptysInfluencedTotalsDiff = Math.abs(stageMOpptyInfluencedTotal.doubleValue()
                - prodMOpptyInfluencedTotal.doubleValue());

       // assertTrue("pipeline totals differ by too much. Stage: " + stagePipeLineTotal + ", prod: " + prodPipeLineTotal,
        //        pipeLineTotalsDiff <= ACCEPTABLE_DIFFERENCE_AMOUNT);
        assertTrue("opptys created totals differ by too much. Stage: " + stageOpptysCreatedTotal + ", prod: "
                        + prodOpptysCreatedTotal,
                opptysCreatedTotalsDiff <= ACCEPTABLE_DIFFERENCE_AMOUNT);
        assertTrue("MI Pipeine totals differ by too much. Stage: " + stageMIPipelineTotal + ", prod: "
                        + prodMIPipelineTotal,
                miPipelineTotalsDiff <= ACCEPTABLE_DIFFERENCE_AMOUNT);
        assertTrue("opptys influenced totals differ by too much. Stage: " + stageMOpptyInfluencedTotal + ", prod: "
                        + prodMOpptyInfluencedTotal,
                opptysInfluencedTotalsDiff <= ACCEPTABLE_DIFFERENCE_AMOUNT);



    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }



}
