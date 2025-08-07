
package com.sokolov.telegram;

import com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.entity.chat.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatGPTService {
    private ChatGPT chatGPT;

    private List<Message> messageHistory = new ArrayList<>(); // Chat history with ChatGPT - needed for dialogues

    public ChatGPTService(String token) {
        this.chatGPT = ChatGPT.builder()
                .apiKey(token)
                .apiHost("https://openai.javarush.com/")
                .build()
                .init();
    }

    /**
     * Single request to ChatGPT in "request" -> "response" format.
     * Request consists of two parts:
     *      prompt - question context
     *      question - the request itself
     */
    public String sendMessage(String prompt, String question) {
        Message system = Message.ofSystem(prompt);
        Message message = Message.of(question);
        messageHistory = new ArrayList<>(Arrays.asList(system, message));

        return sendMessagesToChatGPT();
    }

    /**
     * Requests to ChatGPT with message history preservation.
     * Method setPrompt() sets the request context
     */
    public void setPrompt(String prompt) {
        Message system = Message.ofSystem(prompt);
        messageHistory = new ArrayList<>(List.of(system));
    }

    /**
     * Requests to ChatGPT with message history preservation.
     * Method addMessage() adds a new question (message) to the chat.
     */
    public String addMessage(String question) {
        Message message = Message.of(question);
        messageHistory.add(message);

        return sendMessagesToChatGPT();
    }

    /**
     * Send a series of messages to ChatGPT: prompt, message1, answer1, message2, answer2, ..., messageN
     * ChatGPT's response is added to the end of messageHistory for subsequent use
     */
    private String sendMessagesToChatGPT(){
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(ChatCompletion.Model.GPT4oMini) // GPT4Turbo or GPT4oMini or GPT_3_5_TURBO
                .messages(messageHistory)
                .maxTokens(3000)
                .temperature(0.9)
                .build();

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);
        Message res = response.getChoices().get(0).getMessage();
        messageHistory.add(res);

        return res.getContent();
    }
}
