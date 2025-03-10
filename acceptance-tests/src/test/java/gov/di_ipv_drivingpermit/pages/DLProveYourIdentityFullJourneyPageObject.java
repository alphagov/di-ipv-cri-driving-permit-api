package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gov.di_ipv_drivingpermit.model.Address;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.di_ipv_drivingpermit.pages.Headers.ORCHESTRATOR_STUB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DLProveYourIdentityFullJourneyPageObject extends UniversalSteps {

    private final ConfigurationService configurationService;
    private static final Logger LOGGER = LogManager.getLogger();

    @FindBy(xpath = "//*[@value=\"Full journey route\"]")
    public WebElement fullJourneyRouteButton;

    @FindBy(id = "submitButton")
    public WebElement continueSubmitButton;

    @FindBy(id = "addressSearch")
    public WebElement postcodeField;

    @FindBy(id = "continue")
    public WebElement continueButton;

    @FindBy(id = "addressResults")
    public WebElement ChooseYourAddressFromTheList;

    @FindBy(id = "addressYearFrom")
    public WebElement EnterTheYearYouStartedLivingAtThisAddress;

    @FindBy(xpath = "//button[@class='govuk-button button']")
    public WebElement IConfirmMyDetailsAreCorrect;

    @FindBy(id = "isAddressMoreThanThreeMonths-lessThanThreeMonths")
    public WebElement NO;

    @FindBy(xpath = "//button[@class='govuk-button button']")
    public WebElement FindAddress;

    @FindBy(id = "Q00033-TSBBANKPLC-label")
    public WebElement loanTSBBANKPLC;

    @FindBy(id = "Q00033-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement loanNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00018-OVER500UPTO600-label")
    public WebElement OVER500UPTO600;

    @FindBy(id = "Q00018-UPTO600-label")
    public WebElement UPTO600;

    @FindBy(id = "Q00018-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement mortgagePaymentNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00009-SANTANDERANMFMORTGAGE-label")
    public WebElement SANTANDERANMFMORTGAGE;

    @FindBy(id = "Q00009-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement mortgageCompanyNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00002-2002-label")
    public WebElement Year2002;

    @FindBy(id = "Q00002-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement currentAddressNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00043-UPTO48MONTHS-label")
    public WebElement UPTO48MONTHS;

    @FindBy(xpath = "//*[@id=\"Q00043-OVER36MONTHSUPTO48MONTHS-label\"]")
    public WebElement OVER36MONTHSUPTO48MONTHS;

    @FindBy(id = "Q00043-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement loanTermNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00042-OVER550UPTO600-label")
    public WebElement OVER550UPTO600;

    @FindBy(id = "Q00042-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement loanMonthlyNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00015-UPTO60000-label")
    public WebElement UPTO60000;

    @FindBy(id = "Q00015-OVER35000UPTO60000-label")
    public WebElement OVER35000UPTO60000;

    @FindBy(id = "Q00015-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement mortgageLeftToPayNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00039-UPTO6750-label")
    public WebElement UPTO6750;

    @FindBy(id = "Q00039-OVER6500UPTO6750-label")
    public WebElement OVER6500UPTO6750;

    @FindBy(id = "Q00039-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement loanToPayBackNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(xpath = "//label[@id='Q00019-KA-label']")
    public WebElement KA;

    @FindBy(id = "Q00019-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement twoLetterMortgageNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00048-TSBBANKPLC-label")
    public WebElement TSBBANKPLC;

    @FindBy(id = "Q00048-NONEOFTHEABOVEDOESNOTAPPLY-label")
    public WebElement currentAccountNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(id = "Q00020-021963-label")
    public WebElement February1963;

    @FindBy(xpath = "//label[@id='Q00020-NONEOFTHEABOVEDOESNOTAPPLY-label']")
    public WebElement dobNONEOFTHEABOVEDOESNOTAPPLY;

    @FindBy(xpath = "//*[@class='govuk-heading-m']")
    public WebElement govukHeading;

    @FindBy(id = "journey")
    public WebElement ukDrivingLicenceRadioBtn;

    @FindBy(id = "journey-4")
    public WebElement proveYourIdentityAnotherWayRadioBtn;

    @FindBy(xpath = "//*[@id='main-content']/div[3]/div/dl/div[2]/dd/details/summary/span")
    public WebElement dlVc;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div[3]/div/dl/div[2]/dd/details/div/pre")
    public WebElement dlJsonPayload;

    @FindBy(id = "targetEnvironment")
    public WebElement ChooseYourEnvironmentFromTheList;

    public DLProveYourIdentityFullJourneyPageObject() {
        this.configurationService = new ConfigurationService(System.getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
    }

    public void navigateToOrchestratorStub() {

        String orchestratorStubUrl = configurationService.getOrchestratorStubUrl();
        Driver.get().get(orchestratorStubUrl);
        assertExpectedPage(ORCHESTRATOR_STUB, false);
    }

    public void clickOnFullJourneyRouteButton() {
        fullJourneyRouteButton.click();
    }

    public void clickOnUKDrivingLicenceAndContinue() {
        ukDrivingLicenceRadioBtn.click();
        continueSubmitButton.click();
    }

    public void selectContinueButton() {
        continueSubmitButton.click();
    }

    public void addPostcode(String housename) {
        postcodeField.sendKeys(housename);
        continueButton.click();
    }

    public void selectAddressFromDropdown(String address) {
        Select select = new Select(ChooseYourAddressFromTheList);
        select.selectByValue(address);
        continueButton.click();
        BrowserUtils.waitForPageToLoad(100);
    }

    public void enterAddressExpiry(String expiryDate) {
        EnterTheYearYouStartedLivingAtThisAddress.sendKeys(expiryDate);
        continueButton.click();
    }

    public void selectNoFor3MonthsInAddress(Integer int1) {
        NO.click();
        FindAddress.click();
    }

    public void assertPreviousAddressTitle() {
        assertTrue(FindAddress.isDisplayed());
    }

    public void answerKBVQuestion() {
        BrowserUtils.waitFor(2);
        String kennethFirstQuestion = Driver.get().getTitle();
        kennethFirstQuestion.trim();
        System.out.println("kennethFirstQuestion = " + kennethFirstQuestion);
        switch (kennethFirstQuestion) {
            case "Which provider did you take out a loan with? – GOV.UK One Login":
                try {
                    if (loanTSBBANKPLC.isDisplayed()) {
                        loanTSBBANKPLC.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (loanNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        loanNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            case "What is the outstanding balance of your current mortgage? – GOV.UK One Login":
                try {
                    if (OVER35000UPTO60000.isDisplayed()) {
                        OVER35000UPTO60000.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (mortgageLeftToPayNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        mortgageLeftToPayNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            case "How much is your monthly mortgage payment? – GOV.UK One Login":
                try {
                    if (OVER500UPTO600.isDisplayed()) {
                        OVER500UPTO600.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (UPTO600.isDisplayed()) {
                        UPTO600.click();
                        continueButton.click();
                    }
                }
                break;
            case "Which lender did you borrow your mortgage from? – GOV.UK One Login":
                try {
                    if (SANTANDERANMFMORTGAGE.isDisplayed()) {
                        SANTANDERANMFMORTGAGE.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (mortgageCompanyNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        mortgageCompanyNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            case "In which year did you move to your current address? – GOV.UK One Login":
                try {
                    if (Year2002.isDisplayed()) {
                        Year2002.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (currentAddressNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        currentAddressNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            case "How long do you have to pay back your loan? – GOV.UK One Login":
                BrowserUtils.waitFor(2);
                try {
                    if (OVER36MONTHSUPTO48MONTHS.isDisplayed()) {
                        OVER36MONTHSUPTO48MONTHS.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (UPTO48MONTHS.isDisplayed()) {
                        UPTO48MONTHS.click();
                        continueButton.click();
                    }
                }
                break;
            case "How much of your loan do you pay back every month? – GOV.UK One Login":
                try {
                    if (OVER550UPTO600.isDisplayed()) {
                        OVER550UPTO600.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (loanMonthlyNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        loanMonthlyNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            case "How much do you have left to pay on your mortgage? – GOV.UK One Login":
                try {
                    if (UPTO60000.isDisplayed()) {
                        UPTO60000.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (OVER35000UPTO60000.isDisplayed()) {
                        OVER35000UPTO60000.click();
                        continueButton.click();
                    }
                }
                break;
            case "How much of your loan do you have left to pay back? – GOV.UK One Login":
                try {
                    if (UPTO6750.isDisplayed()) {
                        UPTO6750.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (OVER6500UPTO6750.isDisplayed()) {
                        OVER6500UPTO6750.click();
                        continueButton.click();
                    }
                }
                break;
            case "What are the first 2 letters of the first name of the other person on your mortgage? – GOV.UK One Login":
                KA.click();
                continueButton.click();
                break;
            case "Who have you opened a current account with? – GOV.UK One Login":
                try {
                    if (TSBBANKPLC.isDisplayed()) {
                        TSBBANKPLC.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (currentAccountNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        currentAccountNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            case "When was the other person on your mortgage born? – GOV.UK One Login":
                try {
                    if (February1963.isDisplayed()) {
                        February1963.click();
                        continueButton.click();
                    }
                } catch (Exception e) {
                    if (dobNONEOFTHEABOVEDOESNOTAPPLY.isDisplayed()) {
                        dobNONEOFTHEABOVEDOESNOTAPPLY.click();
                        continueButton.click();
                    }
                }
                break;
            default:
                System.out.println("First question not answered");
        }
    }

    public void confirmClick() {
        IConfirmMyDetailsAreCorrect.click();
    }

    public void validateAddressVc(String currentAddress) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, String>> vcMap = getVcMap(objectMapper);
        Map<String, String> addressVc =
                vcMap.get("Cri Type: https://review-a.staging.account.gov.uk");

        List<Address> addressList = getAddresses(objectMapper, addressVc);

        LocalDate validFrom = null;
        LocalDate validUntil = null;

        for (Address address : addressList) {
            if (address.getAddressType().name().equals("CURRENT")) {
                validFrom = address.getValidFrom();
            } else {
                validUntil = address.getValidUntil();
            }
        }

        if (null != validFrom && addressList.size() > 1) {
            assertEquals(validFrom, validUntil);
        }

        for (Address address : addressList) {
            if (currentAddress.contains(address.getPostalCode())) {
                assertEquals("CURRENT", address.getAddressType().name());
            } else {
                assertEquals("PREVIOUS", address.getAddressType().name());
            }
        }
    }

    public void validateFraudVc() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, String>> vcMap = getVcMap(objectMapper);
        Map<String, String> fraudVc =
                vcMap.get("Cri Type: https://review-f.staging.account.gov.uk");

        List<Address> addressList = getAddresses(objectMapper, fraudVc);
        if (addressList.size() > 1) {
            assertEquals(2, addressList.size());
        } else {
            assertEquals(1, addressList.size());
        }
    }

    public void validateDrivingLicenceVc() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Map<String, String>> vcMap = getVcMap(objectMapper);
        Map<String, String> drivingLicenceVc =
                vcMap.get("Cri Type: https://review-d.integration.account.gov.uk");
        dlVc.click();
    }

    public void validateScoreInStubIs(String validityScore, String strengthScore)
            throws IOException {
        new DrivingLicencePageObject()
                .scoreIs(validityScore, strengthScore, dlJsonPayload.getText());
    }

    public void govUkHeadingText(String expectedText) {
        Assert.assertEquals(expectedText, govukHeading.getText());
    }

    public void clickOnProveYourIdentityAnotherWayAndContinue() {
        proveYourIdentityAnotherWayRadioBtn.click();
        continueSubmitButton.click();
    }

    private List<Address> getAddresses(ObjectMapper objectMapper, Map<String, String> fraudVc)
            throws JsonProcessingException {
        TypeReference<HashMap<String, Object>> typeRef =
                new TypeReference<HashMap<String, Object>>() {};

        TypeReference<List<Address>> addressListRef = new TypeReference<>() {};
        String vcString = objectMapper.writeValueAsString(fraudVc.get("vc"));

        Map<String, Object> vc = objectMapper.readValue(vcString, typeRef);
        Map<String, Object> credentialSubject = (Map) vc.get("credentialSubject");

        List<Address> addressList = (List<Address>) credentialSubject.get("address");
        addressList =
                objectMapper
                        .registerModule(new JavaTimeModule())
                        .convertValue(addressList, addressListRef);
        return addressList;
    }

    private Map<String, Map<String, String>> getVcMap(ObjectMapper objectMapper)
            throws JsonProcessingException {
        Map<String, Map<String, String>> vcMap = new HashMap<>();
        List<WebElement> elements =
                Driver.get().findElements(By.className("govuk-summary-list__row"));
        for (WebElement element : elements) {
            String key =
                    element.findElement(By.tagName("dt")).findElement(By.tagName("span")).getText();
            String stringValue =
                    element.findElement(By.tagName("dd"))
                            .findElement(By.tagName("details"))
                            .findElement(By.tagName("div"))
                            .findElement(By.tagName("pre"))
                            .findElement(By.tagName("code"))
                            .getAttribute("innerHTML");
            Map<String, String> vc = objectMapper.readValue(stringValue, Map.class);
            vcMap.put(key, vc);
        }
        return vcMap;
    }

    public void selectTargetEnvironmentFromDropdown(String environment) {
        Select select = new Select(ChooseYourEnvironmentFromTheList);
        select.selectByValue(environment);
    }
}
