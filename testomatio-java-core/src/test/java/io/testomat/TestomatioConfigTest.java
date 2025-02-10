package io.testomat;

import org.testng.annotations.Test;

/**
 * Created by Lolik on 08.02.2025
 */
public class TestomatioConfigTest {

  @Test
  public void apiKey() {
    System.out.println(TestomatioConfig.getApiKey());
  }

  @Test
  public void hostWithSlash() {
    System.out.println(TestomatioConfig.getHost());
  }
}
