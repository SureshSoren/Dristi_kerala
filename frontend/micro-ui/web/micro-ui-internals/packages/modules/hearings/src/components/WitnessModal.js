import { Button, CardText, Modal } from "@egovernments/digit-ui-react-components";
import { FileUploadIcon } from "../../../dristi/src/icons/svgIndex";
import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Urls } from "../hooks/services/Urls";
import { hearingService } from "../hooks/services";

const Heading = (props) => {
  return (
    <div style={{ width: "300px", height: "28px" }}>
      <p
        style={{
          fontWeight: 700,
          fontSize: "24px",
          lineHeight: "28.13px",
          color: "#0A0A0A",
        }}
      >
        {props.label}
      </p>
    </div>
  );
};

const Close = () => (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#0A0A0A">
    <path d="M0 0h24v24H0V0z" fill="none" />
    <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
  </svg>
);

const ForwardArrowIcon = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" className="bi bi-arrow-right-short" viewBox="0 0 16 16">
    <path
      fillRule="evenodd"
      d="M4 8a.5.5 0 0 1 .5-.5h5.793L8.146 5.354a.5.5 0 1 1 .708-.708l3 3a.5.5 0 0 1 0 .708l-3 3a.5.5 0 0 1-.708-.708L10.293 8.5H4.5A.5.5 0 0 1 4 8"
    />
  </svg>
);

const CloseBtn = (props) => {
  return (
    <div onClick={props?.onClick} style={{ paddingTop: 0 }}>
      <div className={"icon-bg-secondary"} style={{ backgroundColor: "#ffff", cursor: "pointer" }}>
        {" "}
        <Close />{" "}
      </div>
    </div>
  );
};

const BackBtn = ({ text }) => {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
      <div>{text}</div>
      <div style={{ width: "24px", height: "24px", marginLeft: "4px" }}>
        <ForwardArrowIcon />
      </div>
    </div>
  );
};

const WitnessModal = ({ handleClose, hearingId, setSignedDocumentUploadID, handleProceed }) => {
  const { t } = useTranslation();
  const [isUploaded, setUploaded] = useState(false);
  const UploadSignatureModal = window?.Digit?.ComponentRegistryService?.getComponent("UploadSignatureModal");
  const tenantId = window?.Digit.ULBService.getCurrentTenantId();
  const [formData, setFormData] = useState({}); // storing the file upload data
  const [openUploadSignatureModal, setOpenUploadSignatureModal] = useState(false);
  const { uploadDocuments } = Digit.Hooks.orders.useDocumentUpload();
  const name = "Signature";
  const uploadModalConfig = useMemo(() => {
    return {
      key: "uploadSignature",
      populators: {
        inputs: [
          {
            name: name,
            documentHeader: "Signature",
            type: "DragDropComponent",
            uploadGuidelines: "Ensure the image is not blurry and under 5MB.",
            maxFileSize: 5,
            maxFileErrorMessage: "CS_FILE_LIMIT_5_MB",
            fileTypes: ["JPG", "PNG", "JPEG"],
            isMultipleUpload: false,
          },
        ],
        validation: {},
      },
    };
  }, [name]);

  const onSelect = (key, value) => {
    if (value === null) {
      setFormData({});
    } else {
      setFormData((prevData) => ({
        ...prevData,
        [key]: value,
      }));
    }
  };

  useEffect(() => {
    const upload = async () => {
      if (formData?.uploadSignature?.Signature?.length > 0) {
        const uploadedFileId = await uploadDocuments(formData?.uploadSignature?.Signature, tenantId);
        setSignedDocumentUploadID(uploadedFileId?.[0]?.fileStoreId);
        setUploaded(true);
      }
    };

    upload();
  }, [formData]);

  const reqBody = {
    hearing: { tenantId },
    criteria: {
      tenantID: tenantId,
      hearingId: hearingId,
    },
  };

  const handleDownload = async () => {
    try {
      const res = await hearingService.customApiService(Urls.hearing.downloadWitnesspdf, reqBody, { applicationNumber: "", cnrNumber: "" });
      // complete the download part
      console.log(res, "lllppp");
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div>
      {!openUploadSignatureModal && (
        <Modal
          popupStyles={{
            height: "300px", // Adjusted height for the modal
            maxHeight: "300px",
            width: "500px", // Width of the modal
            position: "absolute",
            top: "50%",
            left: "50%",
            transform: "translate(-50%, -50%)",
            borderRadius: "4px",
          }}
          headerBarMainStyle={{
            padding: "0px 24px 0px 24px",
          }}
          popupModuleActionBarStyles={{
            display: "flex",
            justifyContent: "flex-end",
            position: "absolute",
            right: 0,
            bottom: 0,
            width: "100%",
            borderTop: "1px solid #dbd7d2",
            padding: "10px 16px 16px 0px",
          }}
          actionSaveLabelStyles={{
            backgroundColor: "#BB2C2F",
            width: "150px",
            height: "40px",
            padding: "8px 24px",
          }}
          headerBarMain={<Heading label={t("Witness Deposition Upload")} />}
          headerBarEnd={<CloseBtn onClick={handleClose} />}
          actionSaveLabel={<BackBtn text={t("PROCEED")} />}
          isDisabled={!isUploaded}
          actionSaveOnSubmit={() => handleProceed()} // pass the handler of next modal
          formId="modal-action"
        >
          <div
            style={{
              height: "calc(100% - 50px)",
              padding: "5px 24px",
              width: "100%",
              display: "flex",
              flexDirection: "column",
              justifyContent: "center",
            }}
          >
            {!isUploaded ? (
              <React.Fragment>
                <div className="sign-button-wrap">
                  <Button
                    icon={<FileUploadIcon />}
                    label={t("Upload the Signatured Document")}
                    onButtonClick={() => {
                      setOpenUploadSignatureModal(true);
                    }}
                    className={"upload-signature"}
                    labelClassName={"upload-signature-label"}
                  />
                </div>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "flex-start", gap: "20px" }}>
                  <div style={{ display: "flex", alignItems: "center" }}>
                    <h2 style={{ margin: 0 }}>{t("Download the Witness Deposition")}</h2>
                    <button
                      style={{ marginLeft: "10px", color: "#007E7E", cursor: "pointer", textDecoration: "underline", background: "none" }}
                      onClick={handleDownload}
                    >
                      {t("CLICK_HERE")}
                    </button>
                  </div>
                </div>
              </React.Fragment>
            ) : (
              <div>
                <div style={{ backgroundColor: "#E4F2E4", fontWeight: 700, borderRadius: "5px", fontSize: "24px", color: "teal", padding: "3px" }}>
                  <p>{t("Documented Uploaded")}</p>
                </div>
              </div>
            )}
          </div>
        </Modal>
      )}
      {openUploadSignatureModal && (
        <UploadSignatureModal
          t={t}
          key={name}
          name={name}
          setOpenUploadSignatureModal={setOpenUploadSignatureModal}
          onSelect={onSelect}
          config={uploadModalConfig}
          formData={formData}
        />
      )}
    </div>
  );
};

export default WitnessModal;
