package au.id.rleach.easyprefix;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Plugin(
        id = "easyprefix",
        name = "EasyPrefix",
        authors = "ryantheleach",
        version = "1.0.0",
        description = "Adds scprefix & scsuffix commands to add prefixes and suffixes to scoreboard"
)
public class EasyPrefix {

    @Inject private Logger logger;

    static final Text teamKey = Text.of("team");
    static final Text prefixKey = Text.of("prefix");
    static final Text suffixKey = Text.of("suffix");

    @Listener
    public void onServerStart(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, makePrefixCommand(), "prefix", "scprefix");
        Sponge.getCommandManager().register(this, makeSuffixCommand(), "suffix", "scsuffix");
    }

    private CommandCallable makePrefixCommand() {
        return  fixCommand(prefixKey)
                .executor(doFix(prefix(), prefixKey))
                .build();
    }
    private CommandCallable makeSuffixCommand() {
        return  fixCommand(suffixKey)
                .executor(doFix(suffix(), suffixKey))
                .build();
    }


    private CommandSpec.Builder fixCommand(Text fixKey) {
        return CommandSpec.builder()
                ///scoreboard teams option teamname prefix value
                .arguments(
                        GenericArguments.choices(teamKey, this::getTeams, this::teamFromString, false),
                        GenericArguments.string(fixKey)
                ).permission("minecraft.command.scoreboard");
    }

    private CommandExecutor doFix(BiConsumer<Text, Team> setFix, Text fixKey) {
        return (CommandSource src, CommandContext commandContext)->{
            try {
                Team team = (Team) commandContext.getOne(teamKey).get();
                Text fix = deSerialize((String) commandContext.getOne(fixKey).get());
                setFix.accept(fix, team);
            } catch (Exception e){
                throw new CommandException(Text.of("Unable to set ",fixKey," on Team", e));
            }
            return CommandResult.success();
        };
    }

    private BiConsumer<Text, Team> suffix(){
        return (fix, team) -> team.setSuffix(fix);
    }

    private BiConsumer<Text, Team> prefix(){
        return (fix, team) -> team.setPrefix(fix);
    }

    private Text deSerialize(String s){
        return TextSerializers.formattingCode('&').deserialize(s);
    }

    private Collection<String> getTeams(){
        Optional<Scoreboard> scoreboard = Sponge.getServer().getServerScoreboard();
        Scoreboard sc = scoreboard.get();
        Stream<String> x = sc.getTeams().stream().map(Team::getName);
        return x.collect(Collectors.toSet());
    }

    @Nullable
    private Team teamFromString(String s){
        Scoreboard sc = Sponge.getServer().getServerScoreboard().get();
        return sc.getTeam(s).orElse(null);
    }
}