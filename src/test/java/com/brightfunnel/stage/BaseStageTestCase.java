package com.brightfunnel.stage;

import com.brightfunnel.ApplicationContextConfig;
import com.brightfunnel.pages.BasePage;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.brightfunnel.pages.BasePage.*;
import static java.lang.Thread.sleep;

/**
 * Base test case which will have some shared functionality with each stage test
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/brightfunnel/brightfunnel-context.xml")
public class BaseStageTestCase {


    public WebDriver driver;

    @Value("${bf.username}")
    public String bfUsername;

    @Value("${bf.password}")
    public String bfPassword;

    @Value("${target.orgIds}")
    public int[] orgIds;

    @Value("${webdriver.type}")
    public String webDriverType;

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


    public String compareDataRows(Map columnHeaderMap, Map<String,Object> stageRowData, Map<String,Object> prodRowData) {


        StringBuffer results = new StringBuffer();

        String messageTemplate = "\tFAILED[\t%s- does not match for [%s]. Stage: %s, Prod: %s.]\n";
        for(String col : stageRowData.keySet()){
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
                    if(stageVal.toString().contains("%") && prodVal.toString().contains("%")){
                        boolean compare = comparePercentages(stageVal.toString(), prodVal.toString());
                        if(!compare)
                            results.append(String.format(messageTemplate, header, stageRowData.get(COL_1), stageVal, prodVal));
                    }else if(!stageVal.toString().equals(prodVal.toString())){
                        results.append(String.format(messageTemplate, header, stageRowData.get(COL_1), stageVal, prodVal));
                    }
                }

            }

        }
        return results.toString();
    }

    private boolean comparePercentages(String stageVal, String prodVal) {

        try{
            Integer stage = Integer.parseInt(stageVal.replaceAll("[$,()%]", ""));
            Integer prod = Integer.parseInt(stageVal.replaceAll("[$,()%]", ""));

            if(Math.abs((prod - stage)) > maxDiffPercent){
                return false;
            }
        }catch(Exception e){

        }
        return true;
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

    /**
     * Looks up the column header values for the displayed data table
     * @return
     */
    public Map<String, Object> getDataHeaderMap(String xpath) {

        Map<String,Object> headerMap = new HashMap<>();

        java.util.List<WebElement> columns = driver.findElements(By.xpath(xpath));
        int colIndex = 1;

        for(int i=0; i < columns.size(); i++){
            WebElement col = columns.get(i);
            String val = col.getText();

            if(val.length() == 0 && headerMap.isEmpty())
                continue;
            headerMap.put("col" + colIndex, val);
            colIndex++;
        }

        return headerMap;
    }


    public Map<String,Map> getTableDataMap(BasePage currentPage, String rowsXpath) {

        Map<String,Map> dataMap = new HashMap<>();

        int count = 0;

        while(count < 3){
            try{
                List<WebElement> dataRows = driver.findElements(By.xpath(rowsXpath));

                for(WebElement row : dataRows){
                    if(row.getText().length() == 0)
                        continue;
                    Map rowData = currentPage.getDataRowMap(row);
                    String key = (String) rowData.get(COL_1);

                    dataMap.put(key, rowData);
                }
                break;
            }catch(StaleElementReferenceException e){
                System.out.println("Stale element reference. retrying");
                sleepForSeconds(5);
            }
            count++;
        }


        return dataMap;
    }

    private void sleepForSeconds(int seconds) {
        try{
            Thread.sleep(seconds * 1000);
        }catch(Exception e){

        }
    }

    public StringBuffer compareDataSets(int orgId, Map<String, Object> columnHeaderMap, Map<String, Map> stageDataMap, Map<String, Map> prodDataMap) {
        StringBuffer comparisonResult = new StringBuffer();
        String messageTemplate = "%s - %s match fail for %s.- [%s]";
        for(String key : stageDataMap.keySet()){
            Map stageRowData = stageDataMap.get(key);
            Map prodRowData = prodDataMap.get(key);
            String columnHeader = (String) columnHeaderMap.get(COL_1);
            String rowName = (String) stageRowData.get(COL_1);
            if(prodRowData == null){
                System.out.println("Missing " + columnHeader + "[" + key + "] in production");
                continue;
            }

            String result = compareDataRows(columnHeaderMap, stageRowData, prodRowData);
            if(result.length() > 0)
                comparisonResult.append(comparisonResult.append(
                        String.format(messageTemplate, orgId, columnHeader, rowName, result)));
        }

        if(comparisonResult.length() > 0)
            System.out.println("Failed Comparison for OrgId: " + orgId + " - " + comparisonResult);

        return comparisonResult;
    }
}
