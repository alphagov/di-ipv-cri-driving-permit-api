package gov.di_ipv_drivingpermit.pages;

import gov.di_ipv_drivingpermit.utilities.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.support.PageFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.waitForSpecificPageWithTitleToFullyLoad;
import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.waitForUrlToContain;
import static org.junit.Assert.assertTrue;

public class UniversalSteps {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final int MAX_WAIT_SEC = 15;

    public UniversalSteps() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void assertExpectedPage(String expectedTitle, boolean exactMatch) {
        boolean success =
                waitForSpecificPageWithTitleToFullyLoad(expectedTitle, exactMatch, MAX_WAIT_SEC);

        String title = Driver.get().getTitle();
        if (title == null) {
            title = "Driver had no page title";
        }

        LOGGER.info(
                "Page title is: {}, Expected {} - ComparingWithExactMatch {}",
                title,
                expectedTitle,
                exactMatch);

        assertTrue(success);
    }

    public static void driverClose() {
        Driver.closeDriver();
    }

    public void assertURLContains(String expected) {
        boolean status = waitForUrlToContain(expected, MAX_WAIT_SEC);

        String url = Driver.get().getCurrentUrl();
        LOGGER.info("Page url: " + url);

        assertTrue(status);
    }

    // Method to read the JSON from a file
    public static String getJsonPayload(String fileName) {
        String filePath = "src/test/resources/Data/" + fileName + ".json";
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
