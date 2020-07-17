/*
 * Copyright (c) 2020. AxonIQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.axoniq.axonserver.connector.impl;

import io.grpc.stub.ClientCallStreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

/**
 * Lock-based synchronized implementation of a {@link ClientCallStreamObserver}. Acts as a wrapper of another {@code
 * ClientCallStreamObserver}, to which all of operations will be delegated, adding synchronization logic to {@link
 * #onNext(Object)}, {@link #onCompleted()} and {@link #onError(Throwable)}.
 *
 * @param <T> the type of value returned by this stream
 */
public class SynchronizedRequestStream<T> extends ClientCallStreamObserver<T> {

    private final ClientCallStreamObserver<T> delegate;
    private final AtomicBoolean lock = new AtomicBoolean(false);

    /**
     * Instantiate a {@link SynchronizedRequestStream}, delegating all operations to the given {@code requestStream}
     *
     * @param requestStream the {@link ClientCallStreamObserver} to delegate method invocations to
     */
    public SynchronizedRequestStream(ClientCallStreamObserver<T> requestStream) {
        delegate = requestStream;
    }

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {
        delegate.cancel(message, cause);
    }

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public void setOnReadyHandler(Runnable onReadyHandler) {
        delegate.setOnReadyHandler(onReadyHandler);
    }

    @Override
    public void disableAutoInboundFlowControl() {
        delegate.disableAutoInboundFlowControl();
    }

    @Override
    public void request(int count) {
        delegate.request(count);
    }

    @Override
    public void setMessageCompression(boolean enable) {
        delegate.setMessageCompression(enable);
    }

    @Override
    public void onNext(T value) {
        inLock(() -> delegate.onNext(value));
    }

    @Override
    public void onError(Throwable t) {
        inLock(() -> delegate.onError(t));
    }

    @Override
    public void onCompleted() {
        inLock(delegate::onCompleted);
    }

    private void inLock(Runnable action) {
        while (!lock.compareAndSet(false, true)) {
            Thread.yield();
        }
        try {
            action.run();
        } finally {
            lock.set(false);
        }
    }
}
