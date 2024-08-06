const caseService = require('../service/caseService');
const pdfService = require('../service/pdfService');
const fileService = require('../service/fileService');
const formatUtil = require('../util/formatUtil');

exports.generateCasePdf = async (req, res, next) => {
    try {
        const cases = req.body.cases;

        const litigants = await individualService.getComplainantsDetails(cases);
        const respondents = await individualService.getRespondentsDetails(cases);
        const witnessed = await individualService.getWitnessDetails(cases);
        const advocates = await individualService.getAdvocateDetails(cases);

        const chequeDetails = await individualService.getChequeDetails(cases);
        const debtLiabilityDetails = await individualService.getDebtLiabilityDetails(cases);
        const demandNoticeDetails = await individualService.getDemandNoticeDetails(cases);
        const delayCondonationDetails = await individualService.getDelayCondonationDetails(cases);

        const prayerSwornStatementDetails = await individualService.getPrayerSwornStatementDetails(cases);

        const formattedData = formatUtil.formatCaseData(caseData);
        const pdf = await pdfService.generatePDF(formattedData);
        const finalPdf = await fileService.appendFilesToPDF(pdf, req.files);
        await caseService.sendPDFBack(finalPdf);
        res.status(200).send('PDF generated and sent back successfully');
    } catch (error) {
        next(error);
    }
};
