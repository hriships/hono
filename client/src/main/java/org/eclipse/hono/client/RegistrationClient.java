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

package org.eclipse.hono.client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A client for accessing Hono's Registration API.
 * <p>
 * An instance of this interface is always scoped to a specific tenant.
 * </p>
 * <p>
 * See Hono's <a href="https://github.com/eclipse/hono/wiki/Device-Registration-API">
 * Registration API specification</a> for a description of the result codes returned.
 * </p>
 */
public interface RegistrationClient {

    /**
     * Checks whether a given device is registered.
     * 
     * @param deviceId The id of the device to check.
     * @param resultHandler The handler to invoke with the result code of the operation.
     */
    void get(String deviceId, Handler<AsyncResult<Integer>> resultHandler);

    /**
     * Registers a device with Hono.
     * <p>
     * A device needs to be (successfully) registered before a client can upload
     * telemetry data for it.
     * </p>
     * 
     * @param deviceId The id of the device to register.
     * @param resultHandler The handler to invoke with the result code of the operation.
     */
    void register(String deviceId, Handler<AsyncResult<Integer>> resultHandler);

    /**
     * Deregisters a device from Hono.
     * <p>
     * Once a device has been (successfully) deregistered, no more telemtry data can be uploaded
     * for it nor can commands be sent to it anymore.
     * </p>
     * 
     * @param deviceId The id of the device to deregister.
     * @param resultHandler The handler to invoke with the result code of the operation.
     */
    void deregister(String deviceId, Handler<AsyncResult<Integer>> resultHandler);

    /**
     * Closes the AMQP link(s) with the Hono server this client is configured to use.
     * <p>
     * The underlying AMQP connection to the server is not affected by this operation.
     * </p>
     * 
     * @param closeHandler A handler that is called back with the result of the attempt to close the links.
     */
    void close(Handler<AsyncResult<Void>> closeHandler);
}
