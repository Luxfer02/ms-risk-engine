package es.NTTEnterprise.RIntellix.ms_risk_engine.utils;

/**
 * Constants for model payload field names used in mapper classes.
 * Centralizes all English field names to prevent hardcoding and enable easy
 * maintenance.
 *
 * @author Lucía Fernández Mancebo
 * @date 05/09/2026
 */
public final class ModelPayloadFieldNames {

    private ModelPayloadFieldNames() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    // ============================================================
    // DEMOGRAPHIC FIELDS
    // ============================================================

    /** Age field name in model payload. */
    public static final String FIELD_AGE = "age";

    /** Gender field name in model payload. */
    public static final String FIELD_GENDER = "gender";

    /** Marital status field name in model payload. */
    public static final String FIELD_MARITAL_STATUS = "maritalStatus";

    /** Education level field name in model payload. */
    public static final String FIELD_EDUCATION = "education";

    /** Employment status field name in model payload. */
    public static final String FIELD_EMPLOYMENT_STATUS = "employmentStatus";

    /** Occupation sector field name in model payload. */
    public static final String FIELD_OCCUPATION_SECTOR = "occupationSector";

    /** Number of dependents field name in model payload. */
    public static final String FIELD_DEPENDENTS = "dependents";

    // ============================================================
    // HOUSING & MORTGAGE FIELDS
    // ============================================================

    /** Home ownership type field name in model payload. */
    public static final String FIELD_HOME_OWNERSHIP = "homeOwnership";

    /** Property value field name in model payload. */
    public static final String FIELD_PROPERTY_VALUE = "propertyValue";

    /** Has mortgage flag field name in model payload. */
    public static final String FIELD_HAS_MORTGAGE = "hasMortgage";

    // ============================================================
    // INCOME FIELDS
    // ============================================================

    /** Annual income field name in model payload. */
    public static final String FIELD_ANNUAL_INCOME = "annualIncome";

    // ============================================================
    // LOAN FIELDS
    // ============================================================

    /** Loan type field name in model payload. */
    public static final String FIELD_LOAN_TYPE = "loanType";

    /** Loan purpose field name in model payload. */
    public static final String FIELD_PURPOSE = "purpose";

    /** Loan amount field name in model payload. */
    public static final String FIELD_LOAN_AMOUNT = "loanAmount";

    /** Loan term in months field name in model payload. */
    public static final String FIELD_TERM_MONTHS = "termMonths";

    /** Interest rate field name in model payload. */
    public static final String FIELD_INTEREST_RATE = "interestRate";

    // ============================================================
    // FINANCIAL METRICS
    // ============================================================

    /** Loan-to-value ratio field name in model payload. */
    public static final String FIELD_LTV = "ltv";

    /** Debt-to-income ratio field name in model payload. */
    public static final String FIELD_DTI = "dti";

    /** Existing obligations field name in model payload. */
    public static final String FIELD_EXISTING_OBLIGATIONS = "existingObligations";

    // ============================================================
    // CREDIT HISTORY FIELDS
    // ============================================================

    /** Number of previous loans field name in model payload. */
    public static final String FIELD_PREVIOUS_LOANS_COUNT = "previousLoansCount";

    /** Number of previous defaults field name in model payload. */
    public static final String FIELD_PREVIOUS_DEFAULTS_COUNT = "previousDefaultsCount";

    // ============================================================
    // CREDIT CARD MODEL PAYLOAD FIELDS
    // ============================================================

    /** Request type field name in credit card model payload. */
    public static final String FIELD_REQUEST_TYPE = "request_type";

    /** Request ID field name in credit card model payload. */
    public static final String FIELD_REQUEST_ID = "request_id";

    /** Employment seniority years field name in credit card model payload. */
    public static final String FIELD_EMPLOYMENT_SENIORITY_YEARS = "employmentSeniorityYears";

    /** Employment seniority months field name in credit card model payload. */
    public static final String FIELD_EMPLOYMENT_SENIORITY_MONTHS = "employmentSeniorityMonths";

    /** Income type field name in credit card model payload. */
    public static final String FIELD_INCOME_TYPE = "incomeType";

    /** Credit limit field name in credit card model payload. */
    public static final String FIELD_CREDIT_LIMIT = "creditLimit";

    /** Is revolving credit card field name in credit card model payload. */
    public static final String FIELD_IS_REVOLVING = "isRevolving";

    /** LTI field name in credit card model payload. */
    public static final String FIELD_LTI = "lti";

}
