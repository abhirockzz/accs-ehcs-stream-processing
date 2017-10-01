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
public class LeaderboardSSEChannel {

    private static final Logger LOGGER = Logger.getLogger(LeaderboardSSEChannel.class.getName());

    public LeaderboardSSEChannel() { 
    }

    private final SseBroadcaster broadcaster = new SseBroadcaster();
    
    public EventOutput subscribe() {
        final EventOutput eOutput = new EventOutput();
        broadcaster.add(eOutput);
        LOGGER.log(Level.INFO, "Client Subscribed successfully {0}", eOutput.toString());
        return eOutput;
    }

    public void broadcast(String name, String data) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder
                                        //.name(name)
                                        .data(String.class, data)
                                        .mediaType(MediaType.TEXT_PLAIN_TYPE)
                                        .build();
        
        broadcaster.broadcast(event);
        LOGGER.log(Level.INFO, "Broadcasted leaderboard state "+ data);
    }
}
