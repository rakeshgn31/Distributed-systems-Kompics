/*
 * Welcome to NetBeans...!!!
 */
package TrackerNode;

import DKVNetwork.DKVAddress;
import DKVNetwork.DKVMessage;
import DKVUtilities.OperationsHelper;
import DataStoreNode.Node;
import Operations.GetRequest;
import Operations.GetResponse;
import Operations.HeartbeatRequest;
import Operations.HeartbeatResponse;
import Operations.JoinApprove;
import Operations.JoinRequest;
import Operations.PutRequest;
import Operations.PutResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.Stop;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author admin
 */
class NodeInformation {
    
    int nNodeID;
    DKVAddress nodeAddress;    
    boolean bUnderReplicated;
    ArrayList<Integer> replicaNodeListID;
    
    public NodeInformation() {
        
        nNodeID = 0;
        bUnderReplicated = false;        
        replicaNodeListID = new ArrayList<>();
    }
}

public class Tracker extends ComponentDefinition {
    
    private static final Logger LOG = LoggerFactory.getLogger(Node.class);
    private static final int REPLICATION_DEGREE = 3;
    
    private static int PUT_REQ_COUNTER = 0;
    private static int GET_REQ_COUNTER = 0;
    private static int CAS_REQ_COUNTER = 0;
    private static short m_nNodeIDCounter = 0;
  
    private int m_fdTimeout;
    private final short m_nNumNodes; 
    private DKVAddress m_selfAddress;
     
    // Failure Detection required variables
    private UUID m_nTimeoutID;    
    private HashMap<Integer, NodeInformation> m_mapNodes;          // Union of correct and suspected nodes
    private HashMap<Integer, NodeInformation> m_mapFailedNodes;    // Nodes that did not respond to 2 successive heartbeats
    private ArrayList<Integer> m_arrCorrectNodes;           // Nodes that responded to the heartbeats in time
    private ArrayList<Integer> m_arrSuspectedNodes;         // Nodes that did not respond to hearbeats once
    
    // GET, PUT and CAS operations store counter
    private HashMap<Integer, Integer> m_mapGetRequests;
    private HashMap<Integer, Integer> m_mapPutRequests;
    private HashMap<Integer, Integer> m_mapCASRequests;
    
    Positive<Network> net = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);
    
    public Tracker(Init init) {
        
        m_selfAddress = init.selfAddress;
        m_fdTimeout = init.nFDTimeout;
        m_nNumNodes = (short) init.nNumOfNodes;
        
        m_mapNodes = new HashMap<>();
        m_mapFailedNodes = new HashMap<>();
        m_arrCorrectNodes = new ArrayList<>();
        m_arrSuspectedNodes = new ArrayList<>();        
        
        m_mapPutRequests = new HashMap<>();
        m_mapGetRequests = new HashMap<>();
        m_mapCASRequests = new HashMap<>();
        
        subscribe(startHandler, control);
        subscribe(stopHandler, control);
        subscribe(joinReqHandler, net);
        subscribe(hbRespHandler, net);
        subscribe(getReqHandler, net);
        subscribe(getRespHandler, net);
        subscribe(putReqHandler, net);
        subscribe(putRespHandler, net);
        subscribe(fdTimeoutHandler, timer);
    }
    
    private void broadcastHeartbeatRequests() {
        
        // Broadcast the heartbeat requests to both correct nodes and suspected nodes
        m_mapNodes.values().stream().forEach((node) -> {
            trigger(new DKVMessage(m_selfAddress, node.nodeAddress,
                    Transport.TCP, new HeartbeatRequest()), net);
        });        
    }
    
    @Override
    public void tearDown() {
        trigger(new CancelPeriodicTimeout(m_nTimeoutID), timer);
    }
        
    Handler<Start> startHandler = new Handler<Start>() {        
        @Override
        public void handle(Start event) {

            try{
                LOG.info("Tracker Node started and entering the sleep phase. ADDR.:" +
                                                m_selfAddress.getIp().toString());
                Thread.sleep(60000);
            } catch(InterruptedException ex) {}
            
            // Schedule a timeout for the specified amount of time
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, m_fdTimeout);
            FailureDetectorTimeout timeout = new FailureDetectorTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timer);
            m_nTimeoutID = timeout.getTimeoutId();
            broadcastHeartbeatRequests();                        
        }
    };
    
    Handler<Stop> stopHandler = new Handler<Stop>() {
        @Override
        public void handle(Stop event) {
            
            m_selfAddress = null;
            m_mapNodes.clear();
            m_arrSuspectedNodes.clear();
            m_mapFailedNodes.clear();
            
            tearDown();
        }                
    };

    ClassMatchedHandler<JoinRequest, DKVMessage> joinReqHandler = new ClassMatchedHandler<JoinRequest, DKVMessage>() {
        @Override
        public void handle(JoinRequest req, DKVMessage msg) {
            
            LOG.info("Received JOIN Req from " + msg.getSource().getIp().toString());
            if(m_nNodeIDCounter < m_nNumNodes) {
                
                m_nNodeIDCounter++;
                NodeInformation node = new NodeInformation();
                node.nNodeID = m_nNodeIDCounter;
                node.nodeAddress = msg.getSource();
                
                // Assign replica node IDs
                // If n = 5, n-1 = 4 then replicas for 4 will n+1 = 5 and 1
                if(m_nNodeIDCounter == (m_nNumNodes - 1) ) {
                    
                    node.replicaNodeListID.add(node.nNodeID + 1);
                    node.replicaNodeListID.add(1);
                } else if(m_nNodeIDCounter == m_nNumNodes) {
                    
                    node.replicaNodeListID.add(1);
                    node.replicaNodeListID.add(2);
                } else {
                    node.replicaNodeListID.add(node.nNodeID + 1);
                    node.replicaNodeListID.add(node.nNodeID + 2);
                }
                
                m_mapNodes.putIfAbsent(node.nNodeID, node);
                
                trigger(new DKVMessage(m_selfAddress, msg.getSource(), Transport.TCP,
                                                    new JoinApprove(m_nNodeIDCounter)), net);                
            } else {
                LOG.info("REJECTED JOIN Request from " + msg.getSource().getIp().toString());
            }
        }
    };
    
    ClassMatchedHandler<HeartbeatResponse, DKVMessage> hbRespHandler = new ClassMatchedHandler<HeartbeatResponse, DKVMessage>() {
        @Override
        public void handle(HeartbeatResponse resp, DKVMessage msg) {
            
            NodeInformation node = null;
            
            // Check if the node is in list of proper nodes or failed nodes
            if(m_mapNodes.containsKey(resp.NodeID)) {
            
                node = m_mapNodes.get(resp.NodeID);            
            } else if(m_mapFailedNodes.containsKey(resp.NodeID)) {
                
                node = m_mapFailedNodes.get(resp.NodeID);
                m_mapFailedNodes.remove(resp.NodeID);
            }
            
            if(node != null) {
                
                // If the object is in the list of suspected nodes remove it
                if(m_arrSuspectedNodes.contains(node.nNodeID)) {

                    m_arrSuspectedNodes.remove((Integer)node.nNodeID);
                }
                
                if( !m_arrCorrectNodes.contains(node.nNodeID)) {

                    m_arrCorrectNodes.add(node.nNodeID);
                }
            } else {
                if( !m_arrCorrectNodes.contains(resp.NodeID)) {
                    
                        m_arrCorrectNodes.add(resp.NodeID);
                    }
            }
        }
    };
    
    Handler<FailureDetectorTimeout> fdTimeoutHandler = new Handler<FailureDetectorTimeout>() {
        @Override
        public void handle(FailureDetectorTimeout event) {
            
            // Check which nodes did not respond and move them
            // to suspected or failed list accordingly
            for(int nodeID : m_mapNodes.keySet()) {
                
                // Did not respond to the request
                if( !m_arrCorrectNodes.contains(nodeID) ) {
                    
                    // Check if it did not respond to second successive request
                    if( !m_arrSuspectedNodes.contains(nodeID)) {
                        m_arrSuspectedNodes.add(nodeID);
                        LOG.info("Node with ID " + nodeID + " suspected.");
                    } else {
                        // Add it to the failed list of nodes
                        m_mapFailedNodes.putIfAbsent(nodeID, m_mapNodes.get(nodeID));
                        LOG.info("Node with ID " + nodeID + " moved to failed list.");
                        
                        // Remove from the suspected list and also from the complete nodes list
                        m_arrSuspectedNodes.remove((Integer)nodeID);
                        m_mapNodes.remove(nodeID);
                    }
                }
            }
            
            // Clear this round of correct nodes and start broadcast again
            m_arrCorrectNodes.clear();
            
            // Now broadcast again
            broadcastHeartbeatRequests();
        }
    };

    ClassMatchedHandler<GetRequest, DKVMessage> getReqHandler = new ClassMatchedHandler<GetRequest, DKVMessage>() {
        @Override
        public void handle(GetRequest req, DKVMessage msg) {
            
            LOG.info("Received GET request for the key: " + req.Key);
            int nNodeID = OperationsHelper.getNodeIDForKeyStorage(req.Key, m_nNumNodes);
            
            // Trigger requests to the corresponding node and its replicas
            ArrayList<Integer> arrNodes = new ArrayList<>();
            if(m_mapNodes.containsKey(nNodeID)) {
                arrNodes.add(nNodeID);
                for(int repNodeID : m_mapNodes.get(nNodeID).replicaNodeListID) {
                    if(m_mapNodes.containsKey(repNodeID)) {
                        arrNodes.add(repNodeID);
                    }
                }
            } else {
                if(m_mapFailedNodes.containsKey(nNodeID)) {                    
                    
                    for(int repNodeID : m_mapNodes.get(nNodeID).replicaNodeListID) {
                        if(m_mapNodes.containsKey(repNodeID)) {
                            arrNodes.add(repNodeID);
                        }
                    }                    
                }
            }
                
            // Majority
            if(arrNodes.size() >= 2) { 

                GET_REQ_COUNTER++;
                for(int nodeID : arrNodes) {
                    trigger(new DKVMessage(m_selfAddress, m_mapNodes.get(nodeID).nodeAddress, Transport.TCP,
                                                new GetRequest(GET_REQ_COUNTER, req.Key)), net);
                }

                // Store the request
                m_mapGetRequests.putIfAbsent(GET_REQ_COUNTER, 0);

                // If not all three nodes (Primary + 2 replicas) are alive
                if(arrNodes.size() != 3) {
                    LOG.info("Unable to deliver GET request to one of the replica.Still majority prevails");
                }
            } else {
                LOG.info("Unable to execute GET request as the partition is currently down.");
            }
        }
    };
    
    ClassMatchedHandler<GetResponse, DKVMessage> getRespHandler = new ClassMatchedHandler<GetResponse, DKVMessage>() {
        @Override
        public void handle(GetResponse resp, DKVMessage msg) {
            
            if(resp.opExitCode == OperationsHelper.OperationExitCode.OP_SUCCESS) {
                if(m_mapGetRequests.containsKey(resp.RequestID)) {
                    
                    int ack = m_mapGetRequests.get(resp.RequestID);
                    ack++;
                    m_mapGetRequests. put(resp.RequestID, ack);

                    if(m_mapGetRequests.get(resp.RequestID) >= 2) {

                        StringBuilder strBuilder = new StringBuilder();                    
                        for(String str : resp.listValues) {
                            strBuilder.append(str);
                            strBuilder.append(" ");
                        }

                        LOG.info("GET Response - Key: " + resp.Key + "  List of Values are: " + strBuilder.toString());
                        m_mapGetRequests.remove(resp.RequestID);
                    }
                }
            }
        }
    };
    
    ClassMatchedHandler<PutRequest, DKVMessage> putReqHandler = new ClassMatchedHandler<PutRequest, DKVMessage>() {
        @Override
        public void handle(PutRequest req, DKVMessage msg) {
            
         LOG.info("Received PUT request for the key: " + req.Key);
            int nNodeID = OperationsHelper.getNodeIDForKeyStorage(req.Key, m_nNumNodes);
            
            // Trigger requests to the corresponding node and its replicas
            ArrayList<Integer> arrNodes = new ArrayList<>();
            if(m_mapNodes.containsKey(nNodeID)) {
                arrNodes.add(nNodeID);
                for(int repNodeID : m_mapNodes.get(nNodeID).replicaNodeListID) {
                    if(m_mapNodes.containsKey(repNodeID)) {
                        arrNodes.add(repNodeID);
                    }
                }
            } else {
                if(m_mapFailedNodes.containsKey(nNodeID)) {                    
                    
                    for(int repNodeID : m_mapNodes.get(nNodeID).replicaNodeListID) {
                        if(m_mapNodes.containsKey(repNodeID)) {
                            arrNodes.add(repNodeID);
                        }
                    }                    
                }
            }
                
            // Majority
            if(arrNodes.size() >= 2) { 

                PUT_REQ_COUNTER++;
                for(int nodeID : arrNodes) {                    
                    trigger(new DKVMessage(m_selfAddress, m_mapNodes.get(nodeID).nodeAddress, Transport.TCP,
                                    new PutRequest(PUT_REQ_COUNTER, req.Key, req.listValues)), net);
                }

                // Store the request
                m_mapPutRequests.putIfAbsent(PUT_REQ_COUNTER, 0);

                // If not all three nodes (Primary + 2 replicas) are alive
                if(arrNodes.size() != 3) {
                    LOG.info("Unable to deliver PUT request to one of the replica.Still majority prevails");
                }
            } else {
                LOG.info("Unable to execute PUT request as the partition is currently down.");
            }
        }
    };
    
    ClassMatchedHandler<PutResponse, DKVMessage> putRespHandler = new ClassMatchedHandler<PutResponse, DKVMessage>() {
        @Override
        public void handle(PutResponse resp, DKVMessage msg) {
            
            if(resp.opExitCode == OperationsHelper.OperationExitCode.OP_SUCCESS) {
                if(m_mapPutRequests.containsKey(resp.RequestID)) {

                    int ack = m_mapPutRequests.get(resp.RequestID);
                    ack++;
                    m_mapPutRequests. put(resp.RequestID, ack);

                    if(m_mapPutRequests.get(resp.RequestID) >= 2) {

                        LOG.info("PUT Response - Key: " + resp.Key + "  stored successfully");
                        m_mapPutRequests.remove(resp.RequestID);
                    }
                }
            }
        }
    };
        
    public static class FailureDetectorTimeout extends Timeout {

        public FailureDetectorTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }
  
    public static class Init extends se.sics.kompics.Init<Tracker> {

        public final DKVAddress selfAddress;
        public final int nNumOfNodes;
        public final int nFDTimeout;

        public Init(DKVAddress self, int numNodes, int fdTimeout) {

            this.selfAddress = self;
            this.nNumOfNodes = numNodes;
            this.nFDTimeout = fdTimeout;            
        }
    }
}
