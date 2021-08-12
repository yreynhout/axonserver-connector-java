package io.axoniq.axonserver.connector.event;

import io.axoniq.axonserver.grpc.event.Event;

import java.util.concurrent.CompletableFuture;

/**
 * @author Marc Gathier
 * @since 4.6.0
 */
public interface EventTransformation {

    String transformationId();

    EventTransformation replaceEvent(long token, long previousToken, Event event);

    EventTransformation deleteEvent(long token, long previousToken);

    EventTransformation replaceSnapshot(long token, long previousToken, Event event);

    EventTransformation deleteSnapshot(long token, long previousToken);

    CompletableFuture<Void> apply(long lastEventToken, long lastSnapshotToken);

    default CompletableFuture<Void> apply(long lastEventToken) {
        return apply(lastEventToken, -1);
    }

    CompletableFuture<Void> cancel();

}
