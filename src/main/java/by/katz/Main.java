package by.katz;

import com.vk.api.sdk.objects.users.Fields;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        MyVkClient.INSTANCE.init(log);
        simpleTest(6332939);
    }

    private static void simpleTest(int simpleId) {
        var vkClient = MyVkClient.INSTANCE;
        var user = vkClient.getUser(simpleId, Fields.FIRST_NAME_NOM, Fields.LAST_NAME_NOM);
        if (user == null) {
            log.error("Cant get user with id: " + simpleId);
            return;
        }
        log.info("[TEST] Name: {} {}", user.getFirstName(), user.getLastName());
    }

}