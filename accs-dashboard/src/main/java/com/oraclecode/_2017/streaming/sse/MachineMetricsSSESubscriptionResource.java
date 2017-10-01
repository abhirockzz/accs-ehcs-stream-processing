package com.oraclecode._2017.streaming.sse;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 * This class allows clients to subscribe to events by
 * sending a HTTP GET to host:port/events. The server will keep the connection open
 * and send events (as and when received) unless closed by the client
 * 
 */
@Stateless
@Path("machine")
public class MachineMetricsSSESubscriptionResource {

    /**
     * Call me to subscribe to events
     * 
     * @return EventOutput which will keep the connection open
     */
    @Inject
    MachineMetricSSEChannel channel;
    
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe() {
        return channel.subscribe();
    }

}
