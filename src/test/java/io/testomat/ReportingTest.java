package io.testomat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 18.11.2024
 */
public class ReportingTest {

  private static final Logger logger = LoggerFactory.getLogger(ReportingTest.class);

  @Test
  public void cleanTest(){
    logger.info("Clean Test");
  }
}
