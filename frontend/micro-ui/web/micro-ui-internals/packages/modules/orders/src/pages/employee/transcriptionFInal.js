"use client";
import React from "react";
import { useState, useEffect, useRef } from "react";

const bufferSize = 4096;
function getTranscription(inputSource) {
    const [context, setContext] = useState(null);
    const [globalStream, setGlobalStream] = useState(null);
    const [processor, setProcessor] = useState(null);
    const [isRecording, setIsRecording] = useState(false);
    //   const endTimeRef = useRef([0, 0, 0]);
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
    const [roomId, setRoomId] = useState(null);
    const [audioUrl, setAudioUrl] = useState("");
  debugger;

  useEffect(() => {
    // if (isRecording) {
    initWebSocket();
    startRecording();
    // }
  }, []);

  const joinRoom = () => {
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const message = {
        type: "joined_room",
        room_id: roomId,
      };
      websocket.send(JSON.stringify(message));
    }
  };

  const createRoom = () => {
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const message = {
        type: "create_room",
        room_id: roomId,
      };
      websocket.send(JSON.stringify(message));
    }
  };

  const initWebSocket = (offset = "config") => {
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
      if (data.type === "joined_room" || data.type === "refresh_transcription") {
        handleRoomJoined(data);
      } else {
        setClientId(data.client_id);
        setTranscriptionUrl(data.transcript_url);
        setAudioUrl(data.audio_url);
        updateTranscription(data);
      }
    };

    setWebsocket(ws);
  };

  const handleRoomJoined = (data) => {
    setClientId(data.client_id);
    setRoomId(data.room_id);
    setTranscriptionUrl(data.transcript_url);
    setAudioUrl(data.audio_url);
  };

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
      setTranscription((prev) => prev + newTranscription + " ");
    } else {
      setTranscription((prev) => prev + transcriptData.text + " ");
    }
    setEditableTranscription((prev) => prev + transcriptData.text + " ");
    setSendOriginal((prev) => prev + transcriptData.text + " ");
  };

  const startRecording = () => {
    if (isRecording) {
      stopRecording();
      return;
    }

    setIsRecording(true);
    initWebSocket("login");
    createRoom();
    joinRoom();

    if (inputSource === "mic") {
      startMicRecording();
    } else {
      stopRecording();
    }
  };

  const stopRecording = () => {
    if (!isRecording) return;

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

  return editableTranscription;
}

export default getTranscription;
