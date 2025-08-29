package huhncode.lucid.lucidaddon;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import huhncode.lucid.lucidaddon.commands.CommandExample;
import huhncode.lucid.lucidaddon.commands.PlayerInfoCommand;
import huhncode.lucid.lucidaddon.hud.HudExample;
import huhncode.lucid.lucidaddon.services.CrystalBreakTracker;
import huhncode.lucid.lucidaddon.hud.CrystalPerSecondHud;
import huhncode.lucid.lucidaddon.services.KillDetectionService;

// import huhncode.lucid.lucidaddon.modules.AfkLog;
// import huhncode.lucid.lucidaddon.modules.AutoGG;
// import huhncode.lucid.lucidaddon.modules.AutoTotem;
// import huhncode.lucid.lucidaddon.modules.ChatBot;
// import huhncode.lucid.lucidaddon.modules.AutoTreeFarmer;
// import huhncode.lucid.lucidaddon.modules.ChatFonts;
// import huhncode.lucid.lucidaddon.modules.AntiItemDestroy;
// import huhncode.lucid.lucidaddon.modules.MultiCommand;
// import huhncode.lucid.lucidaddon.modules.StrongholdFinder;
// import huhncode.lucid.lucidaddon.modules.PacketLogger;
// import huhncode.lucid.lucidaddon.modules.BetterMacros;
// import huhncode.lucid.lucidaddon.modules.KillTrackerModule;
// import huhncode.lucid.lucidaddon.modules.KeyHolder;
// import huhncode.lucid.lucidaddon.modules.PingSpoofer;
// import huhncode.lucid.lucidaddon.modules.ChangingBlockStateESP;
import huhncode.lucid.lucidaddon.modules.*;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class LucidAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Lucid");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");
    public static final GuiTheme THEME = new MeteorGuiTheme();
    public static CrystalBreakTracker crystalBreakTracker;
    public static KillDetectionService killDetectionService;


    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Lucid");

        killDetectionService = new KillDetectionService();
        MeteorClient.EVENT_BUS.subscribe(killDetectionService);

        crystalBreakTracker = new CrystalBreakTracker();
        MeteorClient.EVENT_BUS.subscribe(crystalBreakTracker);
        // Modules
        //Modules.get().add(new TestMessageModule());
        //Modules.get().add(new ModuleExample());
        //Modules.get().add(new SlotSwitcher());
        Modules.get().add(new ChatBot());
        Modules.get().add(new ChatFonts());
        Modules.get().add(new AfkLog());
        Modules.get().add(new AutoGG());
        Modules.get().add(new AutoTotem());
        Modules.get().add(new AntiItemDestroy());
        Modules.get().add(new MultiCommand());
        Modules.get().add(new StrongholdFinder());
        Modules.get().add(new PacketLogger());
        //Modules.get().add(new BetterMacros());
        Modules.get().add(new KillTrackerModule());
        Modules.get().add(new KeyHolder());
        Modules.get().add(new PingSpoofer());
        Modules.get().add(new ChangingBlockStateESP());
        

        

        //Modules.get().add(new FakeInventory());
        //Modules.get().add(new InstantCrystalBreaker());
        //Modules.get().add(new PearlTrajectory());




        // Commands
        Commands.add(new CommandExample());
        Commands.add(new PlayerInfoCommand());


        // HUD
        Hud.get().register(HudExample.INFO);
        Hud.get().register(CrystalPerSecondHud.INFO);
        
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