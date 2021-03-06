/*
   Copyright (c) 2012 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/* $Id$ */
package com.linkedin.r2.filter.message.rest;

import com.linkedin.r2.filter.NextFilter;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.rest.RestRequest;
import com.linkedin.r2.message.rest.RestResponse;

import java.util.Map;

/**
 * A filter that processes {@link com.linkedin.r2.message.rest.RestRequest}s and
 * {@link com.linkedin.r2.message.rest.RestResponse}s.
 *
 * @author Chris Pettitt
 */
public interface RestFilter
{
  /**
   * Method to be invoked for each {@link com.linkedin.r2.message.rest.RestRequest} message.
   *
   * @param req the {@link com.linkedin.r2.message.rest.RestRequest} message.
   * @param requestContext the {@link com.linkedin.r2.message.RequestContext} of the request.
   * @param wireAttrs the wire attributes of the request.
   * @param nextFilter the next filter in the chain.  Concrete implementations should invoke
   *                   {@link com.linkedin.r2.filter.NextFilter#onRequest} to continue the filter chain.
   */

  void onRestRequest(RestRequest req,
                     RequestContext requestContext,
                     Map<String, String> wireAttrs,
                     NextFilter<RestRequest, RestResponse> nextFilter);

  /**
   * Method to be invoked for each {@link RestResponse} message.
   *
   * @param res the {@link RestResponse} message.
   * @param requestContext the {@link RequestContext} of the request.
   * @param wireAttrs the wire attributes of the response.
   * @param nextFilter the next filter in the chain.  Concrete implementations should invoke
   *                   {@link NextFilter#onResponse} to continue the filter chain.
   */
  void onRestResponse(RestResponse res,
                      RequestContext requestContext,
                      Map<String, String> wireAttrs,
                      NextFilter<RestRequest, RestResponse> nextFilter);

  /**
   * Method to be invoked when an error is encountered.
   *
   * @param ex the {@link Throwable} representation of the error.
   * @param requestContext the {@link RequestContext} of the request.
   * @param wireAttrs the wire attributes of the response (if any).
   * @param nextFilter the next filter in the chain.  Concrete implementations should invoke
   *                   {@link NextFilter#onError} to continue the filter chain.
   */
  void onRestError(Throwable ex,
                   RequestContext requestContext,
                   Map<String, String> wireAttrs,
                   NextFilter<RestRequest, RestResponse> nextFilter);
}
