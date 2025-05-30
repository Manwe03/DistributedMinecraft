package com.manwe.dsl.connectionRouting;

import net.minecraft.network.FriendlyByteBuf;

public class TransientEntityInformation {
    final float headpYRot;
    final float headpXRot;

    public TransientEntityInformation(float pXRot, float pYRot) {
        this.headpXRot = pXRot;
        this.headpYRot = pYRot;
    }

    public TransientEntityInformation(FriendlyByteBuf buf){
        this.headpXRot = buf.readFloat();
        this.headpYRot = buf.readFloat();
    }

    public void write(FriendlyByteBuf buf){
        buf.writeFloat(this.headpXRot);
        buf.writeFloat(this.headpYRot);
    }

    public float getHeadXRot() {
        return headpXRot;
    }

    public float getHeadYRot() {
        return headpYRot;
    }
}
