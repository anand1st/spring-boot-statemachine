package org.test.spring.boot.statemachine.scorecard;

import javax.validation.constraints.NotNull;

/**
 *
 * @author anand
 */
@lombok.Data
public class ScorecardStateDto {
    
    @NotNull
    private String userId;
    @NotNull
    private ScorecardEvents scorecardEvents;
}
