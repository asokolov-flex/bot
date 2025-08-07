package com.sokolov.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.sokolov.telegram.BotMessages.*;

@Slf4j
public class TinderBoltApp extends MultiSessionTelegramBot {

    public static final String TELEGRAM_BOT_NAME = "super_tinder_ai_bot"; //TODO: name of bot

    public static final String TELEGRAM_BOT_TOKEN = "TELEGRAM_TOKEN"; //TODO: token of bot

    public static final String OPEN_AI_TOKEN = "GPT TOKEN"; //TODO: token GPT

    public static final String API_TELEGRAM = "https://api.telegram.org/file/bot";

    private ChatGPTService chatGPTService = new ChatGPTService(OPEN_AI_TOKEN);

    private DialogMode currentMode = null;

    private ArrayList<String> messages = new ArrayList<>();

    private UserInfo me;

    private int questionCount = 0;

    private UserInfo she;

    private int questionCountShe = 0;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) throws TelegramApiException {

        String message = getMessageText();

        if (start(message)) return;

        if (gpt(message)) return;

        if (date(message)) return;

        if (messaging(message)) return;

        if (profile(message)) return;

        if (opener(message)) return;

        sendTextMessage("Hello: your message " + message);

        sendTextButtonsMessage("Choose your type: ", "Start", "start", "Stop", "stop");

    }

    private boolean opener(String message) {

        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;

            sendPhotoMessage("opener");

            she = new UserInfo();

            questionCountShe = 1;

            sendTextMessage(WHAT_S_THE_NAME);

            return true;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {

            switch (questionCountShe) {
                case 1:
                    she.name = message;

                    questionCountShe = 2;

                    sendTextMessage(HOW_OLD_IS_SHE);

                    return true;
                case 2:
                    she.age = message;

                    questionCountShe = 3;

                    sendTextMessage(DOES_SHE_HAVE_ANY_HOBBIES);

                    return true;
                case 3:
                    she.hobby = message;

                    questionCountShe = 4;

                    sendTextMessage(WHAT_S_HER_OCCUPATION);

                    return true;
                case 4:
                    she.occupation = message;

                    questionCountShe = 5;

                    sendTextMessage(WHAT_S_HER_DATING_GOAL);

                    return true;
                case 5:
                    she.goals = message;

                    var aboutFriend = she.toString();

                    log.info("User info about friend: {}", aboutFriend);

                    var openerPrompt = loadPrompt("opener");

                    var answer = chatGPTService.sendMessage(openerPrompt, aboutFriend);

                    log.info("GPT answer: {}", answer);

                    Message msg = sendTextMessage(WAIT_A_BIT_I_M_THINKING);

                    updateTextMessage(msg, answer);

                    return true;
            }


            return true;
        }
        return false;
    }

    private boolean profile(String message) {

        if (message.equals("/profile")) {
            currentMode = DialogMode.PROFILE;

            sendPhotoMessage("profile");

            me = new UserInfo();

            questionCount = 1;

            sendTextMessage(HOW_OLD_ARE_YOU);

            return true;
        }

        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {

            switch (questionCount) {
                case 1:
                    me.age = message;

                    questionCount = 2;

                    sendTextMessage(WHAT_S_YOUR_OCCUPATION);

                    return true;
                case 2:
                    me.occupation = message;

                    questionCount = 3;

                    sendTextMessage(DO_YOU_HAVE_ANY_HOBBIES);

                    return true;
                case 3:
                    me.hobby = message;

                    questionCount = 4;

                    sendTextMessage(WHAT_DON_T_YOU_LIKE_IN_PEOPLE);

                    return true;

                case 4:
                    me.annoys = message;

                    questionCount = 5;

                    sendTextMessage(WHAT_S_YOUR_DATING_GOAL);

                    return true;

                case 5:
                    me.goals = message;

                    var aboutMyself = me.toString();

                    log.info("User info about himself: {}", aboutMyself);

                    var prompt = loadPrompt("profile");

                    var answer = chatGPTService.sendMessage(prompt, aboutMyself);

                    log.info("GPT answer: {}", answer);

                    Message msg = sendTextMessage("Wait a bit, I'm thinking \uD83E\uDDE0");

                    updateTextMessage(msg, answer);

                    return true;
            }
        }

        return false;
    }

    private boolean messaging(String message) {
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;

            sendPhotoMessage("message");

            sendTextButtonsMessage(HELLO_SEND_IN_THE_CHAT_YOURS_DIALOG,
                    NEXT_MESSAGE, "message_next",
                    INVITE_ON_A_DATE, "message_date");

            return true;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {

            log.info("User prompt: {}", message);

            var query = getCallbackQueryButtonKey();

            if (query.startsWith("message_")) {
                // prompt message_next or message_date
                var prompt = loadPrompt(query);

                var userChatHistory = String.join("\n\n", messages);

                log.info("User chat history: {}", userChatHistory);

                var answer = chatGPTService.sendMessage(prompt, userChatHistory); // 10 sec

                Message msg = sendTextMessage(WAIT_A_BIT_I_M_THINKING);

                log.info("GPT answer: {}", answer);

                updateTextMessage(msg, answer);

                return true;
            }

            messages.add(message);

            return true;
        }
        return false;
    }

    private boolean date(String message) {
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;

            sendPhotoMessage("date");

            var text = loadMessage("date");

            sendTextButtonsMessage(text,
                    "Ariana Grande", "date_grande",
                    "Margot Robbie", "date_robbie",
                    "Zendaya", "date_zendaya",
                    "Ryan Gosling", "date_gosling",
                    "Tom Hardy", "date_hardy");

            return true;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            var query = getCallbackQueryButtonKey();

            if (query.startsWith("date_")) {

                sendPhotoMessage(query);

                sendTextMessage(DATE_IN_5_MESSAGES_️);

                var prompt = loadPrompt(query);

                chatGPTService.setPrompt(prompt);

                return true;
            }

            var answer = chatGPTService.addMessage(message);

            log.info("User prompt: {}", message);

            log.info("GPT answer: {}", answer);

            Message msg = sendTextMessage(WAIT_A_BIT_I_M_THINKING);

            updateTextMessage(msg, answer);

            return true;
        }
        return false;
    }

    private boolean gpt(String message) {
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;

            log.info("Go to ChatGPT mode");

            sendPhotoMessage("gpt");

            var text = loadMessage("gpt");

            sendTextMessage(text);

            return true;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {

            var prompt = loadPrompt("gpt");

            log.info("ChatGPT question: {}", message);

            var answer = chatGPTService.sendMessage(prompt, message);

            log.info("ChatGPT answer: {}", answer);

            Message msg = sendTextMessage(WAIT_A_BIT_I_M_THINKING);

            updateTextMessage(msg, answer);

            return true;
        }
        return false;
    }

    private boolean start(String message) {
        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;

            sendPhotoMessage("main");

            sendTextMessage(I_CAN_HELP_YOU_TO_FIND_A_MATCH);

            String userInfo = new UserInfo().toString();

            String mainMessage = loadMessage("main");

            sendTextMessage(mainMessage);

            log.info("User info: {}", userInfo);

            showMainMenu("Main menu", "/start",
                    "Ask GPT \uD83E\uDDE0", "/gpt",
                    "Generate Profile \uD83D\uDE0E", "/profile",
                    "Message to get acquainted \uD83E\uDD70", "/opener",
                    "Messaging on your behalf \uD83D\uDE08", "/message",
                    "Messaging with celebrities \uD83D\uDD25", "/date",
                    "Stop", "/stop");

            return true;
        }

        return false;
    }

    private void saveCurrentUserPhoto(List<List<PhotoSize>> userProfilePhotos) throws TelegramApiException {
        if (userProfilePhotos != null && !userProfilePhotos.isEmpty()) {
            log.info("User profile photos: {}", userProfilePhotos);

            List<PhotoSize> photoSizes = userProfilePhotos.get(0);

            PhotoSize bestPhoto = photoSizes.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElseThrow();

            String fileId = bestPhoto.getFileId();

            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);

            AbsSender bot = this;

            File file = bot.execute(getFile);

            String fileUrl = API_TELEGRAM + TELEGRAM_BOT_TOKEN + "/" + file.getFilePath();

            try (InputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                 FileOutputStream out = new FileOutputStream("user_profile_photo.jpg")) {

                byte[] buffer = new byte[1024];

                int n;

                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Photo saved as user_profile_photo.jpg");
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }

}
