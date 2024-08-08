import { Button, TextArea } from "@egovernments/digit-ui-components";
import { ActionBar, CardLabel, Dropdown, LabelFieldPair } from "@egovernments/digit-ui-react-components";
import debounce from "lodash/debounce";
import React, { useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { Urls } from "../../hooks/services/Urls";
import AddParty from "./AddParty";
import AdjournHearing from "./AdjournHearing";
import EndHearing from "./EndHearing";
import EvidenceHearingHeader from "./EvidenceHeader";
import HearingSideCard from "./HearingSideCard";
import MarkAttendance from "./MarkAttendance";

const SECOND = 1000;

const InsideHearingMainPage = () => {
  const history = useHistory();
  const [activeTab, setActiveTab] = useState("Transcript/Summary");
  const [transcriptText, setTranscriptText] = useState("");
  const [hearing, setHearing] = useState({});
  const [witnessDepositionText, setWitnessDepositionText] = useState("");
  const [caseData, setCaseData] = useState(null);
  const [options, setOptions] = useState([]);
  const [additionalDetails, setAdditionalDetails] = useState({});
  const [selectedWitness, setSelectedWitness] = useState({});
  const [addPartyModal, setAddPartyModal] = useState(false);
  const [adjournHearing, setAdjournHearing] = useState(false);
  const [endHearingModalOpen, setEndHearingModalOpen] = useState(false);
  const textAreaRef = useRef(null);
  const [isOpen, setIsOpen] = useState(false);
  const [attendees, setAttendees] = useState([]);
  const tenantId = window?.Digit.ULBService.getCurrentTenantId();
  const { hearingId } = Digit.Hooks.useQueryParams();
  const [filingNumber, setFilingNumber] = useState("");
  const { t } = useTranslation();
  let roomIdLet = null;

  const onCancel = () => {
    setAddPartyModal(false);
  };

  const onClickAddWitness = () => {
    setAddPartyModal(true);
  };

  const userType = Digit?.UserService?.getType?.();

  if (!hearingId) {
    const contextPath = window?.contextPath || "";
    history.push(`/${contextPath}/${userType}/home/pending-task`);
  }

  const userRoles = Digit?.UserService?.getUser?.()?.info?.roles || [];

  const userHasRole = (userRole) => {
    return userRoles.some((role) => role.code === userRole);
  };

  // if (!userHasRole("HEARING_VIEWER")) {
  //   history.push(`/${window.contextPath}/${userType}/home/home-pending-task`);
  // }

  const reqBody = {
    hearing: { tenantId },
    criteria: {
      tenantID: tenantId,
      hearingId: hearingId,
    },
  };
  const { data: hearingsData } = Digit.Hooks.hearings.useGetHearings(
    reqBody,
    { applicationNumber: "", cnrNumber: "", hearingId },
    "dristi",
    !!userHasRole("HEARING_VIEWER"),
    10 * SECOND
  );

  const { mutateAsync: _updateTranscriptRequest } = Digit.Hooks.useCustomAPIMutationHook({
    url: Urls.hearing.hearingUpdate,
    params: { applicationNumber: "", cnrNumber: "" },
    body: { tenantId, hearingType: "", status: "" },
    config: {
      mutationKey: "updateTranscript",
    },
  });

  const updateTranscriptRequest = useMemo(() => debounce(_updateTranscriptRequest, 1000), [_updateTranscriptRequest]);

  const { data: caseDataResponse, refetch: refetchCase } = Digit.Hooks.dristi.useSearchCaseService(
    {
      criteria: [
        {
          filingNumber,
        },
      ],
      tenantId,
    },
    {},
    "dristi",
    filingNumber,
    filingNumber
  );

  useEffect(() => {
    if (hearingsData) {
      const hearingData = hearingsData?.HearingList?.[0];
      // hearing data with particular id will always give array of one object
      if (hearingData) {
        setHearing(hearingData);
        setTranscriptText(hearingData?.transcript[0]);
        setAttendees(hearingData.attendees || []);
        setFilingNumber(hearingData?.filingNumber[0]);
      }
    }
  }, [hearingsData]);

  useEffect(() => {
    if (caseDataResponse) {
      setCaseData(caseDataResponse);
      const responseList = caseDataResponse?.criteria?.[0]?.responseList?.[0];
      setAdditionalDetails(responseList?.additionalDetails);
      setOptions(
        responseList?.additionalDetails?.witnessDetails?.formdata?.map((witness) => ({
          label: `${witness.data.firstName} ${witness.data.lastName}`,
          value: witness.data.uuid,
        }))
      );
      const selectedWitness = responseList?.additionalDetails?.witnessDetails?.formdata?.[0]?.data || {};
      setSelectedWitness(selectedWitness);
      setWitnessDepositionText(hearing?.additionalDetails?.witnessDepositions?.find((witness) => witness.uuid === selectedWitness.uuid)?.deposition);
    }
  }, [caseDataResponse, hearing]);

  const handleModal = () => {
    setIsOpen(!isOpen);
  };

  const handleChange = (e) => {
    const newText = e.target.value;
    if (activeTab === "Witness Deposition") {
      setWitnessDepositionText(newText);
    } else {
      setTranscriptText(newText);

      if (Object.keys(hearing).length === 0) {
        console.warn("Hearing object is empty");
        return hearing;
      }

      const updatedHearing = structuredClone(hearing);

      if (activeTab === "Witness Deposition") {
        if (!updatedHearing?.additionalDetails?.witnesses) {
          updatedHearing.additionalDetails.witnesses = [];
        }
        const newWitness = {
          uuid: selectedWitness?.data?.uuid,
          name: selectedWitness?.data?.name,
          depositionText: newText,
        };
        updatedHearing.additionalDetails.witnesses.push(newWitness);
      } else {
        updatedHearing.transcript[0] = newText;
      }
      if (userHasRole("EMPLOYEE")) {
        updateTranscriptRequest({ body: { hearing: updatedHearing } });
      }
    }
  };

  const isDepositionSaved = Boolean(
    hearing?.additionalDetails?.witnessDepositions?.find((witness) => witness.uuid === selectedWitness.uuid)?.deposition
  );

  const saveWitnessDeposition = () => {
    const updatedHearing = structuredClone(hearing);
    updatedHearing.additionalDetails = updatedHearing.additionalDetails || {};
    updatedHearing.additionalDetails.witnessDepositions = updatedHearing.additionalDetails.witnessDepositions || [];
    if (isDepositionSaved) {
      return;
    }
    updatedHearing.additionalDetails.witnessDepositions.push({
      ...selectedWitness,
      deposition: witnessDepositionText,
    });
    _updateTranscriptRequest({ body: { hearing: updatedHearing } }).then((res) => {
      setHearing(res.hearing);
    });
  };

  const handleDropdownChange = (selectedWitnessOption) => {
    const selectedUUID = selectedWitnessOption.value;
    const selectedWitness = additionalDetails?.witnessDetails?.formdata?.find((w) => w.data.uuid === selectedUUID)?.data || {};
    setSelectedWitness(selectedWitness);
    console.debug(hearing, selectedWitness);
    setWitnessDepositionText(
      hearing?.additionalDetails?.witnessDepositions?.find((witness) => witness.uuid === selectedWitness.uuid)?.deposition || ""
    );
  };

  const handleEndHearingModal = () => {
    setEndHearingModalOpen(!endHearingModalOpen);
  };

  const handleExitHearing = () => {
    history.push(`/${window.contextPath}/${userType}/home/home-pending-task`);
  };

  const attendanceCount = useMemo(() => hearing?.attendees?.filter((attendee) => attendee.wasPresent).length || 0, [hearing]);

  const [context, setContext] = useState(null);
  const [globalStream, setGlobalStream] = useState(null);
  const [processor, setProcessor] = useState(null);
  const [isRecording, setIsRecording] = useState(false);
  const startTimeRef = useRef([0, 0, 0]);
  const endTimeRef = useRef([0, 0, 0]);
  const [transcription, setTranscription] = useState("");
  const [webSocketStatus, setWebSocketStatus] = useState("Not Connected");
  const [transcriptionUrl, setTranscriptionUrl] = useState("");
  const [editableTranscription, setEditableTranscription] = useState("");
  const [sendOriginal, setSendOriginal] = useState("");
  const [clientId, setClientId] = useState(null);
  const [detectedLanguage, setDetectedLanguage] = useState("Undefined");
  const [currentPosition, setCurrentPosition] = useState(0);
  const [selectedLanguage, setSelectedLanguage] = useState("english");
  const [selectedAsrModel, setSelectedAsrModel] = useState("bhashini");
  const [websocket, setWebsocket] = useState(null);
  const inputSourceRef = useRef("mic");
  // const roomIdInputRef = useRef(null);
  const [roomId, setRoomId] = useState(null);
  const [audioUrl, setAudioUrl] = useState("");
  const [isConnected, setIsConnected] = useState(false);

  const bufferSize = 4096;

  useEffect(() => {
    initWebSocket();
    createRoom();
  }, []);

  const joinRoom = () => {
    console.log(websocket, "websocket join room", WebSocket.OPEN);
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const message = {
        type: "joined_room",
        room_id: roomId,
      };
      console.log(websocket, message, "websocket join room success");

      websocket.send(JSON.stringify(message));
    }
  };

  const createRoom = () => {
    console.log(websocket, "websocket create room");
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const message = {
        type: "create_room",
        room_id: roomId,
      };
      websocket.send(JSON.stringify(message));
    }
  };

  const initWebSocket = () => {
    const websocketAddress = "wss://dristi-kerala-dev.pucar.org/transcription";

    if (!websocketAddress) {
      console.log("WebSocket address is required.");
      return;
    }

    const ws = new WebSocket(websocketAddress);

    ws.onopen = () => {
      console.log("WebSocket connection established");
      setWebSocketStatus("Connected");
    };

    ws.onclose = (event) => {
      console.log("WebSocket connection closed", event);
      setWebSocketStatus("Not Connected");
    };

    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("onmessage data", data);
      if (data.type === "joined_room" || data.type === "refresh_transcription") {
        console.log("recieved", data);
        handleRoomJoined(data);
        roomIdLet = data.room_id;
      } else {
        console.log(roomId, audioUrl, "sam", clientId);
        updateTranscription(data);
      }
    };

    setWebsocket(ws);
  };

  const handleRoomJoined = (data) => {
    console.log(data, "joined Data");
    setClientId(data.client_id);
    setRoomId(data.room_id);
    // roomIdInputRef.current.value = data.room_id;
    setTranscriptionUrl(data.transcript_url);
    setAudioUrl(data.audio_url);
  };

  // useEffect(() => {
  //   console.log("roomId effect", roomId);
  //   if (websocket && roomId) {
  //     // console.log(roomId, roomIdInputRef?.current.value, "roomId");
  //     const message = {
  //       type: "config",
  //       room_id: roomId,
  //     };
  //     websocket.send(JSON.stringify(message));
  //     // sendAudioConfig(newContext);
  //   }
  // }, [roomId]);

  const updateTranscription = (transcriptData) => {
    if (transcriptData.words && transcriptData.words.length > 0) {
      const newTranscription = transcriptData.words
        .map((wordData) => {
          const probability = wordData.probability;
          let color = "black";
          if (probability > 0.9) color = "green";
          else if (probability > 0.6) color = "orange";
          else color = "red";
          return `<span style="color: ${color}">${wordData.word} </span>`;
        })
        .join("");
      console.log("sam", newTranscription);
      setTranscription((prev) => prev + newTranscription + " ");
    } else {
      setTranscription((prev) => prev + transcriptData.text + " ");
    }
    setEditableTranscription((prev) => prev + transcriptData.text + " ");
    setSendOriginal((prev) => prev + transcriptData.text + " ");
  };
  const startRecording = () => {
    joinRoom();
    if (isRecording) {
      return;
    }
    setEditableTranscription(transcriptText);
    setIsRecording(true);

    const inputSource = inputSourceRef.current.value;
    if (inputSource === "mic") {
      startMicRecording();
    } else {
      window.alert("mic is not connected!");
    }
  };

  const stopRecording = () => {
    if (!isRecording) return;

    setTranscriptText(editableTranscription);

    setIsRecording(false);

    if (globalStream) {
      globalStream.getTracks().forEach((track) => track.stop());
      setGlobalStream(null);
    }
    if (processor) {
      setCurrentPosition(context.currentTime);
      processor.disconnect();
      setProcessor(null);
    }
    if (context) {
      context.close().then(() => setContext(null));
    }
    const now = new Date();
    endTimeRef.current = [now.getHours(), now.getMinutes(), now.getSeconds()];
  };

  const processAudio = (e, audioContext) => {
    if (!audioContext) {
      console.error("Audio context is not initialized");
      return;
    }
    const inputSampleRate = audioContext.sampleRate;
    const outputSampleRate = 16000;

    const left = e.inputBuffer.getChannelData(0);
    const downsampledBuffer = downsampleBuffer(left, inputSampleRate, outputSampleRate);
    const audioData = convertFloat32ToInt16(downsampledBuffer);
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const audioBase64 = bufferToBase64(audioData);
      const message = {
        type: "audio",
        data: audioBase64,
        room_id: roomId,
        client_id: clientId,
      };
      websocket.send(JSON.stringify(message));
    }
  };

  const downsampleBuffer = (buffer, inputSampleRate, outputSampleRate) => {
    if (inputSampleRate === outputSampleRate) {
      return buffer;
    }
    const sampleRateRatio = inputSampleRate / outputSampleRate;
    const newLength = Math.round(buffer.length / sampleRateRatio);
    const result = new Float32Array(newLength);
    let offsetResult = 0;
    let offsetBuffer = 0;
    while (offsetResult < result.length) {
      const nextOffsetBuffer = Math.round((offsetResult + 1) * sampleRateRatio);
      let accum = 0,
        count = 0;
      for (let i = offsetBuffer; i < nextOffsetBuffer && i < buffer.length; i++) {
        accum += buffer[i];
        count++;
      }
      result[offsetResult] = accum / count;
      offsetResult++;
      offsetBuffer = nextOffsetBuffer;
    }
    return result;
  };

  const convertFloat32ToInt16 = (buffer) => {
    const l = buffer.length;
    const buf = new Int16Array(l);
    for (let i = 0; i < l; i++) {
      buf[i] = Math.min(1, buffer[i]) * 0x7fff;
    }
    return buf.buffer;
  };

  const bufferToBase64 = (buffer) => {
    const binary = String.fromCharCode.apply(null, new Uint8Array(buffer));
    return window.btoa(binary);
  };

  const startMicRecording = () => {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    const newContext = new AudioContext();
    setContext(newContext);

    sendAudioConfig(newContext);
    navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then((stream) => {
        setGlobalStream(stream);
        const input = newContext.createMediaStreamSource(stream);
        const newProcessor = newContext.createScriptProcessor(bufferSize, 1, 1);
        newProcessor.onaudioprocess = (e) => processAudio(e, newContext);
        input.connect(newProcessor);
        newProcessor.connect(newContext.destination);
        setProcessor(newProcessor);
      })
      .catch((error) => console.error("Error accessing microphone", error));
  };

  const sendAudioConfig = (context) => {
    if (!context) {
      console.error("Audio context is not initialized");
      return;
    }
    const audioConfig = {
      type: "config",
      room_id: roomId,
      client_id: clientId,
      data: {
        sampleRate: context.sampleRate,
        bufferSize: bufferSize,
        channels: 1,
        language: selectedLanguage !== "multilingual" ? selectedLanguage : null,
        asr_model: selectedAsrModel,
        processing_strategy: "silence_at_end_of_chunk",
        processing_args: {
          chunk_length_seconds: 1,
          chunk_offset_seconds: 0.1,
        },
      },
    };

    websocket.send(JSON.stringify(audioConfig));
  };

  return (
    <div className="admitted-case" style={{ display: "flex" }}>
      <div className="left-side" style={{ padding: "24px 40px" }}>
        <React.Fragment>
          <EvidenceHearingHeader
            caseData={caseData?.criteria?.[0]?.responseList?.[0]}
            hearing={hearing}
            setActiveTab={setActiveTab}
            activeTab={activeTab}
            filingNumber={filingNumber}
            onAddParty={onClickAddWitness}
          ></EvidenceHearingHeader>
        </React.Fragment>
        {activeTab === "Witness Deposition" && (
          <div style={{ width: "100%", marginTop: "15px", marginBottom: "10px" }}>
            <LabelFieldPair className="case-label-field-pair">
              <CardLabel className="case-input-label">{`Select Witness`}</CardLabel>
              <Dropdown
                option={options}
                optionKey={"label"}
                select={handleDropdownChange}
                freeze={true}
                disable={false}
                style={{ width: "100%", height: "40px", fontSize: "16px" }}
              />
            </LabelFieldPair>
            {userHasRole("EMPLOYEE") && (
              <div style={{ width: "151px", height: "19px", fontSize: "13px", color: "#007E7E", marginTop: "2px" }}>
                <button
                  style={{
                    background: "none",
                    border: "none",
                    padding: 0,
                    margin: 0,
                    cursor: "pointer",
                    fontSize: "13px",
                    color: "#007E7E",
                    fontWeight: 700,
                  }}
                  onClick={onClickAddWitness}
                >
                  + {t("CASE_ADD_PARTY")}
                </button>
              </div>
            )}
          </div>
        )}
        <div style={{ padding: "40px, 40px", gap: "16px" }}>
          <div style={{ gap: "16px", border: "1px solid", marginTop: "2px" }}>
            {userHasRole("EMPLOYEE") ? (
              <React.Fragment>
                <TextArea
                  ref={textAreaRef}
                  style={{ width: "100%", minHeight: "40vh" }}
                  value={!isRecording ? transcriptText : editableTranscription}
                  onChange={handleChange}
                  disabled={activeTab === "Witness Deposition" && isDepositionSaved}
                />
                <input type="radio" id="micInput" name="inputSource" value="mic" defaultChecked ref={inputSourceRef} />
                {!isConnected && (
                  <button
                    onClick={() => {
                      initWebSocket("login");
                      createRoom();
                      setIsConnected(true);
                    }}
                  >
                    Connect
                  </button>
                )}
                {isConnected && !isRecording && (
                  <button
                    onClick={() => {
                      startRecording();
                    }}
                  >
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <g clip-path="url(#clip0_4370_85283)">
                        <path
                          d="M7 24H9V22H7V24ZM12 13C13.66 13 14.99 11.66 14.99 10L15 4C15 2.34 13.66 1 12 1C10.34 1 9 2.34 9 4V10C9 11.66 10.34 13 12 13ZM11 24H13V22H11V24ZM15 24H17V22H15V24ZM19 10H17.3C17.3 13 14.76 15.1 12 15.1C9.24 15.1 6.7 13 6.7 10H5C5 13.41 7.72 16.23 11 16.72V20H13V16.72C16.28 16.23 19 13.41 19 10Z"
                          fill="#3D3C3C"
                        />
                      </g>
                      <defs>
                        <clipPath id="clip0_4370_85283">
                          <rect width="24" height="24" fill="white" />
                        </clipPath>
                      </defs>
                    </svg>
                  </button>
                )}
                {isConnected && isRecording && <button onClick={stopRecording}>stop recording</button>}
              </React.Fragment>
            ) : (
              <>
                <TextArea
                  style={{ width: "100%", minHeight: "40vh", cursor: "default", backgroundColor: "#E8E8E8", color: "#3D3C3C" }}
                  value={activeTab === "Witness Deposition" ? witnessDepositionText : transcriptText}
                  disabled
                ></TextArea>
              </>
            )}
          </div>
        </div>
        <div style={{ marginTop: "10px", marginBottom: "50px" }}>
          {activeTab === "Witness Deposition" && userHasRole("EMPLOYEE") && (
            <div>
              <Button
                label={t("SAVE_WITNESS_DEPOSITION")}
                isDisabled={isDepositionSaved}
                onClick={() => {
                  saveWitnessDeposition();
                }}
              ></Button>
            </div>
          )}
        </div>
      </div>
      <div className="right-side">
        <HearingSideCard hearingId={hearingId} caseId={caseData?.criteria?.[0]?.responseList?.[0]?.id} filingNumber={filingNumber}></HearingSideCard>
        {adjournHearing && <AdjournHearing hearing={hearing} updateTranscript={_updateTranscriptRequest} tenantID={tenantId} />}
      </div>
      <ActionBar>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            width: "100%",
          }}
        >
          <div
            style={{
              display: "flex",
              gap: "16px",
            }}
          >
            <button
              style={{
                border: "1px solid blue",
                backgroundColor: "#e6f0ff",
                color: "#1a73e8",
                fontWeight: "bold",
                padding: "10px 20px",
                borderRadius: "5px",
                cursor: "pointer",
                display: "inline-block",
                fontSize: "16px",
              }}
            >
              Attendance: <strong>{attendanceCount}</strong>
            </button>
            {userHasRole("EMPLOYEE") && <Button label={"Mark Attendance"} variation={"teritiary"} onClick={handleModal} style={{ width: "100%" }} />}
          </div>
          {userHasRole("EMPLOYEE") ? (
            <div
              style={{
                display: "flex",
                gap: "16px",
                width: "100%",
              }}
            >
              <Button label={t("ADJOURN_HEARING")} variation={"secondary"} onClick={() => setAdjournHearing(true)} style={{ width: "100%" }} />

              <Button label={t("END_HEARING")} variation={"primary"} onClick={handleEndHearingModal} style={{ width: "100%" }} />
            </div>
          ) : (
            <Button label={t("EXIT_HEARING")} variation={"primary"} onClick={handleExitHearing} />
          )}
          {isOpen && (
            <MarkAttendance
              handleModal={handleModal}
              attendees={attendees}
              setAttendees={setAttendees}
              hearingData={hearing}
              setAddPartyModal={setAddPartyModal}
            />
          )}
        </div>
      </ActionBar>

      <div>
        {addPartyModal && (
          <AddParty
            onCancel={onCancel}
            onAddSuccess={() => {
              refetchCase();
            }}
            caseData={caseData}
            tenantId={tenantId}
            hearingId={hearingId}
          ></AddParty>
        )}
      </div>
      {endHearingModalOpen && <EndHearing handleEndHearingModal={handleEndHearingModal} hearingId={hearingId} hearing={hearing} />}
    </div>
  );
};

export default InsideHearingMainPage;
