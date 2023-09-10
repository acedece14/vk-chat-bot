/*
 * Copyright (c) acedece14@gmail.com / vk.com/id6332939
 */

package by.katz.bot;

import by.katz.Utils;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.Fields;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static by.katz.Utils.isNumeric;

public class BotCommands {

    /**
     * Ответить на вопрос "Кто что-то там?" выбрав случайного пользователя в чате
     *
     * @param message  исходное сообщение
     * @param text     текст, который отправил паользователь
     * @param vkClient экземпляр клиента для vk api
     * @param appeal   обращение к пользователю
     */
    public static void cmdWho(Message message, String text, MyVkClient vkClient, String appeal) {
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

    /**
     * По команде заводится таймер на заваривание "дошика", если указано время,то таймер заведётся на указанное кол-во минут
     *
     * @param message  исходное сообщение
     * @param text     текст, который отправил паользователь
     * @param vkClient экземпляр клиента для vk api
     * @param appeal   обращение к пользователю
     */
    public static void cmdDoDoshik(Message message, String text, MyVkClient vkClient, String appeal) {
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

    /**
     * Отправить в чат случайную картинку
     *
     * @param message  исходное сообщение
     * @param vkClient экземпляр клиента для vk api
     * @param appeal   обращение к пользователю
     */
    public static void cmdRandomPicture(Message message, MyVkClient vkClient, String appeal) {
        try {
            var image = ImageIO.read(new URL("https://random.imagecdn.app/500/150"));
            var tmpFile = Files.createTempFile("vk", ".jpg").toFile();
            ImageIO.write(image, "jpg", tmpFile);
            var uploadUrl = vkClient.getApi()   // получаем адрес сервера для заливки  картинки
                  .photos()
                  .getMessagesUploadServer(vkClient.getGroupActor())
                  .execute().getUploadUrl();
            var uploadResponse = vkClient.getApi()// заливаем на сервер
                  .upload()
                  .photo(uploadUrl.toString(), tmpFile)
                  .execute();
            var savedImage = vkClient.getApi()// сохраняем на сервере и получаем данные сохранённой картинки
                  .photos()
                  .saveMessagesPhoto(vkClient.getGroupActor(), uploadResponse.getPhoto())
                  .server(uploadResponse.getServer())
                  .hash(uploadResponse.getHash())
                  .execute().get(0);
            var attach = String.format("photo%d_%d", savedImage.getOwnerId(), savedImage.getId());
            vkClient.sendMessage(message, appeal, attach);
        } catch (IOException | ApiException | ClientException e) {throw new RuntimeException(e);}
    }
}
