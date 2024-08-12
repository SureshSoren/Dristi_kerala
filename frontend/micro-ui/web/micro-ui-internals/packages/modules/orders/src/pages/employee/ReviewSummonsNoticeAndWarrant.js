import React, { useEffect, useMemo, useState } from "react";
import { Header, InboxSearchComposer } from "@egovernments/digit-ui-react-components";
import { SummonsTabsConfig } from "../../configs/SuumonsConfig";
import { useTranslation } from "react-i18next";
import DocumentModal from "../../components/DocumentModal";
import PrintAndSendDocumentComponent from "../../components/Print&SendDocuments";
import DocumentViewerWithComment from "../../components/DocumentViewerWithComment";
import AddSignatureComponent from "../../components/AddSignatureComponent";
import CustomStepperSuccess from "../../components/CustomStepperSuccess";
import UpdateDeliveryStatusComponent from "../../components/UpdateDeliveryStatusComponent";
import { taskService } from "../../hooks/services";

const defaultSearchValues = {
  eprocess: "",
  caseId: "",
};

const ReviewSummonsNoticeAndWarrant = () => {
  const { t } = useTranslation();
  const tenantId = window?.Digit.ULBService.getCurrentTenantId();
  const [defaultValues, setDefaultValues] = useState(defaultSearchValues);
  const [config, setConfig] = useState(SummonsTabsConfig?.SummonsTabsConfig?.[0]);
  const [showActionModal, setShowActionModal] = useState(false);
  const [isSigned, setIsSigned] = useState(false);
  const [actionModalType, setActionModalType] = useState("");
  const [isDisabled, setIsDisabled] = useState(true);
  const [rowData, setRowData] = useState({});
  const [taskDocuments, setTaskDocumens] = useState([]);
  const [step, setStep] = useState(0);
  const [signatureId, setSignatureId] = useState("");

  const [tabData, setTabData] = useState(
    SummonsTabsConfig?.SummonsTabsConfig?.map((configItem, index) => ({ key: index, label: configItem.label, active: index === 0 ? true : false }))
  );

  const handleOpen = (props) => {
    //change status to signed or unsigned
  };

  const handleSubmitButtonDisable = (disable) => {
    console.log("disable :>> ", disable);
    setIsDisabled(disable);
  };

  const handleClose = () => {
    setShowActionModal(false);
  };
  useEffect(() => {
    // Set default values when component mounts
    setDefaultValues(defaultSearchValues);
    const isSignSuccess = localStorage.getItem("esignProcess");
    const isRowData = JSON.parse(localStorage.getItem("ESignSummons"));
    if (isSignSuccess) {
      if (rowData) {
        setRowData(isRowData);
      }
      setShowActionModal(true);
      setActionModalType("SIGN_PENDING");
      setStep(1);
      localStorage.removeItem("esignProcess");
      localStorage.removeItem("ESignSummons");
    }
  }, []);

  const onTabChange = (n) => {
    setTabData((prev) => prev.map((i, c) => ({ ...i, active: c === n ? true : false }))); //setting tab enable which is being clicked
    setConfig(SummonsTabsConfig?.SummonsTabsConfig?.[n]); // as per tab number filtering the config
  };

  const getTaskDocuments = async () => {
    try {
      const response = await window?.Digit?.DRISTIService.getTaskDocuments(
        {
          taskId: rowData?.id,
        },
        {}
      );
      console.log("response :>> ", response);
    } catch (error) {
      setTaskDocumens([
        {
          // fileName: "Summons Document",
          fileStoreId: "03e93220-7254-4877-ac80-bb808a722a61",
          documentName: "file_example_JPG_100kB.jpg",
          // documentType: "image/jpeg",
        },
        {
          // fileName: "Vakalatnama Document",
          fileStoreId: "03e93220-7254-4877-ac80-bb808a722a61",
          documentName: "file_example_JPG_100kB.jpg",
          // documentType: "image/jpeg",
        },
      ]);
    }
  };

  const infos = useMemo(() => {
    if (rowData?.taskDetails) {
      const caseDetails = JSON.parse(rowData?.taskDetails);
      return [
        { key: "Issued to", value: caseDetails?.respondentDetails?.name },
        { key: "Issued Date", value: rowData?.createdDate },
        { key: "Next Hearing Date", value: "04/07/2024" },
        { key: "Amount Paid", value: `Rs. ${caseDetails?.deliveryChannels?.fees}` },
        { key: "Channel Details", value: caseDetails?.deliveryChannels?.channelName },
      ];
    }
  }, [rowData]);

  const links = useMemo(() => {
    return [{ text: "View order", link: "" }];
  }, []);

  const documents = useMemo(() => {
    return taskDocuments;
  }, [taskDocuments]);

  const submissionData = useMemo(() => {
    return [
      { key: "SUBMISSION_DATE", value: "25-08-2001", copyData: false },
      { key: "SUBMISSION_ID", value: "875897348579453457", copyData: true },
    ];
  }, []);

  const handleSubmit = async () => {
    try {
      const localStorageID = localStorage.getItem("fileStoreId");
      const documents = Array.isArray(rowData?.documents) ? rowData.documents : [];
      const documentsFile =
        signatureId !== "" || localStorageID
          ? {
              documentType: "SIGNED",
              fileStore: signatureId || localStorageID,
            }
          : null;

      localStorage.removeItem("fileStoreId");

      const reqBody = {
        task: {
          ...rowData,
          documents: documentsFile ? [...documents, documentsFile] : documents,
          tenantId,
        },
        tenantId,
      };

      // Attempt to upload the document and handle the response
      const update = await taskService.UploadTaskDocument(reqBody, { tenantId });
      console.log("Document upload successful:", update);
    } catch (error) {
      // Handle errors that occur during the upload process
      console.error("Error uploading document:", error);
    }
  };

  const unsignedModalConfig = useMemo(() => {
    return {
      handleClose: handleClose,
      heading: { label: "Review Document: Summons Document" },
      actionSaveLabel: "E-sign",
      isStepperModal: true,
      actionSaveOnSubmit: () => {},
      steps: [
        {
          type: "document",
          modalBody: <DocumentViewerWithComment infos={infos} documents={documents} links={links} />,
          actionSaveOnSubmit: () => {},
        },
        {
          heading: { label: "Add Signature (1)" },
          actionSaveLabel: "Send Email",
          actionCancelLabel: "Back",
          modalBody: (
            <AddSignatureComponent
              t={t}
              isSigned={isSigned}
              handleSigned={() => setIsSigned(true)}
              rowData={rowData}
              setSignatureId={setSignatureId}
            />
          ),
          isDisabled: isSigned ? false : true,
          actionSaveOnSubmit: handleSubmit,
        },
        {
          type: "success",
          hideSubmit: true,
          modalBody: <CustomStepperSuccess closeButtonAction={handleClose} t={t} submissionData={submissionData} documents={documents} />,
        },
      ],
    };
  }, [documents, infos, isSigned, links, submissionData, t]);

  const signedModalConfig = useMemo(() => {
    return {
      handleClose: handleClose,
      heading: { label: "Print & Send Documents" },
      actionSaveLabel: "Mark As Sent",
      isStepperModal: false,
      modalBody: <PrintAndSendDocumentComponent infos={infos} documents={documents} links={links} t={t} />,
      actionSaveOnSubmit: handleClose,
    };
  }, [documents, infos, links, t]);

  const sentModalConfig = useMemo(() => {
    return {
      handleClose: handleClose,
      heading: { label: "Print & Send Documents" },
      actionSaveLabel: "Mark As Sent",
      isStepperModal: false,
      modalBody: <UpdateDeliveryStatusComponent infos={infos} links={links} t={t} handleSubmitButtonDisable={handleSubmitButtonDisable} />,
      actionSaveOnSubmit: handleClose,
      isDisabled: isDisabled,
    };
  }, [infos, isDisabled, links, t]);

  useEffect(() => {
    if (rowData?.id) getTaskDocuments();
  }, [rowData]);

  return (
    <div className="review-summon-warrant">
      <div className="header-wraper">
        <Header>{t("REVIEW_SUMMON_NOTICE_WARRANTS_TEXT")}</Header>
      </div>

      <div className="inbox-search-wrapper pucar-home home-view">
        {/* Pass defaultValues as props to InboxSearchComposer */}
        <InboxSearchComposer
          configs={config}
          defaultValues={defaultValues}
          showTab={true}
          tabData={tabData}
          onTabChange={onTabChange}
          additionalConfig={{
            resultsTable: {
              onClickRow: (props) => {
                console.log("props?.original :>> ", props?.original);
                setRowData(props?.original);
                setActionModalType("SIGN_PENDING");
                setShowActionModal(true);
                setStep(0);
                setIsSigned(props?.original?.documentStatus === "SIGN_PENDING" ? false : true);
              },
            },
          }}
        ></InboxSearchComposer>
        {showActionModal && (
          <DocumentModal
            config={config?.label === "Pending" ? (actionModalType !== "SIGN_PENDING" ? signedModalConfig : unsignedModalConfig) : sentModalConfig}
            currentStep={step}
          />
        )}
      </div>
    </div>
  );
};

export default ReviewSummonsNoticeAndWarrant;
