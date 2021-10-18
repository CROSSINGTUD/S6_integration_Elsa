composableCrypto.register({
    name: "ELSA",
    generate: function (specGenerators, children) {
        const result = {
            apis: {
                elsaClientToES: {
                    "openapi": "3.0.1",
                    "info": {
                        "description": "A description of the API used for communication between clients and the Evidence Service in ELSA",
                        "title": "ELSA Evidence Service API",
                        "version": "1.0.0"
                    },
                    "servers": [{
                        "url": "/"
                    }],
                    "tags": [{
                        "description": "Operations that belong to ELSA",
                        "name": "elsa"
                    }],
                    "paths": {
                        "/item-ids": {
                            "get": {
                                "operationId": "elsaESGetItemIDs",
                                "responses": {
                                    "200": {
                                        "content": {
                                            "application/json": {
                                                "schema": {
                                                    "items": {
                                                        "type": "string"
                                                    },
                                                    "type": "array"
                                                }
                                            }
                                        },
                                        "description": "Operation completed without errors"
                                    }
                                },
                                "summary": "Get the IDs of all data items",
                                "tags": ["elsa"]
                            }
                        },
                        "/evidence-items/{dataItemId}": {
                            "get": {
                                "operationId": "elsaESGetProofOfIntegrity",
                                "parameters": [{
                                    "description": "id of the data item to get or add a decommitment for",
                                    "explode": false,
                                    "in": "path",
                                    "name": "dataItemId",
                                    "required": true,
                                    "schema": {
                                        "type": "string"
                                    },
                                    "style": "simple"
                                }],
                                "responses": {
                                    "200": {
                                        "content": {
                                            "application/json": {
                                                "schema": {
                                                    "items": {
                                                        "$ref": "#/components/schemas/EvidenceItem"
                                                    },
                                                    "type": "array"
                                                }
                                            }
                                        },
                                        "description": "Operation completed without errors"
                                    },
                                    "404": {
                                        "content": {},
                                        "description": "Proof of integrity does not exist"
                                    }
                                },
                                "summary": "Get the proof of integrity of a data item",
                                "tags": ["elsa"]
                            }
                        },
                        "/commitments": {
                            "post": {
                                "operationId": "elsaESAddCom",
                                "requestBody": {
                                    "$ref": "#/components/requestBodies/inline_object",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "properties": {
                                                    "itemIDs": {
                                                        "items": {
                                                            "type": "string"
                                                        },
                                                        "type": "array"
                                                    },
                                                    "vectorCommitmentScheme": {
                                                        "type": "string"
                                                    },
                                                    "commitment": {
                                                        "type": "string"
                                                    },
                                                    "timestampService": {
                                                        "type": "string"
                                                    }
                                                },
                                                "required": ["commitment", "itemIDs", "timestampService", "vectorCommitmentScheme"],
                                                "type": "object"
                                            }
                                        }
                                    },
                                    "description": "Commitment",
                                    "required": true
                                },
                                "responses": {
                                    "400": {
                                        "content": {},
                                        "description": "A required property is not set"
                                    }
                                },
                                "summary": "Add a commitment",
                                "tags": ["elsa"]
                            }
                        },
                        "/commitments/renew": {
                            "post": {
                                "operationId": "elsaESAddComRenew",
                                "requestBody": {
                                    "$ref": "#/components/requestBodies/inline_object_1",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "properties": {
                                                    "vectorCommitmentScheme": {
                                                        "type": "string"
                                                    },
                                                    "commitment": {
                                                        "type": "string"
                                                    },
                                                    "timestampService": {
                                                        "type": "string"
                                                    }
                                                },
                                                "required": ["commitment", "timestampService", "vectorCommitmentScheme"],
                                                "type": "object"
                                            }
                                        }
                                    },
                                    "description": "Commitment",
                                    "required": true
                                },
                                "responses": {
                                    "400": {
                                        "content": {},
                                        "description": "A required property is not set"
                                    }
                                },
                                "summary": "Add a commitment renewal",
                                "tags": ["elsa"]
                            }
                        }
                    },
                    "components": {
                        "requestBodies": {
                            "inline_object_1": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "$ref": "#/components/schemas/inline_object_1"
                                        }
                                    }
                                },
                                "required": true
                            },
                            "inline_object": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "$ref": "#/components/schemas/inline_object"
                                        }
                                    }
                                },
                                "required": true
                            }
                        },
                        "schemas": {
                            "EvidenceItem": {
                                "properties": {
                                    "vectorCommitmentScheme": {
                                        "type": "string"
                                    },
                                    "commitment": {
                                        "type": "string"
                                    },
                                    "decommitment": {
                                        "type": "string"
                                    },
                                    "timeStampService": {
                                        "type": "string"
                                    },
                                    "timestamp": {
                                        "type": "string"
                                    },
                                    "time": {
                                        "type": "string"
                                    }
                                },
                                "required": ["commitment", "decommitment", "time", "timeStampService", "timestamp", "vectorCommitmentScheme"],
                                "type": "object"
                            },
                            "inline_object": {
                                "properties": {
                                    "itemIDs": {
                                        "items": {
                                            "type": "string"
                                        },
                                        "type": "array"
                                    },
                                    "vectorCommitmentScheme": {
                                        "type": "string"
                                    },
                                    "commitment": {
                                        "type": "string"
                                    },
                                    "timestampService": {
                                        "type": "string"
                                    }
                                },
                                "required": ["commitment", "itemIDs", "timestampService", "vectorCommitmentScheme"],
                                "type": "object"
                            },
                            "inline_object_1": {
                                "properties": {
                                    "vectorCommitmentScheme": {
                                        "type": "string"
                                    },
                                    "commitment": {
                                        "type": "string"
                                    },
                                    "timestampService": {
                                        "type": "string"
                                    }
                                },
                                "required": ["commitment", "timestampService", "vectorCommitmentScheme"],
                                "type": "object"
                            }
                        }
                    }
                },
                elsaClientToSH: {
                    "openapi": "3.0.1",
                    "info": {
                        "description": "A description of the API used for communication between clients and shareholders in ELSA",
                        "title": "ELSA Shareholder API",
                        "version": "1.0.0"
                    },
                    "servers": [{
                        "url": "/"
                    }],
                    "tags": [{
                        "description": "Operations that belong to ELSA",
                        "name": "elsa"
                    }],
                    "paths": {
                        "/shares/{dataItemId}": {
                            "get": {
                                "operationId": "elsaSHGetShare",
                                "parameters": [{
                                    "description": "id of the data item to get or add a share for",
                                    "explode": false,
                                    "in": "path",
                                    "name": "dataItemId",
                                    "required": true,
                                    "schema": {
                                        "type": "string"
                                    },
                                    "style": "simple"
                                }],
                                "responses": {
                                    "200": {
                                        "content": {
                                            "application/json": {
                                                "schema": {
                                                    "$ref": "#/components/schemas/Share"
                                                }
                                            }
                                        },
                                        "description": "Operation completed without errors"
                                    },
                                    "404": {
                                        "content": {},
                                        "description": "Share does not exist"
                                    }
                                },
                                "summary": "Get a share of a data item",
                                "tags": ["elsa"]
                            },
                            "post": {
                                "operationId": "elsaSHAddShare",
                                "parameters": [{
                                    "description": "id of the data item to get or add a share for",
                                    "explode": false,
                                    "in": "path",
                                    "name": "dataItemId",
                                    "required": true,
                                    "schema": {
                                        "type": "string"
                                    },
                                    "style": "simple"
                                }],
                                "requestBody": {
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "$ref": "#/components/schemas/Share"
                                            }
                                        }
                                    },
                                    "description": "Share of a data item",
                                    "required": true
                                },
                                "responses": {
                                    "400": {
                                        "content": {},
                                        "description": "Data is not set"
                                    }
                                },
                                "summary": "Add a share of a data item",
                                "tags": ["elsa"]
                            }
                        },
                        "/shares/{dataItemId}/{decomIndex}": {
                            "get": {
                                "operationId": "elsaSHGetDecom",
                                "parameters": [{
                                    "description": "id of the data item to get or add a decommitment for",
                                    "explode": false,
                                    "in": "path",
                                    "name": "dataItemId",
                                    "required": true,
                                    "schema": {
                                        "type": "string"
                                    },
                                    "style": "simple"
                                }, {
                                    "description": "index of the decommitment. Indices may not be contiguous!",
                                    "explode": false,
                                    "in": "path",
                                    "name": "decomIndex",
                                    "required": true,
                                    "schema": {
                                        "minimum": 0,
                                        "type": "integer"
                                    },
                                    "style": "simple"
                                }],
                                "responses": {
                                    "200": {
                                        "content": {
                                            "application/json": {
                                                "schema": {
                                                    "$ref": "#/components/schemas/Decommitment"
                                                }
                                            }
                                        },
                                        "description": "Operation completed without errors"
                                    },
                                    "404": {
                                        "content": {},
                                        "description": "Decommitment does not exist"
                                    }
                                },
                                "summary": "Get a decommitment value",
                                "tags": ["elsa"]
                            },
                            "post": {
                                "operationId": "elsaSHAddDecom",
                                "parameters": [{
                                    "description": "id of the data item to get or add a decommitment for",
                                    "explode": false,
                                    "in": "path",
                                    "name": "dataItemId",
                                    "required": true,
                                    "schema": {
                                        "type": "string"
                                    },
                                    "style": "simple"
                                }, {
                                    "description": "index of the decommitment. Indices may not be contiguous!",
                                    "explode": false,
                                    "in": "path",
                                    "name": "decomIndex",
                                    "required": true,
                                    "schema": {
                                        "minimum": 0,
                                        "type": "integer"
                                    },
                                    "style": "simple"
                                }],
                                "requestBody": {
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "$ref": "#/components/schemas/Decommitment"
                                            }
                                        }
                                    },
                                    "description": "Decommitment",
                                    "required": true
                                },
                                "responses": {
                                    "400": {
                                        "content": {},
                                        "description": "Data is not set"
                                    },
                                    "404": {
                                        "content": {},
                                        "description": "Decommitment does not exist"
                                    }
                                },
                                "summary": "Add a decommitment value",
                                "tags": ["elsa"]
                            }
                        }
                    },
                    "components": {
                        "schemas": {
                            "Share": {
                                "properties": {
                                    "data": {
                                        "type": "string"
                                    }
                                },
                                "required": ["data"],
                                "type": "object"
                            },
                            "Decommitment": {
                                "properties": {
                                    "data": {
                                        "type": "string"
                                    }
                                },
                                "required": ["data"],
                                "type": "object"
                            }
                        }
                    }
                }
            },
            computingNodes: {
                elsaClient: {
                    displayName: "ELSA Data Owner",
                    clients: ['elsaClientToES', 'elsaClientToSH'],
                    servers: []
                },
                elsaES: {
                    displayName: "ELSA Evidence Service",
                    clients: [],
                    servers: ['elsaClientToES']
                },
                elsaShareholder: {
                    displayName: "ELSA Shareholder",
                    clients: [],
                    servers: ['elsaClientToSH']
                }
            }
        };

        const tsGenerator = specGenerators[children['Timestamp'].name];
        if (tsGenerator) {
            const tsResult = tsGenerator.generate(specGenerators, children['Timestamp'].children);
            Object.assign(result.apis, tsResult.apis);
            result.computingNodes.elsaES.clients.push(...tsResult.computingNodes.tsClient.clients);
            if (tsResult.computingNodes.tsVerifier) {
                result.computingNodes.elsaClient.clients.push(...tsResult.computingNodes.tsVerifier.clients);
            }
        }

        return result;
    },
    verify: function (specGenerators, children, existingApiSet, warnings, errors) {
        const genResult = this.generate(specGenerators, children);
        if (!isContainedIn(genResult, existingApiSet)) {
            warnings.push("The API SET does not contain all definitions required for ELSA.");
        }
    }
});

// Taken from https://stackoverflow.com/questions/23045652/object-comparing-check-if-an-object-contains-the-whole-other-object
function isContainedIn(a, b) {
    if (typeof a != typeof b)
        return false;
    if (Array.isArray(a) && Array.isArray(b)) {
        // assuming same order at least
        for (var i = 0, j = 0, la = a.length, lb = b.length; i < la && j < lb; j++)
            if (isContainedIn(a[i], b[j]))
                i++;
        return i == la;
    } else if (Object(a) === a) {
        for (var p in a)
            if (!(p in b && isContainedIn(a[p], b[p])))
                return false;
        return true;
    } else
        return a === b;
}