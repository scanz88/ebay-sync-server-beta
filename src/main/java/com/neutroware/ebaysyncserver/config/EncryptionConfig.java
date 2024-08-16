package com.neutroware.ebaysyncserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class EncryptionConfig {

    @Value("${encryption.password}")
    String password;
    @Value("${encryption.salt}")
    String salt;

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(password, salt);
    }
}
