import React, { useState, useEffect, useCallback } from "react";
import { InboxSearchComposer } from "@egovernments/digit-ui-react-components";
import { EpostTrackingConfig } from "../../../../../../../../../micro-ui/web/micro-ui-internals/packages/modules/orders/src/configs/E-PostTrackingConfig";
import { HomeService } from "../../../../../../../../../micro-ui/web/micro-ui-internals/packages/modules/home/src/hooks/services";
import { useLocation } from "react-router-dom/cjs/react-router-dom.min";
import PrintAndSendDocumentsModal from "./PrintAndSendDocumentsModal";
import UpdateEPostStatus from "./UpdateEPostStatus";
import { Modal } from '@egovernments/digit-ui-react-components';
import { useTranslation } from 'react-i18next';


const EpostTrackingPage = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const { state } = location;
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const outboxFilters = ["DELIVERED", "NOT_DELIVERED"];
  const inboxFilters = ["IN_TRANSIT", "NOT_UPDATED"];
  const [config, setConfig] = useState(EpostTrackingConfig({ inboxFilters, outboxFilters })?.[0]);
  const [stepper, setStepper] = useState(0);
  const [rowData, setRowData] = useState();
  const [form, setForm] = useState();
  const DocViewerWrapper = Digit?.ComponentRegistryService?.getComponent("DocViewerWrapper");
  const [fileStoreId, setFileStoreID] = useState("2aefb901-edc6-4a45-95f8-3ea383a513f5");
  const [showDocument, setShowDocument] = useState(false);
  const [tabData, setTabData] = useState(
    EpostTrackingConfig({ inboxFilters, outboxFilters })?.map((configItem, index) => ({
      key: index,
      label: configItem.label,
      active: index === 0 ? true : false,
    }))
  );

  const onTabChange = (n) => {
    setTabData((prev) => prev.map((i, c) => ({ ...i, active: c === n ? true : false })));
    setConfig(EpostTrackingConfig({ inboxFilters, outboxFilters })?.[n]);
  };

  useEffect(() => {
    getTotalCountForTab(EpostTrackingConfig({ inboxFilters, outboxFilters }));
  }, [state]);

  const getTotalCountForTab = useCallback(
    async function (tabConfig) {
      const updatedTabData = await Promise.all(
        tabConfig?.map(async (configItem, index) => {
          const response = await HomeService.customApiService(configItem?.apiDetails?.serviceName, configItem?.apiDetails?.requestBody);
          const totalCount = response?.EPostTracker?.length;
          return {
            key: index,
            label: totalCount ? `${configItem.label} (${totalCount})` : `${configItem.label} (0)`,
            active: index === 0 ? true : false,
          };
        }) || []
      );
      setTabData(updatedTabData);
    },
    [tenantId]
  );

  const onClose = () => {
    if(showDocument) {
      setShowDocument(false);
    } else {
      setStepper(0);
    }
  }

  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF" style={{ width: '24px', height: '24px' }}>
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const CloseBtn = ({ onClick, isMobileView }) => (
    <div onClick={onClick} style={isMobileView ? { padding: 5 } : null}>
      <div style={{ backgroundColor: "#505A5F", display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '8px' }}>
        <Close />
      </div>
    </div>
  );

  const onRowClick = (row) => {
    setRowData(row);
    // setFileStoreID(rowData?.original?.fileStoreId);
    setStepper(stepper + 1);
  }

  return (
    <div style={{ padding: "16px", margin: "24px" }}>
      <div style={{ display: "flex", alignItems: "center", gap: "16px" }}><svg width="40" height="41" viewBox="0 0 40 41" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M20 2.16699C14.4767 2.16699 10 6.64366 10 12.167C10 19.3103 20 30.5003 20 30.5003C20 30.5003 30 19.3103 30 12.167C30 6.64366 25.5234 2.16699 20 2.16699ZM20 5.50033C23.6767 5.50033 26.6667 8.49033 26.6667 12.167C26.6667 15.517 23.1985 21.101 19.9968 25.2659C16.7951 21.1076 13.3334 15.527 13.3334 12.167C13.3334 8.49033 16.3234 5.50033 20 5.50033ZM20 8.83366C19.116 8.83366 18.2681 9.18485 17.643 9.80997C17.0179 10.4351 16.6667 11.2829 16.6667 12.167C16.6667 13.051 17.0179 13.8989 17.643 14.524C18.2681 15.1491 19.116 15.5003 20 15.5003C20.8841 15.5003 21.7319 15.1491 22.3571 14.524C22.9822 13.8989 23.3334 13.051 23.3334 12.167C23.3334 11.2829 22.9822 10.4351 22.3571 9.80997C21.7319 9.18485 20.8841 8.83366 20 8.83366ZM8.00134 25.5003L3.33337 37.167H36.6667L31.9987 25.5003H28.0209C27.2125 26.7187 26.401 27.847 25.6543 28.8337H29.7461L31.7448 33.8337H8.25525L10.2539 28.8337H14.3457C13.5991 27.847 12.7875 26.7187 11.9792 25.5003H8.00134Z" fill="#77787B" />
      </svg>
        <strong style={{ fontSize: "24px", fontFamily: "roboto" }}>
          Kollam Postal Hub
        </strong>
      </div>
      <div style={{ margin: "24px" }}>

        <strong style={{ fontSize: "24px" }}>
          Online Process Memo
        </strong>
      </div>
      <InboxSearchComposer configs={config}
        showTab={true}
        tabData={tabData}
        onTabChange={onTabChange}
        additionalConfig={{
          resultsTable: {
            onClickRow: onRowClick,
          },
        }}
      >

      </InboxSearchComposer>
      {stepper === 1 && !showDocument && <PrintAndSendDocumentsModal onClose={onClose} stepper={stepper} setStepper={setStepper} rowData={rowData} form={form} setForm={setForm} fileStoreId={fileStoreId} />}
      {stepper === 2 && !showDocument && <UpdateEPostStatus onClose={onClose} rowData={rowData} form={form} setForm={setForm} setShowDocument={setShowDocument} />}
      {showDocument && <Modal
          popupStyles={{
              height: "80%",
              width: "60%",
              top: "10%",
              left: "20%",
          }}
          headerBarMain={<h1 className="heading-m">{t("Documents")}</h1>}
          headerBarEnd={<CloseBtn onClick={onClose} />}
          popupModuleActionBarStyles={{
            display: "none",
          }}
      >
         <DocViewerWrapper
            docWidth={"calc(80vw* 62/ 100)"}
            docHeight={"60vh"}
            fileStoreId={fileStoreId}
            tenantId={tenantId}
          /> 
      </Modal>}
    </div>
  );
}


export default EpostTrackingPage;