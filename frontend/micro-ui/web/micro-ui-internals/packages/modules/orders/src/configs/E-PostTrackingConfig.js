export const EpostTrackingConfig = ({ inboxFilters, outboxFilters }) => {
    const TabSearchConfig = [
        {
        label: "All",
        type: "search",
        apiDetails: {
            serviceName: "/epost-tracker/epost/v1/_getEPost",
            requestParam: {
                tenantId: Digit.ULBService.getCurrentTenantId(),
            },
            requestBody: {
                apiOperation: "SEARCH",
                Individual: {
                    tenantId: Digit.ULBService.getCurrentTenantId(),
                },
                ePostTrackerSearchCriteria: {
                    processNumber: "",
                    deliveryStatusList:[],
                    pagination: {
                        limit: 10,
                        offset: 0
                    }
                },
            },
            masterName: "commonUiConfig",
            moduleName: "PreHearingsConfig",
            minParametersForSearchForm: 0,
            tableFormJsonPath: "requestParam",
            filterFormJsonPath: "requestBody.ePostTrackerSearchCriteria",
            searchFormJsonPath: "requestBody.ePostTrackerSearchCriteria",
        },
        sections: {
            search: {
                uiConfig: {
                    formClassName: "custom-both-clear-search",
                    primaryLabel: "ES_COMMON_SEARCH",
                    secondaryLabel: "ES_COMMON_CLEAR_SEARCH",
                    minReqFields: 0,
                    fields: [
                        {
                            isMandatory: false,
                            type: "text",
                            key: "caseId",
                            disable: false,
                            populators: {
                                name: "processNumber",
                                placeholder: "Search Eprocess No",
                            },
                        },
                        // {
                        //     type: "dropdown",
                        //     isMandatory: false,
                        //     disable: false,
                        //     populators: {
                        //         name: "receivedDate",
                        //         type: "receivedDate",
                        //         optionsKey: "date",
                        //         defaultText: "Date Recieved",
                        //         allowMultiSelect: true,
                        //         options: [
                        //             "IN_TRANSIT",
                        //             "NOT_UPDATED",
                        //             "DELIVERED",
                        //             "NOT_DELIVERED"
                        //         ],
                        //     },
                        // },
                        {
                            type: "dropdown",
                            isMandatory: false,
                            disable: false,
                            populators: {
                                defaultText: "Status",
                                // selected:,
                                styles: { width: "250px" },
                                name: "deliveryStatusList.[0]",
                                options: [
                                   "IN_TRANSIT",
                                    "NOT_UPDATED",
                                    "DELIVERED",
                                    "NOT_DELIVERED"
                                ],
                            },
                        },
                        // {
                        //   type: "dropdown",
                        //   isMandatory: false,
                        //   disable: false,
                        //   populators: {
                        //     placeholder: "Type",
                        //     styles: { width: "250px" },
                        //     name: "type",
                        //     options: [
                        //       "IN_TRANSIT",
                        //       "NOT_UPDATED",
                        //     ],
                        //   },
                        // },
                    ],
                },
                show: true,
            },
            searchResult: {
                tenantId: Digit.ULBService.getCurrentTenantId(),
                uiConfig: {
                    columns: [
                        {
                            label: "E-Process No.",
                            jsonPath: "processNumber",
                        },
                        {
                            label: "Bar Code",
                            jsonPath: "trackingNumber",
                        },
                        {
                            label: "Pincode",
                            jsonPath: "pinCode",
                        },
                        {
                            label: "Address",
                            jsonPath: "address",
                        },
                        {
                            label: "Delivery Status",
                            jsonPath: "deliveryStatus",
                            // additionalCustomization: true,
                        },
                        {
                            label: "Remarks",
                            jsonPath: "remarks"
                        }
                    ],
                    enableColumnSort: true,
                    resultsJsonPath: "EPostTracker",
                },
                show: true,
            },
        },
    },
    {
        label: "Inbox",
        type: "search",
        apiDetails: {
            serviceName: "/epost-tracker/epost/v1/_getEPost",
            requestParam: {
                tenantId: Digit.ULBService.getCurrentTenantId(),
            },
            requestBody: {
                apiOperation: "SEARCH",
                Individual: {
                    tenantId: Digit.ULBService.getCurrentTenantId(),
                },
                ePostTrackerSearchCriteria: {
                    processNumber: "",
                    deliveryStatusList: inboxFilters,
                    pagination: {
                        limit: 10,
                        offset: 0
                    }
                },
            },
            masterName: "commonUiConfig",
            moduleName: "PreHearingsConfig",
            minParametersForSearchForm: 0,
            tableFormJsonPath: "requestParam",
            filterFormJsonPath: "requestBody.ePostTrackerSearchCriteria",
            searchFormJsonPath: "requestBody.ePostTrackerSearchCriteria",
        },
        sections: {
            search: {
                uiConfig: {
                    formClassName: "custom-both-clear-search",
                    primaryLabel: "ES_COMMON_SEARCH",
                    secondaryLabel: "ES_COMMON_CLEAR_SEARCH",
                    minReqFields: 0,
                    fields: [
                        {
                            isMandatory: false,
                            type: "text",
                            key: "caseId",
                            disable: false,
                            populators: {
                                name: "processNumber",
                                placeholder: "Search Eprocess No",
                            },
                        },
                        // {
                        //     type: "dropdown",
                        //     isMandatory: false,
                        //     disable: false,
                        //     populators: {
                        //         name: "receivedDate",
                        //         type: "receivedDate",
                        //         optionsKey: "date",
                        //         defaultText: "Date Recieved",
                        //         allowMultiSelect: true,
                        //         options: [
                        //             "IN_TRANSIT",
                        //             "NOT_UPDATED",
                        //         ],
                        //     },
                        // },
                        {
                            type: "dropdown",
                            isMandatory: false,
                            disable: false,
                            populators: {
                                defaultText: "Status",
                                styles: { width: "250px" },
                                name: "deliveryStatusList.[0]",
                                options: [
                                    "IN_TRANSIT",
                                    "NOT_UPDATED",
                                ],
                            },
                        },
                        // {
                        //   type: "dropdown",
                        //   isMandatory: false,
                        //   disable: false,
                        //   populators: {
                        //     placeholder: "Type",
                        //     styles: { width: "250px" },
                        //     name: "type",
                        //     options: [
                        //       "IN_TRANSIT",
                        //       "NOT_UPDATED",
                        //     ],
                        //   },
                        // },
                    ],
                },
                show: true,
            },
            searchResult: {
                tenantId: Digit.ULBService.getCurrentTenantId(),
                uiConfig: {
                    columns: [
                        {
                            label: "E-Process No.",
                            jsonPath: "processNumber",
                        },
                        {
                            label: "Bar Code",
                            jsonPath: "trackingNumber",
                        },
                        {
                            label: "Pincode",
                            jsonPath: "pinCode",
                        },
                        {
                            label: "Address",
                            jsonPath: "address",
                        },
                        {
                            label: "Delivery Status",
                            jsonPath: "deliveryStatus",
                            // additionalCustomization: true,
                        },
                        {
                            label: "Remarks",
                            jsonPath: "remarks"
                        }
                    ],
                    enableColumnSort: true,
                    resultsJsonPath: "EPostTracker",
                },
                show: true,
            },
        },
    },
    {
        label: "Outbox",
        type: "search",
        apiDetails: {
            serviceName: "/epost-tracker/epost/v1/_getEPost",
            requestParam: {
                tenantId: Digit.ULBService.getCurrentTenantId(),
            },
            requestBody: {
                apiOperation: "SEARCH",
                Individual: {
                    tenantId: Digit.ULBService.getCurrentTenantId(),
                },
                ePostTrackerSearchCriteria: {
                    processNumber: "",
                    deliveryStatusList: outboxFilters,
                    pagination: {
                        limit: 10,
                        offset: 0
                    }
                },
            },
            masterName: "commonUiConfig",
            moduleName: "PreHearingsConfig",
            minParametersForSearchForm: 0,
            tableFormJsonPath: "requestParam",
            filterFormJsonPath: "requestBody.ePostTrackerSearchCriteria",
            searchFormJsonPath: "requestBody.ePostTrackerSearchCriteria",
        },
        sections: {
            search: {
                uiConfig: {
                    formClassName: "custom-both-clear-search",
                    primaryLabel: "ES_COMMON_SEARCH",
                    secondaryLabel: "ES_COMMON_CLEAR_SEARCH",
                    minReqFields: 0,
                    fields: [
                        {
                            isMandatory: false,
                            type: "text",
                            key: "caseId",
                            disable: false,
                            populators: {
                                name: "processNumber",
                                placeholder: "Search Eprocess No",
                            },
                        },
                        // {
                        //     type: "dropdown",
                        //     isMandatory: false,
                        //     disable: false,
                        //     populators: {
                        //         name: "receivedDate",
                        //         type: "receivedDate",
                        //         optionsKey: "date",
                        //         defaultText: "Date Recieved",
                        //         allowMultiSelect: true,
                        //         options: [
                        //             "DELIVERED",
                        //             "NOT_DELIVERED",
                        //         ],
                        //     },
                        // },
                        {
                            type: "dropdown",
                            isMandatory: false,
                            disable: false,
                            populators: {
                                defaultText: "Status",
                                styles: { width: "250px" },
                                name: "deliveryStatusList.[0]",
                                options: [
                                    "DELIVERED",
                                    "NOT_DELIVERED",
                                ],
                            },
                        },
                        // {
                        //   type: "dropdown",
                        //   isMandatory: false,
                        //   disable: false,
                        //   populators: {
                        //     placeholder: "Type",
                        //     styles: { width: "250px" },
                        //     name: "type",
                        //     options: [
                        //       "IN_TRANSIT",
                        //       "NOT_UPDATED",
                        //     ],
                        //   },
                        // },
                    ],
                },
                show: true,
            },
            searchResult: {
                tenantId: Digit.ULBService.getCurrentTenantId(),
                uiConfig: {
                    columns: [
                        {
                            label: "E-Process No.",
                            jsonPath: "processNumber",
                        },
                        {
                            label: "Bar Code",
                            jsonPath: "trackingNumber",
                        },
                        {
                            label: "Pincode",
                            jsonPath: "pinCode",
                        },
                        {
                            label: "Address",
                            jsonPath: "address",
                        },
                        {
                            label: "Delivery Status",
                            jsonPath: "deliveryStatus",
                            // additionalCustomization: true,
                        },
                        {
                            label: "Remarks",
                            jsonPath: "remarks"
                        }
                    ],
                    enableColumnSort: true,
                    resultsJsonPath: "EPostTracker",
                },
                show: true,
            },
        },
    },


]

return TabSearchConfig;

}