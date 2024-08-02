import React, { useEffect, useState, useTransition } from "react";
import { Header, Button, InboxSearchComposer } from "@egovernments/digit-ui-react-components";
import PrintAndSendDocumentModal from "../../components/Print&SendDocuments";
import { SummonsTabsConfig } from "../../configs/SuumonsConfig";
import { useTranslation } from "react-i18next";
import ReviewDocumentModal from "../../components/ReviewDocumentModal";
import OrderSignatureModal from "../../pageComponents/OrderSignatureModal";
import OrderSucessModal from "../../pageComponents/OrderSucessModal";
import { useHistory } from "react-router-dom";

const defaultSearchValues = {
  eprocess: "",
  caseId: "",
};

const ReviewSummonsNoticeAndWarrant = () => {
  const { t } = useTranslation();
  const [openSigned, setOpenSigned] = useState(false);
  const [openUnsigned, setOpenUnsigned] = useState(false);
  const [defaultValues, setDefaultValues] = useState(defaultSearchValues);
  const [config, setConfig] = useState(SummonsTabsConfig?.SummonsTabsConfig?.[0]);
  const [showsignatureModal, setShowsignatureModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const history = useHistory();
  const order = {
    orderType: "Summons Document",
    orderModelInfo: {
      header: "You have successfully sent summons via email",
      subHeader: "CS_ORDER_CREATED_SUBTEXT",
    },
  };

  const [tabData, setTabData] = useState(
    SummonsTabsConfig?.SummonsTabsConfig?.map((configItem, index) => ({ key: index, label: configItem.label, active: index === 0 ? true : false }))
  );

  const handleOpen = (props) => {
    //change status to signed or unsigned
    if (props.values.status === "PAYMENT_PENDING") setOpenSigned(true);
    else setOpenUnsigned(true);
  };

  const handleClose = () => {
    setOpenSigned(false);
    setOpenUnsigned(false);
  };
  useEffect(() => {
    // Set default values when component mounts
    setDefaultValues(defaultSearchValues);
    const isSignSuccess = localStorage.getItem("esignProcess");
    if (isSignSuccess) {
      setShowsignatureModal(true);
      localStorage.removeItem("esignProcess");
    }
  }, []);

  const onTabChange = (n) => {
    setTabData((prev) => prev.map((i, c) => ({ ...i, active: c === n ? true : false }))); //setting tab enable which is being clicked
    setConfig(SummonsTabsConfig?.SummonsTabsConfig?.[n]); // as per tab number filtering the config
  };

  const handleGoBackSignatureModal = () => {
    setShowsignatureModal(false);
    setOpenSigned(true);
  };

  const handleSendEmail = () => {
    setShowSuccessModal(true);
  };

  const handleCloseSuccessfulModal = () => {
    history.push(`/${window.contextPath}/employee/orders/Summons&Notice`, {
      from: "orderSuccessModal",
    });
    setShowSuccessModal(false);
  };

  return (
    <div style={{ width: "1,440px", height: "1,227px", padding: "48px 64px" }}>
      <div>
        <div style={{ display: "flex", gap: "20px" }}>
          <Header styles={{ fontSize: "32px" }}>{t("Review Summons, Notices & Warrants")}</Header>
        </div>

        <div className="inbox-search-wrapper">
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
                  handleOpen(props);
                },
              },
            }}
          ></InboxSearchComposer>
          {openUnsigned && <PrintAndSendDocumentModal handleClose={handleClose} />}
          {openSigned && (
            <ReviewDocumentModal handleClose={handleClose} setOpenSigned={setOpenSigned} setShowsignatureModal={setShowsignatureModal} />
          )}
          {showsignatureModal && (
            <OrderSignatureModal
              t={t}
              order={order}
              handleIssueOrder={handleSendEmail}
              handleGoBackSignatureModal={handleGoBackSignatureModal}
              saveOnsubmitLabel={"Send Email"}
            />
          )}
          {showSuccessModal && <OrderSucessModal t={t} order={order} handleClose={handleCloseSuccessfulModal} />}
        </div>
      </div>
    </div>
  );
};

export default ReviewSummonsNoticeAndWarrant;
