/**
 * Distribution module for Testomat.io Java Reporter.
 * 
 * <p>This module provides unified access to framework-specific reporters through classifiers:</p>
 * <ul>
 *   <li>junit - for JUnit 5 integration</li>
 *   <li>testng - for TestNG integration</li> 
 *   <li>cucumber - for Cucumber integration</li>
 * </ul>
 * 
 * @since 0.6.0
 */
public final class Distribution {
    
    private Distribution() {
    }
    
    /**
     * Distribution module version.
     */
    public static final String VERSION = "0.6.0";
}