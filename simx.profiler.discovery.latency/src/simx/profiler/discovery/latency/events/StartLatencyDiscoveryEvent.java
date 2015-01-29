/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simx.profiler.discovery.latency.events;

import simx.profiler.model.ActorInstance;

/**
 *
 * @author Stephan Rehfeld
 */
public class StartLatencyDiscoveryEvent {
    public final ActorInstance actorInstance;
    
    public StartLatencyDiscoveryEvent( final ActorInstance actorInstance ) {
        this.actorInstance = actorInstance;
    }
}
