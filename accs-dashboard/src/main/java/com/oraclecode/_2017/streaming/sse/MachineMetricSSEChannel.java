package com.oraclecode._2017.streaming.sse;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;

/**
 * Just a (singleton) wrapper on top of Jersey SseBroadcaster 
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class MachineMetricSSEChannel {

    private static final Logger LOGGER = Logger.getLogger(MachineMetricSSEChannel.class.getName());

    public MachineMetricSSEChannel() { 
    }

    /**
     * SseBroadcaster
     * 1. tracks client stats
     * 2. automatically dispose server resources if clients disconnect
     * 3. is thread safe
     */
    private final SseBroadcaster broadcaster = new SseBroadcaster();

    /**
     * add to SSE broadcaster list of clients/subscribers
     * @return EventOutput which will keep the connection open.
     */
    public EventOutput subscribe() {
        final EventOutput eOutput = new EventOutput();
        broadcaster.add(eOutput);
        LOGGER.log(Level.INFO, "Client Subscribed successfully {0}", eOutput.toString());
        return eOutput;
    }


    public void broadcast(String name, String metric) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder
                                        //.name(name)
                                        .data(String.class, metric)
                                        .mediaType(MediaType.TEXT_PLAIN_TYPE)
                                        .build();
        
        broadcaster.broadcast(event);
        LOGGER.log(Level.INFO, "Broadcasted machine metric "+ metric);
    }
}
