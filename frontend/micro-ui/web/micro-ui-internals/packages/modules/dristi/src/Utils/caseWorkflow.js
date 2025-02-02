export const CaseWorkflowState = {
  CASE_RE_ASSIGNED: "CASE_RE_ASSIGNED",
  DRAFT_IN_PROGRESS: "DRAFT_IN_PROGRESS",
  UNDER_SCRUTINY: "UNDER_SCRUTINY",
  CASE_ADMITTED: "CASE_ADMITTED",
  PENDING_ADMISSION: "PENDING_ADMISSION",
  PENDINGESIGN: "PENDINGESIGN",
  PENDINGPAYMENT: "PENDINGPAYMENT",
  PENDINGREVIEW: "PENDINGREVIEW",
  PENDINGAPPROVAL: "PENDINGAPPROVAL",
  PENDINGSUBMISSION: "PENDINGSUBMISSION",
  COMPLETED: "COMPLETED",
  ABATED: "ABATED",
  REJECTED: "REJECTED",
};

export const CaseWorkflowAction = {
  EDIT_CASE: "EDIT_CASE",
  VALIDATE: "VALIDATE",
  SEND_BACK: "SEND_BACK",
  ESIGN: "ESIGN",
  PAY: "PAY",
  APPROVE: "APPROVE",
  REJECT: "REJECT",
  AUTOESCALATE: "AUTOESCALATE",
  ABANDON: "ABANDON",
  CREATE: "CREATE",
};
