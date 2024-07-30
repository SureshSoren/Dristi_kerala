import { useMemo, useCallback } from "react";

const useESign = () => {
  const tenantId = window?.Digit.ULBService.getCurrentTenantId();
  const storedObj = useMemo(() => localStorage.getItem("signStatus"), []);
  const parsedObj = JSON.parse(storedObj) || [];

  const handleEsign = useCallback(
    async (name, eSignFIleId, pageModule) => {
      try {
        const newSignStatuses = [...parsedObj, { name: name, isSigned: true }];
        localStorage.setItem("signStatus", JSON.stringify(newSignStatuses));

        const eSignResponse = await Digit.DRISTIService.eSignService({
          ESignParameter: {
            uidToken: "3456565",
            consent: "6564",
            authType: "6546",
            fileStoreId: eSignFIleId,
            tenantId: tenantId,
            pageModule: pageModule,
          },
        });
        if (eSignResponse) {
          const eSignData = {
            path: window.location.pathname,
            param: window.location.search,
            isEsign: true,
          };
          localStorage.setItem("esignResponse", JSON.stringify(eSignResponse));
          localStorage.setItem("eSignWindowObject", JSON.stringify(eSignData));
          localStorage.setItem("esignProcess", true);
          const form = document.createElement("form");
          form.method = "POST";
          form.action = "https://es-staging.cdac.in/esignlevel1/2.1/form/signdoc";
          const eSignRequestInput = document.createElement("input");
          eSignRequestInput.type = "hidden";
          eSignRequestInput.name = "eSignRequest";
          eSignRequestInput.value = eSignResponse?.ESignForm?.eSignRequest;
          const aspTxnIDInput = document.createElement("input");
          aspTxnIDInput.type = "hidden";
          aspTxnIDInput.name = "aspTxnID";
          aspTxnIDInput.value = eSignResponse?.ESignForm?.aspTxnID;
          const contentTypeInput = document.createElement("input");
          contentTypeInput.type = "hidden";
          contentTypeInput.name = "Content-Type";
          contentTypeInput.value = "application/xml";
          form.appendChild(eSignRequestInput);
          form.appendChild(aspTxnIDInput);
          form.appendChild(contentTypeInput);
          document.body.appendChild(form);
          form.submit();
          document.body.removeChild(form);
        }
      } catch (error) {
        console.error("API call failed:", error);
      }
    },
    [parsedObj]
  );

  const checkSignStatus = (name, formData, uploadModalConfig, onSelect, setIsSigned) => {
    const setValue = (value, input) => {
      if (Array.isArray(input)) {
        onSelect(uploadModalConfig.key, {
          ...formData[uploadModalConfig.key],
          ...input.reduce((res, curr) => {
            res[curr] = value[curr];
            return res;
          }, {}),
        });
      } else {
        onSelect(uploadModalConfig.key, { ...formData[uploadModalConfig.key], [input]: value });
      }
    };

    const isSignSuccess = localStorage.getItem("isSignSuccess");
    const storedESignObj = localStorage.getItem("signStatus");
    const parsedESignObj = JSON.parse(storedESignObj);

    if (isSignSuccess) {
      const matchedSignStatus = parsedESignObj.find((obj) => obj.name === name && obj.isSigned === true);
      if (isSignSuccess === "success" && matchedSignStatus) {
        setValue({ aadharsignature: name }, ["aadharsignature"]);
        setIsSigned(true);
      }

      localStorage.removeItem("name");
      localStorage.removeItem("isSignSuccess");
    }
  };

  return { handleEsign, checkSignStatus };
};

export default useESign;
