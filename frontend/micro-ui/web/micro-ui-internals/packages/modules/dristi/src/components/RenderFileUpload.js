import React, { useEffect, useState } from "react";
import { CloseIcon, FileIcon } from "../icons/svgIndex";
function RenderFileUpload({ handleDeleteFile, fileData, input, index, uploadErrorInfo, disableUploadDelete = false }) {
  const [file, setFile] = useState(null);

  useEffect(() => {
    if (fileData.fileStore) {
      const draftFile = new File(["draft content"], fileData.documentName, {
        type: fileData.documentType,
      });
      setFile(draftFile);
    }
  }, [fileData]);

  return (
    <div className={`uploaded-file-div-main upload-${!!uploadErrorInfo ? "error" : "successful"}`} style={{ padding: "10px" }}>
      <div className={`uploaded-file-div-sub ${!!uploadErrorInfo ? "error" : ""}`}>
        <div className="uploaded-file-div-icon-area">
          <div className="uploaded-file-icon">
            <FileIcon />
          </div>
          <span style={{ margin: "2px", color: "#505A5F", fontSize: "14px" }}>{fileData.fileStore ? file?.name : fileData?.name}</span>
        </div>
        <div className="reupload-or-delete-div">
          <div
            style={{
              padding: "0",
              cursor: disableUploadDelete ? "not-allowed" : "pointer",
              opacity: disableUploadDelete ? 0.5 : 1,
            }}
            onClick={() => {
              if (!disableUploadDelete) {
                handleDeleteFile(input, index);
              }
            }}
            key={`Delete-${input.name}`}
            className="delete-button"
          >
            <CloseIcon />
          </div>
        </div>
      </div>
    </div>
  );
}

export default RenderFileUpload;
