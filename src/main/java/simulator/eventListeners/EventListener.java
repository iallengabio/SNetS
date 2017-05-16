package simulator.eventListeners;

import simulator.Event;


/**
 * Interface das classes que lidam com a execu��o dos eventos no simulador alterando o estado do sistema
 *
 * @author Iallen
 */
public interface EventListener {

    /**
     * executa um determinado evento 'e'.
     *
     * @param e
     */
    public abstract void execute(Event e);
}

