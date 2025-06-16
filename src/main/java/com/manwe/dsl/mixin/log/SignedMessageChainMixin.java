package com.manwe.dsl.mixin.log;

import net.minecraft.network.chat.*;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.time.Instant;

import static com.manwe.dsl.DistributedServerLevels.LOGGER;

@Mixin(SignedMessageChain.class)
public class SignedMessageChainMixin {

    @Shadow
    @Nullable
    SignedMessageLink nextLink;

    @Shadow
    Instant lastTimeStamp;


    /**
     * @author
     * @reason
     */
    @Overwrite
    public SignedMessageChain.Decoder decoder(final ProfilePublicKey pPublicKey) {
        final SignatureValidator signaturevalidator = pPublicKey.createSignatureValidator();
        System.out.println("DECODER CALL");
        return new SignedMessageChain.Decoder() {
            @Override
            public PlayerChatMessage unpack(@Nullable MessageSignature signature, SignedMessageBody body) throws SignedMessageChain.DecodeException {
                if (signature == null) {
                    System.out.println("Signature == null");
                    throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.missingProfileKey"));
                } else if (pPublicKey.data().hasExpired()) {
                    System.out.println("data hasExpired");
                    throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.expiredProfileKey"));
                } else if (nextLink == null) {
                    System.out.println("nextLink == null");
                    throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.chain_broken"));
                } else if (body.timeStamp().isBefore(lastTimeStamp)) {
                    System.out.println("timeStamp is Before lastTimeStamp");
                    setChainBroken();
                    throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.out_of_order_chat"));
                } else {
                    System.out.println("success 1");
                    lastTimeStamp = body.timeStamp();
                    PlayerChatMessage message = new PlayerChatMessage(nextLink, signature, body, null, FilterMask.PASS_THROUGH);
                    if (!message.verify(signaturevalidator)) {
                        System.out.println("invalid signature");
                        setChainBroken();
                        throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.invalid_signature"));
                    } else {
                        if (message.hasExpiredServer(Instant.now())) {
                            LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", body.content());
                        }
                        System.out.println("success 2");
                        nextLink = nextLink.advance();
                        return message;
                    }
                }
            }

            @Override
            public void setChainBroken() {
                nextLink = null;
            }
        };
    }
}
