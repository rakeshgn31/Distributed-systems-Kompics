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
public class ScenarioGMPLauncher {
    
     public static void main(String[] args) {
         
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario simleGMPScenario = ScenarioGen1.simpleGMPScenario();
        simleGMPScenario.simulate(LauncherComp.class);
    }
}
