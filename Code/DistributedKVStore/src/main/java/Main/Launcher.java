package Main;

/*
 * Welcome to NetBeans...!!!
 */
import ClientNode.ClientParent;
import DKVNetwork.DKVAddress;
import DKVNetwork.DKVAddressConverter;
import DKVNetwork.DKVMessage;
import DKVNetwork.DKVPacketHeader;
import DKVSerializers.GETSerializer;
import DKVSerializers.GroupMembershipSerializer;
import DKVSerializers.HeartbeatSerializer;
import DKVSerializers.NetSerializer;
import DKVSerializers.PUTSerializer;
import DataStoreNode.NodeParent;
import Operations.GetRequest;
import Operations.GetResponse;
import Operations.HeartbeatRequest;
import Operations.HeartbeatResponse;
import Operations.JoinApprove;
import Operations.JoinRequest;
import Operations.PutRequest;
import Operations.PutResponse;
import TrackerNode.TrackerParent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import se.sics.kompics.Kompics;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.network.netty.serialization.Serializers;

/**
 *
 * @author admin
 */
public class Launcher {

    static {
        // register
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new GroupMembershipSerializer(), "gmpS");
        Serializers.register(new HeartbeatSerializer(), "hbS");
        Serializers.register(new PUTSerializer(), "putS");
        Serializers.register(new GETSerializer(), "getS");
        
        // map
        Serializers.register(DKVAddress.class, "netS");
        Serializers.register(DKVPacketHeader.class, "netS");
        Serializers.register(DKVMessage.class, "netS");

        Serializers.register(JoinRequest.class, "gmpS");
        Serializers.register(JoinApprove.class, "gmpS");
        
        Serializers.register(HeartbeatRequest.class, "hbS");
        Serializers.register(HeartbeatResponse.class, "hbS");
        
        Serializers.register(PutRequest.class, "putS");
        Serializers.register(PutResponse.class, "putS");
        
        Serializers.register(GetRequest.class, "getS");
        Serializers.register(GetResponse.class, "getS");

        // conversions
        Conversions.register(new DKVAddressConverter());
    }

    public static void main(String[] args) {
        
        try{
            // Read the tracker port and form its address object first
            InetAddress tracAddr = InetAddress.getByName(args[0]);
            int tracPort = Integer.parseInt(args[1]);
            DKVAddress trackAddr = new DKVAddress(tracAddr, tracPort);
            
            switch (args.length) {
            // Node
                case 4:
                    InetAddress nodeAddress = InetAddress.getByName(args[2]);
                    int nodePort = Integer.parseInt(args[3]);
                    DKVAddress nodeAddr = new DKVAddress(nodeAddress, nodePort);                    
                    Kompics.createAndStart(NodeParent.class, new NodeParent.Init(nodeAddr, trackAddr));
                    System.out.println("Starting Node");
                    break;

                case 2:                    
                    int nNodes = 8;
                    int fdTO = 4000;
                    Kompics.createAndStart(TrackerParent.class, new TrackerParent.Init(trackAddr, nNodes, fdTO));
                    System.out.println("Starting Tracker");
                    break;

                case 5:                    
                    InetAddress cAddr = InetAddress.getByName(args[2]);
                    int clientPort = Integer.parseInt(args[3]);
                    DKVAddress clientAddr = new DKVAddress(cAddr, clientPort);
                    int clientID = Integer.parseInt(args[4]);
                    boolean bPGReq = true;
                    boolean bCASReq = false;
                    Kompics.createAndStart(ClientParent.class, new ClientParent.Init(clientID, clientAddr,
                                                                                trackAddr, bPGReq, bCASReq));
                    System.out.println("Starting Client");
                    break;

                default:
                    System.err.println("Invalid number of parameters");
                    System.exit(1);
            }
        } catch(UnknownHostException ex) {   
        }
        
        try {
            Thread.sleep(3600000);
        } catch (InterruptedException ex) {
            System.exit(1);
        }
        
        Kompics.shutdown();
        System.exit(0);
    }
}