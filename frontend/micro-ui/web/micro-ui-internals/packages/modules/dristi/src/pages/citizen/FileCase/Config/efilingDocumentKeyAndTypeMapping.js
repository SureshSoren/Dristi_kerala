export const efilingDocumentKeyAndTypeMapping = {
  returnMemoFileUpload: "CHEQUE_RETURN_MEMO",
  legalDemandNoticeFileUpload: "LEGAL_NOTICE",
  vakalatnamaFileUpload: "VAKALAT_NAMA",
  inquiryAffidavitFileUpload: "AFFIDAVIT",
  memorandumOfComplaint: "COMPLAINT_MEMO",
};

export const efilingDocumentTypeAndKeyMapping = {
  CHEQUE_RETURN_MEMO: "returnMemoFileUpload",
  LEGAL_NOTICE: "legalDemandNoticeFileUpload",
  VAKALAT_NAMA: "vakalatnamaFileUpload",
  AFFIDAVIT: "inquiryAffidavitFileUpload",
  COMPLAINT_MEMO: "memorandumOfComplaint",
};

export const ocrErrorLocations = {
  inquiryAffidavitFileUpload: {
    name: "respondentDetails",
    index: 0,
    fieldName: "image",
    configKey: "litigentDetails",
    inputlist: ["inquiryAffidavitFileUpload.document", "image"],
    fileName: null,
  },
  legalDemandNoticeFileUpload: {
    name: "demandNoticeDetails",
    index: 0,
    fieldName: "image",
    configKey: "caseSpecificDetails",
    inputlist: ["legalDemandNoticeFileUpload.document"],
    fileName: null,
  },
};
