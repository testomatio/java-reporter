package io.testomat.unit;

import io.testomat.TestomatConfig;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 30.01.2025
 */
public class TestomatConfigTest {


  @Test
  public void testConfig(){
    System.out.println("SONE INFO");
    System.out.println(TestomatConfig.getProject());
    System.out.println(TestomatConfig.getReporterInterval());
  }
}
