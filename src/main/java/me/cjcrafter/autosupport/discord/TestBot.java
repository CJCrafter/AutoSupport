package me.cjcrafter.autosupport.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.net.URL;

public class TestBot {

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault("MTAyMTc5NjExOTYxOTA0MzM3OA.GfIJJc.23de_twv6sFx1ONVOBriuV83f17RNtUJg-OdGU");

        URL folder = TestBot.class.getClassLoader().getResource("examples");
        DiscordImpl autosupport = new DiscordImpl(folder, System.out::println);

        builder.addEventListeners(autosupport);

        builder.enableIntents(GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_BANS);

        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);

        JDA jda = builder.build().awaitReady();
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.watching("you sleep"));
    }
}
