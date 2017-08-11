package com.brightfunnel.stage;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.brightfunnel.pages.BasePage.*;

/**
 * Base test case which will have some shared functionality with each stage test
 */
public class BaseStageTestCase extends TestCase {


    public WebDriver driver;

    public enum WEBDRIVER_TYPE{
        CHROME_DRIVER,
        HTML_UNIT_FOR_CHROME,
        HTML_UNIT_FOR_FIREFOX,
        HTML_UNIT_FOR_IE
    }

    public static String USER_NAME;
    public static String PASSWORD;
    public static final int NUM_DATA_ROWS_TO_INSPECT = 5;
    public int[] orgIds;

    public static final int ACCEPTABLE_DIFFERENCE_AMOUNT = 1_000;

    @Before
    public void setUp() throws Exception {
        initDriver();
        USER_NAME = System.getenv("BF_USERNAME");
        PASSWORD = System.getenv("BF_PASSWORD");


        assertNotNull(USER_NAME, "Unable to retrieve username from system environment variable: BF_USERNAME");
        assertNotNull(PASSWORD, "Unable to retrieve password from system environment variable: BF_PASSWORD");

        String orgIdProp = System.getenv("BF_ORGIDS");

        assertNotNull("Unable to retreive target orgIDS from system environement variable: BF_ORGIDS");
        String [] ids = orgIdProp.split(",");
        orgIds = new int[ids.length];
        for(int i=0; i < ids.length; i++){
            orgIds[i] = Integer.parseInt(ids[i]);
        }
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
                    results.append("Stage campaign Ids don't match. Stage: "  + stageVal +
                            ", prod: " + prodVal );
                }
            }else{
                BigDecimal stageVal = (BigDecimal) stageRowData.get(col);
                BigDecimal prodVal = (BigDecimal) prodRowData.get(col);

                BigDecimal diff = prodVal.subtract(stageVal).abs();
                if(diff.doubleValue() > ACCEPTABLE_DIFFERENCE_AMOUNT){
                    results.append(String.format(messageTemplate, header, stageRowData.get(COL_1), stageVal, prodVal));
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
        driver.quit();
    }

    public void initDriver(){
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }
}
