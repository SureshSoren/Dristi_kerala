const axios = require('axios');
const config = require('../config/config');
 
// Function to map Individual to Party with partyType from caseRequest
const mapIndividualToParty = (individual, partyType, partyCategory) => {
    return {
        id: individual.id,
        tenantId: individual.tenantId,
        individualId: individual.individualId,
        name: individual.name,
        address: individual.address,
        phone: individual.mobileNumber,
        email: individual.email,
        partyCategory: partyCategory,
        partyType: partyType,
        documents: []
    };
};
 
exports.getLitigantsData = async (cases) => {
    const individualSearchData = {
        id: cases.litigants.map(litigant => litigant.individualId)
    };
   
    const response = await axios.post(`${config.individualServiceUrl}/individual/v1/_search`, individualSearchData);
    const individuals = response.data.Individual;
 
    const individualIdToPartyType = new Map();
    caseRequest.cases.litigants.forEach(litigant => {
        individualIdToPartyType.set(litigant.individualId, litigant.partyType, litigant.partyCategory);
    });
 
    const litigants = individuals.map(individual =>
        mapIndividualToParty(individual, individualIdToPartyType.get(individual.individualId))
    );
 
    return litigants;
};
 
exports.getRespondentsData = async (cases) => {
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
 
exports.getRepresentativesData = async (cases) => {
    return cases.additionalDetails.witnessDetails.formdata.map((formData) => {
        const data = formData.data;
        const addressDetail = data.addressDetails[0].addressDetails;
 
        return {
            respondentFirstName: data.respondentFirstName,
            respondentLastName: data.respondentLastName,
            respondentType: data.respondentType.name,
            phone: data.mobileNumber || null,
            email: data.email || null,
            address: JSON.stringify(addressDetail)
        };
    });
};