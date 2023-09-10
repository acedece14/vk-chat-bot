/*
 * Copyright (c) acedece14@gmail.com / vk.com/id6332939
 */

package by.katz.bot;

import by.katz.AppSettings;
import com.vk.api.sdk.client.Lang;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * основной класс-обёртка для обращения к vk.com
 */
public enum MyVkClient {

    INSTANCE;
    private static final String API_VERSION = "5.95";
    @Getter
    private VkApiClient api;
    @Getter
    private GroupActor groupActor;
    private Logger log;

    MyVkClient() {
        try {
            api = new VkApiClient(new HttpTransportClient());
            var groupId = AppSettings.getInstance().getGroupId();
            var accessToken = AppSettings.getInstance().getAccessToken();
            groupActor = new GroupActor(groupId, accessToken);
            api.groups().setLongPollSettings(groupActor, groupId)
                  .apiVersion(API_VERSION)
                  .enabled(true)
                  .messageNew(true)
                  .execute();
            api.groupsLongPoll().getLongPollServer(groupActor, groupId).execute();
            new MyVkNewMessagesHandler(api, groupActor, 25).run();
        } catch (ApiException | ClientException e) {log.error("cant connect to vk", e);}
    }

    public void init(Logger log) {this.log = log;}

    /**
     * получить инфармацию из профиля одного пользователя
     */
    public GetResponse getUser(int id, Fields... fields) {
        var users = getUsers(List.of(id), fields);
        if (users.isEmpty())
            return null;
        return users.get(0);
    }

    /**
     * получить информацию о профилях пользователей
     *
     * @param ids    список id
     * @param fields список полей профиля
     */
    public List<GetResponse> getUsers(List<Integer> ids, Fields... fields) {
        try {

            return api.users()
                  .get(groupActor)
                  .userIds(ids.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                  .fields(fields)
                  .lang(Lang.RU)
                  .execute();
        } catch (ApiException | ClientException e) {
            log.error("cant get user: " + ids);
            return new ArrayList<>();
        }
    }

    /**
     * отправить сообщение в чат
     *
     * @param message     исходное сообщение
     * @param text        текст сообщения для отправки а чат
     * @param attachments прикреплённые картинки/видео/музыка и т.д.
     * @return true
     */
    public boolean sendMessage(Message message, String text, String... attachments) {
        try {
            log.info("[SEND MESSAGE] " + text);
            var msg = api.messages()
                  .send(groupActor)
                  .randomId(new Random().nextInt())
                  .peerId(message.getPeerId())
                  .message(text);
            if (attachments != null)
                msg.attachment(String.join(",", attachments));
            msg.execute();
        } catch (ApiException | ClientException e) {
            log.error("cant send message", e);
            return false;
        }
        return true;
    }

    /**
     * создать обращение к пользователю
     */
    public String getAppeal(Message message, int userId) {
        var user = getUser(message.getFromId(), Fields.FIRST_NAME_NOM);
        if (user == null)
            return null;
        return String.format("[id%d|%s], ", userId, user.getFirstName());
    }

}
