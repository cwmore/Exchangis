package com.webank.wedatasphere.exchangis.dss.appconn.operation.project;

import com.webank.wedatasphere.dss.common.entity.project.DSSProject;
import com.webank.wedatasphere.dss.standard.app.sso.request.SSORequestOperation;
import com.webank.wedatasphere.dss.standard.app.structure.StructureService;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectGetOperation;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectRequestRef;
import com.webank.wedatasphere.dss.standard.common.exception.AppStandardErrorException;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;
import com.webank.wedatasphere.exchangis.dss.appconn.ref.ExchangisProjectResponseRef;
import com.webank.wedatasphere.exchangis.dss.appconn.request.action.ExchangisEntityPostAction;
import com.webank.wedatasphere.exchangis.dss.appconn.request.action.ExchangisGetAction;
import com.webank.wedatasphere.exchangis.dss.appconn.request.action.HttpExtAction;
import com.webank.wedatasphere.exchangis.dss.appconn.response.result.ExchangisEntityRespResult;
import com.webank.wedatasphere.exchangis.dss.appconn.service.ExchangisProjectService;
import com.webank.wedatasphere.exchangis.dss.appconn.utils.AppConnUtils;
import org.apache.linkis.httpclient.request.HttpAction;
import org.apache.linkis.httpclient.response.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author tikazhang
 * @Date 2022/3/22 0:48
 */
public class ExchangisProjectGetOperation extends AbstractExchangisProjectOperation implements ProjectGetOperation {
    private static Logger LOG = LoggerFactory.getLogger(ExchangisProjectDeletionOperation.class);

    private SSORequestOperation<HttpAction, HttpResult> ssoRequestOperation;
    private StructureService structureService;

    public ExchangisProjectGetOperation(StructureService structureService) {
        setStructureService(structureService);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public DSSProject getProject(ProjectRequestRef projectRequestRef) throws ExternalOperationFailedException {
        LOG.info("delete project request => dss_projectId:{}, name:{}, createName:{}", projectRequestRef.getId(),
                projectRequestRef.getName(), projectRequestRef.getCreateBy());
        String url = requestURL("appProject/check");
        ExchangisEntityRespResult.BasicMessageEntity<Map<String, Object>> entity = requestToGetEntity(url,projectRequestRef.getWorkspace(), projectRequestRef,
                (requestRef) ->{
                    ExchangisGetAction exchangisGetAction = new ExchangisGetAction();
                    exchangisGetAction.setUser(requestRef.getCreateBy());
                    exchangisGetAction.setParameter("keywords", projectRequestRef.getName());
                    return (HttpExtAction) exchangisGetAction;
                }, Map.class);
        if (Objects.isNull(entity)){
            throw new ExternalOperationFailedException(31020, "The response entity cannot be empty", null);
        }
        try {
            ExchangisEntityRespResult httpResult = entity.getResult();
            Map resMap = AppConnUtils.getResponseMap(httpResult);
            ExchangisProjectResponseRef responseRef = new ExchangisProjectResponseRef(httpResult, null);
            DSSProject dssProject = new DSSProject();
            Map<String, Object> payloadMap = (Map<String, Object>) resMap.get("payload");
            dssProject.setId(Long.parseLong(payloadMap.get("id").toString()));
            return dssProject;
        } catch (AppStandardErrorException e) {
            throw new ExternalOperationFailedException(90176, "search Exchangis Project failed when get HttpResult", e);
        } catch (Exception e) {
            throw new ExternalOperationFailedException(90176, "search Exchangis project failed when parse response json", e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void setStructureService(StructureService structureService) {
        this.structureService = structureService;
        setSSORequestService(this.structureService);
    }
}
