const fs = require('fs');
const PDFDocument = require('pdf-lib').PDFDocument;

exports.appendFilesToPDF = async (pdfBuffer, files) => {
    const pdfDoc = await PDFDocument.load(pdfBuffer);
    for (const file of files) {
        const fileBuffer = fs.readFileSync(file.path);
        const fileDoc = await PDFDocument.load(fileBuffer);
        const pages = await pdfDoc.copyPages(fileDoc, fileDoc.getPageIndices());
        pages.forEach((page) => {
            pdfDoc.addPage(page);
        });
    }
    const finalPdf = await pdfDoc.save();
    return finalPdf;
};
