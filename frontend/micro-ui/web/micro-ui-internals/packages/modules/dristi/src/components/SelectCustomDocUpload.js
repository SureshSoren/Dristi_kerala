import React, { useState, useEffect } from "react";
import { FileAttachIcon } from "../icons/svgIndex";
import RenderFileUpload from "./RenderFileUpload";
import UploadDocument from "./UploadDocument";
import ESignSignatureModal from "./ESignSignatureModal";

const DragDropJSX = ({ t, currentValue, error }) => {
  return (
    <React.Fragment>
      <div style={{ marginTop: "10px", cursor: "pointer" }}>
        <div style={{ color: "#505A5F" }}>
          <FileAttachIcon />
        </div>
      </div>
    </React.Fragment>
  );
};

function SelectCustomDocUpload({ t, config, formUploadData = {}, setData }) {
  const [upload, setUpload] = useState(false);
  const [eSignModal, setEsignModal] = useState(false);
  const [showDocument, setShowDocument] = useState(false);

  const handleOpenUploadModal = () => {
    setUpload(true);
  };

  const handleCancelUpload = () => {
    setUpload(false);
    setEsignModal(false);
  };

  const handleUploadProceed = () => {
    setUpload(false);
    setEsignModal(true);
  };

  const handleGoBackSignatureModal = () => {
    setUpload(true);
    setEsignModal(false);
  };

  const handleEsign = () => {
    setShowDocument(true);
    setEsignModal(false);
  };

  const handleDeleteFile = () => {
    setShowDocument(false);
    setData({});
  };

  return (
    <React.Fragment>
      {!showDocument && (
        <div className="file-uploader-div-main show-file-uploader">
          <button style={{ background: "none" }} onClick={handleOpenUploadModal}>
            <DragDropJSX />
          </button>
        </div>
      )}
      {upload && (
        <UploadDocument
          config={config}
          t={t}
          handleCancelUpload={handleCancelUpload}
          handleUploadProceed={handleUploadProceed}
          formUploadData={formUploadData}
          setData={setData}
        />
      )}
      {eSignModal && (
        <ESignSignatureModal
          t={t}
          saveOnsubmitLabel={t("Add Document")}
          doctype={formUploadData?.SelectUserTypeComponent?.selectIdType?.code}
          handleGoBackSignatureModal={handleGoBackSignatureModal}
          handleIssueOrder={handleEsign}
        />
      )}
      {showDocument && (
        <div className="drag-drop-visible-main">
          {formUploadData?.SelectUserTypeComponent?.doc?.map((fileData, index) => (
            <RenderFileUpload
              key={`${fileData[0]}-${index}`}
              index={index}
              fileData={fileData?.[1]?.file}
              fileStoreId={fileData?.[1]?.fileStoreId}
              handleDeleteFile={handleDeleteFile}
              t={t}
              displayName={`${config?.[0]?.body?.[0]?.populators?.inputs?.[0]?.options?.[0]?.name}`}
            />
          ))}
        </div>
      )}
    </React.Fragment>
  );
}

export default SelectCustomDocUpload;
