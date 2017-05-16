package simulator;

import simulator.eventListeners.EventListener;

import java.io.Serializable;


@SuppressWarnings("serial")
public class Event implements Serializable {

    private Object object;
    private EventListener eventListener;
    private double timeHours;
    private double id;

    /**
     * Constroi um evento.
     *
     * @param r         Object
     * @param eListener EventListener
     * @param timeHours double
     */
    public Event(Object r, EventListener eListener, double timeHours) {
        this.object = r;
        this.eventListener = eListener;
        this.timeHours = timeHours;
    }

    public void setId(double x) {
        id = x;
    }

    public double getId() {
        return id;
    }
//------------------------------------------------------------------------------

    /**
     * Retorna qual é o escutador do evento.
     *
     * @return EventListener
     */
    public EventListener listener() {
        return this.eventListener;
    }

//------------------------------------------------------------------------------

    /**
     * Retorna o tempo que o evento será disparado em horas.
     *
     * @return double
     */
    public double getTimeHours() {
        return this.timeHours;
    }

//------------------------------------------------------------------------------

    /**
     * Retorna o Objeto associado ao evento.
     *
     * @return Object
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * setObject
     *
     * @param x Object
     */
    public void setObject(Object x) {
        this.object = x;
    }

}

