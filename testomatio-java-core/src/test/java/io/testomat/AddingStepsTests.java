package io.testomat;

import org.testng.annotations.Test;

/**
 * Created by Lolik on 29.01.2025
 */
public class AddingStepsTests {

  @Test
  public void customStepsTest(){
    Testomat.step("My Custom Step Title");
  }

  @Test
  public void stepWithMultilineText(){
    String longMultilineText = """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        Nullam nec purus nec nunc
          Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        Nullam nec purus nec nunc
          Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        Nullam nec purus nec nunc
          Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        Nullam nec purus nec nunc
        """;
    Testomat.step(longMultilineText);
  }

  @Test
  public void stepWithLink(){
    Testomat.step("My Custom Step Title with link to https://www.google.com");
  }



}
