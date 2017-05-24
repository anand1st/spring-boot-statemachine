package org.test.spring.boot.statemachine.scorecard;

import java.util.Objects;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.redis.RedisStateMachinePersister;
import org.test.spring.boot.statemachine.StateMachineListener;

/**
 *
 * @author anand
 */
@Configuration
@lombok.extern.slf4j.Slf4j
public class ScorecardConfig {

    @Autowired
    private ApplicationContext appContext;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private StateMachineMonitor<ScorecardStates, ScorecardEvents> stateMachineMonitor;

    @Bean
    public StateMachinePersist<ScorecardStates, ScorecardEvents, String> stateMachinePersist() {
        return new RepositoryStateMachinePersist<>(new RedisStateMachineContextRepository<>(redisConnectionFactory));
    }

    @Bean
    public StateMachinePersister<ScorecardStates, ScorecardEvents, String> redisStateMachinePersister() {
        return new RedisStateMachinePersister<>(stateMachinePersist());
    }

    @Bean("scorecardStateMachineTarget")
    @Scope("prototype")
    public StateMachine<ScorecardStates, ScorecardEvents> stateMachineTarget() throws Exception {
        StateMachineBuilder.Builder<ScorecardStates, ScorecardEvents> builder
                = StateMachineBuilder.<ScorecardStates, ScorecardEvents>builder();
        builder.configureConfiguration().withMonitoring().monitor(stateMachineMonitor).and()
                .withConfiguration().beanFactory(appContext).listener(new StateMachineListener<>());
        builder.configureStates().withStates()
                .initial(ScorecardStates.START, action("start", "startUserObj"))
                .state(ScorecardStates.CREATE_SCORECARD, action("create scorecard", "createScorecardUserObj"))
                .state(ScorecardStates.WAITING_FOR_APPROVAL_SCORECARD,
                        action("waiting for approval", "waitingForApprovalUserObj"))
                .choice(ScorecardStates.PROCESS_APPROVAL_STATUS_SCORECARD)
                .state(ScorecardStates.WAITING_FOR_VERIFICATION_SCORECARD,
                        action("waiting for verification", "waitingForVerificationUserObj"))
                .choice(ScorecardStates.PROCESS_VERIFICATION_STATUS_SCORECARD)
                .end(ScorecardStates.PUBLISH_SCORECARD);
        builder.configureTransitions()
                .withExternal()
                .source(ScorecardStates.START)
                .event(ScorecardEvents.START_SCORECARD_CREATION)
                .target(ScorecardStates.CREATE_SCORECARD)
                .and()
                .withExternal()
                .source(ScorecardStates.CREATE_SCORECARD)
                .event(ScorecardEvents.USER_SUBMIT_SCORECARD)
                .target(ScorecardStates.WAITING_FOR_APPROVAL_SCORECARD)
                .and()
                .withExternal()
                .source(ScorecardStates.WAITING_FOR_APPROVAL_SCORECARD)
                .event(ScorecardEvents.APPROVAL_STATUS_SUBMITTED)
                .target(ScorecardStates.PROCESS_APPROVAL_STATUS_SCORECARD)
                .and()
                .withChoice()
                .source(ScorecardStates.PROCESS_APPROVAL_STATUS_SCORECARD)
                .first(ScorecardStates.WAITING_FOR_VERIFICATION_SCORECARD, verifyScoreCardGuard("ok"))
                .last(ScorecardStates.CREATE_SCORECARD)
                .and()
                .withExternal()
                .source(ScorecardStates.WAITING_FOR_VERIFICATION_SCORECARD)
                .event(ScorecardEvents.VERIFICATION_STATUS_SUBMITTED)
                .target(ScorecardStates.PROCESS_VERIFICATION_STATUS_SCORECARD)
                .and()
                .withChoice()
                .source(ScorecardStates.PROCESS_VERIFICATION_STATUS_SCORECARD)
                .first(ScorecardStates.PUBLISH_SCORECARD, verifyScoreCardGuard("ended"))
                .last(ScorecardStates.CREATE_SCORECARD);
        return builder.build();
    }

    @Bean
    public TargetSource poolTargetSource() {
        CommonsPool2TargetSource pool = new CommonsPool2TargetSource();
        pool.setMaxSize(3);
        pool.setTargetBeanName("scorecardStateMachineTarget");
        pool.setTargetClass(StateMachine.class);
        return pool;
    }

    @Bean
    public ProxyFactoryBean stateMachine() {
        ProxyFactoryBean pfb = new ProxyFactoryBean();
        pfb.setTargetSource(poolTargetSource());
        return pfb;
    }

    private Action<ScorecardStates, ScorecardEvents> action(String action, String userObj) {
        return context -> {
            context.getExtendedState().getVariables().put("userObj", userObj);
            context.getExtendedState().getVariables().put("action", action);
        };
    }

    private Guard<ScorecardStates, ScorecardEvents> verifyScoreCardGuard(String userObj) {
        return context -> {
            if (Objects.nonNull(userObj)) {
                context.getExtendedState().getVariables().put("userObj", userObj);
            }
            boolean isApproved = (boolean) context.getMessageHeader("isApproved");
            log.info("Is Approved? {}", isApproved ? "Yes" : "No");
            return isApproved;
        };
    }
}
