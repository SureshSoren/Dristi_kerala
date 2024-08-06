const axios = require('axios');
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
        const addressDetail = data.addressDetails[0].addressDetails;
 
        const address = JSON.stringify({
            locality: addressDetail.locality,
            city: addressDetail.city,
            district: addressDetail.district,
            state: addressDetail.state,
            pincode: addressDetail.pincode
        });
 
        return {
            respondentFirstName: data.respondentFirstName,
            respondentLastName: data.respondentLastName,
            respondentType: data.respondentType.name,
            phone: data.mobileNumber || null,
            email: data.email || null,
            address
        };
    });
};
 
exports.getWitnessDetails = async (cases) => {
    return cases.additionalDetails.witnessDetails.formdata.map((formData) => {
        const data = formData.data;
        const addressDetail = data.addressDetails[0].addressDetails;
 
        const address = JSON.stringify({
            locality: addressDetail.locality,
            city: addressDetail.city,
            district: addressDetail.district,
            state: addressDetail.state,
            pincode: addressDetail.pincode
        });
 
        const additionalDetails = JSON.stringify({
            text: data.witnessAdditionalDetails.text
        });
 
        return {
            witnessFirstName: data.firstName,
            witnessLastName: data.lastName,
            phone: data.phonenumbers.mobileNumber[0] || null,
            email: data.emails.textfieldValue || null,
            address,
            additionalDetails
        };
    });
};

exports.getAdvocateDetails = async (cases) => {
    return cases.additionalDetails.advocateDetails.formdata.map((formData) => {
        const data = formData.data;
 
        return {
            advocateId: data.advocateId,
            advocateName: data.advocateName,
            barRegistrationNumber: data.barRegistrationNumber,
            vakalatnamaFileStore: data.vakalatnamaFileUpload.document[0].fileStore,
            isRepresenting: data.isAdvocateRepresenting.name
        };
    });
};

exports.enrichLitigantsWithAdvocates = (litigants, advocates) => { 
    const advocateMap = new Map();     
    advocates.forEach(advocate => { 
        advocate.representing.forEach(party => { 
            advocateMap.set(party.individualId, advocate.advocateName);
         });
     }); 
     return litigants.map(litigant => { 
        if (advocateMap.has(litigant.individualId)) { 
            litigant.advocateName = advocateMap.get(litigant.individualId); 
        } 
        return litigant; 
    });
};

exports.getDebtLiabilityDetails = (caseDetails) => {
    const debtLiabilityData = caseDetails.debtLiabilityDetails.formdata[0]?.data || {};
 
    return {
        natureOfDebt: debtLiabilityData.liabilityNature?.name || null,
        totalAmountCoveredByCheque: debtLiabilityData.liabilityType?.showAmountCovered ? debtLiabilityData.liabilityAmountCovered || null : null,
        proofOfLiabilityFileStore: debtLiabilityData.debtLiabilityFileUpload?.document[0]?.fileStore || null,
        additionalDetails: debtLiabilityData.additionalDebtLiabilityDetails?.text || null
    };
};

exports.getDemandNoticeDetails = (caseDetails) => {
    const demandNoticeData = caseDetails.demandNoticeDetails.formdata[0]?.data || {};
 
    return {
        modeOfDispatch: demandNoticeData.modeOfDispatchType?.modeOfDispatchType?.name || null,
        dateOfIssuance: demandNoticeData.dateOfIssuance || null,
        dateOfDispatch: demandNoticeData.dateOfDispatch || null,
        legalDemandNoticeFileStore: demandNoticeData.legalDemandNoticeFileUpload?.document[0]?.fileStore || null,
        proofOfDispatchFileStore: demandNoticeData.proofOfDispatchFileUpload?.document[0]?.fileStore || null,
        proofOfService: demandNoticeData.proofOfService?.code || null,
        dateOfDeemedService: demandNoticeData.dateOfDeemedService || null,
        dateOfAccrual: demandNoticeData.dateOfAccrual || null,
        proofOfAcknowledgmentFileStore: demandNoticeData.proofOfAcknowledgmentFileUpload?.document[0]?.fileStore || null,
        replyReceived: demandNoticeData.proofOfReply?.code || null,
        dateOfReply: demandNoticeData.dateOfReply || null,
        proofOfReplyFileStore: demandNoticeData.proofOfReplyFileUpload?.document[0]?.fileStore || null
    };
};

exports.getDelayCondonationDetails = (caseDetails) => {
    const delayData = caseDetails.delayApplications.formdata[0]?.data || {};
 
    return {
        reasonForDelay: delayData.delayCondonationType?.name || null,
        fileUploadFileStore: delayData.condonationFileUpload?.document?.[0]?.fileStore || null
    };
};

exports.getPrayerSwornStatementDetails = (caseDetails) => {
    const swornStatementData = caseDetails.prayerSwornStatement.formdata[0]?.data || {};
 
    return {
        prayerAndSwornStatementType: swornStatementData.prayerAndSwornStatementType?.name || null,
        whetherComplainantWillingToSettle: swornStatementData.infoBoxData?.data || null,
        circumstancesUnderWhichComplainantWillingToSettle: swornStatementData.caseSettlementCondition?.text || null,
        memorandumOfComplaint: swornStatementData.memorandumOfComplaint?.text || null,
        prayerForRelief: swornStatementData.prayerForRelief?.text || null,
        swornStatement: swornStatementData.swornStatement?.document?.[0]?.fileStore || null,
        additionalDetails: swornStatementData.additionalDetails?.text || null,
        additionalActsSectionsToCharge: swornStatementData.additionalActsSections?.text || null,
        complainantWith: swornStatementData.SelectUploadDocWithName || null
    };
};