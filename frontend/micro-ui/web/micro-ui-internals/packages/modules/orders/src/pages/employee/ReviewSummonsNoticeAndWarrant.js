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
import { formatDate } from "../../utils";
import { taskService } from "../../hooks/services";

const defaultSearchValues = {
  eprocess: "",
  caseId: "",
};

const handleTaskDetails = (taskDetails) => {
  try {
    const parsed = JSON.parse(taskDetails);
    if (typeof parsed === "string") {
      return JSON.parse(parsed);
    }
    return parsed;
  } catch (error) {
    console.error("Failed to parse taskDetails:", error);
    return null;
  }
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
  const [nextHearingDate, setNextHearingDate] = useState();
  const [step, setStep] = useState(0);
  const [signatureId, setSignatureId] = useState("");
  const [deliveryChannel, setDeliveryChannel] = useState("");
  const [refetch, setRefetch] = useState(false);
  const [taskDetails, setTaskDetails] = useState({});

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
    localStorage.removeItem("SignedFileStoreID");
    setShowActionModal(false);
    setRefetch(!refetch);
  };
  useEffect(() => {
    // Set default values when component mounts
    setDefaultValues(defaultSearchValues);
    const isSignSuccess = localStorage.getItem("esignProcess");
    const isRowData = JSON.parse(localStorage.getItem("ESignSummons"));
    const delieveryCh = localStorage.getItem("delieveryChannel");
    if (isSignSuccess) {
      if (rowData) {
        setRowData(isRowData);
      }
      if (delieveryCh) {
        setDeliveryChannel(delieveryCh);
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

  function findNextHearings(objectsList) {
    const now = Date.now();
    const futureStartTimes = objectsList.filter((obj) => obj.startTime > now);
    futureStartTimes.sort((a, b) => a.startTime - b.startTime);
    return futureStartTimes.length > 0 ? futureStartTimes[0] : null;
  }

  const getHearingFromCaseId = async () => {
    try {
      const response = await Digit.HearingService.searchHearings(
        {
          criteria: {
            tenantId: Digit.ULBService.getCurrentTenantId(),
            filingNumber: rowData?.filingNumber,
          },
        },
        {}
      );
      setNextHearingDate(findNextHearings(response?.HearingList));
    } catch (error) {
      console.error("error :>> ", error);
    }
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
    if (rowData?.taskDetails || nextHearingDate) {
      const caseDetails = handleTaskDetails(rowData?.taskDetails);
      return [
        { key: "Issued to", value: caseDetails?.respondentDetails?.name },
        { key: "Issued Date", value: rowData?.createdDate },
        // { key: "Next Hearing Date", value: nextHearingDate?.startTime ? formatDate(nextHearingDate?.startTime) : "N/A" },
        { key: "Amount Paid", value: `Rs. ${caseDetails?.deliveryChannels?.fees || 100}` },
        { key: "Channel Details", value: caseDetails?.deliveryChannels?.channelName },
      ];
    }
  }, [rowData, nextHearingDate]);

  const links = useMemo(() => {
    return [{ text: "View order", link: "" }];
  }, []);

  const documents = useMemo(() => {
    if (rowData?.documents)
      return rowData?.documents?.map((document) => {
        return { ...document, fileName: "Summons Document" };
      });
  }, [rowData]);

  const submissionData = useMemo(() => {
    return [
      { key: "SUBMISSION_DATE", value: "25-08-2001", copyData: false },
      { key: "SUBMISSION_ID", value: "875897348579453457", copyData: true },
    ];
  }, []);

  const handleSubmitEsign = async () => {
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
      localStorage.setItem("SignedFileStoreID", documentsFile?.fileStore);
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
          actionSaveLabel: deliveryChannel === "Post" ? "Proceed to Send" : "Send Email",
          actionCancelLabel: "Back",
          modalBody: (
            <AddSignatureComponent
              t={t}
              isSigned={isSigned}
              handleSigned={() => setIsSigned(true)}
              rowData={rowData}
              setSignatureId={setSignatureId}
              deliveryChannel={deliveryChannel}
            />
          ),
          isDisabled: isSigned ? false : true,
          actionSaveOnSubmit: handleSubmitEsign,
        },
        {
          type: "success",
          hideSubmit: true,
          modalBody: (
            <CustomStepperSuccess
              closeButtonAction={handleClose}
              t={t}
              submissionData={submissionData}
              documents={documents}
              deliveryChannel={deliveryChannel}
            />
          ),
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
      modalBody: (
        <PrintAndSendDocumentComponent infos={infos} documents={documents?.filter((docs) => docs.documentType === "SIGNED")} links={links} t={t} />
      ),
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
    // if (rowData?.id) getTaskDocuments();
    if (rowData?.filingNumber) getHearingFromCaseId();
  }, [rowData]);

  return (
    <div className="review-summon-warrant">
      <div className="header-wraper">
        <Header>{t("REVIEW_SUMMON_NOTICE_WARRANTS_TEXT")}</Header>
      </div>

      <div className="inbox-search-wrapper pucar-home home-view">
        {/* Pass defaultValues as props to InboxSearchComposer */}
        <InboxSearchComposer
          key={`inbox-composer-${refetch}`}
          configs={config}
          defaultValues={defaultValues}
          showTab={true}
          tabData={tabData}
          onTabChange={onTabChange}
          additionalConfig={{
            resultsTable: {
              onClickRow: (props) => {
                setRowData(props?.original);
                setActionModalType(props?.original?.documentStatus);
                setShowActionModal(true);
                setStep(0);
                setIsSigned(props?.original?.documentStatus === "SIGN_PENDING" ? false : true);
                setDeliveryChannel(handleTaskDetails(props?.original?.taskDetails)?.deliveryChannels?.channelName);
                setTaskDetails(handleTaskDetails(props?.original?.taskDetails));
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
