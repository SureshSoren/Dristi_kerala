import React from "react";
import ReactDOM from "react-dom";
import { initLibraries } from "@egovernments/digit-ui-libraries";
import "./index.css";
import App from "./App";

initLibraries();

const user = window.Digit.SessionStorage.get("User");

if (!user || !user.access_token || !user.info) {
  // login detection

  const parseValue = (value) => {
    try {
      return JSON.parse(value);
    } catch (e) {
      return value;
    }
  };

  const getFromStorage = (key) => {
    const value = window.localStorage.getItem(key);
    return value && value !== "undefined" ? parseValue(value) : null;
  };

  const token = getFromStorage("token");

  const citizenToken = getFromStorage("Citizen.token");
  const citizenInfo = getFromStorage("Citizen.user-info");
  const citizenTenantId = getFromStorage("Citizen.tenant-id");

  const employeeToken = getFromStorage("Employee.token");
  const employeeInfo = getFromStorage("Employee.user-info");
  const employeeTenantId = getFromStorage("Employee.tenant-id");
  const userType = token === citizenToken ? "citizen" : "employee";

  localStorage.setItem("user_type", userType);
  localStorage.setItem("userType", userType);

  const getUserDetails = (access_token, info) => ({
    token: access_token,
    access_token,
    info,
  });

  const userDetails =
    userType === "citizen"
      ? getUserDetails(citizenToken, citizenInfo)
      : getUserDetails(employeeToken, employeeInfo);

  localStorage.setItem("User", userDetails);
  localStorage.setItem("Citizen.tenantId", citizenTenantId);
  localStorage.setItem("Employee.tenantId", employeeTenantId);
  // end
}

ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById("root")
);
