import useGetAdvocateClerk from "./dristi/useGetAdvocateClerk";
import useGetAdvocateClientServices from "./dristi/useGetAdvocateClientServices";
import useGetIndividualAdvocate from "./dristi/useGetIndividualAdvocate";
import useGetIndividualUser from "./dristi/useGetIndividualUser";
import useIndividualService from "./dristi/useIndividualService";

import { DRISTIService } from "../services";
import useGetEvidence from "./dristi/useGetEvidence";
import useGetOrders from "./dristi/useGetOrders";
import useGetSubmissions from "./dristi/useGetSubmissions";
import useInboxCustomHook from "./dristi/useInboxCustomHook";
import useSearchCaseService from "./dristi/useSearchCaseService";
import usePaymentCalculator from "./dristi/usePaymentCalculator";
import { useToast } from "../components/Toast/useToast.js";
import useCreateHearings from "./dristi/useCreateHearings.js";
import useEvidenceDetails from "./dristi/useEvidenceDetails.js";
import useBillSearch from "./dristi/useBillSearch";
import useCreateDemand from "./dristi/useCreateDemand";
import useApplicationDetails from "./dristi/useApplicationDetails.js";
import useGetOCRData from "./dristi/useGetOCRData.js";

export const Urls = {
  Authenticate: "/user/oauth/token",
  dristi: {
    individual: "/individual/v1/_create",
    searchIndividual: "/individual/v1/_search",
    searchIndividualAdvocate: "/advocate/advocate/v1/_search",
    searchIndividualClerk: "/advocate/clerk/v1/_search",
    updateAdvocateDetails: "/advocate/advocate/v1/_update",
    caseCreate: "/case/v1/_create",
    caseUpdate: "/case/v1/_update",
    caseSearch: "/case/v1/_search",
    evidenceSearch: "/evidence/v1/_search",
    evidenceCreate: "/evidence/v1/_create",
    evidenceUpdate: "/evidence/v1/_update",
    searchHearings: "/hearing/v1/search",
    createHearings: "/hearing/v1/create",
    updateHearings: "/hearing/v1/update",
    demandCreate: "/billing-service/demand/_create",
    ordersSearch: "/order/v1/search",
    ordersCreate: "/order/v1/create",
    submissionsSearch: "/application/v1/search",
    submissionsUpdate: "/application/v1/update",
    pendingTask: "/analytics/pending_task/v1/create",
    //Solutions
    billFileStoreId: "/etreasury/payment/v1/_getPaymentReceipt",
    eSign: "/e-sign-svc/v1/_esign",
    paymentCalculator: "/payment-calculator/v1/case/fees/_calculate",
    fetchBill: "/billing-service/bill/v2/_fetchbill",
    searchBill: "/billing-service/bill/v2/_search",
    eTreasury: "/etreasury/payment/v1/_processChallan",
    demandCreate: "/billing-service/demand/_create",
    sendOCR: "/ocr-service/verify",
    receiveOCR: "/ocr-service/documentType",
  },
  case: {
    addWitness: "/case/case/v1/add/witness",
  },
  FileFetchById: "/filestore/v1/files/id",
};

const dristi = {
  useGetAdvocateClerk,
  useGetAdvocateClientServices,
  useGetIndividualAdvocate,
  useIndividualService,
  useGetIndividualUser,
  useInboxCustomHook,
  useSearchCaseService,
  usePaymentCalculator,
  useCreateHearings,
  useGetEvidence,
  useGetOrders,
  useGetSubmissions,
  useApplicationDetails,
  useEvidenceDetails,
  useToast,
  useBillSearch,
  useCreateDemand,
  useGetOCRData,
};

const Hooks = {
  dristi,
};

const Utils = {
  dristi: {},
};
export const CustomizedHooks = {
  Hooks,
  DRISTIService,
  Utils,
};
