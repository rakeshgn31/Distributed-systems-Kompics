/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Scenarios;

import DKVNetwork.DKVAddress;
import DataStoreNode.NodeParent;
import TrackerNode.TrackerParent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.StartNodeEvent;

/**
 *
 * @author aruna
 */
public class ScenarioGen1 {
    
    public static int nNumNodes = 5;
    public static int nFDTimout = 4000;
    
    static Operation1 startTrackerOp = new Operation1<StartNodeEvent, Integer>() {
        
        //stratnodeevent 
        @Override
        public StartNodeEvent generate(final Integer node) {
         return new StartNodeEvent() {
                DKVAddress selfAdr;
                
                {
                    try {
                        selfAdr = new DKVAddress(InetAddress.getByName("localhost"), 35000);
                        
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

             @Override
             public Address getNodeAddress() {
                 return selfAdr;
                 
             }

             @Override
             public Class<? extends ComponentDefinition> getComponentDefinition() {
                 
              return TrackerParent.class;   
             }

             @Override
             public Init getComponentInit() {
                 
                 return new TrackerParent.Init(selfAdr, nNumNodes, nFDTimout);
             }
              @Override
                public String toString() {
                    return "StartTracker<" + selfAdr.toString() + ">";
                }
          };
      }
    };
    
   static Operation1 startNodeOp = new Operation1<StartNodeEvent,Integer>() {
        //stratnodeevent 
        @Override
        public StartNodeEvent generate(final Integer self) {
         return new StartNodeEvent() {
                DKVAddress selfAdr;
                DKVAddress trackerAdr;
                {
                    try {
                        selfAdr = new DKVAddress(InetAddress.getByName("localhost"), 35000 + self);
                        trackerAdr=new DKVAddress(InetAddress.getByName("localhost"), 35000);
                        
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }   

             @Override
             public Address getNodeAddress() {
                 return selfAdr;
             }

             @Override
             public Class<? extends ComponentDefinition> getComponentDefinition() {
                 
              return NodeParent.class;   
             }

             @Override
             public Init getComponentInit() {
                 
                 return new NodeParent.Init(selfAdr, trackerAdr);
             }            
          };
      }      
    };
       
    public static SimulationScenario simpleGMPScenario() {      
        SimulationScenario scen = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess tracker = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startTrackerOp, new BasicIntSequentialDistribution(1));
                    }
                };
                
                SimulationScenario.StochasticProcess node;
                node = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(nNumNodes, startNodeOp, new BasicIntSequentialDistribution(6));
                    }
                };

                tracker.start();
                node.startAfterTerminationOf(1000, tracker);
                terminateAfterTerminationOf(10000, tracker);
            }
        };

        return scen;
    }    
}
