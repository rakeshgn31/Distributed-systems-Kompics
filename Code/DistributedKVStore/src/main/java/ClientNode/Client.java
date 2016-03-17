/*
 * Welcome to NetBeans...!!!
 */
package ClientNode;

import DKVNetwork.DKVAddress;
import DKVNetwork.DKVMessage;
import Operations.GetRequest;
import Operations.PutRequest;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author admin
 */
public class Client extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    Positive<Network> net = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);

    private final int m_nClientID;
    private final DKVAddress selfAddress;
    private final DKVAddress trackerAddress;
    
    // For simulation scenarios only
    private final boolean m_bPutGetRequest;
    private final boolean m_bCASOpRequest;

    public Client(Init init) {

        this.m_nClientID = init.nClientID;
        this.selfAddress = init.self;
        this.trackerAddress = init.tracker;
        
        m_bPutGetRequest = init.bPutGetRequest;
        m_bCASOpRequest = init.bCASOpRequest;
        
        // Subscribe to the required ports
        {
            subscribe(startHandler, control);
            subscribe(stopHandler, control);
        }
    }
    
    private void raiseOperationRequestEvents() {
    
        if( m_bPutGetRequest && !m_bCASOpRequest ) {
            
            LOG.info("Client ID: " + m_nClientID + ". Sending PUT requests to application");
            
            // Generate data for Put and raise PUT and GET requests accordingly
            ArrayList<String> arrValues1 = new ArrayList();
            arrValues1.add("Delhi"); arrValues1.add("Mumbai"); arrValues1.add("Bangalore"); arrValues1.add("Chennai");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "India", arrValues1)), net);
           
            ArrayList<String> arrValues2 = new ArrayList();
            arrValues2.add("New York");  arrValues2.add("Los Angeles");  arrValues2.add("Chicago");  arrValues2.add("SFO");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "USA", arrValues2)), net);
         
            ArrayList<String> arrValues3 = new ArrayList();
            arrValues3.add("Stockholm");  arrValues3.add("Gothenburg");  arrValues3.add("Malmo");  arrValues3.add("Uppsala");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "Sweden", arrValues3)), net);
            
            ArrayList<String> arrValues4 = new ArrayList();
            arrValues4.add("London");  arrValues4.add("Manchester");  arrValues4.add("Liverpool");  arrValues4.add("Glasgow");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "UK", arrValues4)), net);
            
            ArrayList<String> arrValues5 = new ArrayList();
            arrValues5.add("Sidney");  arrValues5.add("Melbourne");  arrValues5.add("Brisbane");  arrValues5.add("Perth");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "Australia", arrValues5)), net);

            ArrayList<String> arrValues6 = new ArrayList();            
            arrValues6.add("Cape Town");  arrValues6.add("Cairo");  arrValues6.add("Nairobi");  arrValues6.add("Tunis");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "Africa", arrValues6)), net);
            
            ArrayList<String> arrValues7 = new ArrayList();
            arrValues7.add("Beijing");  arrValues7.add("Hong Kong");  arrValues7.add("Tianjin");  arrValues7.add("Chengdu");
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new PutRequest(0, "China", arrValues7)), net);
            
            // Generate GET Requests now
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "India")), net);
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "Australia")), net);
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "Sweden")), net);
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "UK")), net);
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "USA")), net);
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "China")), net);
            trigger(new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetRequest(0, "Africa")), net);

        } else if( !m_bPutGetRequest && m_bCASOpRequest ) {
            
            // Generate data for Put, raise PUT requests and then trigger CAS requests accordingly
            
        } else {
            LOG.info("Invalid option supplied to Client.");
        }
    }
        
    Handler<Start> startHandler = new Handler<Start>() {

        @Override
        public void handle(Start event) {

            LOG.info("Started client with ID: " + m_nClientID);
            raiseOperationRequestEvents();
        }
    };
    
    Handler<Stop> stopHandler = new Handler<Stop>() {

        @Override
        public void handle(Stop event) {}
    };    

    public static class Init extends se.sics.kompics.Init<Client> {

        public final int nClientID;
        public final DKVAddress self;
        public final DKVAddress tracker;
        
        public final boolean bPutGetRequest;
        public final boolean bCASOpRequest;

        public Init(int nClientID, DKVAddress self, DKVAddress tracker, boolean bPGReq, boolean bCASReq) {

            this.nClientID = nClientID;
            this.self = self;
            this.tracker = tracker;
            
            this.bPutGetRequest = bPGReq;
            this.bCASOpRequest = bCASReq;
        }
    }
}