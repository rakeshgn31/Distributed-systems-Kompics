/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Scenarios;


import java.util.UUID;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author aruna
 */
public class SimulationObserver extends ComponentDefinition{
    private static final Logger LOG = (Logger) LoggerFactory.getLogger(SimulationObserver.class);
    
    Positive<Timer> timer = requires(Timer.class);
    Positive<Network> network = requires(Network.class);

    
    private final int minDeadNodes;

    private UUID timerId;

    public SimulationObserver(Init init) {
        
        minDeadNodes = init.minDeadNodes;

        subscribe(handleStart, control);
        subscribe(handleCheck, timer);
    }
    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            schedulePeriodicCheck();
        }
       
    };
      Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
            
            
            if(gv.getDeadNodes().size() > minDeadNodes) {
                
                StringBuilder builder = new StringBuilder();
                builder.append("Terminating simulation as the min dead nodes: ");
                builder.append(minDeadNodes);
                builder.append("is achieved");                
                LOG.info(builder.toString());
                
                gv.terminate();
            }
        }
    };
      public static class Init extends se.sics.kompics.Init<SimulationObserver> {

       // public final int minPings;
        public final int minDeadNodes;

        public Init(int minDeadNodes) {
            //this.minPings = minPings;
            this.minDeadNodes = minDeadNodes;
        }
    }
        private void schedulePeriodicCheck() {
            
            long period = config().getValue("dkvstore.simulation.checktimeout", Long.class);
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(period, period);
            CheckTimeout timeout = new CheckTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            timerId = timeout.getTimeoutId();
        }
     public static class CheckTimeout extends Timeout {

        public CheckTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }
}
