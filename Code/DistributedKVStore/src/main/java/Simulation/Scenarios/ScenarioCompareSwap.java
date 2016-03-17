/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Scenarios;

import ClientNode.ClientParent;
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
public class ScenarioCompareSwap {
    
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
                        selfAdr = new DKVAddress(InetAddress.getByName("192.193.0.1"), 10000);
                        
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
    
   static Operation1 startNodeOp=new Operation1<StartNodeEvent,Integer>() {
        //stratnodeevent 
        @Override
        public StartNodeEvent generate(final Integer self) {
         return new StartNodeEvent() {
                DKVAddress selfAdr;
                DKVAddress trackerAdr;                
                {
                    try {
                        
                        selfAdr = new DKVAddress(InetAddress.getByName("192.193.0." + self), 10000);
                        trackerAdr=new DKVAddress(InetAddress.getByName("192.193.0.1"), 10000);
                        
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
       
   static Operation1 startClientOp=new Operation1<StartNodeEvent,Integer>() {
        //stratnodeevent 
        @Override
        public StartNodeEvent generate(final Integer self) {
         return new StartNodeEvent() {
                DKVAddress selfAdr;
                DKVAddress trackerAdr;
                {
                    try {
                        
                        selfAdr = new DKVAddress(InetAddress.getByName("192.193.0.0" + self), 10000);
                        trackerAdr=new DKVAddress(InetAddress.getByName("192.193.0.1"), 10000);
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
                 
              return ClientParent.class;   
             }

             @Override
             public Init getComponentInit() {
                 
                 return new ClientParent.Init(0, selfAdr, trackerAdr, true, false);
             }
          };
      }   
    };
   
    public static SimulationScenario simpleCompareSwapScenario() {
        
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
                
                SimulationScenario.StochasticProcess client = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };

                tracker.start();
                node.startAfterTerminationOf(1000, tracker);
                client.startAfterTerminationOf(1000,node);
                terminateAfterTerminationOf(2000, tracker);
            }
        };

        return scen;
    }   
}
