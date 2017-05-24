package org.test.spring.boot.statemachine;

import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 *
 * @author anand
 * @param <S>
 * @param <E>
 */
@lombok.extern.slf4j.Slf4j
public class StateMachineListener<S, E> extends StateMachineListenerAdapter<S, E> {

    @Override
    public void stateChanged(State<S, E> from, State<S, E> to) {
        log.info("Transitioning from {} to {}", from == null ? "null" : from.getId(), to.getId());
    }
}
