import { AppContainer, BreadCrumb, PrivateRoute } from "@egovernments/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";
import { Switch } from "react-router-dom";
import OrdersResponse from "./OrdersResponse";
import OrdersCreate from "./OrdersCreate";
import OrdersHome from "./OrdersHome";
import GenerateOrders from "./GenerateOrders";
import PaymentStatus from "../../components/PaymentStatus";
import EpostTrackingPage from "./E-PostTracking";
import PaymentForSummonModal from "./PaymentForSummonModal";
import MakeSubmissionBail from "./MakeSubmissionBail";
import Transcription from "./transcription";
// import MakeSubmission from "./MakeSubmission";
import ReviewSummonsNoticeAndWarrant from "./ReviewSummonsNoticeAndWarrant";
const bredCrumbStyle = { maxWidth: "min-content" };
const userInfo = JSON.parse(window.localStorage.getItem("user-info"));
let userType = "employee";
if (userInfo) {
  userType = userInfo?.type === "CITIZEN" ? "citizen" : "employee";
}
const ProjectBreadCrumb = ({ location }) => {
  const { t } = useTranslation();
  const crumbs = [
    {
      path: `/${window?.contextPath}/${userType}/home/home-pending-task`,
      content: t("HOME"),
      show: true,
    },
    {
      path: `/${window?.contextPath}/employee`,
      content: t(location.pathname.split("/").pop()),
      show: true,
    },
  ];
  return <BreadCrumb crumbs={crumbs} spanStyle={bredCrumbStyle} />;
};

const App = ({ path, stateCode, userType, tenants }) => {
  return (
    <Switch>
      <AppContainer className="ground-container order-submission">
        <React.Fragment>
          <ProjectBreadCrumb location={window.location} />
        </React.Fragment>
        <PrivateRoute path={`${path}/orders-response`} component={() => <OrdersResponse></OrdersResponse>} />
        <PrivateRoute path={`${path}/orders-create`} component={() => <OrdersCreate />} />
        <PrivateRoute path={`${path}/orders-home`} component={() => <OrdersHome />} />
        <PrivateRoute path={`${path}/generate-orders`} component={() => <GenerateOrders />} />
        {/* <PrivateRoute path={`${path}/make-submission`} component={() => <MakeSubmission />} /> */}
        <PrivateRoute path={`${path}/Summons&Notice`} component={() => <ReviewSummonsNoticeAndWarrant />} />
        <PrivateRoute path={`${path}/payment-screen`} component={() => <PaymentStatus />} />
        <PrivateRoute path={`${path}/payment-modal`} component={() => <PaymentForSummonModal />} />
        <PrivateRoute path={`${path}/makesubmissionbail`} component={() => <MakeSubmissionBail />} />
        {/* <PrivateRoute path={`${path}/make-submission`} component={() => <MakeSubmission />} /> */}
        <PrivateRoute path={`${path}/tracking`} component={() => <EpostTrackingPage />} />
        <PrivateRoute path={`${path}/transcription`} component={() => <Transcription />} />
      </AppContainer>
    </Switch>
  );
};

export default App;
