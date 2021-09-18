package api.utils;

import api.Objects;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class Bank {

    public static boolean openBank() {
        if (isOpen()) return true;
        SimpleObject bank = getNearbyBank();
        if (Objects.isValid(bank)) {
            bank.click(0);
            ClientContext.instance().sleepCondition(Bank::isOpen, 2000);
        }
        return isOpen();
    }

    public static SimpleObject getNearbyBank() {
        return Objects.getNearest("Bank booth");
    }

    public static boolean depositAll() {
        return ClientContext.instance().bank.depositInventory();
    }
    public static boolean isOpen() {
        return ClientContext.instance().bank.bankOpen();
    }
}
