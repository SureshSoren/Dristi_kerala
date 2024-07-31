const axios = require('axios');
const config = require('../config/config');

exports.getCaseData = async (requestData) => {
    const response = await axios.post(`${config.caseServiceUrl}/getCaseData`, requestData);
    return response.data;
};

exports.sendPDFBack = async (pdf) => {
    await axios.post(`${config.caseServiceUrl}/receivePDF`, pdf, {
        headers: {
            'Content-Type': 'application/pdf'
        }
    });
};
