package lk.tech.tgcontrollerbot.configuration;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(TelegramRuntimeHints.class)
public class TelegramRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader cl) {

        // --- KEY API RESPONSE ---
        register(hints, "org.telegram.telegrambots.meta.api.objects.ApiResponse");
        register(hints, "org.telegram.telegrambots.meta.api.objects.ResponseParameters");

        // --- BASIC UPDATE TREE ---
        register(hints, "org.telegram.telegrambots.meta.api.objects.Update");
        register(hints, "org.telegram.telegrambots.meta.api.objects.Message");
        register(hints, "org.telegram.telegrambots.meta.api.objects.Chat");
        register(hints, "org.telegram.telegrambots.meta.api.objects.User");
        register(hints, "org.telegram.telegrambots.meta.api.objects.MessageEntity");
        register(hints, "org.telegram.telegrambots.meta.api.objects.CallbackQuery");

        // --- FULL CHAT MEMBER TREE ---
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft");

        // --- DESERIALIZER + FACTORY ---
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.serialization.ChatMemberDeserializer");
        register(hints, "org.telegram.telegrambots.meta.api.objects.chatmember.serialization.ChatMemberFactory");

        // --- API METHODS ---
        register(hints, "org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodBoolean");
        register(hints, "org.telegram.telegrambots.meta.api.methods.send.SendMessage");
        register(hints, "org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText");
        register(hints, "org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook");
        register(hints, "org.telegram.telegrambots.meta.api.methods.updates.GetUpdates");

        // --- SESSION / ABSENDER ---
        register(hints, "org.telegram.telegrambots.meta.TelegramBotsApi");
        register(hints, "org.telegram.telegrambots.bots.DefaultBotOptions");
        register(hints, "org.telegram.telegrambots.updatesreceivers.DefaultBotSession");

        // --- PROXIES ---
        hints.proxies().registerJdkProxy(
                org.telegram.telegrambots.meta.generics.BotOptions.class
        );
    }

    private void register(RuntimeHints hints, String className) {
        try {
            Class<?> cls = Class.forName(className);
            hints.reflection().registerType(
                    cls,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.PUBLIC_FIELDS,
                    MemberCategory.DECLARED_FIELDS
            );
        } catch (Exception ignored) {}
    }
}
