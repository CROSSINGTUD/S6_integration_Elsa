composableCrypto.register({
    name: "Remote_non_interactive_Signature_based_Timestamp",
    generate: function (specGenerators, children) {
        const result = {
            apis: {
                tsClientToStamper: {
                    "openapi": "3.0.1",
                    "info": {
                        "description": "A description of the API used for communication between clients and the Timestamp Service (Stamp) in ELSA",
                        "title": "ELSA Timestamp Service API (Stamp)",
                        "version": "1.0.0"
                    },
                    "servers": [{
                        "url": "/"
                    }],
                    "tags": [{
                        "description": "Operations that belong to timestamping",
                        "name": "ts"
                    }],
                    "paths": {
                        "/timestamps": {
                            "post": {
                                "operationId": "tsCreateStamp",
                                "requestBody": {
                                    "$ref": "#/components/requestBodies/inline_object",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "properties": {
                                                    "message": {
                                                        "type": "string"
                                                    }
                                                },
                                                "required": ["message"],
                                                "type": "object"
                                            }
                                        }
                                    },
                                    "description": "Message",
                                    "required": true
                                },
                                "responses": {
                                    "400": {
                                        "content": {},
                                        "description": "Message is not set"
                                    },
                                    "200": {
                                        "content": {
                                            "application/json": {
                                                "schema": {
                                                    "$ref": "#/components/schemas/Timestamp"
                                                }
                                            }
                                        },
                                        "description": "Operation completed without errors"
                                    }
                                },
                                "summary": "Create a timestamp",
                                "tags": ["ts"]
                            }
                        }
                    },
                    "components": {
                        "requestBodies": {
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
                            "Timestamp": {
                                "properties": {
                                    "time": {
                                        "type": "string"
                                    },
                                    "stamp": {
                                        "type": "string"
                                    }
                                },
                                "required": ["stamp", "time"],
                                "type": "object"
                            },
                            "inline_object": {
                                "properties": {
                                    "message": {
                                        "type": "string"
                                    }
                                },
                                "required": ["message"],
                                "type": "object"
                            }
                        }
                    }
                }
            },
            computingNodes: {
                tsClient: {
                    displayName: "TS Client",
                    clients: ['tsClientToStamper'],
                    servers: []
                },
                tsStamper: {
                    displayName: "TS Stamper",
                    clients: [],
                    servers: ['tsClientToStamper']
                }
            }
        };

        return result;
    },
    verify: function (specGenerators, children, existingApiSet, warnings, errors) {

    }
});