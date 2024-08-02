import React, { useEffect, useMemo, useState } from "react";
import SignatureCard from "./SignatureCard";
import { DRISTIService } from "../services";
import { useLocation } from "react-router-dom/cjs/react-router-dom.min";

function SelectSignature({ t, config, onSelect, formData = {}, errors }) {
  const inputs = useMemo(
    () =>
      config?.populators?.inputs || [
        {
          key: "complainantDetails",
          label: "CS_COMPLAINT_DETAILS",
          icon: "LitigentIcon",
          config: [{ type: "title", value: "name" }],
          data: [{ name: "Sheetal Arora" }, { name: "Mehul Das" }],
        },
      ],
    [config?.populators?.inputs]
  );

  const storedObj = useMemo(() => localStorage.getItem("signStatus"), []);
  const parsedObj = JSON.parse(storedObj) || [];
  const handleAadharClick = async (data, name) => {
    try {
      const newSignStatuses = [...parsedObj, { name: name, isSigned: true }];
      localStorage.setItem("signStatus", JSON.stringify(newSignStatuses));

      const eSignResponse = await DRISTIService.eSignService({
        ESignParameter: {
          uidToken: "3456565",
          consent: "6564",
          authType: "6546",
          fileStoreId: process.env.REACT_APP_ESIGN_FILESTORE_ID,
          tenantId: "kl",
          pageModule: "ci",
        },
      });
      if (eSignResponse) {
        const eSignData = {
          path: window.location.pathname,
          param: window.location.search,
          isEsign: true,
          data: data,
        };
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
  };
  return (
    <div className="select-signature-main">
      {inputs.map((input, inputIndex) => (
        <React.Fragment>
          <div className="select-signature-header">
            <h1 className="signature-label">{config?.label}</h1>
          </div>
          <div className="select-signature-list">
            {input.data.map((item, itemIndex) => (
              <SignatureCard
                key={inputIndex + itemIndex}
                index={itemIndex}
                data={item}
                input={input}
                t={t}
                formData={formData}
                onSelect={onSelect}
                configKey={config.key}
                handleAadharClick={handleAadharClick}
              />
            ))}
          </div>
        </React.Fragment>
      ))}
    </div>
  );
}

export default SelectSignature;
