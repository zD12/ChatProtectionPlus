package net.willhastings.ChatProtectionPlus.Listeners;

import net.willhastings.ChatProtectionPlus.ChatProtectionPlus;
import net.willhastings.ChatProtectionPlus.CustomFunction;
import net.willhastings.ChatProtectionPlus.util.Config;
import net.willhastings.ChatProtectionPlus.util.MessageHandler;
import net.willhastings.ChatProtectionPlus.util.User;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener 
{
	private MessageHandler messageHandler = ChatProtectionPlus.messageHandler;
	
	public ChatListener(ChatProtectionPlus plgin) 
	{
		plgin.getServer().getPluginManager().registerEvents(this, plgin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) 
	{
		Player player = event.getPlayer();		
		
		if(event.isCancelled()) return;
		else if(!Config.USE_ANTICAPS && !Config.USE_ANTICHAT_SPAM) return;
		
		if(!player.isOnline()) return;
		
		if(CustomFunction.isChatMuted())
		{
			if(CustomFunction.hasPermission(player, "cpp.ignore.chatlock") || CustomFunction.hasPermission(player, "cpp.ignore.*")) return;
			player.sendMessage(messageHandler.getMessage("server.chatlocked", true));
			event.setCancelled(true);
			return;
		}
		
		String pMessage = event.getMessage();
		User user = CustomFunction.getUser(player);
		
		if(Config.USE_ANTICHAT_SPAM) 
		{
			if(CustomFunction.hasPermission(player, "cpp.ignore.chatspam") || CustomFunction.hasPermission(player, "cpp.ignore.*")) return;
			Long currTime = System.currentTimeMillis();	
			Long timeDif = (currTime - user.getPrevMessageTime());
			
			if(timeDif < Config.MILIS_BETWEEN_MESSAGES_ALLOWED) 
			{
				if(Config.NOTIFY_USER) 
				{
					event.getPlayer().sendMessage(messageHandler.getFormatedMessage("user.spam.chat", true, user.getChatInfactions(), Config.MAX_ALLOWED_CHATSPAM_INFRACTION));
					
					if(Config.NOTIFY_ADMIN) 
					{
						CustomFunction.NotifyAdministrators(messageHandler.getFormatedMessage("admin.spam.chat", true, player.getDisplayName(), timeDif));
					}
				}
				
				if(user.getChatInfactions() >= Config.MAX_ALLOWED_CHATSPAM_INFRACTION) 
				{				
					if(Config.BAN_FOR_CHAT_SPAMING) 
					{
						Bukkit.broadcastMessage(messageHandler.getFormatedMessage("spam.ban.chat", true, player.getDisplayName()));
						Bukkit.banIP(player.getAddress().getAddress().getHostAddress());
						player.kickPlayer(messageHandler.getMessage("user.ban.chat", false));
						return;
					} 
					else 
					{
						Bukkit.broadcastMessage(messageHandler.getFormatedMessage("spam.kick.chat", true, player.getDisplayName()));
						player.kickPlayer(messageHandler.getMessage("user.kick.chat", false));
						return;
					}
				}
				user.giveChatInfraction(1);
				event.setCancelled(true);
				return;
			} 
			else 
			{
				user.setPrevMessageTime(currTime);
			}
			
			if(pMessage.equals(user.getPrevMessage())) 
			{
				if(Config.NOTIFY_USER) 
				{
					event.getPlayer().sendMessage(messageHandler.getMessage("user.repeated.message", true));
				}
				event.setCancelled(true);
				return;
			} 
			else 
			{
				user.setPrevMessage(pMessage);
			}
		}	
		
		if(Config.USE_ANTICAPS) 
		{
			if(CustomFunction.hasPermission(player, "cpp.ignore.anticaps") || CustomFunction.hasPermission(player, "cpp.ignore.*")) return;
			if(pMessage.length() < 1) return;
			double upperCaseCount = CustomFunction.capitalCount(pMessage);			
			if((upperCaseCount / pMessage.length()) * 100 > Config.ALLOWED_CAPITALS_PERCENT) 
			{
				if(Config.NOTIFY_USER) event.getPlayer().sendMessage(messageHandler.getMessage("user.caps", true));
				event.setMessage(pMessage.toLowerCase());
				return;
			}
		}
	}

}
