package me.MiniDigger.VoxelGamesLib.api.lang;

import me.MiniDigger.VoxelGamesLib.api.command.CommandArguments;
import me.MiniDigger.VoxelGamesLib.api.command.CommandInfo;
import me.MiniDigger.VoxelGamesLib.api.role.Role;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Handles all commands related to lang and i18n
 */
@SuppressWarnings("JavaDoc") // commands don't need javadoc, go read the command's descriptions
public class LangCommands {
    
    @Inject
    private LangHandler langHandler;
    
    @CommandInfo(name = "lang", perm = "command.lang", role = Role.DEFAULT)
    public void lang(CommandArguments args) {
        StringBuilder sb = new StringBuilder();
        for (Locale loc : langHandler.getInstalledLocales()) {
            sb.append(loc.getName()).append(" ");
        }
        Lang.msg(args.getSender(), LangKey.LANG_INSTALLED, sb.toString());
        Lang.msg(args.getSender(), LangKey.LANG_USING, args.getSender().getLocale().getName());
    }
    
    @CommandInfo(name = "lang.set", perm = "command.lang.set", role = Role.DEFAULT, min = 1)
    public void set(CommandArguments args) {
        Optional<Locale> loc = Locale.fromTag(args.getArg(0));
        if (!loc.isPresent()) {
            Lang.msg(args.getSender(), LangKey.LANG_UNKNOWN, args.getArg(0));
            return;
        }
        args.getSender().setLocal(loc.get());
        Lang.msg(args.getSender(), LangKey.LANG_SET, args.getSender().getLocale().getName());
    }
}
