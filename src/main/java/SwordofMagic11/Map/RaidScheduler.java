package SwordofMagic11.Map;

import SwordofMagic11.Component.SomTask;

import java.time.LocalDateTime;

import static SwordofMagic11.Component.Function.broadcast;
import static SwordofMagic11.Component.Function.randomBool;
import static SwordofMagic11.SomCore.Log;
import static SwordofMagic11.SomCore.TaskOwner;

public class RaidScheduler {

    private static boolean isTrigger = false;

    public static void run() {
        SomTask.asyncTimer(() -> {
            LocalDateTime time = LocalDateTime.now();
            if (time.getHour() == 19 && time.getMinute() == 55) {
                if (!isTrigger) {
                    isTrigger = true;
                    trigger(DefenseBattle::open, DefenseBattle.Display);
                }
            } else if (time.getHour() == 20 && time.getMinute() == 55) {
                if (!isTrigger) {
                    isTrigger = true;
                    trigger(PvPRaid::open, PvPRaid.Display);
                }
            } else {
                isTrigger = false;
            }
        }, 600, TaskOwner);
    }


    public static void trigger(Runnable runnable, String text) {
        broadcast("§e5分後§aに" + text + "§aが§b開催§aされます");
        SomTask.asyncDelay(() -> {
            broadcast("§e3分後§aに" + text + "§aが§b開催§aされます");
            SomTask.asyncDelay(() -> {
                broadcast("§e1分後§aに" + text + "§aが§b開催§aされます");
                SomTask.asyncDelay(() -> {
                    broadcast("§aまもなく" + text + "§aが§b開催§aされます");
                    SomTask.asyncDelay(runnable, 20*10);
                }, 20*50);
            }, 20*60*2);
        }, 20*60*2);
    }
}
