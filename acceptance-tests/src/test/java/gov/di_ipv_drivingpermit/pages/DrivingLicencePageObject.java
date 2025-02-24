package gov.di_ipv_drivingpermit.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gov.di_ipv_drivingpermit.service.ConfigurationService;
import gov.di_ipv_drivingpermit.utilities.BrowserUtils;
import gov.di_ipv_drivingpermit.utilities.Driver;
import gov.di_ipv_drivingpermit.utilities.TestDataCreator;
import gov.di_ipv_drivingpermit.utilities.TestInput;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import static gov.di_ipv_drivingpermit.pages.Headers.IPV_CORE_STUB;
import static gov.di_ipv_drivingpermit.utilities.BrowserUtils.checkOkHttpResponseOnLink;
import static java.lang.System.getenv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DrivingLicencePageObject extends UniversalSteps {

    private final ConfigurationService configurationService;
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String STUB_VC_PAGE_TITLE = "IPV Core Stub Credential Result - GOV.UK";
    private static final String STUB_ERROR_PAGE_TITLE = "IPV Core Stub - GOV.UK";

    // Should be separate stub page

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a/button")
    public WebElement visitCredentialIssuers;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI dev\"]")
    public WebElement drivingLicenceCRIDev;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Build\"]")
    public WebElement drivingLicenceCRIBuild;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Staging\"]")
    public WebElement drivingLicenceCRIStaging;

    @FindBy(xpath = "//*[@value=\"Driving Licence CRI Integration\"]")
    public WebElement drivingLicenceCRIIntegration;

    @FindBy(id = "rowNumber")
    public WebElement selectRow;

    @FindBy(xpath = "//*[@id=\"context\"]")
    public WebElement selectInputContextValue;

    @FindBy(id = "claimsText")
    public WebElement selectInputSharedClaimsValue;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/div/pre")
    public WebElement JSONPayload;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details")
    public WebElement errorResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/details/summary/span")
    public WebElement viewResponse;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement searchButton;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[3]/div/button")
    public WebElement searchButtonRawJson;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement goToDLCRIButton;

    // ---------------------

    // Should be separate select issuer page

    @FindBy(xpath = "//*[@id=\"licenceIssuer-fieldset\"]/div/div[3]")
    public WebElement orLabel;

    @FindBy(id = "licenceIssuer-DVLA-label")
    public WebElement optionDVLA;

    @FindBy(id = "licenceIssuer")
    public WebElement radioBtnDVLA;

    @FindBy(id = "licenceIssuer-DVA-label")
    public WebElement optionDVA;

    @FindBy(id = "licenceIssuer-DVA")
    public WebElement radioBtnDVA;

    @FindBy(id = "licenceIssuer-noLicence-label")
    public WebElement noDLOption;

    @FindBy(id = "licenceIssuer-noLicence")
    public WebElement noDLRadioBtn;

    @FindBy(id = "submitButton")
    public WebElement CTButton;

    @FindBy(id = "licenceIssuer-error")
    public WebElement radioButtonError;

    @FindBy(xpath = "/html/body/div[2]/nav/ul/li[2]/a")
    public WebElement languageToggle;

    @FindBy(xpath = "/html/body/div[2]/nav/ul/li[1]/a")
    public WebElement languageToggleWales;

    // ---------------

    @FindBy(className = "error-summary")
    public WebElement errorSummary;

    @FindBy(xpath = "//*[@class='govuk-notification-banner__content']")
    public WebElement userNotFoundInThirdPartyBanner;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]")
    public WebElement userNotFoundInThirdPartyBannerDva;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div/a")
    public WebElement proveAnotherWay;

    @FindBy(id = "govuk-notification-banner-title")
    public WebElement errorText;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/div[1]/div[2]/p[1]")
    public WebElement thereWasAProblemFirstSentence;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/p")
    public WebElement pageDescriptionHeading;

    @FindBy(xpath = "//*[@id=\"cookies-banner-main\"]")
    public WebElement cookieBanner;

    @FindBy(xpath = "/html/body/div[2]/div/p/strong")
    public WebElement betaBanner;

    @FindBy(className = "govuk-phase-banner__text")
    public WebElement betaBannerText;

    @FindBy(xpath = "//*[@id=\"cookies-banner-main\"]/div[2]/button[2]")
    public WebElement rejectAnalysisButton;

    @FindBy(xpath = "//*[@id=\"cookies-rejected\"]/div[1]/div/div/p")
    public WebElement rejectanalysisActual;

    @FindBy(xpath = "//*[@id=\"cookies-rejected\"]/div[1]/div/div/p/a")
    public WebElement changeCookieButton;

    @FindBy(xpath = "/html/head/link[1]")
    public WebElement cookiePreference;

    @FindBy(className = "govuk-error-summary__title")
    public WebElement errorSummaryTitle;

    @FindBy(id = "drivingLicenceNumber")
    public WebElement LicenceNumber;

    @FindBy(xpath = "//*[@id=\"dvaLicenceNumber\"]")
    public WebElement LicenceNumberDva;

    @FindBy(id = "surname")
    public WebElement LastName;

    @FindBy(id = "firstName")
    public WebElement FirstName;

    @FindBy(id = "middleNames")
    public WebElement MiddleNames;

    @FindBy(id = "dateOfBirth-day")
    public WebElement birthDay;

    @FindBy(id = "dateOfBirth-month")
    public WebElement birthMonth;

    @FindBy(id = "dateOfBirth-year")
    public WebElement birthYear;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-day\"]")
    public WebElement birthDayDva;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-day\"]")
    public WebElement birthMonthDva;

    @FindBy(xpath = "//*[@id=\"dvaDateOfBirth-day\"]")
    public WebElement birthYearDva;

    @FindBy(id = "expiryDate-day")
    public WebElement LicenceValidToDay;

    @FindBy(id = "expiryDate-month")
    public WebElement LicenceValidToMonth;

    @FindBy(id = "expiryDate-year")
    public WebElement LicenceValidToYear;

    @FindBy(id = "issueDate-day")
    public WebElement LicenceIssueDay;

    @FindBy(id = "issueDate-month")
    public WebElement LicenceIssueMonth;

    @FindBy(id = "issueDate-year")
    public WebElement LicenceIssueYear;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-day\"]")
    public WebElement LicenceIssueDayDva;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-month\"]")
    public WebElement LicenceIssueMonthDva;

    @FindBy(xpath = "//*[@id=\"dateOfIssue-year\"]")
    public WebElement LicenceIssueYearDva;

    @FindBy(id = "issueNumber")
    public WebElement IssueNumber;

    @FindBy(id = "postcode")
    public WebElement Postcode;

    @FindBy(id = "consentCheckbox")
    public WebElement consentDVLACheckbox;

    @FindBy(xpath = "//button[@class='govuk-button button']")
    public WebElement Continue;

    @FindBy(xpath = "//*[@id=\"confirmDetails\"]")
    public WebElement correctDetailsRadioButton;

    @FindBy(xpath = "//*[@id=\"confirmDetails-detailsNotConfirmed\"]")
    public WebElement incorrectDetailsRadioButton;

    @FindBy(id = "header")
    public WebElement pageHeader;

    @FindBy(xpath = "//*[@class='govuk-back-link']")
    public WebElement back;

    @FindBy(className = "govuk-details__summary-text")
    public WebElement whyWeText;

    @FindBy(className = "govuk-details__text")
    public WebElement whyWePara;

    @FindBy(id = "consentDVACheckbox")
    public WebElement consentDVACheckbox;

    // Error summary items

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#licenceIssuer')]")
    public WebElement InvalidDocumentIssuerInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'-day')]")
    public WebElement InvalidDOBErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'LicenceNumber')]")
    public WebElement InvalidDrivingLicenceErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#surname')]")
    public WebElement InvalidLastNameErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#firstName')]")
    public WebElement InvalidFirstNameErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#middleNames')]")
    public WebElement InvalidMiddleNamesErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#issueDate-day')]")
    public WebElement InvalidIssueDateErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#expiryDate-day')]")
    public WebElement InvalidValidToDateErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#issueNumber')]")
    public WebElement InvalidIssueNumberErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#postcode')]")
    public WebElement InvalidPostcodeErrorInSummary;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#consentCheckbox')]")
    public WebElement dvlaConsentErrorInSummary;

    // -------------------------

    // Field errors

    @FindBy(id = "dateOfBirth-error")
    public WebElement InvalidDateOfBirthFieldError;

    @FindBy(id = "surname-error")
    public WebElement InvalidLastNameFieldError;

    @FindBy(id = "firstName-error")
    public WebElement InvalidFirstNameFieldError;

    @FindBy(id = "middleNames-error")
    public WebElement InvalidMiddleNamesFieldError;

    @FindBy(id = "issueDate-error")
    public WebElement InvalidIssueDateFieldError;

    @FindBy(id = "expiryDate-error")
    public WebElement InvalidValidToDateFieldError;

    @FindBy(id = "drivingLicenceNumber-error")
    public WebElement DrivingLicenceFieldError;

    @FindBy(id = "issueNumber-error")
    public WebElement InvalidIssueNumberFieldError;

    @FindBy(id = "postcode-error")
    public WebElement InvalidPostcodeFieldError;

    @FindBy(id = "consentCheckbox-error")
    public WebElement dvlaConsentCheckboxError;

    @FindBy(id = "consentDVACheckbox-error")
    public WebElement dvaConsentCheckboxError;

    @FindBy(xpath = "//*[@id=\"main-content\"]/div/div/form/h2")
    public WebElement dvlaConsentSection;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/ul/li[1]/a")
    public WebElement oneLoginLink;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/ul/li[2]/a")
    public WebElement dvlaLink;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/p[1]")
    public WebElement dvlaSentence;

    @FindBy(xpath = "//*[@id=\"consentCheckbox-hint\"]/p[2]")
    public WebElement dvlaSentenceTwo;

    // ------------------------

    // --- Hints ---
    @FindBy(id = "dateOfBirth-hint")
    public WebElement dateOfBirthHint;

    @FindBy(id = "issueDate-hint")
    public WebElement issueDateHint;

    @FindBy(id = "drivingLicenceNumber-hint")
    public WebElement licenceNumberHint;

    @FindBy(id = "issueNumber-hint")
    public WebElement issueNumberHint;

    @FindBy(id = "firstName-hint")
    public WebElement firstNameHint;

    @FindBy(id = "middleNames-hint")
    public WebElement middleNameHint;

    @FindBy(id = "expiryDate-hint")
    public WebElement validToHint;

    @FindBy(id = "postcode-hint")
    public WebElement postcodeHint;

    // --- Legend text ---
    @FindBy(xpath = "//*[@id=\"dateOfBirth-fieldset\"]/legend")
    public WebElement dateOfBirthLegend;

    @FindBy(xpath = "//*[@id=\"issueDate-fieldset\"]/legend")
    public WebElement issueDateLegend;

    @FindBy(xpath = "//*[@id=\"expiryDate-fieldset\"]/legend")
    public WebElement validToLegend;

    // --- Label text ---
    @FindBy(id = "drivingLicenceNumber-label")
    public WebElement licenceNumberFieldLabel;

    @FindBy(id = "issueNumber-label")
    public WebElement issueNumberFieldLabel;

    @FindBy(id = "postcode-label")
    public WebElement postcodeLabel;

    public DrivingLicencePageObject() {
        this.configurationService = new ConfigurationService(getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
        TestDataCreator.createDefaultResponses();
    }

    // Should be in stub page

    public void navigateToIPVCoreStub() {
        String coreStubUrl = configurationService.getCoreStubUrl(true);
        Driver.get().get(coreStubUrl);
        assertPageTitle(IPV_CORE_STUB, true);
    }

    public void whyWeNeedToKnowThis() {
        assertTrue(whyWeText.isDisplayed());
        LOGGER.info(whyWeText.getText());
    }

    public void clickOnWhyWeNeedLink() {
        whyWeText.click();
    }

    public void paragraphValidation() {
        assertTrue(whyWePara.isDisplayed());
        LOGGER.info(whyWePara.getText());
    }

    public void navigateToDrivingLicenceCRIOnTestEnv() {
        visitCredentialIssuers.click();
        assertPageTitle(IPV_CORE_STUB, true);
        String dlCRITestEnvironment = configurationService.getDlCRITestEnvironment();
        LOGGER.info("dlCRITestEnvironment = {}", dlCRITestEnvironment);
        if (dlCRITestEnvironment.equalsIgnoreCase("dev")
                || dlCRITestEnvironment.equalsIgnoreCase("local")) {
            drivingLicenceCRIDev.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Build")) {
            drivingLicenceCRIBuild.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Staging")) {
            drivingLicenceCRIStaging.click();
        } else if (dlCRITestEnvironment.equalsIgnoreCase("Integration")) {
            drivingLicenceCRIIntegration.click();
        } else {
            LOGGER.info("No test environment is set");
        }
    }

    public void searchForUATUser(String number) {
        assertURLContains("credential-issuer?cri=driving-licence");
        selectRow.sendKeys(number);
        searchButton.click();
    }

    public void enterContextValue(String contextValue) {
        assertURLContains("credential-issuer?cri=driving-licence");
        selectInputContextValue.sendKeys(contextValue);
    }

    public void enterSharedClaimsRawJSONValue(String jsonFileName) {
        String sharedClaimsRawJson = getJsonPayload(jsonFileName);
        if (sharedClaimsRawJson != null) {
            selectInputSharedClaimsValue.sendKeys(sharedClaimsRawJson);
            searchButtonRawJson.click();
        } else {
            throw new RuntimeException("Failed to load JSON from file: " + jsonFileName);
        }
    }

    public void navigateToDrivingLicenceResponse(String validOrInvalid) {
        assertURLContains("callback");

        if ("Invalid".equalsIgnoreCase(validOrInvalid)) {
            assertPageTitle(STUB_ERROR_PAGE_TITLE, false);
            assertURLContains("callback");
            BrowserUtils.waitForVisibility(errorResponse, 10);
            errorResponse.click();
        } else {
            assertPageTitle(STUB_VC_PAGE_TITLE, false);
            assertURLContains("callback");
            BrowserUtils.waitForVisibility(viewResponse, 10);
            viewResponse.click();
        }
    }

    public void navigateToDrivingLicenceCRI() {
        goToDLCRIButton.click();
    }

    // ------------------

    // Should be seperate page

    public void cookieBannerIsDisplayed() {
        BrowserUtils.waitForVisibility(cookieBanner, 10);
    }

    public void betaBanner() {
        betaBanner.isDisplayed();
    }

    public void betaBannerSentence(String expectedText) {
        assertEquals(expectedText, betaBannerText.getText());
        LOGGER.info("actualText = {}", betaBannerText.getText());
    }

    public void rejectAnalysisCookie(String rejectAnalysis) {
        BrowserUtils.waitForVisibility(rejectAnalysisButton, 10);
        rejectAnalysisButton.click();
    }

    public void rejectCookieSentence(String rejectanalysisSentence) {
        assertEquals(rejectanalysisSentence, rejectanalysisActual.getText());
        LOGGER.info(rejectanalysisActual.getText());
    }

    public void AssertChangeCookieLink(String changeCookieLink) {
        changeCookieButton.click();
    }

    public void AssertcookiePreferencePage() {

        String changeCookiePageUrl = cookiePreference.getAttribute("href");
        checkOkHttpResponseOnLink(changeCookiePageUrl);
    }

    public void titleDVLAWithRadioBtn(String expectedText) {
        optionDVLA.isDisplayed();
        assertEquals(expectedText, optionDVLA.getText());
        radioBtnDVLA.isDisplayed();
    }

    public void titleDVAWithRadioBtn(String expectedText) {
        optionDVA.isDisplayed();
        assertEquals(expectedText, optionDVA.getText());
        radioBtnDVA.isDisplayed();
    }

    public void noDrivingLicenceBtn(String expectedText) {
        noDLOption.isDisplayed();
        assertEquals(expectedText, noDLOption.getText());
        noDLRadioBtn.isDisplayed();
    }

    public void ContinueButton(String expectedText) {
        CTButton.isDisplayed();
        assertEquals(expectedText, CTButton.getText());
        CTButton.isEnabled();
    }

    public void clickOnDVLARadioButton() {
        radioBtnDVLA.click();
        CTButton.click();
    }

    public void clickOnDVARadioButton() {
        radioBtnDVA.click();
        CTButton.click();
    }

    public void clickOnIDoNotHaveAUKDrivingLicenceRadioButton() {
        noDLRadioBtn.click();
        Continue.click();
    }

    public void pageTitleDVLAValidation() {
        assert (Driver.get().getTitle().contains("We’ll check your details with DVLA "));
    }

    public void noSelectContinue() {
        CTButton.click();
    }

    public void assertErrorTitle(String expectedText) {
        assertEquals(expectedText, InvalidDocumentIssuerInSummary.getText());
    }

    public void errorMessage() {
        errorSummary.isDisplayed();
    }

    public void errorLink() {
        errorSummary.click();
        radioBtnDVLA.isEnabled();
    }

    public void validateErrorText(String expectedText) {
        assertEquals(expectedText, radioButtonError.getText().trim().replace("\n", ""));
    }

    public void drivingLicencePageURLValidationWelsh() {
        assertURLContains("/licence-issuer/?lng=cy");
    }

    public void assertOrLabelText(String expectedText) {
        assertEquals(expectedText, orLabel.getText());
    }

    // -----------------------

    public void drivingLicencePageURLValidation(String path) {
        assertURLContains(path);
    }

    public void assertUserRoutedToIpvCore() {
        assertPageTitle("IPV Core Stub - GOV.UK", false);
    }

    public void assertUserRoutedToIpvCoreErrorPage() {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        String expUrl =
                coreStubUrl
                        + "callback?error=access_denied&error_description=Authorization+permission+denied";
        String actUrl = Driver.get().getCurrentUrl();
        LOGGER.info("expectedUrl = {}", expUrl);
        LOGGER.info("actualUrl = {}", actUrl);
        assertEquals(actUrl, expUrl);
    }

    public void jsonErrorResponse(String expectedErrorDescription, String expectedErrorStatusCode)
            throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = {}", result);

        JsonNode insideError = getJsonNode(result, "errorObject");
        LOGGER.info("insideError = {}", insideError);

        JsonNode errorDescription = insideError.get("description");
        JsonNode statusCode = insideError.get("httpstatusCode");
        String ActualErrorDescription = insideError.get("description").asText();
        String ActualStatusCode = insideError.get("httpstatusCode").asText();

        LOGGER.info("errorDescription = {}", errorDescription);
        LOGGER.info("statusCode = {}", statusCode);
        LOGGER.info("testErrorDescription = {}", expectedErrorDescription);
        LOGGER.info("testStatusCode = {}", expectedErrorStatusCode);

        assertEquals(expectedErrorDescription, ActualErrorDescription);
        assertEquals(expectedErrorStatusCode, ActualStatusCode);
    }

    public void checkScoresAndTypeInStubIs(String validityScore, String strengthScore, String type)
            throws IOException {
        scoreAndTypeIs(validityScore, strengthScore, type, JSONPayload.getText());
    }

    public void scoreIs(
            String expectedValidityScore, String expectedStrengthScore, String jsonPayloadText)
            throws IOException {
        String result = jsonPayloadText;
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        String validityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(expectedValidityScore, validityScore);

        String strengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(expectedStrengthScore, strengthScore);
    }

    public void scoreAndTypeIs(
            String expectedValidityScore,
            String expectedStrengthScore,
            String expectedType,
            String jsonPayloadText)
            throws IOException {
        String result = jsonPayloadText;
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");

        String validityScore = evidence.get(0).get("validityScore").asText();
        assertEquals(expectedValidityScore, validityScore);

        String strengthScore = evidence.get(0).get("strengthScore").asText();
        assertEquals(expectedStrengthScore, strengthScore);

        String type = evidence.get(0).get("type").asText();
        assertEquals(expectedType, type);
    }

    public void assertCheckDetailsWithinVc(
            String checkMethod,
            String identityCheckPolicy,
            String checkDetailsType,
            String drivingLicenceCRIVC)
            throws IOException {
        JsonNode vcNode = getJsonNode(drivingLicenceCRIVC, "vc");
        List<JsonNode> evidence = getListOfNodes(vcNode, "evidence");
        JsonNode firstItemInEvidenceArray = evidence.get(0);
        LOGGER.info("firstItemInEvidenceArray = {}", firstItemInEvidenceArray);
        if (checkDetailsType.equals("success")) {
            JsonNode checkDetailsNode = firstItemInEvidenceArray.get("checkDetails");
            JsonNode checkMethodNode = checkDetailsNode.get(0).get("checkMethod");
            String actualCheckMethod = checkMethodNode.asText();
            LOGGER.info("actualCheckMethod = {}", actualCheckMethod);
            JsonNode identityCheckPolicyNode = checkDetailsNode.get(0).get("identityCheckPolicy");
            String actualidentityCheckPolicy = identityCheckPolicyNode.asText();
            LOGGER.info("actualidentityCheckPolicy = {}", actualidentityCheckPolicy);
            JsonNode activityFromNode = checkDetailsNode.get(0).get("activityFrom");
            String actualactivityFrom = activityFromNode.asText();
            LOGGER.info("actualactivityFrom = {}", actualactivityFrom);
            Assert.assertEquals(checkMethod, actualCheckMethod);
            Assert.assertEquals(identityCheckPolicy, actualidentityCheckPolicy);
            if (!StringUtils.isEmpty(activityFromNode.toString())) {
                assertEquals(
                        "[{\"checkMethod\":"
                                + checkMethodNode.toString()
                                + ","
                                + "\"identityCheckPolicy\":"
                                + identityCheckPolicyNode.toString()
                                + ","
                                + "\"activityFrom\":"
                                + activityFromNode.toString()
                                + "}]",
                        checkDetailsNode.toString());
            }
        } else {
            JsonNode failedCheckDetailsNode = firstItemInEvidenceArray.get("failedCheckDetails");
            JsonNode checkMethodNode = failedCheckDetailsNode.get(0).get("checkMethod");
            String actualCheckMethod = checkMethodNode.asText();
            LOGGER.info("actualCheckMethod = {}", actualCheckMethod);
            JsonNode identityCheckPolicyNode =
                    failedCheckDetailsNode.get(0).get("identityCheckPolicy");
            String actualidentityCheckPolicy = identityCheckPolicyNode.asText();
            LOGGER.info("actualidentityCheckPolicy = {}", actualidentityCheckPolicy);
            Assert.assertEquals(checkMethod, actualCheckMethod);
            Assert.assertEquals(identityCheckPolicy, actualidentityCheckPolicy);
            assertEquals(
                    "[{\"checkMethod\":"
                            + checkMethodNode.toString()
                            + ","
                            + "\"identityCheckPolicy\":"
                            + identityCheckPolicyNode.toString()
                            + "}]",
                    failedCheckDetailsNode.toString());
        }
    }

    public void userNotFoundInThirdPartyErrorIsDisplayed() {
        assertTrue(userNotFoundInThirdPartyBanner.isDisplayed());
        LOGGER.info(userNotFoundInThirdPartyBanner.getText());
    }

    public void userNotFoundInThirdPartyErrorIsDisplayedDva() {
        assertTrue(userNotFoundInThirdPartyBannerDva.isDisplayed());
        LOGGER.info(userNotFoundInThirdPartyBannerDva.getText());
    }

    public void userReEntersLastName(String invalidLastName) {
        LastName.clear();
        LastName.sendKeys(invalidLastName);
    }

    public void userReEntersFirstName(String invalidFirstName) {
        FirstName.clear();
        FirstName.sendKeys(invalidFirstName);
    }

    public void userReEntersMiddleNames(String invalidMiddleNames) {
        MiddleNames.clear();
        MiddleNames.sendKeys(invalidMiddleNames);
    }

    public void userReEntersBirthDay(String invalidBirthDay) {
        birthDay.clear();
        birthDay.sendKeys(invalidBirthDay);
    }

    public void userReEntersDvaBirthDay(String invalidBirthDay) {
        birthDayDva.clear();
        birthDayDva.sendKeys(invalidBirthDay);
    }

    public void userReEntersDvaBirthMonth(String invalidBirthMonth) {
        birthMonthDva.clear();
        birthMonthDva.sendKeys(invalidBirthMonth);
    }

    public void userReEntersDvaBirthYear(String invalidBirthYear) {
        birthYearDva.clear();
        birthYearDva.sendKeys(invalidBirthYear);
    }

    public void userReEntersBirthMonth(String invalidBirthMonth) {
        birthMonth.clear();
        birthMonth.sendKeys(invalidBirthMonth);
    }

    public void userReEntersBirthYear(String invalidBirthYear) {
        birthYear.clear();
        birthYear.sendKeys(invalidBirthYear);
    }

    public void userReEntersIssueDay(String invalidLicenceIssueDay) {
        LicenceIssueDay.clear();
        LicenceIssueDay.sendKeys(invalidLicenceIssueDay);
    }

    public void userReEntersDvaIssueDay(String invalidLicenceIssueDay) {
        LicenceIssueDayDva.clear();
        LicenceIssueDayDva.sendKeys(invalidLicenceIssueDay);
    }

    public void userReEntersDvaIssueMonth(String invalidLicenceIssueMonth) {
        LicenceIssueMonthDva.clear();
        LicenceIssueMonthDva.sendKeys(invalidLicenceIssueMonth);
    }

    public void userReEntersDvaIssueYear(String invalidLicenceIssueYear) {
        LicenceIssueYearDva.clear();
        LicenceIssueYearDva.sendKeys(invalidLicenceIssueYear);
    }

    public void userReEntersLicenceNumber(String invalidLicenceNumber) {
        LicenceNumber.clear();
        LicenceNumber.sendKeys(invalidLicenceNumber);
    }

    public void userReEntersDvaLicenceNumber(String invalidLicenceNumber) {
        LicenceNumberDva.clear();
        LicenceNumberDva.sendKeys(invalidLicenceNumber);
    }

    public void userReEntersIssueMonth(String invalidLicenceIssueMonth) {
        LicenceIssueMonth.clear();
        LicenceIssueMonth.sendKeys(invalidLicenceIssueMonth);
    }

    public void userReEntersIssueYear(String invalidLicenceIssueYear) {
        LicenceIssueYear.clear();
        LicenceIssueYear.sendKeys(invalidLicenceIssueYear);
    }

    public void userReEntersIssueNumber(String invalidIssueNumber) {
        IssueNumber.clear();
        IssueNumber.sendKeys(invalidIssueNumber);
    }

    public void userReEntersValidToDay(String invalidValidToDate) {
        LicenceValidToDay.clear();
        LicenceValidToDay.sendKeys(invalidValidToDate);
    }

    public void userReEntersValidToMonth(String invalidValidToMonth) {
        LicenceValidToMonth.clear();
        LicenceValidToMonth.sendKeys(invalidValidToMonth);
    }

    public void userReEntersValidToYear(String invalidValidToYear) {
        LicenceValidToYear.clear();
        LicenceValidToYear.sendKeys(invalidValidToYear);
    }

    public void userReEntersPostcode(String invalidPostcode) {
        Postcode.clear();
        Postcode.sendKeys(invalidPostcode);
    }

    public void userEntersData(String issuer, String drivingLicenceSubjectScenario) {
        TestInput drivingLicenceSubject =
                TestDataCreator.getTestUserFromMap(issuer, drivingLicenceSubjectScenario);
        if (issuer.equals("DVLA")) {
            LicenceNumber.sendKeys(drivingLicenceSubject.getLicenceNumber());
            birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
            birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
            birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
            LicenceIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
            LicenceIssueMonth.sendKeys(drivingLicenceSubject.getIssueMonth());
            LicenceIssueYear.sendKeys(drivingLicenceSubject.getIssueYear());
            if (null != drivingLicenceSubject.getIssueNumber()) {
                IssueNumber.sendKeys(drivingLicenceSubject.getIssueNumber());
            }
            consentDVLACheckbox.click();
        }
        if (null != drivingLicenceSubject.getMiddleNames()) {
            MiddleNames.sendKeys(drivingLicenceSubject.getMiddleNames());
        }

        LastName.sendKeys(drivingLicenceSubject.getLastName());
        FirstName.sendKeys(drivingLicenceSubject.getFirstName());
        LicenceValidToDay.sendKeys(drivingLicenceSubject.getValidToDay());
        LicenceValidToMonth.sendKeys(drivingLicenceSubject.getValidToMonth());
        LicenceValidToYear.sendKeys(drivingLicenceSubject.getValidToYear());
        Postcode.sendKeys(drivingLicenceSubject.getPostcode());
    }

    // Why is this invalid
    public void userEntersInvalidDrivingDetails() {
        DrivingLicencePageObject enterYourDetailsExactlyDVLAPage = new DrivingLicencePageObject();
        enterYourDetailsExactlyDVLAPage.LicenceNumber.sendKeys("PARKE610112PBFGI");
        enterYourDetailsExactlyDVLAPage.LastName.sendKeys("Testlastname");
        enterYourDetailsExactlyDVLAPage.FirstName.sendKeys("Testfirstname");
        enterYourDetailsExactlyDVLAPage.birthDay.sendKeys("11");
        enterYourDetailsExactlyDVLAPage.birthMonth.sendKeys("10");
        enterYourDetailsExactlyDVLAPage.birthYear.sendKeys("1962");
        enterYourDetailsExactlyDVLAPage.LicenceValidToDay.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.LicenceValidToMonth.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.LicenceValidToYear.sendKeys("2030");
        enterYourDetailsExactlyDVLAPage.LicenceIssueDay.sendKeys("10");
        enterYourDetailsExactlyDVLAPage.LicenceIssueMonth.sendKeys("12");
        enterYourDetailsExactlyDVLAPage.LicenceIssueYear.sendKeys("2018");
        enterYourDetailsExactlyDVLAPage.IssueNumber.sendKeys("01");
        enterYourDetailsExactlyDVLAPage.Postcode.sendKeys("BS98 1AA");
        consentDVLACheckbox.click();

        BrowserUtils.waitForPageToLoad(10);
    }

    public void enterInvalidLastAndFirstName() {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LastName.sendKeys("Parker!");
        drivingLicencePageObject.FirstName.sendKeys("Peter@@!");
        drivingLicencePageObject.MiddleNames.sendKeys("@@@@@@@");
    }

    public void enterDvlaBirthYear(String day, String month, String year) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.birthDay.clear();
        drivingLicencePageObject.birthDay.click();
        drivingLicencePageObject.birthDay.sendKeys(day);
        drivingLicencePageObject.birthMonth.clear();
        drivingLicencePageObject.birthMonth.click();
        drivingLicencePageObject.birthMonth.sendKeys(month);
        drivingLicencePageObject.birthYear.clear();
        drivingLicencePageObject.birthYear.click();
        drivingLicencePageObject.birthYear.sendKeys(year);
    }

    public void enterIssueDate(String day, String month, String year) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceIssueDay.clear();
        drivingLicencePageObject.LicenceIssueDay.click();
        drivingLicencePageObject.LicenceIssueDay.sendKeys(day);
        drivingLicencePageObject.LicenceIssueMonth.clear();
        drivingLicencePageObject.LicenceIssueMonth.click();
        drivingLicencePageObject.LicenceIssueMonth.sendKeys(month);
        drivingLicencePageObject.LicenceIssueYear.clear();
        drivingLicencePageObject.LicenceIssueYear.click();
        drivingLicencePageObject.LicenceIssueYear.sendKeys(year);
    }

    public void enterValidToDate(String day, String month, String year) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceValidToDay.clear();
        drivingLicencePageObject.LicenceValidToDay.click();
        drivingLicencePageObject.LicenceValidToDay.sendKeys(day);
        drivingLicencePageObject.LicenceValidToMonth.clear();
        drivingLicencePageObject.LicenceValidToMonth.click();
        drivingLicencePageObject.LicenceValidToMonth.sendKeys(month);
        drivingLicencePageObject.LicenceValidToYear.clear();
        drivingLicencePageObject.LicenceValidToYear.click();
        drivingLicencePageObject.LicenceValidToYear.sendKeys(year);
    }

    public void enterLicenceNumber(String licenceNumber) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.LicenceNumber.clear();
        drivingLicencePageObject.LicenceNumber.click();
        drivingLicencePageObject.LicenceNumber.sendKeys(licenceNumber);
    }

    public void clearIssueNumber() {
        this.IssueNumber.clear();
    }

    public void enterIssueNumber(String issueNumber) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.IssueNumber.clear();
        drivingLicencePageObject.IssueNumber.click();
        drivingLicencePageObject.IssueNumber.sendKeys(issueNumber);
    }

    public void enterPostcode(String postcode) {
        DrivingLicencePageObject drivingLicencePageObject = new DrivingLicencePageObject();
        drivingLicencePageObject.Postcode.clear();
        drivingLicencePageObject.Postcode.click();
        drivingLicencePageObject.Postcode.sendKeys(postcode);
    }

    public void userReEntersDataAsADrivingLicenceSubject(String drivingLicenceSubjectScenario) {
        TestInput drivingLicenceSubject =
                TestDataCreator.getTestUserFromMap("DVLA", drivingLicenceSubjectScenario);

        LicenceNumber.clear();
        LastName.clear();
        FirstName.clear();
        MiddleNames.clear();
        birthDay.clear();
        birthMonth.clear();
        birthYear.clear();
        LicenceValidToDay.clear();
        LicenceValidToMonth.clear();
        LicenceValidToYear.clear();
        LicenceIssueDay.clear();
        LicenceIssueMonth.clear();
        LicenceIssueYear.clear();
        IssueNumber.clear();
        Postcode.clear();
        LicenceNumber.sendKeys(drivingLicenceSubject.getLicenceNumber());
        LastName.sendKeys(drivingLicenceSubject.getLastName());
        FirstName.sendKeys(drivingLicenceSubject.getFirstName());
        if (null != drivingLicenceSubject.getMiddleNames()) {
            MiddleNames.sendKeys(drivingLicenceSubject.getMiddleNames());
        }
        if (null != drivingLicenceSubject.getIssueNumber()) {
            IssueNumber.sendKeys(drivingLicenceSubject.getIssueNumber());
        }
        birthDay.sendKeys(drivingLicenceSubject.getBirthDay());
        birthMonth.sendKeys(drivingLicenceSubject.getBirthMonth());
        birthYear.sendKeys(drivingLicenceSubject.getBirthYear());
        LicenceValidToDay.sendKeys(drivingLicenceSubject.getValidToDay());
        LicenceValidToMonth.sendKeys(drivingLicenceSubject.getValidToMonth());
        LicenceValidToYear.sendKeys(drivingLicenceSubject.getValidToYear());
        LicenceIssueDay.sendKeys(drivingLicenceSubject.getIssueDay());
        LicenceIssueMonth.sendKeys(drivingLicenceSubject.getIssueMonth());
        LicenceIssueYear.sendKeys(drivingLicenceSubject.getIssueYear());
        Postcode.sendKeys(drivingLicenceSubject.getPostcode());
    }

    public void assertInvalidDoBInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidDOBErrorInSummary.getText());
    }

    public void assertInvalidDoBOnField(String expectedText) {
        assertEquals(expectedText, InvalidDateOfBirthFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidIssueDateInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidIssueDateErrorInSummary.getText());
    }

    public void assertInvalidIssueDateOnField(String expectedText) {
        assertEquals(expectedText, InvalidIssueDateFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidValidToDateInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidValidToDateErrorInSummary.getText());
    }

    public void assertInvalidValidToDateOnField(String expectedText) {
        assertEquals(expectedText, InvalidValidToDateFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidLicenceNumberInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidDrivingLicenceErrorInSummary.getText());
    }

    public void assertInvalidLicenceNumberOnField(String expectedText) {
        assertEquals(expectedText, DrivingLicenceFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidIssueNumberInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidIssueNumberErrorInSummary.getText());
    }

    public void assertInvalidIssueNumberOnField(String expectedText) {
        assertEquals(expectedText, InvalidIssueNumberFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidPostcodeInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidPostcodeErrorInSummary.getText());
    }

    public void assertInvalidPostcodeOnField(String expectedText) {
        assertEquals(expectedText, InvalidPostcodeFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidLastNameInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidLastNameErrorInSummary.getText());
    }

    public void assertInvalidLastNameOnField(String expectedText) {
        assertEquals(expectedText, InvalidLastNameFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidFirstNameInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidFirstNameErrorInSummary.getText());
    }

    public void assertInvalidFirstNameOnField(String expectedText) {
        assertEquals(expectedText, InvalidFirstNameFieldError.getText().trim().replace("\n", ""));
    }

    public void assertInvalidMiddleNameInErrorSummary(String expectedText) {
        assertEquals(expectedText, InvalidMiddleNamesErrorInSummary.getText());
    }

    public void assertInvalidMiddleNameOnField(String expectedText) {
        assertEquals(expectedText, InvalidMiddleNamesFieldError.getText().trim().replace("\n", ""));
    }

    public void assertNoConsentGivenInErrorSummary(String expectedText) {
        String formattedErrorText = DVLAConsentCheckboxError.getText().replaceAll("\\s+", " ");
        assertEquals(expectedText, formattedErrorText);
    }

    public void assertNoConsentGivenInDVAErrorSummary(String expectedText) {
        String formattedErrorText = dvaConsentCheckboxError.getText().replaceAll("\\s+", " ");
        assertEquals(expectedText, formattedErrorText);
    }

    public void ciInVC(String ci) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        JsonNode evidenceNode = vcNode.get("evidence");

        List<String> cis = getCIsFromEvidence(evidenceNode);

        if (StringUtils.isNotEmpty(ci)) {
            if (cis.size() > 0) {
                assertTrue(cis.contains(ci));
            } else {
                fail("No CIs found");
            }
        }
    }

    public void assertPersonalNumberInVc(String personalNumber) throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode vcNode = getJsonNode(result, "vc");
        String licenceNumber = getPersonalNumberFromVc(vcNode);
        assertEquals(personalNumber, licenceNumber);
    }

    public void assertJtiPresent() throws IOException {
        String result = JSONPayload.getText();
        LOGGER.info("result = {}", result);
        JsonNode jtiNode = getJsonNode(result, "jti");
        String jti = jtiNode.asText();
        assertNotNull(jti);
    }

    public void validateErrorPageHeading(String errorHeading) {
        assertEquals(errorHeading, pageHeader.getText());
    }

    public void assertPageHeading(String expectedText) {
        assertEquals(expectedText, pageHeader.getText().split("\n")[0]);
    }

    public void assertProveAnotherWayLinkText(String expectedText) {
        assertEquals(expectedText, getParent(proveAnotherWay).getText());
    }

    public void assertErrorPrefix(String expectedText) {
        assertEquals(expectedText, errorText.getText());
    }

    public void assertFirstLineOfUserNotFoundText(String expectedText) {
        assertEquals(expectedText, userNotFoundInThirdPartyBanner.getText().split("\n")[0]);
    }

    public void youWillBeAbleToFindSentence(String expectedText) {
        assertEquals(expectedText, thereWasAProblemFirstSentence.getText());
    }

    public void assertPageSourceContains(String expectedText) {
        assert (Driver.get().getPageSource().contains(expectedText));
    }

    public void assertLastNameLabelText(String expectedText) {
        assertEquals(expectedText, getLabel(getParent(LastName)).getText());
    }

    public void assertGivenNameLegendText(String expectedText) {
        assertEquals(
                expectedText,
                FirstName.findElement(By.xpath("./../../.."))
                        .findElement(By.tagName("legend"))
                        .getText());
    }

    public void assertMiddleNameLabelText(String expectedText) {
        assertEquals(expectedText, getLabel(getParent(MiddleNames)).getText());
    }

    public void assertGivenNameDescription(String expectedText) {
        assertEquals(
                expectedText, getLabel(firstNameHint.findElement(By.xpath("./../.."))).getText());
    }

    public void assertGivenNameHint(String expectedText) {
        assertEquals(expectedText, firstNameHint.getText());
    }

    public void assertMiddleNameHint(String expectedText) {
        assertEquals(expectedText, middleNameHint.getText());
    }

    public void assertDateOfBirthLegendText(String expectedText) {
        assertEquals(expectedText, dateOfBirthLegend.getText());
    }

    public void assertDateOfBirthHintText(String expectedText) {
        assertEquals(expectedText, dateOfBirthHint.getText());
    }

    public void assertBirthDayLabelText(String expectedText) {
        assertEquals(expectedText, getLabel(getParent(birthDay)).getText());
    }

    public void assertBirthMonthLabelText(String expectedText) {
        assertEquals(expectedText, getLabel(getParent(birthMonth)).getText());
    }

    public void assertBirthYearLabelText(String expectedText) {
        assertEquals(expectedText, getLabel(getParent(birthYear)).getText());
    }

    public void assertIssueDateLegendText(String expectedText) {
        assertEquals(expectedText, issueDateLegend.getText());
    }

    public void assertIssueDateHintText(String expectedText) {
        assertEquals(expectedText, issueDateHint.getText());
    }

    public void assertValidToHintText(String expectedText) {
        assertEquals(expectedText, validToHint.getText());
    }

    public void assertValidToHintTextDVA(String expectedText) {
        assertEquals(expectedText, validToHint.getText());
    }

    public void assertLicenceNumberLabelText(String expectedText) {
        assertEquals(expectedText, licenceNumberFieldLabel.getText());
    }

    public void assertLicenceNumberHintText(String expectedText) {
        assertEquals(expectedText, licenceNumberHint.getText());
    }

    public void assertIssueNumberLabelText(String expectedText) {
        assertEquals(expectedText, issueNumberFieldLabel.getText());
    }

    public void assertIssueNumberHintText(String expectedText) {
        assertEquals(expectedText, issueNumberHint.getText());
    }

    public void assertPostcodeLabelText(String expectedText) {
        assertEquals(expectedText, postcodeLabel.getText());
    }

    public void assertPostcodeHintText(String expectedText) {
        assertEquals(expectedText, postcodeHint.getText());
    }

    public void assertPageDescription(String expectedText) {
        assertEquals(expectedText, pageDescriptionHeading.getText());
    }

    public void assertValidToLegend(String expectedText) {
        assertEquals(expectedText, validToLegend.getText());
    }

    public void assertErrorSummaryText(String expectedText) {
        assertEquals(expectedText, errorSummaryTitle.getText());
    }

    public void assertIssueNumberDescriptionText(String expectedText) {
        assertEquals(
                expectedText,
                getParent(issueNumberFieldLabel)
                        .findElement(By.tagName("p"))
                        .getText()
                        .trim()
                        .replace("\n", ""));
    }

    private List<String> getCIsFromEvidence(JsonNode evidenceNode) throws IOException {
        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        List<JsonNode> evidence = objectReader.readValue(evidenceNode);

        List<String> cis =
                getListOfNodes(evidence.get(0), "ci").stream()
                        .map(JsonNode::asText)
                        .collect(Collectors.toList());
        return cis;
    }

    public JsonNode getJsonNode(String result, String vc) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        return jsonNode.get(vc);
    }

    private String getPersonalNumberFromVc(JsonNode vcNode) throws IOException {
        JsonNode credentialSubject = vcNode.findValue("credentialSubject");
        List<JsonNode> evidence = getListOfNodes(credentialSubject, "drivingPermit");

        String licenceNumber = evidence.get(0).get("personalNumber").asText();
        return licenceNumber;
    }

    public List<JsonNode> getListOfNodes(JsonNode vcNode, String evidence) throws IOException {
        JsonNode evidenceNode = vcNode.get(evidence);

        ObjectReader objectReader =
                new ObjectMapper().readerFor(new TypeReference<List<JsonNode>>() {});
        return objectReader.readValue(evidenceNode);
    }

    private WebElement getParent(WebElement webElement) {
        return webElement.findElement(By.xpath("./.."));
    }

    private WebElement getLabel(WebElement webElement) {
        return webElement.findElement(By.tagName("label"));
    }

    public void assertDVLAConsentErrorInErrorSummary(String expectedText) {
        assertEquals(expectedText, dvlaConsentErrorInSummary.getText());
    }

    public void assertDVLAConsentErrorOnCheckbox(String expectedText) {
        assertEquals(expectedText, dvlaConsentCheckboxError.getText().trim().replace("\n", ""));
    }

    public void goToPage(String page) {
        assertPageTitle(page, false);
    }

    public void assertConsentSection(String consentSection) {
        assertEquals(consentSection, dvlaConsentSection.getText());
    }

    public void assertOneLoginPrivacyLink(String oneLoginPrivacyLink) {
        assertEquals(oneLoginPrivacyLink, oneLoginLink.getText());
        String oneLoginDVLALinkUrl = oneLoginLink.getAttribute("href");

        checkOkHttpResponseOnLink(oneLoginDVLALinkUrl);
        oneLoginLink.click();
    }

    public void assertDVLAPrivacyLink(String dvlaPrivacyLink) {
        assertEquals(dvlaPrivacyLink, dvlaLink.getText());
        String oneLoginDVLALinkUrl = dvlaLink.getAttribute("href");

        checkOkHttpResponseOnLink(oneLoginDVLALinkUrl);
    }

    public void assertDVLAContent(String contentDVLA) {
        assertEquals(contentDVLA, dvlaSentence.getText());
    }

    public void assertDVLAContentLineTwo(String contentDVLALine2) {
        assertEquals(contentDVLALine2, dvlaSentenceTwo.getText());
    }

    private JsonNode getVCFromJson(String vc) throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = {}", result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        return jsonNode.get(vc);
    }

    public void expiryAbsentFromVC(String exp) throws JsonProcessingException {
        assertNbfIsRecentAndExpiryIsNull();
    }

    private void assertNbfIsRecentAndExpiryIsNull() throws JsonProcessingException {
        String result = JSONPayload.getText();
        LOGGER.info("result = {}", result);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        JsonNode nbfNode = jsonNode.get("nbf");
        JsonNode expNode = jsonNode.get("exp");
        String nbf = jsonNode.get("nbf").asText();
        LOGGER.info("nbf = {}", nbfNode);
        LOGGER.info("exp = {}", expNode);
        LocalDateTime nbfDateTime =
                LocalDateTime.ofEpochSecond(Long.parseLong(nbf), 0, ZoneOffset.UTC);

        assertNull(expNode);
        assertTrue(isWithinRange(nbfDateTime));
    }

    boolean isWithinRange(LocalDateTime testDate) {
        LocalDateTime nbfMin = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(30);
        LocalDateTime nbfMax = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30);
        LOGGER.info("nbfMin {}", nbfMin);
        LOGGER.info("nbfMax {}", nbfMax);
        LOGGER.info("nbf {}", testDate);

        return testDate.isBefore(nbfMax) && testDate.isAfter(nbfMin);
    }
}
