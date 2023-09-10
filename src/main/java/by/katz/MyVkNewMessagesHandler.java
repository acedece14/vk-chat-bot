package by.katz;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.events.longpoll.GroupLongPollApi;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.Fields;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static by.katz.Utils.isNumeric;

@Slf4j
public class MyVkNewMessagesHandler extends GroupLongPollApi {

    protected MyVkNewMessagesHandler(VkApiClient client, GroupActor actor, int waitTime) {super(client, actor, waitTime);}

    @Override protected void messageNew(Integer groupId, Message message) {
        log.info("[NEW MESSAGE] " + message.toString());
        var text = message.getText();
        if (text == null)
            return;
        var vkClient = MyVkClient.INSTANCE;
        var appeal = vkClient.getAppeal(message, message.getFromId());
        if (text.equals("книги"))
            vkClient.sendMessage(message, appeal + " книги - это хорошо");
        if (text.contains("хуй"))
            vkClient.sendMessage(message, appeal + " не ругайся, сука");
        if (text.startsWith("кто "))
            doWho(message, text, vkClient, appeal);
        if (text.startsWith("дошик"))
            doDoshirak(text, message, appeal, vkClient);
        if (text.equals("картинка"))
            sendRandomPicture(message, appeal, vkClient);
    }

    private void sendRandomPicture(Message message, String appeal, MyVkClient vkClient) {
        try {
            var image = ImageIO.read(new URL("https://random.imagecdn.app/500/150"));
            var tmpFile = Files.createTempFile("vk", ".jpg").toFile();
            ImageIO.write(image, "jpg", tmpFile);
            var uploadUrl = vkClient.getApi().photos().getMessagesUploadServer(vkClient.getGroupActor()).execute().getUploadUrl();
            var uploadResponse = vkClient.getApi()
                  .upload()
                  .photo(uploadUrl.toString(), tmpFile)
                  .execute();
            var savedImage = vkClient.getApi()
                  .photos()
                  .saveMessagesPhoto(vkClient.getGroupActor(), uploadResponse.getPhoto())
                  .server(uploadResponse.getServer())
                  .hash(uploadResponse.getHash())
                  .execute().get(0);
            var attach = String.format("photo%d_%d", savedImage.getOwnerId(), savedImage.getId());
            vkClient.sendMessage(message, appeal, attach);
        } catch (IOException | ApiException | ClientException e) {throw new RuntimeException(e);}
    }

    private void doWho(Message message, String text, MyVkClient vkClient, String appeal) {
        try {
            var response = vkClient.getApi()
                  .messages()
                  .getConversationMembers(vkClient.getGroupActor(), message.getPeerId())
                  .execute();
            if (response == null || response.getCount() == 0) {
                vkClient.sendMessage(message, "Ошибка получения пользователей");
                return;
            }
            var users = response.getItems().stream().filter(u -> u.getMemberId() > 0)
                  .sorted(new Utils.MyRandomComparator<>())
                  .limit(1)
                  .toList();
            var user = vkClient.getUser(users.get(0).getMemberId(), Fields.FIRST_NAME_NOM);
            if (user == null) {
                vkClient.sendMessage(message, "Ошибка получения пользователя");
                return;
            }
            vkClient.sendMessage(message, appeal + " кто " + text.substring(4) + "? Это " + user.getFirstName());
        } catch (ApiException | ClientException e) {throw new RuntimeException(e);}
    }

    private void doDoshirak(String text, Message message, String appeal, MyVkClient vkClient) {
        new Thread(() -> {
            var minutesToWait = 4;
            var splitted = text.split(" ");
            if (splitted.length == 2 && isNumeric(splitted[1]))
                minutesToWait = Integer.parseInt(splitted[1]);
            vkClient.sendMessage(message, appeal + " жди, скоро напомню");
            Utils.sleepInMinutes(minutesToWait);
            vkClient.sendMessage(message, appeal + " дошик готов!");
        }).start();
    }
}
