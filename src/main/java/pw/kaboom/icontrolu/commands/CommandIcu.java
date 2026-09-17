package pw.kaboom.icontrolu.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import pw.kaboom.icontrolu.modules.Control;
import pw.kaboom.icontrolu.modules.PlayerControl;

import java.util.Optional;

import static io.papermc.paper.command.brigadier.Commands.*;
import static pw.kaboom.icontrolu.commands.arguments.PlayerOrUUIDArgumentType.getPlayer;
import static pw.kaboom.icontrolu.commands.arguments.PlayerOrUUIDArgumentType.playerOrUUID;

public final class CommandIcu {
    private static final SimpleCommandExceptionType EX_NOT_CONTROLLING =
            new SimpleCommandExceptionType(
                    new LiteralMessage("You are not controlling anyone at the moment")
            );
    private static final SimpleCommandExceptionType EX_TARGET_SELF =
            new SimpleCommandExceptionType(
                    new LiteralMessage("You are already controlling yourself")
            );
    private static final DynamicCommandExceptionType EX_ALREADY_IN_CONTROL =
            new DynamicCommandExceptionType(player ->
                    new LiteralMessage("You are already controlling \"" + player + "\"")
            );
    private static final DynamicCommandExceptionType EX_CONTROL_BY_OTHER =
            new DynamicCommandExceptionType(player ->
                    new LiteralMessage("Player \"" + player + "\" is already being controlled")
            );
    private static final SimpleCommandExceptionType EX_CANTSEE =
            new SimpleCommandExceptionType(
                    new LiteralMessage("You may not control this player")
            );
    private final Control controlModule;

    public CommandIcu(final Control controlModule) {
        this.controlModule = controlModule;
    }

    public void build(final LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .requires(restricted(src ->
                        src.getSender().hasPermission("icu.command")
                                && src.getSender() instanceof Player
                ))
                .then(literal("stop")
                        .executes(ctx -> {
                            final Player controller = getSender(ctx);
                            final Player target = controlModule.stopControl(controller)
                                    .orElseThrow(EX_NOT_CONTROLLING::create);
                            controller.sendMessage(
                                    Component.text("You are no longer controlling \"")
                                            .append(Component.text(target.getName()))
                                            .append(Component.text("\". You are invisible for "))
                                            .append(Component.text(
                                                    PlayerControl.getVisibilityDelay()
                                            ))
                                            .append(Component.text(" seconds."))
                            );
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(literal("control")
                        .then(argument("player", playerOrUUID())
                                .executes(ctx -> {
                                    final Player target = getPlayer(ctx, "player");
                                    final Player controller = getSender(ctx);

                                    // (obviously) you can't target yourself
                                    if (target == controller) {
                                        throw EX_TARGET_SELF.create();
                                    }

                                    // is target already controlled by sender
                                    final Optional<Player> otherTarget =
                                            controlModule.getTarget(controller);
                                    if (otherTarget.isPresent()) {
                                        throw EX_ALREADY_IN_CONTROL.create(
                                                otherTarget.get().getName()
                                        );
                                    }

                                    // is target controlled by some other person
                                    if (controlModule.getController(target).isPresent()) {
                                        throw EX_CONTROL_BY_OTHER.create(target.getName());
                                    }

                                    // can the controller see the target
                                    if (!controller.canSee(target)) {
                                        throw EX_CANTSEE.create();
                                    }

                                    // if all above checks pass, control the target
                                    controlModule.control(controller, target);
                                    controller.sendMessage(
                                            Component.text("You are now controlling \"")
                                                    .append(Component.text(target.getName()))
                                                    .append(Component.text("\""))
                                    );

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
    }

    private static Player getSender(final CommandContext<CommandSourceStack> ctx) {
        return (Player) ctx.getSource().getSender();
    }
}
