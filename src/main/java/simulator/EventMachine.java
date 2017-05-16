package simulator;

import network.Circuit;

import java.io.Serializable;
import java.util.Vector;

@SuppressWarnings("serial")
public class EventMachine implements Serializable {
    private Vector<Event> eventList;
    private double countEvent = 0;

    public EventMachine() {
        this.eventList = new Vector<Event>();
    }

//------------------------------------------------------------------------------

    /**
     * Insere um evento na máquina de eventos.
     *
     * @param e Event
     */
    public void insert(Event e) {
        e.setId(this.countEvent);
        this.countEvent++;
        int i = 0;
        while (i < eventList.size() &&
                (eventList.elementAt(i)).getTimeHours() < e.getTimeHours()) {
            i++;
        }
        eventList.insertElementAt(e, i);
    }

//------------------------------------------------------------------------------

    /**
     * Inicia a execução da máquina de eventos. A máquina
     * de eventos executa até não existir mais eventos
     * no eventList.
     */
    public void executeEvents() {
        while (eventList.size() > 0) {
            Event e = eventList.firstElement();
            eventList.removeElementAt(0);
            e.listener().execute(e);
        }
    }

    /**
     * Retorna o número de eventos agendados (existente no eventList).
     *
     * @return int
     */
    public int size() {
        return this.eventList.size();
    }

    /**
     * Finaliza a máquina de eventos (limpa eventList).
     */
    public void stopMachine() {
        this.eventList.removeAllElements();
    }

    /**
     * remove o evento que contem a requisição
     *
     * @param request RequestMother
     */
    public void remove(Circuit request) {
        for (int i = 0; i < this.eventList.size(); i++) {
            Event event = this.eventList.get(i);
            if (event.getObject() instanceof Circuit)
                if (event.getObject() == request) {
                    this.eventList.remove(event);
                    break;
                }
        }
    }
}

