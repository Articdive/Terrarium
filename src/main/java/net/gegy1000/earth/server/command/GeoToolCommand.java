package net.gegy1000.earth.server.command;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.message.EarthLocateMessage;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GeoToolCommand extends CommandBase {
    @Override
    public String getName() {
        return "geotool";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return DeferredTranslator.translateString(sender, "commands.earth.geotool.usage");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = CommandBase.getCommandSenderAsPlayer(sender);

        EarthCapability earthData = player.world.getCapability(TerrariumEarth.earthCap, null);
        if (earthData != null) {
            ContainerUi ui = ContainerUi.builder(player)
                    .withTitle(DeferredTranslator.translate(player, new TextComponentTranslation("container.earth.geotool.name")))
                    .withElement(Items.COMPASS, TextFormatting.BOLD + "Where am I?", () -> this.handleLocate(player, earthData))
                    .build();
            player.displayGUIChest(ui.createInventory());
        } else {
            throw DeferredTranslator.createException(player, "commands.earth.wrong_world");
        }
    }

    private void handleLocate(EntityPlayerMP player, EarthCapability earthData) {
        double latitude = earthData.getLatitude(player.posX, player.posZ);
        double longitude = earthData.getLongitude(player.posX, player.posZ);
        if (TerrariumHandshakeTracker.isFriendly(player)) {
            TerrariumEarth.NETWORK.sendTo(new EarthLocateMessage(latitude, longitude), player);
        } else {
            String location = TextFormatting.BOLD.toString() + TextFormatting.UNDERLINE + String.format("%.5f, %.5f", latitude, longitude);
            player.sendMessage(DeferredTranslator.translate(player, new TextComponentTranslation("geotool.earth.locate.success", location)));
        }
    }
}
