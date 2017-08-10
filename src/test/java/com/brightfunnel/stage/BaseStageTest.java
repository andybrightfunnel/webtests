package com.brightfunnel.stage;

import junit.framework.TestCase;

import java.util.List;

/**
 * Base test case which will have some shared functionality with each stage test
 */
public class BaseStageTest extends TestCase {



    protected String listToString(List<String> results) {
        StringBuffer sb = new StringBuffer();

        for(String result : results)
            sb.append("**\t").append(result).append("\n");

        return sb.toString();
    }
}
