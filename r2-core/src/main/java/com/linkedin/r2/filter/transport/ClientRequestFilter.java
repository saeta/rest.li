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
package com.linkedin.r2.filter.transport;

import com.linkedin.r2.filter.NextFilter;
import com.linkedin.r2.filter.message.rest.BaseRestFilter;
import com.linkedin.r2.message.Request;
import com.linkedin.r2.message.RequestContext;
import com.linkedin.r2.message.Response;
import com.linkedin.r2.message.rest.RestRequest;
import com.linkedin.r2.message.rest.RestResponse;
import com.linkedin.r2.transport.common.bridge.client.TransportClient;
import com.linkedin.r2.transport.common.bridge.common.TransportCallback;
import com.linkedin.r2.transport.common.bridge.common.TransportResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Filter implementation which sends requests through a specified {@link TransportClient}.
 *
 * @author Chris Pettitt
 * @version $Revision$
 */
public class ClientRequestFilter extends BaseRestFilter
{
  private final TransportClient _client;

  /**
   * Construct a new instance using the specified client.
   *
   * @param client the {@link TransportClient} used to send requests.
   */
  public ClientRequestFilter(TransportClient client)
  {
    _client = client;
  }

  @Override
  public void onRestRequest(RestRequest req, final RequestContext requestContext,
                            Map<String, String> wireAttrs,
                            final NextFilter<RestRequest, RestResponse> nextFilter)
  {
    try
    {
      _client.restRequest(req, requestContext, wireAttrs, createCallback(requestContext, nextFilter));
    }
    catch (Exception e)
    {
      nextFilter.onError(e, requestContext, new HashMap<String, String>());
    }
  }

  private <REQ extends Request, RES extends Response> TransportCallback<RES> createCallback(
          final RequestContext requestContext,
          final NextFilter<REQ, RES> nextFilter)
  {
    return new TransportCallback<RES>()
    {
      @Override
      public void onResponse(TransportResponse<RES> res)
      {
        final Map<String, String> wireAttrs = new HashMap<String, String>(res.getWireAttributes());
        if (res.hasError())
        {
          nextFilter.onError(res.getError(), requestContext, wireAttrs);
        }
        else
        {
          nextFilter.onResponse(res.getResponse(), requestContext, wireAttrs);
        }
      }
    };
  }
}
