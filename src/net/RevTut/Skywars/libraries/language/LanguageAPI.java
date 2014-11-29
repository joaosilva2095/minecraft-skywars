package net.RevTut.Skywars.libraries.language;

import net.RevTut.Skywars.libraries.reflection.ReflectionAPI;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Language Library.
 *
 * <P>A library with methods related to the languages.</P>
 *
 * @author Joao Silva
 * @version 1.0
 */
public final class LanguageAPI {

    /**
     * Constructor of AppearanceAPI
     */
    private LanguageAPI() {}

    /**
     * Get the language of a player using Bukkit methods
     *
     * @param player player to get the language
     * @return language of the player
     */
    public static Language getLanguage(Player player){
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        String lang = entityPlayer.locale;

        return getByCode(lang);
    }

    /**
     * Get the language of a player using Reflection
     *
     * @param player player to get the language
     * @return language of the player
     */
    public static Language getLanguageNMS(Player player){
        try {
            Object playerMethod = ReflectionAPI.getMethod(player.getClass(), "getHandle").invoke(player, (Object[]) null);
            Field field = ReflectionAPI.getField(playerMethod.getClass(), "locale");

            String lang = (String) field.get(playerMethod);
            field.setAccessible(false);

            return getByCode(lang);
        } catch (Throwable t) {
            Logger.getLogger("Minecraft").log(Level.WARNING, "Error while getting player language in LanguageAPI!");
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Get a language by it code
     *
     * @param code code of the language
     * @return language with that code
     */
    public static Language getByCode(String code) {
        for (Language language : Language.values())
            if (language.getCode().equalsIgnoreCase(code))
                return language;
        return null;
    }

    /**
     * Get a language by it name
     *
     * @param name name of the language
     * @return language with that name
     */
    public static Language getByName(String name) {
        for (Language language : Language.values())
            if (language.getName().equalsIgnoreCase(name))
                return language;
        return null;
    }
}
