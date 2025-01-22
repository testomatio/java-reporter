package io.testomat;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by Lolik on 20.12.2024
 */
public class TestomatConfigTest {

  @Test
  public void getDummyFromSystemProperties() {
    System.setProperty("testomat.dummy", "loliktest");
    TestomatConfig.properties = TestomatConfig.loadProperties();
    Assert.assertEquals(TestomatConfig.TESTOMAT_DUMMY, "loliktest");
  }

  @Test
  public void getProjectFromPropertiesFile() {
    Assert.assertEquals(TestomatConfig.TESTOMAT_PROJECT, "java-reporter");
  }
}
