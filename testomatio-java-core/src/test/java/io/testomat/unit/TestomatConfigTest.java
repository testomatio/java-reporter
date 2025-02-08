package io.testomat.unit;

import io.testomat.TestomatioConfig;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 30.01.2025
 */
public class TestomatConfigTest {


  @Test
  public void testConfig(){
    System.out.println("SONE INFO");
    System.out.println(TestomatioConfig.getProject());
    System.out.println(TestomatioConfig.getReporterInterval());
  }
}
