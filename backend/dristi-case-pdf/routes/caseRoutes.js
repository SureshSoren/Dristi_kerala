const express = require('express');
const router = express.Router();
const caseController = require('../controller/caseController');

router.post('/generate', caseController.generateCase);

module.exports = router;
