package huhncode.lucid.lucidaddon;

import huhncode.lucid.lucidaddon.commands.CommandExample;
import huhncode.lucid.lucidaddon.hud.HudExample;
//import huhncode.lucid.lucidaddon.modules.TestMessageModule;
//import huhncode.lucid.lucidaddon.modules.ModuleExample;
//import huhncode.lucid.lucidaddon.modules.SlotSwitcher;
import huhncode.lucid.lucidaddon.modules.ChatBot;
import huhncode.lucid.lucidaddon.modules.ChatFonts;
import huhncode.lucid.lucidaddon.modules.AfkLog;
import huhncode.lucid.lucidaddon.modules.AutoGG;
import huhncode.lucid.lucidaddon.modules.AutoTotem;
import huhncode.lucid.lucidaddon.modules.FakeInventory;



//import huhncode.lucid.lucidaddon.commands.PlayerInfoCommand;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;

public class LucidAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Lucid");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");
    public static final GuiTheme THEME = new MeteorGuiTheme();

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Lucid");

        // Modules
        //Modules.get().add(new TestMessageModule());
        //Modules.get().add(new ModuleExample());
        //Modules.get().add(new SlotSwitcher());
        Modules.get().add(new ChatBot());
        Modules.get().add(new ChatFonts());
        Modules.get().add(new AfkLog());
        Modules.get().add(new AutoGG());
        Modules.get().add(new AutoTotem());
        Modules.get().add(new FakeInventory());



        // Commands
        Commands.add(new CommandExample());
        //Commands.add(new PlayerInfoCommand());


        // HUD
        Hud.get().register(HudExample.INFO);
        
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "huhncode.lucid.lucidaddon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("MeteorDevelopment", "meteor-Lucid");
    }
}