/*
 * Copyright (c) acedece14@gmail.com / vk.com/id6332939
 */

package by.katz.bot;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.events.longpoll.GroupLongPollApi;
import com.vk.api.sdk.objects.messages.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * Хэндлер для приёма входящих сообщений
 */
@Slf4j
public class MyVkNewMessagesHandler extends GroupLongPollApi {

    protected MyVkNewMessagesHandler(VkApiClient client, GroupActor actor, int waitTime) {super(client, actor, waitTime);}

    /**
     * Вызывается каждый раз,когда в чат с ботом приходит сообщение
     *
     * @param groupId id группы
     * @param message пришедшее сообщение
     */
    @Override protected void messageNew(Integer groupId, Message message) {
        log.info("[NEW MESSAGE] " + message.toString());
        var text = message.getText();
        if (text == null)
            return;
        var vkClient = MyVkClient.INSTANCE;
        var appeal = vkClient.getAppeal(message, message.getFromId());
        if (text.equalsIgnoreCase("мяу"))
            vkClient.sendMessage(message, "мяу-мяу-мяу");
        if (text.toLowerCase().contains("хуй"))
            vkClient.sendMessage(message, appeal + " не ругайся, сука");
        if (text.toLowerCase().startsWith("кто "))
            BotCommands.cmdWho(message, text, vkClient, appeal);
        if (text.toLowerCase().startsWith("дошик"))
            BotCommands.cmdDoDoshik(message, text, vkClient, appeal);
        if (text.equalsIgnoreCase("картинка"))
            BotCommands.cmdRandomPicture(message, vkClient, appeal);
    }

}
