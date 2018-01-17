package com.rwtema.careerbees.networking;

import com.rwtema.careerbees.entity.EntityChunkData;
import com.rwtema.careerbees.gui.ContainerSettings;
import com.rwtema.careerbees.items.ItemBeeGun;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class BeeNetworking {
	public static SimpleNetworkWrapper net;

	public static void init() {
		net = new SimpleNetworkWrapper("PracBees");
		IMessageHandler<MessageBase, IMessage> genericHandler = (message, ctx) -> {
			message.onReceived(ctx);return null;
		};
		net.registerMessage(genericHandler, EntityChunkData.PacketEntityChunkData.class, 0, Side.CLIENT);
		net.registerMessage(genericHandler, ContainerSettings.MessageNBT.class, 1, Side.SERVER);
		net.registerMessage(genericHandler, ItemBeeGun.PacketSlotSelection.class, 2, Side.SERVER);
	}

	public abstract static class MessageBase implements IMessage {

		public void onReceived(MessageContext ctx) {

		}
	}

	public abstract static class MessageClientToServer extends MessageBase {

		@Override
		public void onReceived(@Nonnull MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> runServer(ctx, player));
		}

		protected abstract void runServer(MessageContext ctx, EntityPlayerMP player);

		public void writeNBT(NBTTagCompound tag, @Nonnull ByteBuf buf){
			new PacketBuffer(buf).writeCompoundTag(tag);
		}

		@Nullable
		public NBTTagCompound readNBT(@Nonnull ByteBuf bf){
			try {
				return new PacketBuffer(bf).readCompoundTag();
			} catch (IOException e) {
				throw new DecoderException(e);
			}
		}
	}

	public abstract static class MessageServerToClient extends MessageBase {
		@Override
		@SideOnly(Side.CLIENT)
		public void onReceived(MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> runClient(ctx, Minecraft.getMinecraft().player));
		}

		@SideOnly(Side.CLIENT)
		protected abstract void runClient(MessageContext ctx, EntityPlayer player);
	}
}
