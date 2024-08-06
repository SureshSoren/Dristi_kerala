const individualService = require('../service/individualService');
const caseService = require('../service/caseService');
const pdfService = require('../service/pdfService');
const fileService = require('../service/fileService');
const formatUtil = require('../util/formatUtil');

exports.generateCasePdf = async (req, res, next) => {
    try {
        const cases = req.body.cases;
        const litigants = await individualService.getLitigantsData(cases);
        const respondents = await individualService.getRespondentsData(cases);
        const witnessed = await individualService.getWitnessesData(cases);
        const representatives = await individualService.getRepresentativesData(cases);
        const formattedData = formatUtil.formatCaseData(caseData);
        const pdf = await pdfService.generatePDF(formattedData);
        const finalPdf = await fileService.appendFilesToPDF(pdf, req.files);
        await caseService.sendPDFBack(finalPdf);
        res.status(200).send('PDF generated and sent back successfully');
    } catch (error) {
        next(error);
    }
};
