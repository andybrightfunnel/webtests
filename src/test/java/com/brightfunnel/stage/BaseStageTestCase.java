package com.brightfunnel.stage;

import com.brightfunnel.ContextConfig;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.brightfunnel.pages.BasePage.*;

/**
 * Base test case which will have some shared functionality with each stage test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ContextConfig.class })
public class BaseStageTestCase extends TestCase {


    public WebDriver driver;

    @Value("${bf.username}")
    public String bfUsername;

    @Value("${bf.password}")
    public String bfPassword;

    @Value("${target.orgIds}")
    public int[] orgIds;

    @Value("${webdriver.type}")
    private String webDriverType;

    @Value("${max.diff.percent}")
    public Double maxDiffPercent;


    @Before
    public void setUp() throws Exception {
        initDriver();
    }

    protected String listToString(List<String> results) {
        StringBuffer sb = new StringBuffer();

        for(String result : results)
            sb.append("**\t").append(result).append("\n");

        return sb.toString();
    }


    public String compareDataRows(Map columnHeaderMap, Map stageRowData, Map prodRowData) {


        String[] columns = getDataColumnMapKeys();

        StringBuffer results = new StringBuffer();

        String messageTemplate = "\tFAILED[\t%s- does not match for [%s]. Stage: %s, Prod: %s.]\n";
        for(String col : columns){
            String header = (String)columnHeaderMap.get(col);
            if(COL_1.equals(col)){
                String stageVal = (String)stageRowData.get(col);
                String prodVal = (String) prodRowData.get(col);
                if(!stageVal.equals(prodVal)){
                    results.append("Row Values Don't Match. Stage: "  + stageVal +
                            ", prod: " + prodVal );
                }
            }else{

                Object stageVal =  stageRowData.get(col);
                Object prodVal =  prodRowData.get(col);

                if(prodVal == null){
                    results.append("Missing campaign group on production: " + col);
                    continue;
                }

                if(stageVal instanceof BigDecimal && prodVal instanceof BigDecimal){
                    BigDecimal diff = ((BigDecimal)prodVal).subtract(((BigDecimal)stageVal)).abs();
                    if(diff.doubleValue() > 0){
                        double diffPercent = diff.doubleValue() / ((BigDecimal)prodVal).doubleValue();
                        if(diffPercent > maxDiffPercent){
                            results.append(String.format(messageTemplate, header, stageRowData.get(COL_1), stageVal, prodVal));
                        }
                    }

                }else{
                    if(!stageVal.toString().equals(prodVal.toString())){
                        results.append(String.format(messageTemplate, header, stageRowData.get(COL_1), stageVal, prodVal));
                    }
                }

            }

        }
        return results.toString();
    }

    public String[] getDataColumnMapKeys() {
        return new String[] { COL_1, COL_2, COL_3, COL_4, COL_5 };
    }

    @After
    public void tearDown() throws Exception {

        if(driver != null)
            driver.quit();
    }

    public void initDriver(){

        switch (webDriverType){
            case "ChromeDriver":
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability("chrome.switches", Arrays.asList(new String[]{
                    "--start-maximized",
                    "--disable-popup-blocking" }));
                capabilities.setCapability("--start-maximized", false);
                driver = new ChromeDriver(capabilities);
                break;
            case "HtmlUnitDriver":
                driver = new HtmlUnitDriver();
                break;
            case "FireFoxDriver":
                driver = new FirefoxDriver();
            case "SafariDriver":
                driver = new SafariDriver();
                break;
            case "InternetExplorerDriver":
                driver = new InternetExplorerDriver();
                break;
            case "GhostDriver":
                driver = new PhantomJSDriver();
                break;
            default:
                driver = new ChromeDriver();
        }

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }
}
