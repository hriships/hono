/**
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial creation
 */
package org.eclipse.hono.util;

import java.util.Objects;

import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;

/**
 * A stream producing a given number of telemetry data messages.
 *
 */
public class TelemetryDataReadStream implements ReadStream<Message> {

    private static final Logger LOG                     = LoggerFactory.getLogger(TelemetryDataReadStream.class);
    private static final String DEVICE_BUMLUX_TEMP_4711 = "bumlux:temp:4711";

    private int                 messagesToSend;
    private int                 counter;
    private Handler<Void>       endHandler;
    private Handler<Message>    handler;
    private boolean             paused;
    private Vertx               vertx;

    /**
     * @param count the number of messages to produce.
     */
    public TelemetryDataReadStream(final Vertx vertx, final int count) {
        this.vertx = Objects.requireNonNull(vertx);
        messagesToSend = count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.vertx.core.streams.ReadStream#exceptionHandler(io.vertx.core.Handler)
     */
    @Override
    public ReadStream<Message> exceptionHandler(final Handler<Throwable> handler) {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.vertx.core.streams.ReadStream#handler(io.vertx.core.Handler)
     */
    @Override
    public ReadStream<Message> handler(final Handler<Message> handler) {
        this.handler = handler;
        if (handler != null) {
            sendMessages();
        }
        return this;
    }

    private synchronized boolean isFinished() {
        return counter >= messagesToSend;
    }

    private synchronized boolean sendMore() {
        return !isFinished() && !paused;
    }

    private void sendMessages() {
        vertx.setPeriodic(3, id -> {
            if (sendMore()) {
                int messageId = counter++;
                LOG.trace("producing new telemetry message [id: {}]", messageId);
                handler.handle(TestSupport.newTelemetryData(String.valueOf(messageId), DEVICE_BUMLUX_TEMP_4711, messageId % 35));
            }
            if (isFinished()) {
                vertx.cancelTimer(id);
                if (endHandler != null) {
                    Handler<Void> theHandler = endHandler;
                    endHandler = null;
                    vertx.runOnContext(theHandler);
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.vertx.core.streams.ReadStream#pause()
     */
    @Override
    public synchronized ReadStream<Message> pause() {
        LOG.trace("pausing production of random telemetry data");
        paused = true;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.vertx.core.streams.ReadStream#resume()
     */
    @Override
    public synchronized ReadStream<Message> resume() {
        LOG.trace("resuming production of random telemetry data");
        paused = false;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.vertx.core.streams.ReadStream#endHandler(io.vertx.core.Handler)
     */
    @Override
    public ReadStream<Message> endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
        return this;
    }
}
