package io.axoniq.axonserver.connector.event;

import java.util.concurrent.CompletableFuture;

/**
 * @author Marc Gathier
 * @since 4.6.0
 */
public interface EventTransformationChannel {

    CompletableFuture<EventTransformation> startTransformation();

    CompletableFuture<EventTransformation> getTransformation(String transformationId);

}
