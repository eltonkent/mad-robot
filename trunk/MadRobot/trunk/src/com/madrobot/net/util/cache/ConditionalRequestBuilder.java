/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.madrobot.net.util.cache;

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.RequestWrapper;

import com.madrobot.net.HeaderConstants;
import com.madrobot.net.util.cache.annotation.Immutable;

import android.util.Log;

/**
 * @since 4.1
 */
@Immutable
class ConditionalRequestBuilder {


    /**
     * When a {@link HttpCacheEntry} is stale but 'might' be used as a response
     * to an {@link HttpRequest} we will attempt to revalidate the entry with
     * the origin.  Build the origin {@link HttpRequest} here and return it.
     *
     * @param request the original request from the caller
     * @param cacheEntry the entry that needs to be re-validated
     * @return the wrapped request
     * @throws ProtocolException when I am unable to build a new origin request.
     */
    public HttpRequest buildConditionalRequest(HttpRequest request, HttpCacheEntry cacheEntry)
            throws ProtocolException {
        RequestWrapper wrapperRequest = new RequestWrapper(request);
        wrapperRequest.resetHeaders();
        Header eTag = cacheEntry.getFirstHeader(HeaderConstants.ETAG);
        if (eTag != null) {
            wrapperRequest.setHeader(HeaderConstants.IF_NONE_MATCH, eTag.getValue());
        }
        Header lastModified = cacheEntry.getFirstHeader(HeaderConstants.LAST_MODIFIED);
        if (lastModified != null) {
            wrapperRequest.setHeader(HeaderConstants.IF_MODIFIED_SINCE, lastModified.getValue());
        }
        boolean mustRevalidate = false;
        for(Header h : cacheEntry.getHeaders(HeaderConstants.CACHE_CONTROL)) {
            for(HeaderElement elt : h.getElements()) {
                if (HeaderConstants.CACHE_CONTROL_MUST_REVALIDATE.equalsIgnoreCase(elt.getName())
                    || HeaderConstants.CACHE_CONTROL_PROXY_REVALIDATE.equalsIgnoreCase(elt.getName())) {
                    mustRevalidate = true;
                    break;
                }
            }
        }
        if (mustRevalidate) {
            wrapperRequest.addHeader(HeaderConstants.CACHE_CONTROL, HeaderConstants.CACHE_CONTROL_MAX_AGE + "=0");
        }
        return wrapperRequest;

    }

    /**
     * When a {@link HttpCacheEntry} does not exist for a specific {@link HttpRequest}
     * we attempt to see if an existing {@link HttpCacheEntry} is appropriate by building
     * a conditional {@link HttpRequest} using the variants' ETag values.  If no such values
     * exist, the request is unmodified
     *
     * @param request the original request from the caller
     * @param variants
     * @return the wrapped request
     */
    public HttpRequest buildConditionalRequestFromVariants(HttpRequest request,
            Map<String, Variant> variants) {
        RequestWrapper wrapperRequest;
        try {
            wrapperRequest = new RequestWrapper(request);
        } catch (ProtocolException pe) {
        	Log.d("MadRobot","unable to build conditional request", pe);
            return request;
        }
        wrapperRequest.resetHeaders();

        // we do not support partial content so all etags are used
        StringBuilder etags = new StringBuilder();
        boolean first = true;
        for(String etag : variants.keySet()) {
            if (!first) {
                etags.append(",");
            }
            first = false;
            etags.append(etag);
        }

        wrapperRequest.setHeader(HeaderConstants.IF_NONE_MATCH, etags.toString());
        return wrapperRequest;
    }

    /**
     * Returns a request to unconditionally validate a cache entry with
     * the origin. In certain cases (due to multiple intervening caches)
     * our cache may actually receive a response to a normal conditional
     * validation where the Date header is actually older than that of
     * our current cache entry. In this case, the protocol recommendation
     * is to retry the validation and force syncup with the origin.
     * @param request client request we are trying to satisfy
     * @param entry existing cache entry we are trying to validate
     * @return an unconditional validation request
     */
    public HttpRequest buildUnconditionalRequest(HttpRequest request, HttpCacheEntry entry) {
        RequestWrapper wrapped;
        try {
            wrapped = new RequestWrapper(request);
        } catch (ProtocolException e) {
        	Log.d("MadRobot","unable to build proper unconditional request", e);
            return request;
        }
        wrapped.resetHeaders();
        wrapped.addHeader(HeaderConstants.CACHE_CONTROL,HeaderConstants.CACHE_CONTROL_NO_CACHE);
        wrapped.addHeader(HeaderConstants.PRAGMA,HeaderConstants.CACHE_CONTROL_NO_CACHE);
        wrapped.removeHeaders(HeaderConstants.IF_RANGE);
        wrapped.removeHeaders(HeaderConstants.IF_MATCH);
        wrapped.removeHeaders(HeaderConstants.IF_NONE_MATCH);
        wrapped.removeHeaders(HeaderConstants.IF_UNMODIFIED_SINCE);
        wrapped.removeHeaders(HeaderConstants.IF_MODIFIED_SINCE);
        return wrapped;
    }

}