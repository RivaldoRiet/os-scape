package StallThiever;

import java.util.Arrays;
import java.util.Comparator;

public enum Stall {

    FOOD("Food", 1),
    GENERAL("General", 25),
    MAGIC("Magic", 50),
    SCIMITAR("Scimitar", 70),
    CRAFTING("Crafting", 90);


    private final String name;
    private final int reqLvl;

    private Stall(String name, int reqLvl) {
        this.name = name;
        this.reqLvl = reqLvl;
    }

    public String getName() {
        return name + " Stall";
    }

    public int getReqLvl() {
        return reqLvl;
    }

    static public Stall getMaxStall(int currentLvl) {
        return Arrays.stream(Stall.values()).filter(s -> s.reqLvl <= currentLvl).max(Comparator.comparing(Stall::getReqLvl)).orElse(FOOD);
    }
}
