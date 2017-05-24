package org.test.spring.boot.statemachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.transition.Transition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author anand
 * @param <S>
 * @param <E>
 */
@SpringBootApplication
@EnableTransactionManagement
@lombok.extern.slf4j.Slf4j
public class ApplicationInitializer<S, E> extends WebMvcConfigurerAdapter {

    public static void main(String... args) {
        SpringApplication.run(ApplicationInitializer.class, args);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public StateMachineMonitor<S, E> stateMachineMonitor() {
        return new AbstractStateMachineMonitor<S, E>() {

            @Override
            public void transition(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration) {
                log.info("State Machine: {}, Transition: {} -> {} , Duration: {} ms", stateMachine.getUuid(),
                        transition.getSource().getId(), transition.getTarget().getId(), duration);
            }

            @Override
            public void action(StateMachine<S, E> stateMachine, Action<S, E> action, long duration) {
                log.info("State Machine: {}, Action: {}, Duration: {} ms", stateMachine.getUuid(),
                        stateMachine.getExtendedState().get("action", String.class), duration);
            }
        };
    }
}
