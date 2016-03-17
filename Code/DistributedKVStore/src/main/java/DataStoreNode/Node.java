/*
 * Welcome to NetBeans...!!!
 */
package DataStoreNode;

import DKVNetwork.DKVAddress;
import DKVNetwork.DKVMessage;
import DKVUtilities.OperationsHelper;
import Operations.CASRequest;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;

/**
 *
 * @author admin
 */
public class Node extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Node.class);

    Positive<Network> net = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);

    private final DKVAddress selfAddress;
    private final DKVAddress trackerAddress;

    private int m_nNodeID;
    private HashMap<String, ArrayList<String>> m_mapNodeData;

    public Node(Init init) {

        m_nNodeID = 0;
        this.selfAddress = init.self;
        this.trackerAddress = init.tracker;

        m_mapNodeData = new HashMap<>();
        
        // Subscribe to the required ports
        {
            subscribe(startHandler, control);
            subscribe(joinApprHandler, net);
            subscribe(hbReqHandler, net);
            subscribe(putReqHandler, net);
            subscribe(getReqHandler, net);
        }
    }

    Handler<Start> startHandler = new Handler<Start>() {

        @Override
        public void handle(Start event) {
            
            LOG.info("Sending JOIN REQ to tracker. Address - " + selfAddress.getIp().toString());
            trigger( new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new JoinRequest()), net );
        }
    };

    ClassMatchedHandler<JoinApprove, DKVMessage> joinApprHandler = new ClassMatchedHandler<JoinApprove, DKVMessage>() {
        @Override
        public void handle(JoinApprove content, DKVMessage context) {

            m_nNodeID = content.nNodeID;
            LOG.info("JOIN Request Approved. Assigned ID - " + m_nNodeID);
        }
    };

    ClassMatchedHandler<HeartbeatRequest, DKVMessage> hbReqHandler = new ClassMatchedHandler<HeartbeatRequest, DKVMessage>() {
        @Override
        public void handle(HeartbeatRequest req, DKVMessage msg) {

            LOG.info("Received Heartbeat request from tracker");
            trigger( new DKVMessage(selfAddress, trackerAddress, Transport.TCP, 
                                            new HeartbeatResponse(m_nNodeID)), net );
        }
    };

    ClassMatchedHandler<PutRequest, DKVMessage> putReqHandler = new ClassMatchedHandler<PutRequest, DKVMessage>() {
        @Override
        public void handle(PutRequest req, DKVMessage msg) {

            LOG.info("Received PUT Request to store Key: " + req.Key);

            try {
                // Check if the key already exists and then add/update accordingly
                m_mapNodeData.put(req.Key, req.listValues);
                trigger( new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new PutResponse(
                            req.RequestID, req.Key, OperationsHelper.OperationExitCode.OP_SUCCESS)), net );
                
            } catch(Exception ex) {
                trigger( new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new PutResponse(
                            req.RequestID, req.Key, OperationsHelper.OperationExitCode.OP_EXCEPTION)), net);
            }
        }
    };

    ClassMatchedHandler<GetRequest, DKVMessage> getReqHandler = new ClassMatchedHandler<GetRequest, DKVMessage>() {
        @Override
        public void handle(GetRequest req, DKVMessage msg) {
            
            LOG.info("Received GET Request to retrieve Key: " + req.Key);
            if(m_mapNodeData.containsKey(req.Key)) {
                
                trigger( new DKVMessage(selfAddress, trackerAddress, Transport.TCP, new GetResponse(
                        req.RequestID, req.Key, m_mapNodeData.get(req.Key),
                        OperationsHelper.OperationExitCode.OP_SUCCESS)), net);
            }
        }
    };
    
    ClassMatchedHandler<CASRequest, DKVMessage> casReqHandler = new ClassMatchedHandler<CASRequest, DKVMessage>() {
        @Override
        public void handle(CASRequest v, DKVMessage e) {
            // trigger
        }
    };
    
    public static class Init extends se.sics.kompics.Init<Node> {

        public final DKVAddress self;
        public final DKVAddress tracker;

        public Init(DKVAddress self, DKVAddress tracker) {

            this.self = self;
            this.tracker = tracker;
        }
    }
}
