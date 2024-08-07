const caseService = require('../service/caseService');
const pdfService = require('../service/pdfService');
const fileService = require('../service/fileService');
 
exports.generateCasePdf = async (req, res, next) => {
    try {
        const cases = req.body.cases;
 
        const complainants = await caseService.getComplainantsDetails(cases);
        const respondents = await caseService.getRespondentsDetails(cases);
        const witnesses = await caseService.getWitnessDetails(cases);
        const advocates = await caseService.getAdvocateDetails(cases);
 
        const chequeDetails = await caseService.getChequeDetails(cases);
        const debtLiabilityDetails = await caseService.getDebtLiabilityDetails(cases);
        const demandNoticeDetails = await caseService.getDemandNoticeDetails(cases);
        const delayCondonationDetails = await caseService.getDelayCondonationDetails(cases);
 
        const prayerSwornStatementDetails = await caseService.getPrayerSwornStatementDetails(cases);
 
        const requestInfo = req.body.RequestInfo;
 
        const pdfRequest = {
            RequestInfo: requestInfo,
            caseDetails: {
                id: Date.now(),
                complainants: complainants,
                respondents: respondents,
                witnesses: witnesses,
                advocates: advocates,
                chequeDetails: chequeDetails,
                debtLiabilityDetails: debtLiabilityDetails,
                demandNoticeDetails: demandNoticeDetails,
                delayCondonationDetails: delayCondonationDetails,
                prayerSwornStatementDetails: prayerSwornStatementDetails
            }
        };

        console.log("Pdf Request: {}", pdfRequest);
        const pdf = await pdfService.generatePDF(pdfRequest);
        const finalPdf = await fileService.appendFilesToPDF(pdf, pdfRequest.caseDetails);
 
        // Set the headers to indicate a file attachment in the response
        res.setHeader('Content-Type', 'application/pdf');
        res.setHeader('Content-Disposition', 'attachment; filename="caseDetails.pdf"');
       
        // Send the PDF as the response
        res.send(finalPdf);
    } catch (error) {
        next(error);
    }
};