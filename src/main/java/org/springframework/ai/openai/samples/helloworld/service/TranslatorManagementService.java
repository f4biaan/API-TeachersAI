package org.springframework.ai.openai.samples.helloworld.service;

import org.springframework.web.multipart.MultipartFile;

public interface TranslatorManagementService {
    String imageToText(MultipartFile image);
}
