package com.personalProject.code_reviewer.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Slf4j
@Component
public class HmacValidator {

    @Value("${app.github.webhook-secret}")
    public String webhookSecret;

    public boolean isValidSignature(String payload, String signatureHeader){
        log.atDebug().log("HmacValidator.isValidSignature() - start");
        boolean isValidSignatureValue;
        if(signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            log.atDebug().addArgument(signatureHeader).log("HmacValidator.isValidSignature() - Singature Header: {} is Null or Doesnt start with sha256 prefix");
            return false;
        }
        String receivedHash= signatureHeader.substring("sha256=".length());
        String computedHash= extractComputedHash(payload,webhookSecret);
        log.atDebug().addArgument(receivedHash).addArgument(computedHash)
                .log("HmacValidator.isValidSignature() - Received Hash: {}, ComputedHash: {}");
        isValidSignatureValue= MessageDigest.isEqual(
                computedHash.getBytes(StandardCharsets.UTF_8),
                receivedHash.getBytes(StandardCharsets.UTF_8)
        );
        log.atDebug().addArgument(isValidSignatureValue).log("HmacValidator.isValidSignature() - end | isValidSignatureResult: {}");
        return isValidSignatureValue;

    }

    private String extractComputedHash(String payload, String webhookSecret) {
        log.atDebug().log("HmacValidator.extractComputedHash() - start");
        String resultComputedHash;
        SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8),"HmacSHA256");
        final Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to initialize HmacSHA256 Mac", e);
        }
        byte[] computedHashBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        log.atDebug().addArgument(computedHashBytes).log("HmacValidator.extractComputedHash() - computedHashBytes: {}");
        StringBuilder hexBuilder = new StringBuilder(computedHashBytes.length * 2);
        for (byte b : computedHashBytes) {
            hexBuilder.append(String.format("%02x", b));
        }
        resultComputedHash= hexBuilder.toString();
        log.atDebug().addArgument(resultComputedHash).log("HmacValidator.extractComputedHash() - end | resultComputedHash: {}");
        return resultComputedHash;
    }
}
