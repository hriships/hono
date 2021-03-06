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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A unique identifier for a resource within Hono.
 * <p>
 * Each resource identifier consists of an arbitrary number of path segments.
 * The first segment always contains the name of the <em>endpoint</em> that the
 * resource belongs to.
 * </p>
 * <p>
 * Within the <em>telemetry</em> and <em>registration</em> endpoints the first three
 * segments have the following semantics:
 * <ol>
 * <li>the <em>endpoint</em> name</li>
 * <li>the <em>tenant ID</em></li>
 * <li>an (optional) <em>device ID</em></li>
 * </ol>
 * </p>
 */
public final class ResourceIdentifier {

    private static final int IDX_ENDPOINT = 0;
    private static final int IDX_TENANT_ID = 1;
    private static final int IDX_DEVICE_ID = 2;
    private String[] resourcePath;
    private String resourceId;

    private ResourceIdentifier(final String resourceId, final boolean assumeDefaultTenant) {
        String[] path = resourceId.split("\\/");
        if (assumeDefaultTenant) {
            if (path.length == 0) {
                throw new IllegalArgumentException("resource identifier must at least contain an endpoint");
            } else if (path.length > 2) {
                throw new IllegalArgumentException("resource identifer must not contain more than 2 segments");
            } else {
                setResourcePath(new String[]{path[0], Constants.DEFAULT_TENANT, path.length == 2 ? path[1] : null});
            }
        } else {
            if (path.length < 2) {
                throw new IllegalArgumentException(
                        "resource identifier must at least contain an endpoint and the tenantId");
            } else if (path.length > 3) {
                throw new IllegalArgumentException("resource identifer must not contain more than 3 segments");
            } else {
                setResourcePath(new String[]{path[0], path[1], path.length == 3 ? path[2] : null});
            }
        }
    }

    private ResourceIdentifier(final String endpoint, final String tenantId, final String deviceId) {
        setResourcePath(new String[]{endpoint, tenantId, deviceId});
    }

    private ResourceIdentifier(final String[] path) {
        setResourcePath(path);
    }

    private void setResourcePath(final String[] path) {
        List<String> pathSegments = new ArrayList<>();
        boolean pathContainsNullSegment = false;
        for (String segment : path) {
            if (segment == null) {
                pathContainsNullSegment = true;
            } else if (pathContainsNullSegment) {
                throw new IllegalArgumentException("path may contain trailing null segments only");
            } else {
                pathSegments.add(segment);
            }
        }
        this.resourcePath = pathSegments.toArray(new String[0]);
        createStringRepresentation();
    }

    /**
     * Gets this resource identifier as path segments.
     * 
     * @return the segments.
     */
    public String[] toPath() {
        return Arrays.copyOf(resourcePath, resourcePath.length);
    }

    private void createStringRepresentation() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < resourcePath.length; i++) {
            b.append(resourcePath[i]);
            if (i < resourcePath.length - 1) {
                b.append("/");
            }
        }
        resourceId = b.toString();
    }

    /**
     * Creates a resource identifier from its string representation.
     * <p>
     * The given string is split up into segments using a forward slash as the separator. The first segment is used as
     * the endpoint, the second segment is used as the tenant ID and the third segment (if present) is used as the
     * device ID.
     * </p>
     * 
     * @param resourceId the resource identifier string to parse.
     * @return the resource identifier.
     * @throws NullPointerException if the given string is {@code null}.
     * @throws IllegalArgumentException if the given string does not represent a valid resource identifier.
     */
    public static ResourceIdentifier fromString(final String resourceId) {
        Objects.requireNonNull(resourceId);
        return new ResourceIdentifier(resourceId, false);
    }

    /**
     * Creates a resource identifier from its string representation assuming the default tenant.
     * <p>
     * The given string is split up into segments using a forward slash as the separator. The first segment is used as
     * the endpoint and the second segment (if present) is used as the device ID. The tenant ID is always set to
     * {@link Constants#DEFAULT_TENANT}.
     * </p>
     * 
     * @param resourceId the resource identifier string to parse.
     * @return the resource identifier.
     * @throws NullPointerException if the given string is {@code null}.
     * @throws IllegalArgumentException if the given string does not represent a valid resource identifier.
     */
    public static ResourceIdentifier fromStringAssumingDefaultTenant(final String resourceId) {
        Objects.requireNonNull(resourceId);
        return new ResourceIdentifier(resourceId, true);
    }

    /**
     * Creates a resource identifier from endpoint, tenantId and optionally deviceId.
     *
     * @param endpoint the endpoint of the resource.
     * @param tenantId the tenant identifier.
     * @param deviceId the device identifier, may be {@code null}.
     * @return the resource identifier.
     * @throws NullPointerException if endpoint or tenantId is {@code null}.
     */
    public static ResourceIdentifier from(final String endpoint, final String tenantId, final String deviceId) {
        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(tenantId);
        return new ResourceIdentifier(endpoint, tenantId, deviceId);
    }

    /**
     * Creates a resource identifier from path segments.
     * <p>
     * The given path will be stripped of any trailing {@code null}
     * segments.
     * </p>
     * 
     * @param path the segments of the resource path.
     * @return the resource identifier.
     * @throws NullPointerException if path is {@code null}.
     * @throws IllegalArgumentException if the path contains no segments or contains non-trailing
     *                                  {@code null} segments.
     */
    public static ResourceIdentifier fromPath(final String[] path) {
        Objects.requireNonNull(path);
        if (path.length == 0) {
            throw new IllegalArgumentException("path must have at least one segment");
        } else {
            return new ResourceIdentifier(path);
        }
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return resourcePath[IDX_ENDPOINT];
    }

    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return resourcePath[IDX_TENANT_ID];
    }

    /**
     * @return the deviceId
     */
    public String getDeviceId() {
        if (resourcePath.length > IDX_DEVICE_ID) {
            return resourcePath[IDX_DEVICE_ID];
        } else {
            return null;
        }
    }

    public boolean matches(final String... pattern) {
        if (resourcePath.length != pattern.length) {
            return false;
        } else {
            boolean result = true;
            for (int i = 0; i < resourcePath.length; i++) {
                if ("*".equals(pattern[i])) {
                    continue;
                } else {
                    result &= resourcePath[i].equals(pattern[i]);
                }
            }
            return result;
        }
    }

    /**
     * Gets a string representation of this resource identifier.
     * <p>
     * The string representation consists of all path segments separated by a
     * forward slash ("/").
     * </p>
     * 
     * @return the resource id.
     */
    @Override
    public String toString() {
        return resourceId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ResourceIdentifier that = (ResourceIdentifier) o;
        return resourcePath != null ? Arrays.equals(resourcePath, that.resourcePath) : that.resourcePath == null;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(resourcePath);
    }
}
