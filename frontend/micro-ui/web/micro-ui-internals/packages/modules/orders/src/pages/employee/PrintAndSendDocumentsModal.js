import React, { useState } from "react";
import { Modal, CardLabel } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { printAndSendDocumentsConfig } from "./../../configs/EpostFormConfigs";
import { FormComposerV2 } from "@egovernments/digit-ui-react-components";
import { Urls } from "../../hooks/services/Urls";

const PrintAndSendDocumentsModal = ({ onClose, stepper, setStepper, rowData, form, setForm, fileStoreId }) => {
  const [tempForm, setTempForm] = useState(form);
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCurrentTenantId();

  const config = printAndSendDocumentsConfig;

  const uri = `${window.location.origin}${Urls.FileFetchById}?tenantId=${tenantId}&&fileStoreId=${fileStoreId}`;

  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const Document = () => (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_5177_71858)">
        <path
          d="M14 2H6C4.9 2 4.01 2.9 4.01 4L4 20C4 21.1 4.89 22 5.99 22H18C19.1 22 20 21.1 20 20V8L14 2ZM16 18H8V16H16V18ZM16 14H8V12H16V14ZM13 9V3.5L18.5 9H13Z"
          fill="#505A5F"
        />
      </g>
      <defs>
        <clipPath id="clip0_5177_71858">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );

  const Printer = () => (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_13044_11671)">
        <path d="M0 0H20V20H0V0Z" fill="white" />
        <path
          d="M15.8337 6.66667H4.16699C2.78366 6.66667 1.66699 7.78333 1.66699 9.16667V14.1667H5.00033V17.5H15.0003V14.1667H18.3337V9.16667C18.3337 7.78333 17.217 6.66667 15.8337 6.66667ZM13.3337 15.8333H6.66699V11.6667H13.3337V15.8333ZM15.8337 10C15.3753 10 15.0003 9.625 15.0003 9.16667C15.0003 8.70833 15.3753 8.33333 15.8337 8.33333C16.292 8.33333 16.667 8.70833 16.667 9.16667C16.667 9.625 16.292 10 15.8337 10ZM15.0003 2.5H5.00033V5.83333H15.0003V2.5Z"
          fill="#007E7E"
        />
      </g>
      <defs>
        <clipPath id="clip0_13044_11671">
          <rect width="20" height="20" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );

  const CloseBtn = (props) => {
    return (
      <div onClick={props?.onClick} style={props?.isMobileView ? { padding: 5 } : null}>
        <div className={"icon-bg-secondary"} style={{ backgroundColor: "#505A5F" }}>
          {" "}
          <Close />{" "}
        </div>
      </div>
    );
  };
  const clickableTextStyle = {
    color: "#007E7E",
    textDecoration: "underline",
    cursor: "pointer",
    paddingRight: "1rem",
  };

  const onFormValueChange = (setValue, formData, formState) => {
    if (JSON.stringify(tempForm) !== JSON.stringify(formData)) {
      setTempForm(formData);
    }
  };

  return (
    <Modal
      popupStyles={{
        height: "auto",
        maxHeight: "700px",
        width: "700px",
        position: "absolute",
        top: "50%",
        left: "50%",
        transform: "translate(-50%, -50%)",
        padding: "20px",
      }}
      headerBarMain={<h1 className="heading-m">{t("Print & Send Documents")}</h1>}
      headerBarEnd={<CloseBtn onClick={onClose} />}
      actionCancelLabel={t("Skip")}
      actionCancelOnSubmit={() => setStepper(stepper + 1)}
      actionSaveLabel={t("Save & Proceed")}
      actionSaveOnSubmit={() => {
        setForm(tempForm);
        setStepper(stepper + 1);
      }}
      isDisabled={rowData.original.deliveryStatus === "DELIVERED" || rowData.original.deliveryStatus === "NOT_DELIVERED" ? true : false}
    >
      <div style={{ padding: "16px", marginTop: "1rem" }}>
        <div style={{ backgroundColor: "#F7F5F3", padding: "1rem" }}>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
            <strong>{t("E-post fees")}</strong>
            <span>₹575</span>
            <span style={clickableTextStyle} onClick={() => console.log("View Details clicked")}>
              {t("View Details")}
            </span>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <strong>{t("Received on")}</strong>
            <span>04/07/2024, 12:56</span>
          </div>
        </div>
        <div style={{ marginBottom: "16px" }}>
          <CardLabel style={{ paddingBottom: "1rem" }}>{t("Print Documents (2)")}</CardLabel>
          <div style={{ display: "flex", justifyContent: "space-between", paddingBottom: "1rem" }}>
            <div>
              <Document style={{ paddingLeft: "1rem" }} />
              <span>{t("Summons Document")}</span>
            </div>
            <div style={{ display: "flex" }}>
              <Printer />
              <a
                href={uri}
                target="_blank"
                rel="noreferrer"
                style={{
                  display: "flex",
                  color: "#007E7E",
                  textDecoration: "underline",
                  whiteSpace: "nowrap",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  paddingRight: "1rem",
                  paddingLeft: "5px",
                }}
              >
                {t("Print")}
              </a>
            </div>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", paddingTop: "1rem" }}>
            <div>
              <Document style={{ paddingLeft: "1rem" }} />
              <span>{t("Receiver's Address")}</span>
            </div>
            <div style={{ display: "flex" }}>
              <Printer />
              <a
                href={uri}
                target="_blank"
                rel="noreferrer"
                style={{
                  display: "flex",
                  color: "#007E7E",
                  textDecoration: "underline",
                  whiteSpace: "nowrap",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  paddingRight: "1rem",
                  paddingLeft: "5px",
                }}
              >
                {t("Print")}
              </a>
            </div>
          </div>
        </div>
        <div>
          <CardLabel style={{ paddingBottom: "1rem" }}>{t("Speed Post Details")}</CardLabel>
          <FormComposerV2
            className="formConfig"
            key={"printAndSendDocuments"}
            config={config}
            onFormValueChange={onFormValueChange}
            defaultValues={{
              barCode: `${rowData?.original?.trackingNumber}`,
              dateofBooking: `${rowData?.original?.bookingDate}`,
            }}
          />
        </div>
      </div>
    </Modal>
  );
};
export default PrintAndSendDocumentsModal;
