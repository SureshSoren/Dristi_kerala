const config = require('../config/config'); 
 
exports.getComplainantsDetails = async (cases) => {
    return cases.additionalDetails.complainantDetails.formdata.map((formData) => {
        const data = formData.data;
        const complainantType = data.complainantType;
        const firstName = data.firstName || '';
        const lastName = data.lastName || '';
        const phoneNumber = data.complainantVerification?.mobileNumber || null;
 
        if (complainantType.code === 'REPRESENTATIVE') {
            const companyDetails = data.addressCompanyDetails;
            const companyAddress = {
                locality: companyDetails.locality,
                city: companyDetails.city,
                district: companyDetails.district,
                state: companyDetails.state,
                pincode: companyDetails.pincode
            };
 
            return {
                complainantType: complainantType.name,
                representativeName: `${firstName} ${lastName}`,
                phoneNumber,
                companyName: data.companyName,
                companyAddress: JSON.stringify(companyAddress),
                companyDetailsFileStore: data.companyDetailsUpload?.document[0]?.fileStore || null
            };
        } else {
            const addressDetails = data.complainantVerification?.individualDetails?.addressDetails || {};
            const address = {
                locality: addressDetails.locality,
                city: addressDetails.city,
                district: addressDetails.district,
                state: addressDetails.state,
                pincode: addressDetails.pincode
            };
 
            return {
                complainantType: complainantType.name,
                name: `${firstName} ${lastName}`,
                phoneNumber,
                address: JSON.stringify(address)
            };
        }
    });
};

exports.getRespondentsDetails = async (cases) => {
    return cases.additionalDetails.respondentDetails.formdata.map((formData) => {
        const data = formData.data;
 
        const addresses = data.addressDetails.map((addressDetail) => {
            return {
                locality: addressDetail.addressDetails.locality,
                city: addressDetail.addressDetails.city,
                district: addressDetail.addressDetails.district,
                state: addressDetail.addressDetails.state,
                pincode: addressDetail.addressDetails.pincode
            };
        });

        return {
            respondentFirstName: data.respondentFirstName,
            respondentLastName: data.respondentLastName,
            respondentType: data.respondentType.name,
            phone: data.mobileNumber || null,
            email: data.email || null,
            address: addresses
        };
    });
};

exports.getWitnessDetails = async (cases) => {
    return cases.additionalDetails.witnessDetails.formdata.map((formData) => {
        const data = formData.data;
        const addressDetail = data.addressDetails[0].addressDetails;
 
        const addresses = data.addressDetails.map((addressDetail) => {
            return {
                locality: addressDetail.addressDetails.locality,
                city: addressDetail.addressDetails.city,
                district: addressDetail.addressDetails.district,
                state: addressDetail.addressDetails.state,
                pincode: addressDetail.addressDetails.pincode
            };
        });
 
        const additionalDetails = JSON.stringify({
            text: data.witnessAdditionalDetails.text
        });
 
        return {
            witnessFirstName: data.firstName,
            witnessLastName: data.lastName,
            phone: data.phonenumbers.mobileNumber[0] || null,
            email: data.emails.textfieldValue || null,
            address: addresses,
            additionalDetails
        };
    });
};

exports.getAdvocateDetails = async (cases) => {
    return cases.additionalDetails.advocateDetails.formdata.map((formData) => {
        const data = formData.data;
        
        const vakalatnamaDocument = data.vakalatnamaFileUpload.document.find(doc => doc.fileName === 'UPLOAD_VAKALATNAMA');
        
        return {
            advocateId: data.advocateId,
            advocateName: data.advocateName,
            barRegistrationNumber: data.barRegistrationNumber,
            vakalatnamaFileStore: vakalatnamaDocument ? vakalatnamaDocument.fileStore : null,
            isRepresenting: data.isAdvocateRepresenting.name
        };
    });
};

exports.getChequeDetailsList = (caseDetails) => {
    const chequeDetailsList = caseDetails.chequeDetails.formdata.map(dataItem => {
        const chequeDetailsData = dataItem.data || {};

        const bouncedChequeDocument = chequeDetailsData.depositChequeFileUpload.document.find(doc => doc.fileName === 'CS_BOUNCED_CHEQUE');
        const depositChequeDocument = chequeDetailsData.vakalatnamaFileUpload.document.find(doc => doc.fileName === 'CS_PROOF_DEPOSIT_CHEQUE');
        const returnMemoDocument = chequeDetailsData.vakalatnamaFileUpload.document.find(doc => doc.fileName === 'CS_CHEQUE_RETURN_MEMO');

        return {
            signatoryName: chequeDetailsData.chequeSignatoryName || null,
            bouncedChequeFileStore: bouncedChequeDocument ? bouncedChequeDocument.fileStore : null,
            nameOnCheque: chequeDetailsData.name || null,
            chequeNumber: chequeDetailsData.chequeNumber || null,
            dateOfIssuance: chequeDetailsData.issuanceDate || null,
            bankName: chequeDetailsData.bankName || null,
            ifscCode: chequeDetailsData.ifsc || null,
            chequeAmount: chequeDetailsData.chequeAmount || null,
            dateOfDeposit: chequeDetailsData.depositDate || null,
            depositChequeFileStore: depositChequeDocument ? depositChequeDocument.fileStore : null,
            returnMemoFileStore: returnMemoDocument ? returnMemoDocument.fileStore : null,
            chequeAdditionalDetails: chequeDetailsData.chequeAdditionalDetails?.text || null
        };
    });

    return chequeDetailsList;
};

exports.getDebtLiabilityDetails = (caseDetails) => {

    const debtLiabilityDetailsList = caseDetails.debtLiabilityDetails.formdata.map(dataItem => {
        const debtLiabilityData = dataItem.data || {};
    
        const proofOfLiabilityDocument = debtLiabilityData.debtLiabilityFileUpload?.document.find(doc => doc.fileName === 'CS_PROOF_DEBT');

        return {
            natureOfDebt: debtLiabilityData.liabilityNature?.name || null,
            totalAmountCoveredByCheque: debtLiabilityData.liabilityType?.showAmountCovered ? debtLiabilityData.liabilityAmountCovered || null : null,
            proofOfLiabilityFileStore: proofOfLiabilityDocument ? proofOfLiabilityDocument.fileStore : null,
            additionalDetails: debtLiabilityData.additionalDebtLiabilityDetails?.text || null
        };
    });

    return debtLiabilityDetailsList;
};

exports.getDemandNoticeDetails = (caseDetails) => {
    const demandNoticeDetailsList = caseDetails.demandNoticeDetails.formdata.map(dataItem => {
        const demandNoticeData = dataItem.data || {};
 
        PROOF_OF_DISPATCH_FILE_NAME
        LEGAL_DEMAND_NOTICE
        PROOF_LEGAL_DEMAND_NOTICE_FILE_NAME
        CS_PROOF_TO_REPLY_DEMAND_NOTICE_FILE_NAME

        const legalDemandNoticeDocument = demandNoticeData.legalDemandNoticeFileUpload?.document.find(doc => doc.fileName === 'LEGAL_DEMAND_NOTICE');
        const proofOfServiceDocument = demandNoticeData.proofOfDispatchFileUpload?.document.find(doc => doc.fileName === 'PROOF_OF_DISPATCH_FILE_NAME');
        const proofOfAcknowledgmentDocument = demandNoticeData.proofOfAcknowledgmentFileUpload?.document.find(doc => doc.fileName === 'PROOF_LEGAL_DEMAND_NOTICE_FILE_NAME');
        const proofOfReplyDocument = demandNoticeData.proofOfReplyFileUpload?.document.find(doc => doc.fileName === 'CS_PROOF_TO_REPLY_DEMAND_NOTICE_FILE_NAME');

        return {
            modeOfDispatch: demandNoticeData.modeOfDispatchType?.modeOfDispatchType?.name || null,
            dateOfIssuance: demandNoticeData.dateOfIssuance || null,
            dateOfDispatch: demandNoticeData.dateOfDispatch || null,
            legalDemandNoticeFileStore: legalDemandNoticeDocument ? legalDemandNoticeDocument.fileStore : null,
            proofOfDispatchFileStore: proofOfServiceDocument ? proofOfServiceDocument.fileStore : null,
            proofOfService: demandNoticeData.proofOfService?.code || null,
            dateOfDeemedService: demandNoticeData.dateOfDeemedService || null,
            dateOfAccrual: demandNoticeData.dateOfAccrual || null,
            proofOfAcknowledgmentFileStore: proofOfAcknowledgmentDocument ? proofOfAcknowledgmentDocument.fileStore : null,
            replyReceived: demandNoticeData.proofOfReply?.code || null,
            dateOfReply: demandNoticeData.dateOfReply || null,
            proofOfReplyFileStore: proofOfReplyDocument ? proofOfReplyDocument.fileStore : null,

        };
    });

    return demandNoticeDetailsList;
};

exports.getDelayCondonationDetails = (caseDetails) => {
    const delayCondonationDetailsList = caseDetails.delayApplications.formdata.map(dataItem => {
        const delayData = dataItem.data || {};
    
        const delayCondonationDocument = demandNoticeData.legalDemandNoticeFileUpload?.document.find(doc => doc.fileName === 'CS_DELAY_CONDONATION_APPLICATION');

        return {
            reasonForDelay: delayData.delayApplicationReason.reasonForDelay || null,
            delayCondonationFileStore: delayCondonationDocument ? delayCondonationDocument.fileStore : null
        };
    });

    return delayCondonationDetailsList;
};

exports.getPrayerSwornStatementDetails = (caseDetails) => {
    const prayerSwornStatementDetailsList = caseDetails.prayerSwornStatement.formdata.map(dataItem => {
        const swornStatementData = dataItem.data || {};

        const swornStatementDocument = swornStatementData.swornStatement?.document.find(doc => doc.fileName === 'CS_SWORN_STATEMENT_HEADER');
        const prayerForReliefDocument = swornStatementData.prayerForRelief?.document.find(doc => doc.fileName === 'ATTACHED_DOCUMENT');
        const memorandumOfComplaintDocument = swornStatementData.memorandumOfComplaint?.document.find(doc => doc.fileName === 'ATTACHED_DOCUMENT');
    
        return {
            prayerAndSwornStatementType: swornStatementData.prayerAndSwornStatementType?.name || null,
            whetherComplainantWillingToSettle: swornStatementData.infoBoxData?.data || null,
            circumstancesUnderWhichComplainantWillingToSettle: swornStatementData.caseSettlementCondition?.text || null,
            memorandumOfComplaintText: swornStatementData.memorandumOfComplaint?.text || null,
            memorandumOfComplaintFileStore: memorandumOfComplaintDocument ? memorandumOfComplaintDocument.fileStore : null,
            prayerForReliefText: swornStatementData.prayerForRelief?.text || null,
            prayerForReliefFileStore: prayerForReliefDocument ? prayerForReliefDocument.fileStore : null,
            swornStatement: swornStatementDocument ? swornStatementDocument.fileStore : null,
            additionalDetails: swornStatementData.additionalDetails?.text || null,
            additionalActsSectionsToChargeWith: swornStatementData.additionalActsSections?.text || null
        };
    });

    return prayerSwornStatementDetailsList;
};