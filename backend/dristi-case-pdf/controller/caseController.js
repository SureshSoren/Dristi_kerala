const caseService = require('../services/caseService');
const pdfService = require('../services/pdfService');
const fileService = require('../services/fileService');
const formatUtil = require('../utils/formatUtil');

exports.generateCase = async (req, res, next) => {
    try {
        const caseData = await caseService.getCaseData(req.body);
        const formattedData = formatUtil.formatCaseData(caseData);
        const pdf = await pdfService.generatePDF(formattedData);
        const finalPdf = await fileService.appendFilesToPDF(pdf, req.files);
        await caseService.sendPDFBack(finalPdf);
        res.status(200).send('PDF generated and sent back successfully');
    } catch (error) {
        next(error);
    }
};
