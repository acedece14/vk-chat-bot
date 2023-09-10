package by.katz;

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
        } catch (ApiException | ClientException e) {
            log.error("cant connect to vk", e);
        }
    }

    public void init(Logger log) {
        this.log = log;
    }

    public GetResponse getUser(int id, Fields... fields) {
        var users = getUsers(id, fields);
        if (users.isEmpty())
            return null;
        return users.get(0);
    }

    public List<GetResponse> getUsers(int id, Fields... fields) {
        try {
            return api.users()
                  .get(groupActor)
                  .userIds(String.valueOf(id))
                  .fields(fields)
                  .lang(Lang.RU)
                  .execute();
        } catch (ApiException | ClientException e) {
            log.error("cant get user: " + id);
            return new ArrayList<>();
        }
    }

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

    public String getAppeal(Message message, int userId) {
        var user = getUser(message.getFromId(), Fields.FIRST_NAME_NOM);
        if (user == null)
            return null;
        return String.format("[id%d|%s], ", userId, user.getFirstName());
    }

}
