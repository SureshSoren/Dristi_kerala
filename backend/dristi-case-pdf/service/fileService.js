const axios = require('axios');
const { PDFDocument } = require('pdf-lib');

async function fetchDocument(fileStoreId) {
    const url = `https://dristi-kerala-dev.pucar.org/filestore/v1/files/id?tenantId=kl&fileStoreId=${fileStoreId}`;
    const response = await axios.get(url, { responseType: 'arraybuffer' });

    if (response.headers['content-type'] === 'application/pdf') {
      console.log(response.headers); 
      return response.data;
    } else {
      const imagePdf = await imageToPdf(response.data);
      return imagePdf;
    }
}

async function appendPdfPages(existingPdfDoc, fileStoreId) {
    const documentBytes = await fetchDocument(fileStoreId);
    const fetchedPdfDoc = await PDFDocument.load(documentBytes);
    const fetchedPages = await fetchedPdfDoc.getPages();
    for (const pageIndex of fetchedPages.map((_, i) => i)) {
        const [copiedPage] = await existingPdfDoc.copyPages(fetchedPdfDoc, [pageIndex]);
        existingPdfDoc.addPage(copiedPage);
    }
}

async function appendFilesToPDF(pdf, fileStores) {
    const existingPdfDoc = await PDFDocument.load(pdf);

    for (const fileStoreId of fileStores) {
        if (fileStoreId) {
            await appendPdfPages(existingPdfDoc, fileStoreId);
        }
    }

    return await existingPdfDoc.save();  // Save and return the final PDF
}

async function appendComplainantFilesToPDF(pdf, complainants) {
    const fileStores = complainants.map(c => c.companyDetailsFileStore).filter(Boolean);
    return appendFilesToPDF(pdf, fileStores);
}

async function appendRespondentFilesToPDF(pdf, respondents) {
    const fileStores = respondents.map(r => r.inquiryAffidavitFileStore).filter(Boolean);
    return appendFilesToPDF(pdf, fileStores);
}

async function appendChequeDetailsToPDF(pdf, chequeDetails) {
    const existingPdfDoc = await PDFDocument.load(pdf);

    for (let i = 0; i < chequeDetails.length; i++) {
        const chequeDetail = chequeDetails[i];

        if (chequeDetail.bouncedChequeFileStore) {
            await appendPdfPagesWithHeader(existingPdfDoc, chequeDetail.bouncedChequeFileStore, `Bounced Cheque Document ${i + 1}`);
        }
        if (chequeDetail.depositChequeFileStore) {
            await appendPdfPagesWithHeader(existingPdfDoc, chequeDetail.depositChequeFileStore, `Deposit Cheque Document ${i + 1}`);
        }
        if (chequeDetail.returnMemoFileStore) {
            await appendPdfPagesWithHeader(existingPdfDoc, chequeDetail.returnMemoFileStore, `Return Memo Document ${i + 1}`);
        }
    }
}

async function appendDebtLiabilityFilesToPDF(pdf, debtLiabilityDetails) {
    const fileStores = debtLiabilityDetails.map(d => d.proofOfLiabilityFileStore).filter(Boolean);
    return appendFilesToPDF(pdf, fileStores);
}

async function appendDemandNoticeFilesToPDF(pdf, demandNoticeDetails) {
    const fileStores = [
        ...demandNoticeDetails.map(d => d.legalDemandNoticeFileStore).filter(Boolean),
        ...demandNoticeDetails.map(d => d.proofOfDispatchFileStore).filter(Boolean),
        ...demandNoticeDetails.map(d => d.proofOfAcknowledgmentFileStore).filter(Boolean),
        ...demandNoticeDetails.map(d => d.proofOfReplyFileStore).filter(Boolean)
    ];
    return appendFilesToPDF(pdf, fileStores);
}

async function appendDelayCondonationFilesToPDF(pdf, delayCondonationDetails) {
    const fileStores = delayCondonationDetails.map(d => d.delayCondonationFileStore).filter(Boolean);
    return appendFilesToPDF(pdf, fileStores);
}

async function appendPrayerSwornFilesToPDF(pdf, prayerSwornStatementDetails) {
    const fileStores = [
        ...prayerSwornStatementDetails.map(p => p.memorandumOfComplaintFileStore).filter(Boolean),
        ...prayerSwornStatementDetails.map(p => p.prayerForReliefFileStore).filter(Boolean),
        ...prayerSwornStatementDetails.map(p => p.swornStatement).filter(Boolean)
    ];
    return appendFilesToPDF(pdf, fileStores);
}

async function appendWitnessFilesToPDF(pdf, witnesses) {
    const fileStores = witnesses.map(w => w.witnessAdditionalDetails).filter(Boolean);
    return appendFilesToPDF(pdf, fileStores);
}

async function appendAdvocateFilesToPDF(pdf, advocates) {
    const fileStores = advocates.map(a => a.vakalatnamaFileStore).filter(Boolean);
    return appendFilesToPDF(pdf, fileStores);
}

module.exports = {
    appendComplainantFilesToPDF,
    appendRespondentFilesToPDF,
    appendChequeDetailsToPDF,
    appendDebtLiabilityFilesToPDF,
    appendDemandNoticeFilesToPDF,
    appendDelayCondonationFilesToPDF,
    appendPrayerSwornFilesToPDF,
    appendWitnessFilesToPDF,
    appendAdvocateFilesToPDF
};
