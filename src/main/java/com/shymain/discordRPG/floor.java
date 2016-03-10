package com.shymain.discordRPG;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sx.blah.discord.api.DiscordException;
import sx.blah.discord.api.MissingPermissionsException;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.HTTP429Exception;

public class Floor {
	
	public static String file = System.getProperty("user.home") + "/discordRPG/floors.json";
	
	public static void initialize() throws JSONException, IOException
	{
		String template = "{"
            + "\"events\":{"
            + "\"Rock\":{"
            + "\"ready\":3,"
            + "\"max\":3,"
            + "\"refresh_time\": 60,"
            + "\"drops\":\"iron_ore\""
            + "}"
            + "}"
    		+ "}";
		JSONObject json = new JSONObject(DiscordRPG.readFile(file));
		JSONObject floor = new JSONObject(template);
		json.getJSONObject("floors").put("149548522058809344", floor);
		FileWriter r = new FileWriter(file);
		r.write(json.toString(3));
		r.flush();
		r.close();
	}

	public static Encounter getMonster(MessageReceivedEvent event) throws JSONException, IOException
	{
		JSONObject json = new JSONObject(DiscordRPG.readFile(Monster.file));
		JSONObject json2 = new JSONObject(DiscordRPG.readFile(Store.file));
		JSONObject json3 = new JSONObject(DiscordRPG.readFile(Player.file));
		JSONObject mob = new JSONObject();
		JSONObject player = json3.getJSONObject("players").getJSONObject(event.getMessage().getAuthor().getID());
		JSONArray monsters = json2.getJSONObject("ranks").getJSONObject(Integer.toString(player.getInt("rank"))).getJSONArray("monsters");
		Random r = new Random();
		int ran = r.nextInt(monsters.length());
		String name = monsters.getString(ran);
		mob = json.getJSONObject("monsters").getJSONObject(name);
		Encounter newEncounter = new Encounter(name, mob.getInt("maxhealth"), mob.getInt("health"), mob.getInt("attack"), mob.getInt("speed"));
		return newEncounter;
	}
	
	public static void mineRock(IUser user, IChannel channel) throws JSONException, IOException, MissingPermissionsException, HTTP429Exception, DiscordException
	{
		JSONObject json = new JSONObject(DiscordRPG.readFile(file));
		JSONObject json2 = new JSONObject(DiscordRPG.readFile(Player.file));
		JSONObject player = json2.getJSONObject("players").getJSONObject(user.getID());
		JSONObject floor = json.getJSONObject("floors").getJSONObject(channel.getID());
		JSONObject rock = floor.getJSONObject("events").getJSONObject("Rock");
		if(rock.getInt("ready")==0)
		{
			channel.sendMessage("But there was no ore left.");
		}else{
			int ready = rock.getInt("ready");
			ready--;
			rock.remove("ready");
			rock.put("ready", ready);
			FileWriter r = new FileWriter(file);
			r.write(json.toString(3));
			r.flush();
			r.close();
			Player.inventoryAdd(user, rock.getString("drops"), 1);
			Event rockRefresh = new Event("RockRefreshEvent", user, channel);
			DiscordRPG.timedEvents.put(rockRefresh, rock.getInt("refresh_time"));
			channel.sendMessage("You get a " + rock.getString("drops") + "!");
		}
	}
	
	public static void addRock(IChannel channel) throws JSONException, IOException, MissingPermissionsException, HTTP429Exception, DiscordException
	{
		JSONObject json = new JSONObject(DiscordRPG.readFile(file));
		JSONObject rock = json.getJSONObject("floors").getJSONObject(channel.getID()).getJSONObject("events").getJSONObject("Rock");
		rock.increment("ready");
		FileWriter r = new FileWriter(file);
		r.write(json.toString(3));
		r.flush();
		r.close();
		channel.sendMessage("A vein reappears in one of the rocks.\n"
				+ rock.getInt("ready") + " of " + rock.getInt("max") + " rocks now available.");
		
	}
	
}