package com.seleniumsimplified.webdriver.manager;

import org.openqa.selenium.Platform;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A singleton style manager to maintain Drivers to prevent
 * test slowdown for creating a browser for each class with tests.
 *
 * Also counts time to start a browser and extrapolates from that how much
 * time you have saved using such hacky code.
 */
public class Driver extends Thread{
    private static WebDriver aDriver=null;
    private static long browserStartTime = 0L;
    private static long savedTimecount = 0L;
    public static final long DEFAULT_TIMEOUT_SECONDS = 10;
    private static boolean avoidRecursiveCall=false;
    public static final String BROWSER_PROPERTY_NAME = "selenium2basics.webdriver";


    private static final  String DEFAULT_BROWSER = "GOOGLECHROME";

    public enum BrowserName{FIREFOX, FIREFOXLEGACY, GOOGLECHROME, SAUCELABS, IE, HTMLUNIT, GRID, FIREFOXPORTABLE, FIREFOXMARIONETTE, APPIUM, EDGE}

    public static BrowserName currentDriver;

    private static BrowserName useThisDriver = null;

    // default for browsermob localhost:8080
    // default for fiddler: localhost:8888
    public static String PROXYHOST="localhost";
    public static String PROXYPORT="8888";
    public static String PROXY=PROXYHOST+":"+PROXYPORT;

    public static void set(BrowserName aBrowser){
        useThisDriver = aBrowser;

        // close any existing driver
        if(aDriver != null){
            aDriver.quit();
            aDriver = null;
        }
    }

    public static WebDriver get() {

        if(useThisDriver == null){


            //String defaultBrowser = System.getProperty(BROWSER_PROPERTY_NAME, DEFAULT_BROWSER);
            // to allow setting the browser as a property or an environment variable
            String defaultBrowser = EnvironmentPropertyReader.getPropertyOrEnv(BROWSER_PROPERTY_NAME, DEFAULT_BROWSER);

            /* Note:
                    I generally use the Java 1.7 format that you can see below this chunk
                    of code. where the switch statement uses a String.

                    Java 1.6 does not support this, since people have tried to use this code
                    on older versions of Java, and this is really the only Java 1.7 specific
                    functionality that I use. I have converted this code to Java 1.6 with a
                    series of if statements to replicate the switch statement.

                    I could have used an enum, or refactored it to use objects
                    but I wanted to keep the code simple.

                    Hence the reason this code is different from what you might see in the
                    lecture. But I have left this comment in to explain the situation.
             */
            if("FIREFOX".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.FIREFOX;

            if("FIREFOXLEGACY".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.FIREFOXLEGACY;

            if("FIREFOXPORTABLE".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.FIREFOXPORTABLE;

            // Firefox is for the inbuilt plugin driver, marionette is the newer .exe driver
            // see the
            // package com.seleniumsimplified.webdriver.drivers;
            //  FirefoxMarionetteDriverTest.java for more details
            if("FIREFOXMARIONETTE".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.FIREFOXMARIONETTE;

            if("CHROME".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.GOOGLECHROME;

            if("GOOGLECHROME".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.GOOGLECHROME;

            if("IE".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.IE;

            if("EDGE".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.EDGE;

            if("SAUCELABS".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.SAUCELABS;

            if("HTMLUNIT".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.HTMLUNIT;

            // generic grid
            if("GRID".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.GRID;

            if("APPIUM".contentEquals(defaultBrowser))
                useThisDriver = BrowserName.APPIUM;

            // if none of the previous if statements were exercised then useThisDriver will
            // still be none, this is the 'default' line from the Switch statement.
            if(useThisDriver==null)
                throw new RuntimeException("Unknown Browser in " + BROWSER_PROPERTY_NAME + ": " + defaultBrowser);

            /* Code below requires Java 1.7 to switch on String
            *  This would be my default way of writing the code, but have
            *  not made it the default in this code base because people often compile
            *  against 1.6 settings.
            */
            /*
            switch (defaultBrowser){
                case "FIREFOX":
                    useThisDriver = BrowserName.FIREFOX;
                    break;
                case "CHROME":
                    useThisDriver = BrowserName.GOOGLECHROME;
                    break;
                case "IE":
                    useThisDriver = BrowserName.IE;
                    break;
                case "SAUCELABS":
                    useThisDriver = BrowserName.SAUCELABS;
                    break;
                case "HTMLUNIT":
                    useThisDriver = BrowserName.HTMLUNIT;
                    break;
                case "GRID":
                    useThisDriver = BrowserName.GRID;
                    break;
                default:
                    throw new RuntimeException("Unknown Browser in " + BROWSER_PROPERTY_NAME + ": " + defaultBrowser);
            }
            */

        }


        if(aDriver==null){


            long startBrowserTime = System.currentTimeMillis();

            switch (useThisDriver) {
                case FIREFOX:
                    // selenium 3 defaults to using Marionette for Firefox so needs the geckodriver
                    // use the FirefoxPortable for the legacy Firefox driver
                    setDriverPropertyIfNecessary("webdriver.firefox.driver", "/../tools/marionette/geckodriver.exe", "C://webdrivers/marionette/geckodriver.exe");

                    aDriver = new FirefoxDriver();//profile);
                    currentDriver = BrowserName.FIREFOX;
                    break;

                case FIREFOXLEGACY:

                    // use the legacy selenium driver with the built in firefox
                    FirefoxOptions ffoptions = new FirefoxOptions();
                    ffoptions.setLegacy(true);
                    aDriver = new FirefoxDriver(ffoptions);
                    currentDriver = BrowserName.FIREFOX;
                    break;

                case HTMLUNIT:

                    // HtmlUnitDriver added as a maven dependency - no paths required
                    aDriver = new HtmlUnitDriver(true);
                    currentDriver = BrowserName.HTMLUNIT;
                    break;

                case IE:

                    setDriverPropertyIfNecessary("webdriver.ie.driver", "/../tools/iedriver_32/IEDriverServer.exe", "C://webdrivers/iedriver_32/IEDriverServer.exe");
                    //setDriverPropertyIfNecessary("webdriver.ie.driver", "/../tools/iedriver_64/IEDriverServer.exe", "C://webdrivers/iedriver_64/IEDriverServer.exe");

                    aDriver = new InternetExplorerDriver();
                    currentDriver = BrowserName.IE;
                    break;

                case EDGE:

                    setDriverPropertyIfNecessary("webdriver.edge.driver", "/../tools/edgedriver/MicrosoftWebDriver.exe", "C://webdrivers/edgedriver/MicrosoftWebDriver.exe");

                    aDriver = new EdgeDriver();
                    currentDriver = BrowserName.EDGE;
                    break;

                case GOOGLECHROME:

                    setDriverPropertyIfNecessary("webdriver.chrome.driver","/../tools/chromedriver/chromedriver.exe","C://webdrivers/chromedriver/chromedriver.exe");

                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("disable-plugins");
                    options.addArguments("disable-extensions");


                    // with Chrome v35 it now reports an error on --ignore-certificate-errors
                    // so call with args "test-type"
                    // https://code.google.com/p/chromedriver/issues/detail?id=799
                    options.addArguments("test-type");

                    DesiredCapabilities chromeCapabilities = DesiredCapabilities.chrome();

                    // http://stackoverflow.com/questions/26772793/org-openqa-selenium-unhandledalertexception-unexpected-alert-open
                    //chromeCapabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

                    // convert theoptions to capabilities
                    chromeCapabilities.setCapability(ChromeOptions.CAPABILITY, options);



                    aDriver = new ChromeDriver(chromeCapabilities);
                    currentDriver = BrowserName.GOOGLECHROME;
                    break;

                case SAUCELABS:

                    DesiredCapabilities firefoxCapabilities = DesiredCapabilities.firefox();
                    firefoxCapabilities.setCapability("version", "5");
                    firefoxCapabilities.setCapability("platform", Platform.XP);
                    try {
                        // add url to environment variables to avoid releasing with source
                        String sauceURL = System.getenv("SAUCELABS_URL");
                        aDriver = new RemoteWebDriver(
                                new URL(sauceURL),
                                firefoxCapabilities);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    currentDriver = BrowserName.SAUCELABS;
                    break;

                case APPIUM:

                    // quick hack code to get appium running
                    // only one env variable, your APPIUM_DEVICE_NAME
                    // so amend code for your local version

                    DesiredCapabilities appiumCapabilities = new DesiredCapabilities();

                    // the device name can be seen when you do "adb devices"
                    appiumCapabilities.setCapability("deviceName", EnvironmentPropertyReader.getPropertyOrEnv("APPIUM_DEVICE_NAME", ""));
                    appiumCapabilities.setCapability("platformName", Platform.ANDROID);
                    appiumCapabilities.setCapability("app", EnvironmentPropertyReader.getPropertyOrEnv("APPIUM_BROWSER", "browser"));


                    try {
                        // add url to environment variables to avoid releasing with source
                        String appiumURL = "http://127.0.0.1:4723/wd/hub";
                        aDriver = new RemoteWebDriver(
                                        new URL(appiumURL),
                                        appiumCapabilities);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    currentDriver = BrowserName.APPIUM;
                    break;

                case GRID:

                    String gridBrowser = EnvironmentPropertyReader.getPropertyOrEnv("WEBDRIVER_GRID_BROWSER", "firefox");
                    String gridBrowserVersion = EnvironmentPropertyReader.getPropertyOrEnv("WEBDRIVER_GRID_BROWSER_VERSION", "");
                    String gridBrowserPlatform = EnvironmentPropertyReader.getPropertyOrEnv("WEBDRIVER_GRID_BROWSER_PLATFORM", "");

                    DesiredCapabilities gridCapabilities = new DesiredCapabilities();
                    gridCapabilities.setBrowserName(gridBrowser);
                    if(gridBrowserVersion.length()>0)
                        gridCapabilities.setVersion(gridBrowserVersion);
                    if(gridBrowserPlatform.length()>0)
                        gridCapabilities.setPlatform(Platform.fromString(gridBrowserPlatform));

                    // Allow adding any capability defined as an environment variable
                    // extra environment capabilities start with "WEBDRIVER_GRID_CAP_X_"

                    // e.g. WEBDRIVER_GRID_CAP_X_os_version XP
                    // e.g. WEBDRIVER_GRID_CAP_X_browserstack.debug true
                    Map<String, String> anyExtraCapabilities = System.getenv();
                    addAnyValidExtraCapabilityTo(gridCapabilities, anyExtraCapabilities.keySet());

                    // Now check properties for extra capabilities
                    Properties anyExtraCapabilityProperties = System.getProperties();
                    addAnyValidExtraCapabilityTo(gridCapabilities, anyExtraCapabilityProperties.stringPropertyNames());


                    try {
                        // add url to environment variables to avoid releasing with source
                        String gridURL = EnvironmentPropertyReader.getPropertyOrEnv("WEBDRIVER_GRID_URL", "http://localhost:4444/wd/hub");
                        aDriver = new RemoteWebDriver(new URL(gridURL), gridCapabilities);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    currentDriver = BrowserName.GRID;
                    break;
            }


            long browserStartedTime = System.currentTimeMillis();
            browserStartTime = browserStartedTime - startBrowserTime;

            // we want to shutdown the shared brower when the tests finish
            Runtime.getRuntime().addShutdownHook(
                    new Thread(){
                        public void run(){
                            Driver.quit();
                        }
                    }
            );

        }else{

            try{
                // is browser still alive
                if(aDriver.getWindowHandle()!=null){
                    // assume it is still alive
                }
            }catch(Exception e){
                if(avoidRecursiveCall){
                    // something has gone wrong as we have been here already
                    throw new RuntimeException("something has gone wrong as we have been in Driver.get already");
                }

                quit();
                aDriver=null;
                avoidRecursiveCall = true;
                return get();
            }

            savedTimecount += browserStartTime;
            System.out.println("Saved another " + browserStartTime + "ms : total saved " + savedTimecount + "ms");
        }

        avoidRecursiveCall = false;
        return aDriver;
    }


    /*
    Initially, the tests didn't really run on Grid, and I have code in the tests,
    to handle workarounds on different browsers.

    But as I use grid more, my current approach of using currentDriver to code
    workarounds for specific browsers, doesn't work because I'll just get GRID
    when I want to know FIREFOX.

    So I added a method called currentBrowser to Driver which returns the Browser
    in use.

    I can still find out what currentDriver is with `Driver.currentDriver`.
    But if I want to know the browser, I should use `Driver.currentBrowser()`

     */
    public static BrowserName currentBrowser(){

        // TODO: handle marionette in grid

        if(currentDriver == Driver.BrowserName.GRID){
            // get the current browser from the property or environment
            // if not set then default to firefox
            // make lowercase for consistent comparison
            String gridBrowser = EnvironmentPropertyReader.getPropertyOrEnv("WEBDRIVER_GRID_BROWSER", "firefox").toLowerCase();

            if(gridBrowser.contains("firefox")){
                return BrowserName.FIREFOX;
            }

            if(gridBrowser.contains("chrome")){
                return BrowserName.GOOGLECHROME;
            }

            if(gridBrowser.contains("ie")){
                return BrowserName.IE;
            }

            if(gridBrowser.contains("html")){
                return BrowserName.HTMLUNIT;
            }
        }
        if(currentDriver == BrowserName.SAUCELABS){
            // we hard coded sauce to use firefox
            return BrowserName.FIREFOX;
        }

        return currentDriver;
    }

    private static void addAnyValidExtraCapabilityTo(DesiredCapabilities gridCapabilities, Set<String> possibleCapabilityKeys) {

        String extraCapabilityPrefix = "WEBDRIVER_GRID_CAP_X_";

        for(String capabilityName : possibleCapabilityKeys){

            if(capabilityName.startsWith(extraCapabilityPrefix)){

                String capabilityValue = EnvironmentPropertyReader.getPropertyOrEnv(capabilityName, "");

                if(capabilityValue.length()>0){
                    String capability = capabilityName.replaceFirst(extraCapabilityPrefix,"");
                    System.out.println("To Set Capability " + capability + " with value " + capabilityValue);
                    gridCapabilities.setCapability(capability, capabilityValue);
                }
            }
        }
    }

    private static void setDriverPropertyIfNecessary(String propertyKey, String relativeToUserPath, String absolutePath) {
        // http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html

        if(!System.getProperties().containsKey(propertyKey)){

            String currentDir = System.getProperty("user.dir");
            String chromeDriverLocation = currentDir + relativeToUserPath;
            File driverExe = new File(chromeDriverLocation);
            if(driverExe.exists()){
                System.setProperty(propertyKey, chromeDriverLocation);
            }else{
                driverExe = new File(absolutePath);
                if(driverExe.exists()){
                    System.setProperty(propertyKey, absolutePath);
                }else{
                    // expect an error on the follow through when we try to use the driver
                    // unless the user has it in their Path in which case WebDriver will use that
                }
            }
        }
    }

    public static WebDriver get(String aURL, boolean maximize){
        get();
        aDriver.get(aURL);
        if(maximize){
            try{
                aDriver.manage().window().maximize();
            }catch(UnsupportedCommandException e){
                System.out.println("Remote Driver does not support maximise");
            }catch(WebDriverException e){
                if(currentDriver == BrowserName.APPIUM){
                    System.out.println("Appium does not support maximise");
                }
            }
        }
        return aDriver;
    }

    public static WebDriver get(String aURL){
        return get(aURL,true);
    }

    public static void quit(){
        if(aDriver!=null){
            System.out.println("total time saved by reusing browsers " + savedTimecount + "ms");
            try{
                aDriver.quit();
                aDriver=null;
            }catch(Exception e){
                // I don't care about errors at this point
            }

        }
    }


}
