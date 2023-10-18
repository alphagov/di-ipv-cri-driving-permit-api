package uk.gov.di.ipv.cri.drivingpermit.library.metrics;

public class Definitions {

    // Record all escape routes from the lambdas.
    // OK for expected routes with ERROR being all others
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_OK =
            "lambda_driving_permit_check_completed_ok";
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_COMPLETED_ERROR =
            "lambda_driving_permit_check_completed_error";

    public static final String LAMBDA_ISSUE_CREDENTIAL_COMPLETED_OK =
            "lambda_issue_credential_completed_ok";
    public static final String LAMBDA_ISSUE_CREDENTIAL_COMPLETED_ERROR =
            "lambda_issue_credential_completed_error";

    // Document Status after an attempt
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_VERIFIED_PREFIX =
            "lambda_driving_permit_check_attempt_status_verified_"; // Attempt count appended
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_RETRY =
            "lambda_driving_permit_check_attempt_status_retry";
    public static final String LAMBDA_DRIVING_PERMIT_CHECK_ATTEMPT_STATUS_UNVERIFIED =
            "lambda_driving_permit_check_attempt_status_unverified";

    // FormDataValidator
    public static final String FORM_DATA_VALIDATION_PASS = "form_data_validation_pass";
    public static final String FORM_DATA_VALIDATION_FAIL = "form_data_validation_fail";

    // IssuingAuthority
    public static final String ISSUING_AUTHORITY_PREFIX = "issuing_authority_";

    // DocumentDataVerification (Request Status)
    public static final String DOCUMENT_DATA_VERIFICATION_REQUEST_SUCCEEDED =
            "document_data_verification_request_succeeded";
    public static final String DOCUMENT_DATA_VERIFICATION_REQUEST_FAILED =
            "document_data_verification_request_failed";

    // VC Contra Indicators (CI is Appended)
    public static final String DRIVING_PERMIT_CI_PREFIX = "driving_permit_ci_";

    private Definitions() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }
}
