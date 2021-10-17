package de.cognicrypt.codegenerator.crysl.templates.LongTermSecureArchiving;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import de.cognicrypt.codegenerator.crysl.CrySLCodeGenerator;

public class LongTermSecureArchiving {
	public de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage.DataOwner createELSAClient(String evidenceServiceURL, String shareholderURLs, de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.EvidenceServiceAdapter esAdapter, de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ShareholderAdapter shAdapter) {
		System.out.println("BEGIN GENERATED CONSTRUCTION CODE");
		de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSA.Builder construction = null;
		System.out.println("END GENERATED CONSTRUCTION CODE");
		
		de.tu_darmstadt.crossing.composable_crypto.core.ComponentConfiguration configuration = new de.tu_darmstadt.crossing.composable_crypto.core.ComponentConfiguration()
				.prop("evidenceServiceURL", evidenceServiceURL)
				.prop("shareholderURLs", shareholderURLs)
				.prop("esAdapter", esAdapter)
				.prop("shAdapter", shAdapter);
		de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSA elsa = (de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSA) construction.build();
		
		de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage.DataOwner client = null;
		
		// Codegen
//		CrySLCodeGenerator.getInstance()
//			.includeClass("de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSAClient")
//			.addParameter(client, "client")
//			.addParameter(elsa, "elsa")
//			.generate();
		
		return client;
	}
	
	public java.util.UUID store(de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSAClient client, String data) {
		java.util.UUID uuid = null;
		InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		
		// Codegen
		CrySLCodeGenerator.getInstance()
			.includeClass("de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSAStoreOperation")
			.addParameter(client, "client")
			.addParameter(inputStream, "inputStream")
			.addParameter(uuid, "uuid")
			.generate();
		
		return uuid;
	}
	
	public String retrieve(de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSAClient client, java.util.UUID uuid) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		// Codegen
//		CrySLCodeGenerator.getInstance()
//			.includeClass("de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSARetrieveOperation")
//			.addParameter(client, "client")
//			.addParameter(outputStream, "outputStream")
//			.addParameter(uuid, "uuid")
//			.generate();
		
		String result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
		return result;
	}
}
