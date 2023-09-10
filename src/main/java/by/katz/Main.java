/*
 * Copyright (c) acedece14@gmail.com / vk.com/id6332939
 */

package by.katz;

import by.katz.bot.MyVkClient;
import com.vk.api.sdk.objects.users.Fields;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    private static final int PAVEL_DUROV_ID = 1;

    public static void main(String[] args) {
        MyVkClient.INSTANCE.init(log);  // запускаемся
        simpleTest();                   // тест для нас, что подключились нормально
    }

    private static void simpleTest() {
        var user = MyVkClient.INSTANCE.getUser(PAVEL_DUROV_ID, Fields.FIRST_NAME_NOM, Fields.LAST_NAME_NOM);
        if (user == null) {
            log.error("Cant get user with id: " + PAVEL_DUROV_ID);
            return;
        }
        log.info("[TEST] Durov name: {} {}", user.getFirstName(), user.getLastName());
    }

}