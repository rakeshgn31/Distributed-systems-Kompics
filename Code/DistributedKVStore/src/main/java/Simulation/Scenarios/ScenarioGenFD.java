/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Scenarios;

import DKVNetwork.DKVAddress;
import DataStoreNode.NodeParent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.util.GlobalView;
import static Simulation.Scenarios.ScenarioGen1.nNumNodes;
import static Simulation.Scenarios.ScenarioGen1.nFDTimout;
import TrackerNode.TrackerParent;

/**
 *
 * @author aruna
 */
public class ScenarioGenFD {

    static Operation setupOp = new Operation<SetupEvent>() {
        @Override
        public SetupEvent generate() {
            return new SetupEvent() {
              @Override
                public void setupGlobalView(GlobalView gv) {
                    gv.setValue("simulation.putrequest", 0);
                }  
            };
        }
        
    };    
    static Operation startObserverOp = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            return new StartNodeEvent() {
                DKVAddress selfAdr;
                {
                    try {
                        selfAdr = new DKVAddress(InetAddress.getByName("0.0.0.0"), 0);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("dkvstore.simulation.checktimeout", 4000);
                    return config;
                }
                
                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return SimulationObserver.class;
                }

                @Override
                public Init getComponentInit() {
                    return new SimulationObserver.Init(2);
                }
            };
        }
    };    
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
    static Operation1 startNodeOp=new Operation1<StartNodeEvent,Integer>() {
        //stratnodeevent 
        @Override
        public StartNodeEvent generate(final Integer self) {
         return new StartNodeEvent() {
                DKVAddress selfAdr;
                DKVAddress TrackerAdr;
                {
                    try {
                        selfAdr = new DKVAddress(InetAddress.getByName("192.193.0." + self), 10000);
                        TrackerAdr=new DKVAddress(InetAddress.getByName("192.193.0.1"), 10000);
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
                 
                 return new NodeParent.Init(selfAdr, TrackerAdr);
             }            
          };
      }
    };   
    static Operation1 killNodeOp = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(final Integer self) {
            return new KillNodeEvent() {
                DKVAddress selfAdr;

                {
                    try {
                        selfAdr = new DKVAddress(InetAddress.getByName("192.193.0." + self), 10000);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                
                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }
                
                @Override
                public String toString() {
                    return "KillNode<" + selfAdr.toString() + ">";
                }
            };
        }
    };
    
    public static SimulationScenario simulateFD() {
        SimulationScenario scen = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess setup = new SimulationScenario.StochasticProcess() {
                    {
                        raise(1, setupOp);
                    }
                };
                SimulationScenario.StochasticProcess observer = new SimulationScenario.StochasticProcess() {
                    {
                        raise(1, startObserverOp);
                    }
                };
                SimulationScenario.StochasticProcess tracker= new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startTrackerOp, new BasicIntSequentialDistribution(1));
                    }
                };
                SimulationScenario.StochasticProcess node = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(nNumNodes, startNodeOp, new BasicIntSequentialDistribution(6));
                    }
                };                
                SimulationScenario.StochasticProcess killer = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(2, killNodeOp, new BasicIntSequentialDistribution((6)));
                    }  
                    
                };
                SimulationScenario.StochasticProcess node1 = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startNodeOp, new BasicIntSequentialDistribution(6));
                    }
                };
                
                setup.start();
                observer.startAfterTerminationOf(0, setup);
                tracker.startAfterTerminationOf(1000, observer);
                node.startAfterTerminationOf(1000,tracker);
                killer.startAfterTerminationOf(4000, node);
                node1.startAfterTerminationOf(4000, killer);
                terminateAfterTerminationOf(10000,tracker);
            }
        };

        return scen;
    }            
}
