import com.perfecto.reportium.client.ReportiumClient;
import com.perfecto.reportium.client.ReportiumClientFactory;
import com.perfecto.reportium.model.PerfectoExecutionContext;
import com.perfecto.reportium.model.Project;
import com.perfecto.reportium.test.TestContext;
import com.perfecto.reportium.test.result.TestResultFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Test_Main {

    RemoteWebDriver driver;
    ReportiumClient reportiumClient;

    //TODO: Set your Perfecto Lab user, password and host.
    //TODO: Set your ESPN email and password.
    String ESPN_email			= System.getProperty("np.ESPNuser", "My_Email");
    String ESPN_password		= System.getProperty("np.ESPNpassword", "My_Pass");
    String PERFECTO_HOST 		= System.getProperty("np.testHost", "My_Host.perfectomobile.com");
    String PERFECTO_USER 		= System.getProperty("np.testUsername", "My_User");
    String PERFECTO_PASSWORD 	= System.getProperty("np.testPassword", "My_Pass");

    //TODO: Insert your device capabilities at testng.XML file.
    @BeforeTest
    @Parameters({"platformName" , "model" , "browserName" , "location" , "host" , "user" , "password" , "ESPN_mail" , "ESPN_Pass"})
    public void beforMethod(String platformName, String model, String browserName, String location,
    		String host, String user, String password , String ESPN_mail , String ESPN_Pass) throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("user" , user);
        capabilities.setCapability("password" , password);
        capabilities.setCapability("platformName" , platformName);
        capabilities.setCapability("model" , model);
        capabilities.setCapability("browserName" , browserName);
        capabilities.setCapability("location" , location);

        driver = new RemoteWebDriver(new URL("https://" + host + "/nexperience/perfectomobile/wd/hub") , capabilities);
        driver.manage().timeouts().implicitlyWait(15 , TimeUnit.SECONDS);

        this.ESPN_email = ESPN_mail;
        this.ESPN_password = ESPN_Pass;
        
        //Create Reportium client.
        reportiumClient = new ReportiumClientFactory().createPerfectoReportiumClient(
                        new PerfectoExecutionContext.PerfectoExecutionContextBuilder()
                        .withProject(new Project("Sample Appium-Reportium" , "1.0"))
                        .withContextTags("Regression") //Optional
                        .withWebDriver(driver) //Optional
                        .build());
    }

    //Control the number of times each one of the tests runs.
    //Each times generates a new report for each test.
    //TODO: set the number of times to run the test
    @Test(invocationCount = 1)
    public void testGroup(){
        test();
        test2();
    }

    public void test(){
        try{
            //START TEST
            reportiumClient.testStart("ESPN navigation" , new TestContext("Some tag" , "ESPN")); //Add tags by your choice. 

            reportiumClient.testStep("Navigate to espn site"); //TEST STEP - Open site and navigate.
            driver.get("http://espn.go.com/");
            driver.findElement(By.xpath(PageObjects.mainPageMenu)).click();
            driver.findElement(By.xpath(PageObjects.menuNBA)).click();

            reportiumClient.testStep("Validate text point"); //TEST STEP - Validate text appear.
            Map<String, Object> textToFind = new HashMap<String, Object>();
            textToFind.put("content", "SCORES");
            String TextFindStatus =(String) driver.executeScript("mobile:text:find", textToFind);
            //Assertion - Validate that "Scores" appear in page.
            Assert.assertEquals(TextFindStatus , "true");

            reportiumClient.testStep("Search for a team"); //TEST STEP - Search team.
            driver.findElement(By.xpath(PageObjects.NBATeams)).click();
            driver.findElement(By.xpath(PageObjects.SelectLakers)).click();

            //END TEST - Success
            reportiumClient.testStop(TestResultFactory.createSuccess());

        }catch (Throwable t){
            t.printStackTrace();
            reportiumClient.testStop(TestResultFactory.createFailure(t.getMessage() , t));
        }

    }

    public void test2(){
        try{
            //NEW TEST
            reportiumClient.testStart("ESPN login" , new TestContext("ESPN" , "Selenium" , "Login"));//Add tags by your choice

            reportiumClient.testStep("Navigate to ESPN login page"); //TEST STEP - Click login
            driver.findElement(By.xpath(PageObjects.LoginButton)).click();

            reportiumClient.testStep("Insert login info and press login button"); //TEST STEP - login session
            driver.switchTo().frame("disneyid-iframe"); // Switch to login frame
            driver.findElement(By.xpath(PageObjects.Email)).sendKeys(this.email);
            driver.findElement(By.xpath(PageObjects.password)).sendKeys(this.password);
            driver.findElement(By.xpath(PageObjects.submit)).click();

            reportiumClient.testStep("LogOut"); //TEST STEP - logout session
            driver.findElement(By.xpath(PageObjects.userPlace)).click();
            driver.findElement(By.xpath(PageObjects.logOut)).click();

            //END TEST - Success
            reportiumClient.testStop(TestResultFactory.createSuccess());
        }catch (Throwable t){
            t.printStackTrace();
            reportiumClient.testStop(TestResultFactory.createFailure(t.getMessage() , t));
        }
    }


    @SuppressWarnings("Since15")
    @AfterTest
    public void afterMethod(){
        try{
            driver.manage().deleteAllCookies(); //Removes cookies after test.
            driver.quit();
            String reportURL = reportiumClient.getReportUrl();
            System.out.println(reportURL); //Print URL to console

            if(Desktop.isDesktopSupported())
                Desktop.getDesktop().browse(new URI(reportURL));

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
