/*
 * Copyright (c) acedece14@gmail.com / vk.com/id6332939
 */

package by.katz;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Utils {

    public static boolean isNumeric(String strNum) {
        if (strNum == null)
            return false;
        var pattern = Pattern.compile("-?\\d+?");
        return pattern.matcher(strNum).matches();
    }

    public static void sleepInMinutes(int minutesToWait) {
        try {
            TimeUnit.MINUTES.sleep(minutesToWait);
        } catch (InterruptedException e) {throw new RuntimeException(e);}
    }

    public static final class MyRandomComparator<OBJ> implements Comparator<OBJ> {

        private final Map<OBJ, Integer> map = new IdentityHashMap<>();
        private final Random random;

        public MyRandomComparator() {this(new Random());}

        private MyRandomComparator(Random random) {this.random = random;}

        @Override
        public int compare(OBJ obj1, OBJ obj2) {return Integer.compare(valueFor(obj1), valueFor(obj2));}

        private int valueFor(OBJ obj) {
            synchronized (map) {
                return map.computeIfAbsent(obj, ignore -> random.nextInt());
            }
        }
    }
}
