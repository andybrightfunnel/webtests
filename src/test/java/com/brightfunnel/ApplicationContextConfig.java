package com.brightfunnel;

import org.springframework.context.annotation.*;

/**
 * @author Andy Paladino
 * @version Aug 12, 2017
 */
@Configuration
@ImportResource("classpath:*.brightfunnel-context.xml")
@ComponentScan(basePackages = {"com.brightfunnel"})
@PropertySource("classpath:com/brightfunnel/config-test.properties")
public class ApplicationContextConfig {

}
