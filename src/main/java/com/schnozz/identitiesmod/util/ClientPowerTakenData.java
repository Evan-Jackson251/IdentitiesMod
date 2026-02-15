package com.schnozz.identitiesmod.util;

import java.util.ArrayList;

public class ClientPowerTakenData {
    private static ArrayList<String> powersTaken = new ArrayList<>();

    public static void setPowers(ArrayList<String> list) {
        powersTaken = new ArrayList<>(list);
    }

    public static ArrayList<String> getPowers() {
        return powersTaken;
    }
}