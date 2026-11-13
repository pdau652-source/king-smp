package com.king.addon;

import com.king.addon.modules.KingEsp;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class KingAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("King");

    @Override
    public void onInitialize() {
        Modules.get().add(new KingEsp());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.king.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("king", "king-esp");
    }
}
