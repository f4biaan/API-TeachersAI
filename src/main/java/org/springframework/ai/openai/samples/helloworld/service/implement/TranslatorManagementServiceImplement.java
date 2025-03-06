package org.springframework.ai.openai.samples.helloworld.service.implement;

import org.apache.commons.codec.binary.Base64;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.samples.helloworld.service.TranslatorManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
public class TranslatorManagementServiceImplement implements TranslatorManagementService {
    private final ChatClient chatClient;

    @Autowired
    public TranslatorManagementServiceImplement(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public String imageToText(MultipartFile image) {
        System.out.println("Image to text");
        System.out.println(image.getContentType());

        // image to mimetype image png
        try {
            Resource imageResource = new InputStreamResource(image.getInputStream());

            UserMessage userMessage = new UserMessage(
                    "Explain what do you see on this picture?",
                    // new Media(MimeTypeUtils.IMAGE_PNG, image)
                    Collections.singletonList(new Media(MimeTypeUtils.IMAGE_PNG, imageResource)) // Cambiar aquí
            );

            ChatResponse response = chatClient.call(new Prompt(
                    userMessage,
                    OpenAiChatOptions.builder()
                            .withModel("gpt-4o")
                            .build()
            ));

            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error processing the image", e);
        }
    }

    private String convertToBase64(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            return Base64.encodeBase64String(bytes); // Codify los bytes en Base64
        } catch (IOException e) {
            throw new RuntimeException("Error converting image to Base64", e);
        }
    }

}
