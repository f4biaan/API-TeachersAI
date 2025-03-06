package org.springframework.ai.openai.samples.helloworld.controller;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.openai.samples.helloworld.service.TranslatorManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/translator")
public class TranslatorController {
    /*
      ! This.Controller will be used by app to translate images to text using OpenAI API
     */
    @Autowired
    private TranslatorManagementService translatorService;

    // Endpoint to translate image to text
    // Params get an image and return a string
    @PostMapping(value = "/imageToText")
    public ResponseEntity imageToText(@RequestParam("file") MultipartFile image) {
        return new ResponseEntity(translatorService.imageToText(image), HttpStatus.OK);
    }


}
