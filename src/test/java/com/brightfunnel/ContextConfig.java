package com.brightfunnel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Andy Paladino
 * @version Aug 12, 2017
 */
@Configuration
@ImportResource("classpath:/com/brightfunnel/brightfunnel-context.xml")
@ComponentScan
public class ContextConfig {

}
