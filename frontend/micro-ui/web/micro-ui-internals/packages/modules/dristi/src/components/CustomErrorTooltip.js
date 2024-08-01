import React from "react";
import { InfoToolTipIcon } from "../icons/svgIndex";
import { InfoIcon } from "../icons/svgIndex";

const CustomErrorTooltip = ({ message, showTooltip, icon }) => {
  if (!showTooltip) {
    return null;
  }

  return (
    <div style={{ display: "flex", alignItems: "center" }}>
      <div className="custom-error-tooltip" style={{ display: "flex", alignItems: "center" }}>
        <span style={{ display: "flex", alignItems: "center" }}>{!icon ? <InfoIcon /> : <InfoIcon />}</span>
        <div className="custom-error-tooltip-message" style={{ ...(!message && { border: "none" }) }}>
          {message}
        </div>
      </div>
    </div>
  );
};

export default CustomErrorTooltip;
