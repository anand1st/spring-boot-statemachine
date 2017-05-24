package org.test.spring.boot.statemachine.scorecard;

/**
 *
 * @author anand
 */
public enum ScorecardStates {
    START,
    CREATE_SCORECARD,
    WAITING_FOR_APPROVAL_SCORECARD,
    PROCESS_APPROVAL_STATUS_SCORECARD,
    WAITING_FOR_VERIFICATION_SCORECARD,
    PROCESS_VERIFICATION_STATUS_SCORECARD,
    PUBLISH_SCORECARD;
}
