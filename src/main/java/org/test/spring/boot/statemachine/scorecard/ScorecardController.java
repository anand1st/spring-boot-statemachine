package org.test.spring.boot.statemachine.scorecard;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author anand
 */
@RestController
public class ScorecardController {

    @Autowired
    private StateMachine<ScorecardStates, ScorecardEvents> stateMachine;
    @Autowired
    private StateMachinePersister<ScorecardStates, ScorecardEvents, String> persister;

    @PostMapping("/state")
    public String changeState(@RequestBody @Valid ScorecardStateDto scorecardState) throws Exception {
        persister.restore(stateMachine, scorecardState.getUserId());
        stateMachine.sendEvent(scorecardState.getScorecardEvents());
        String userObj = stateMachine.getExtendedState().get("userObj", String.class);
        persister.persist(stateMachine, scorecardState.getUserId());
        return userObj;
    }

    @PostMapping("/approve/{isApprove}")
    public String approve(@PathVariable("isApprove") boolean isApproved,
            @RequestBody @Valid ScorecardStateDto scorecardState) throws Exception {
        persister.restore(stateMachine, scorecardState.getUserId());
        Message<ScorecardEvents> message = MessageBuilder.withPayload(scorecardState.getScorecardEvents())
                .setHeader("isApproved", isApproved).build();
        stateMachine.sendEvent(message);
        String userObj = stateMachine.getExtendedState().get("userObj", String.class);
        persister.persist(stateMachine, scorecardState.getUserId());
        return userObj;
    }
}
