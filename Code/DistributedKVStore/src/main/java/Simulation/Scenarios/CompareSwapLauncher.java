/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulation.Scenarios;

import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 *
 * @author aruna
 */
public class CompareSwapLauncher {
    
      public static void main(String[] args) {
          
        long seed = 123;
        SimulationScenario.setSeed(seed);
        
        // SimplePutRequestScenario is same as get 
        // there is no difference so same method is invoked here
        SimulationScenario killPongersScenario = ScenarioCompareSwap.simpleCompareSwapScenario();
        killPongersScenario.simulate(LauncherComp.class);
    }  
}
