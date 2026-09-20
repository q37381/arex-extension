package com.arextest.extension.invoker;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.arextest.schedule.extension.invoker.InvokerConstants;
import com.arextest.schedule.extension.invoker.ReplayExtensionInvoker;
import com.arextest.schedule.extension.invoker.ReplayInvocation;
import com.arextest.schedule.extension.model.ReplayInvokeResult;

import java.util.List;
import java.util.Map;

/**
 * @author wildeslam.
 * @create 2023/11/7 15:09
 */
public class DefaultDubboInvoker implements ReplayExtensionInvoker {

  @Override
  public boolean isSupported(String caseType) {
    return InvokerConstants.DUBBO_CASE_TYPE.equalsIgnoreCase(caseType);
  }

  @Override
  public ReplayInvokeResult invoke(ReplayInvocation replayInvocation) {
    ReplayInvokeResult replayInvokeResult = new ReplayInvokeResult();
    try {

      RpcContext.getContext().setAttachments(replayInvocation.get(InvokerConstants.HEADERS, Map.class));

      ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
      reference.setApplication(new ApplicationConfig("defaultDubboInvoker"));
      reference.setUrl(replayInvocation.getUrl());
      reference.setInterface(replayInvocation.get(InvokerConstants.DUBBO_INTERFACE_NAME, String.class));
      reference.setGeneric(true);
      GenericService genericService = reference.get();
      if (genericService == null) {
        return replayInvokeResult;
      }

      Object result = genericService.$invoke(replayInvocation.get(InvokerConstants.DUBBO_METHOD_NAME, String.class),
          (String[]) replayInvocation.get(InvokerConstants.DUBBO_PARAMETER_TYPES, List.class).toArray(new String[0]),
          (replayInvocation.get(InvokerConstants.DUBBO_PARAMETERS, List.class)).toArray());

      replayInvokeResult.setResult(result);
      if (result instanceof RpcResult) {
        replayInvokeResult.setResponseProperties(((RpcResult) result).getAttachments());
      } else {
        replayInvokeResult.setResponseProperties((Map<String, String>) RpcContext.getContext().get("teslaProtocolResponseHeader"));
      }
      // add replayId
    } catch (Exception e) {
      replayInvokeResult.setException(e);
      replayInvokeResult.setErrorMsg(e.getMessage());
    }
    return replayInvokeResult;
  }
}
