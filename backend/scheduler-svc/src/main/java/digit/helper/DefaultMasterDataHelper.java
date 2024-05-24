package digit.helper;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.util.MdmsUtil;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DefaultMasterDataHelper {

    @Autowired
    private MdmsUtil mdmsUtil;
    @Autowired
    private ServiceConstants serviceConstants;
    @Autowired
    private Configuration config;


    public <T> List<T> getDataFromMDMS(Class<T> clazz, String masterName) {
        log.info("operation = getDataFromMDMS, result = IN_PROGRESS");
        RequestInfo requestInfo = new RequestInfo();
        Map<String, Map<String, JSONArray>> defaultHearingsData =
                mdmsUtil.fetchMdmsData(requestInfo, config.getEgovStateTenantId(),
                        serviceConstants.DEFAULT_COURT_MODULE_NAME,
                        Collections.singletonList(masterName));
        JSONArray jsonArray = defaultHearingsData.get("court").get(masterName);
        List<T> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Object obj : jsonArray) {
            T clazzObj = objectMapper.convertValue(obj, clazz);
            result.add(clazzObj);
        }
        log.info("operation = getDataFromMDMS, result = SUCCESS");
        return result;
    }
}
