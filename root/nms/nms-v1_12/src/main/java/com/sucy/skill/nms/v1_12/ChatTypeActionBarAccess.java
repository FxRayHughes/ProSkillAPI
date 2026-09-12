package com.sucy.skill.nms.v1_12;


import java.lang.reflect.Constructor;

/**
 * Action bar packet for 1.12, where the slot is a {@code ChatMessageType}.
 *
 * <p>The enum value is obtained through the packet class's own {@code a(byte)}
 * lookup rather than by constant name, because the constant names are
 * obfuscated while the numeric slot 2 is part of the protocol.</p>
 */
public class ChatTypeActionBarAccess extends ActionBarAccess {
    @Override
    protected Constructor<?> resolvePacketConstructor(Class<?> chatPacket, Class<?> chatBase)
            throws Exception {
        Class<?> chatMessageType = LegacyReflection.nmsClass("ChatMessageType");
        messageType = chatMessageType.getMethod("a", byte.class).invoke(null, (byte) 2);
        return chatPacket.getConstructor(chatBase, chatMessageType);
    }
}
