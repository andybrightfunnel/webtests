package com.brightfunnel.stage;

import junit.framework.TestCase;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base test case which will have some shared functionality with each stage test
 */
public class BaseStageTest extends TestCase {

    public enum WEBDRIVER_TYPE{
        CHROME_DRIVER,
        HTML_UNIT_FOR_CHROME,
        HTML_UNIT_FOR_FIREFOX,
        HTML_UNIT_FOR_IE
    }

    protected String listToString(List<String> results) {
        StringBuffer sb = new StringBuffer();

        for(String result : results)
            sb.append("**\t").append(result).append("\n");

        return sb.toString();
    }

    public WebDriver createDriver(WEBDRIVER_TYPE type){

        WebDriver driver = null;

        switch(type){
            case CHROME_DRIVER:
               driver = new ChromeDriver();
               break;
            case HTML_UNIT_FOR_CHROME:
                driver = new HtmlUnitDriver(DesiredCapabilities.chrome());
                ((HtmlUnitDriver)driver).setJavascriptEnabled(true);

                //client.setThrowExceptionOnScriptError(false)
                break;
            case HTML_UNIT_FOR_FIREFOX:
                driver = new HtmlUnitDriver(true);
                ((HtmlUnitDriver)driver).setJavascriptEnabled(true);
                break;
            case HTML_UNIT_FOR_IE:
                driver = new HtmlUnitDriver(DesiredCapabilities.internetExplorer());
                ((HtmlUnitDriver)driver).setJavascriptEnabled(true);
        }

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        return driver;
    }
}
