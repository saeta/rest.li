/*
   Copyright (c) 2014 LinkedIn Corp.

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


package com.linkedin.restli.internal.server.methods.response;


import com.linkedin.data.schema.StringDataSchema;
import com.linkedin.data.template.InvalidAlternativeKeyException;
import com.linkedin.data.template.KeyCoercer;
import com.linkedin.data.transform.filter.request.MaskTree;
import com.linkedin.pegasus.generator.examples.Foo;
import com.linkedin.restli.common.BatchCreateIdResponse;
import com.linkedin.restli.common.CreateIdEntityStatus;
import com.linkedin.restli.common.CreateIdStatus;
import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.internal.server.RestLiResponseEnvelope;
import com.linkedin.restli.common.RestConstants;
import com.linkedin.restli.internal.common.AllProtocolVersions;
import com.linkedin.restli.internal.server.RoutingResult;
import com.linkedin.restli.internal.server.ServerResourceContext;
import com.linkedin.restli.internal.server.model.ResourceMethodDescriptor;
import com.linkedin.restli.internal.server.response.CreateCollectionResponseEnvelope;
import com.linkedin.restli.internal.server.model.ResourceModel;
import com.linkedin.restli.server.AlternativeKey;
import com.linkedin.restli.server.BatchCreateKVResult;
import com.linkedin.restli.server.BatchCreateResult;
import com.linkedin.restli.server.CreateKVResponse;
import com.linkedin.restli.server.CreateResponse;
import com.linkedin.restli.server.ProjectionMode;
import com.linkedin.restli.server.ResourceContext;
import com.linkedin.restli.server.RestLiServiceException;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * @author kparikh
 */
public class TestBatchCreateResponseBuilder
{
  @DataProvider(name = "createResultBuilderTestData")
  public Object[][] createResultBuilderTestData()
  {
    Map<String, AlternativeKey<?, ?>> alternativeKeyMap = new HashMap<String, AlternativeKey<?, ?>>();
    alternativeKeyMap.put("alt", new AlternativeKey<String, Long>(new TestKeyCoercer(), String.class, new StringDataSchema()));

    List<CreateIdStatus<Long>> expectedStatuses = new ArrayList<CreateIdStatus<Long>>(2);
    expectedStatuses.add(new CreateIdStatus<Long>(201, 1L, null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));
    expectedStatuses.add(new CreateIdStatus<Long>(201, 2L, null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));

    List<CreateIdStatus<String>> expectedAltStatuses = new ArrayList<CreateIdStatus<String>>(2);
    expectedAltStatuses.add(new CreateIdStatus<String>(201, "Alt1", null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));
    expectedAltStatuses.add(new CreateIdStatus<String>(201, "Alt2", null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));

    return new Object[][]
        {
          { null, null, expectedStatuses },
          { "alt", alternativeKeyMap, expectedAltStatuses }
        };
  }

  @Test(dataProvider = "createResultBuilderTestData")
  @SuppressWarnings("unchecked")
  public void testCreateResultBuilder(String altKeyName,
                                      Map<String, AlternativeKey<?, ?>> alternativeKeyMap,
                                      List<CreateIdStatus<Object>> expectedStatuses)
  {
    List<CreateResponse> createResponses = Arrays.asList(new CreateResponse(1L), new CreateResponse(2L));
    BatchCreateResult<Long, Foo> results =
        new BatchCreateResult<Long, Foo>(createResponses);
    Map<String, String> headers = ResponseBuilderUtil.getHeaders();

    ResourceMethodDescriptor mockDescriptor = getMockResourceMethodDescriptor(alternativeKeyMap);
    ResourceContext mockContext = getMockResourceContext(altKeyName);
    RoutingResult routingResult = new RoutingResult(mockContext, mockDescriptor);

    BatchCreateResponseBuilder responseBuilder = new BatchCreateResponseBuilder(null);
    RestLiResponseEnvelope responseData = responseBuilder.buildRestLiResponseData(null,
                                                                                  routingResult,
                                                                                  results,
                                                                                  headers,
                                                                                  Collections.<HttpCookie>emptyList());
    PartialRestResponse restResponse = responseBuilder.buildResponse(routingResult, responseData);

    EasyMock.verify(mockDescriptor);
    ResponseBuilderUtil.validateHeaders(restResponse, headers);

    List<com.linkedin.restli.common.CreateIdStatus<Object>> items = new ArrayList<CreateIdStatus<Object>>();
    for (CreateCollectionResponseEnvelope.CollectionCreateResponseItem item : responseData.getCreateCollectionResponseEnvelope().getCreateResponses())
    {
      items.add((CreateIdStatus<Object>) item.getRecord());
    }

    Assert.assertEquals(restResponse.getEntity(),
            new BatchCreateIdResponse<Object>(items));
    Assert.assertEquals(expectedStatuses, items);
    Assert.assertEquals(restResponse.getStatus(), HttpStatus.S_200_OK);
  }

  @DataProvider(name = "createKVResultBuilderTestData")
  public Object[][] createKVResultBuilderTestData()
  {
    Map<String, AlternativeKey<?, ?>> alternativeKeyMap = new HashMap<String, AlternativeKey<?, ?>>();
    alternativeKeyMap.put("alt", new AlternativeKey<String, Long>(new TestKeyCoercer(), String.class, new StringDataSchema()));

    Foo foo1 = new Foo();
    foo1.setStringField("foo1");
    Foo foo2 = new Foo();
    foo2.setStringField("foo2");

    List<CreateIdEntityStatus<Long, Foo>> expectedResponses = new ArrayList<CreateIdEntityStatus<Long, Foo>>(2);
    expectedResponses.add(new CreateIdEntityStatus<Long, Foo>(201, 1L, foo1, null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));
    expectedResponses.add(new CreateIdEntityStatus<Long, Foo>(201, 2L, foo2, null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));

    List<CreateIdEntityStatus<String, Foo>> expectedAltResponses = new ArrayList<CreateIdEntityStatus<String, Foo>>(2);
    expectedAltResponses.add(new CreateIdEntityStatus<String, Foo>(201, "Alt1", foo1, null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));
    expectedAltResponses.add(new CreateIdEntityStatus<String, Foo>(201, "Alt2", foo2, null, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion()));

    return new Object[][]
            {
                    { null, null, expectedResponses },
                    { "alt", alternativeKeyMap, expectedAltResponses }
            };
  }

  @Test(dataProvider = "createKVResultBuilderTestData")
  public void testCreateKVResultBuilder(String altKeyName,
                                        Map<String, AlternativeKey<?, ?>> alternativeKeyMap,
                                        List<CreateIdEntityStatus<?, Foo>> expectedResponses)
  {
    List<CreateKVResponse<Long, Foo>> createKVResponses = new ArrayList<CreateKVResponse<Long, Foo>>(2);
    Foo foo1 = new Foo();
    foo1.setStringField("foo1");
    Foo foo2 = new Foo();
    foo2.setStringField("foo2");
    createKVResponses.add(new CreateKVResponse<Long, Foo>(1L, foo1));
    createKVResponses.add(new CreateKVResponse<Long, Foo>(2L, foo2));
    BatchCreateKVResult<Long, Foo> results = new BatchCreateKVResult<Long, Foo>(createKVResponses);
    Map<String, String> headers = ResponseBuilderUtil.getHeaders();

    ResourceMethodDescriptor mockDescriptor = getMockResourceMethodDescriptor(alternativeKeyMap);

    ResourceContext mockContext = getMockKVResourceContext(altKeyName);

    RoutingResult routingResult = new RoutingResult(mockContext, mockDescriptor);

    BatchCreateResponseBuilder responseBuilder = new BatchCreateResponseBuilder(null);
    RestLiResponseEnvelope responseData = responseBuilder.buildRestLiResponseData(null,
            routingResult,
            results,
            headers,
            Collections.<HttpCookie>emptyList());
    PartialRestResponse restResponse = responseBuilder.buildResponse(routingResult, responseData);

    EasyMock.verify(mockDescriptor);
    ResponseBuilderUtil.validateHeaders(restResponse, headers);

    List<CreateIdEntityStatus<Object, Foo>> items = new ArrayList<CreateIdEntityStatus<Object, Foo>>();
    for (CreateCollectionResponseEnvelope.CollectionCreateResponseItem item : responseData.getCreateCollectionResponseEnvelope().getCreateResponses())
    {
      @SuppressWarnings("unchecked")
      CreateIdEntityStatus<Object, Foo> record = (CreateIdEntityStatus<Object, Foo>) item.getRecord();
      items.add(record);
    }

    Assert.assertEquals(items, expectedResponses);
    Assert.assertEquals(restResponse.getStatus(), HttpStatus.S_200_OK);
  }

  @DataProvider(name = "exceptionTestData")
  public Object[][] exceptionTestData()
  {
    return new Object[][]
        {
            {new BatchCreateResult<Long, Foo>(Arrays.asList(new CreateResponse(1L), null)),
                "Unexpected null encountered. Null element inside of List inside of a BatchCreateResult returned by the resource method: "},
            {new BatchCreateResult<Long, Foo>(null),
                "Unexpected null encountered. Null List inside of a BatchCreateResult returned by the resource method: "}
        };
  }

  @Test(dataProvider = "exceptionTestData")
  public void testBuilderExceptions(Object result, String expectedErrorMessage)
  {
    Map<String, String> headers = ResponseBuilderUtil.getHeaders();
    ResourceMethodDescriptor mockDescriptor = getMockResourceMethodDescriptor(null);
    ResourceContext mockContext = getMockResourceContext(null);
    RoutingResult routingResult = new RoutingResult(mockContext, mockDescriptor);
    BatchCreateResponseBuilder responseBuilder = new BatchCreateResponseBuilder(null);
    try
    {
      responseBuilder.buildRestLiResponseData(null, routingResult, result, headers, Collections.<HttpCookie>emptyList());
      Assert.fail("buildRestLiResponseData should have thrown an exception because of null elements");
    }
    catch (RestLiServiceException e)
    {
      Assert.assertTrue(e.getMessage().contains(expectedErrorMessage));
    }
  }

  private static ResourceMethodDescriptor getMockResourceMethodDescriptor(Map<String, AlternativeKey<?, ?>> alternativeKeyMap)
  {
    ResourceMethodDescriptor mockDescriptor = EasyMock.createMock(ResourceMethodDescriptor.class);
    if (alternativeKeyMap != null)
    {
      EasyMock.expect(mockDescriptor.getResourceModel()).andReturn(getMockResourceModel(alternativeKeyMap)).atLeastOnce();
    }
    EasyMock.replay(mockDescriptor);
    return mockDescriptor;
  }

  private static ResourceModel getMockResourceModel(Map<String, AlternativeKey<?, ?>> alternativeKeyMap)
  {
    ResourceModel mockResourceModel = EasyMock.createMock(ResourceModel.class);
    EasyMock.expect(mockResourceModel.getAlternativeKeys()).andReturn(alternativeKeyMap).anyTimes();
    EasyMock.replay(mockResourceModel);
    return mockResourceModel;
  }

  private static ResourceContext getMockResourceContext(String altKeyName)
  {
    ServerResourceContext mockContext = EasyMock.createMock(ServerResourceContext.class);
    EasyMock.expect(mockContext.hasParameter(RestConstants.ALT_KEY_PARAM)).andReturn(altKeyName != null).atLeastOnce();
    if (altKeyName != null)
    {
      EasyMock.expect(mockContext.getParameter(RestConstants.ALT_KEY_PARAM)).andReturn(altKeyName).atLeastOnce();
    }
    EasyMock.replay(mockContext);
    return mockContext;
  }

  private static ResourceContext getMockKVResourceContext(String altKeyName)
  {
    ServerResourceContext mockContext = EasyMock.createMock(ServerResourceContext.class);
    EasyMock.expect(mockContext.hasParameter(RestConstants.ALT_KEY_PARAM)).andReturn(altKeyName != null).atLeastOnce();
    if (altKeyName != null)
    {
      EasyMock.expect(mockContext.getParameter(RestConstants.ALT_KEY_PARAM)).andReturn(altKeyName).atLeastOnce();
    }

    // not testing the diversity of options here.
    EasyMock.expect(mockContext.getProjectionMode()).andReturn(ProjectionMode.getDefault()).atLeastOnce();
    EasyMock.expect(mockContext.getProjectionMask()).andReturn(new MaskTree()).atLeastOnce();
    Map<String, String> protocolVersionOnlyHeaders = Collections.singletonMap(RestConstants.HEADER_RESTLI_PROTOCOL_VERSION, AllProtocolVersions.RESTLI_PROTOCOL_2_0_0.getProtocolVersion().toString());
    EasyMock.expect(mockContext.getRequestHeaders()).andReturn(protocolVersionOnlyHeaders).atLeastOnce();

    EasyMock.replay(mockContext);
    return mockContext;
  }

  private class TestKeyCoercer implements KeyCoercer<String, Long>
  {
    @Override
    public Long coerceToKey(String object) throws InvalidAlternativeKeyException
    {
      return Long.parseLong(object.substring(3));
    }

    @Override
    public String coerceFromKey(Long object)
    {
      return "Alt" + object;
    }
  }
}
