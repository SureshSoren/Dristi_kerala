const axios = require('axios');
const path = require('path');

console.log('Resolved config path:', path.resolve(__dirname, '../config/config'));

const config = require('../config/config');

exports.sendPDFBack = async (pdf) => {
    await axios.post(`${config.caseServiceUrl}/receivePDF`, pdf, {
        headers: {
            'Content-Type': 'application/pdf'
        }
    });
};