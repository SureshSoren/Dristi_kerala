import React, { useState } from "react";

const DashboardPage = () => {
  const getCurrentDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0"); 
    const day = String(today.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`; 
  };
  const [stepper, setStepper] = useState(1);
  const [startDate, setStartDate] = useState(getCurrentDate());
  const [endDate, setEndDate] = useState(getCurrentDate());
  const [selectedRange, setSelectedRange] = useState({ startDate: startDate, endDate: endDate });

  const handleSubmit = () => {
    console.log(selectedRange);
    if (startDate == null || endDate == null || startDate > endDate) {
      alert("ok");

      setStepper(4);
    } else {
      setSelectedRange({ startDate: startDate, endDate: endDate });
      console.log(selectedRange);
    }
  };

  return (
    <div style={{ display: "flex" }}>
      <div style={{ display: "flex", flexDirection: "column", width: "220px", padding: "10px", gap: "16px" }}>
        <button
          className="dashboard-btn"
          style={{
            padding: "16px",
            color: stepper === 1 ? "#007E7E" : "#77787B",
            fontWeight: stepper === 1 ? "600" : "400",
          }}
          onClick={() => setStepper(1)}
        >
          Case Dynamics
        </button>
        <button
          className="dashboard-btn"
          style={{
            padding: "16px",
            color: stepper === 2 ? "#007E7E" : "#77787B",
            fontWeight: stepper === 2 ? "600" : "400",
          }}
          onClick={() => setStepper(2)}
        >
          Hearings Analysis
        </button>
        <button
          className="dashboard-btn"
          style={{
            padding: "16px",
            color: stepper === 3 ? "#007E7E" : "#77787B",
            fontWeight: stepper === 3 ? "600" : "400",
          }}
          onClick={() => setStepper(3)}
        >
          Appearance
        </button>
      </div> 
      <div
        style={{
          flexGrow: 1,
          display: "flex",
          flexDirection: "column",
          justifyContent: "flex-start",
          padding: "20px",
          border: "none",
        }}
      >
        <div>
          <div style={{ display: "flex", flexDirection: "row", gap: "15px", justifyContent: "flex-start", marginBottom: "10px" }}>
            <label style={{ display: "flex", gap: "8px" }}>
              From
              <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
            </label>
            <label style={{ display: "flex", gap: "8px" }}>
              to
              <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
            </label>
            <button onClick={handleSubmit} style={{ height: "20px" }}>
              Filter
            </button>
          </div>
        </div>
        <div style={{ flexGrow: 1, width: "100%" }}>
          {stepper === 1 && (
            <iframe
              src={`https://dristi-kerala-dev.pucar.org/kibana/app/dashboards#/view/5ea148e0-54a5-11ef-bca8-d927e7d27354?embed=true&_g=(refreshInterval:(pause:!t,value:60000),time:(from:'${selectedRange.startDate}',to:'${selectedRange.endDate}'))&_a=()&hide-filter-bar=true`}
              height="600"
              width="100%"
              title="Kibana Dashboard"
            />
          )}
          {stepper === 2 && (
            <iframe
              src={`https://dristi-kerala-dev.pucar.org/kibana/app/dashboards#/view/40428570-4b46-11ef-91c9-cf3be62bc7a6?embed=true&_g=(refreshInterval:(pause:!t,value:60000),time:(from:'${selectedRange.startDate}',to:'${selectedRange.endDate}'))&_a=()&hide-filter-bar=true`}
              height="600"
              width="100%"
              title="Kibana Dashboard"
            />
          )}
          {stepper === 3 && (
            <iframe
              src={`https://dristi-kerala-dev.pucar.org/kibana/app/dashboards#/view/aeeb2370-4f23-11ef-a07b-f500038d4785?embed=true&_g=(refreshInterval:(pause:!t,value:60000),time:(from:'${selectedRange.startDate}',to:'${selectedRange.endDate}'))&_a=()&hide-filter-bar=true`}
              height="600"
              width="100%"
              title="Kibana Dashboard"
            />
          )}
          {stepper === 4 && <span>End date should be after the start date</span>}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
