package dev.lazurite.quadz.common.data.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.util.Arrays;

public class Template {
    public static final Template EMPTY = new Template(new Settings("empty", "Empty", "empty", 0, 0, 0, 0, 0, 0, 0, 0, 0));

    private final Settings settings;
    private final JsonObject geo;
    private final JsonObject animation;
    private final byte[] texture;
    private final int originDistance;

    public Template(Settings settings, JsonObject geo, JsonObject animation, byte[] texture, int originDistance) throws RuntimeException {
        if (geo == null || animation == null || texture == null) {
            throw new RuntimeException("Quadcopter template is missing information.");
        }

        this.settings = settings;
        this.geo = geo;
        this.animation = animation;
        this.texture = texture;
        this.originDistance = originDistance;
    }

    private Template(Settings settings) {
        this.settings = settings;
        this.geo = null;
        this.animation = null;
        this.texture = null;
        this.originDistance = 0;
    }

    public PacketByteBuf serialize() {
        PacketByteBuf buf = PacketByteBufs.create();
        settings.serialize(buf);
        buf.writeString(geo.toString());
        buf.writeString(animation.toString());
        buf.writeByteArray(texture);
        buf.writeInt(originDistance + 1);
        return buf;
    }

    public static Template deserialize(PacketByteBuf buf) {
        return new Template(
            Settings.deserialize(buf),
            new JsonParser().parse(buf.readString(32767)).getAsJsonObject(),
            new JsonParser().parse(buf.readString(32767)).getAsJsonObject(),
            buf.readByteArray(),
            buf.readInt());
    }

    public String getId() {
        return this.settings.getId();
    }

    public Settings getSettings() {
        return this.settings;
    }

    public JsonObject getGeo() {
        return this.geo;
    }

    public JsonObject getAnimation() {
        return this.animation;
    }

    public byte[] getTexture() {
        return this.texture;
    }

    public int getOriginDistance() {
        return this.originDistance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Template) {
            Template template = (Template) obj;
            return template.settings.equals(settings) &&
                    template.animation.equals(animation) &&
                    template.geo.equals(geo) &&
                    Arrays.equals(template.texture, texture);
        }

        return false;
    }

    public static class Settings {
        private final String id;
        private final String name;
        private final String author;
        private final float width;
        private final float height;
        private final float cameraX;
        private final float cameraY;
        private final float mass;
        private final float dragCoefficient;
        private final float thrust;
        private final float thrustCurve;
        private final int cameraAngle;

        public Settings(String id, String name, String author, float width, float height, float cameraX, float cameraY, float mass, float dragCoefficient, float thrust, float thrustCurve, int cameraAngle) {
            this.id = id;
            this.name = name;
            this.author = author;
            this.width = width;
            this.height = height;
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.mass = mass;
            this.dragCoefficient = dragCoefficient;
            this.thrust = thrust;
            this.thrustCurve = thrustCurve;
            this.cameraAngle = cameraAngle;
        }

        public void serialize(PacketByteBuf buf) {
            buf.writeString(id);
            buf.writeString(name);
            buf.writeString(author);
            buf.writeFloat(width);
            buf.writeFloat(height);
            buf.writeFloat(cameraX);
            buf.writeFloat(cameraY);
            buf.writeFloat(mass);
            buf.writeFloat(dragCoefficient);
            buf.writeFloat(thrust);
            buf.writeFloat(thrustCurve);
            buf.writeInt(cameraAngle);
        }

        public static Settings deserialize(PacketByteBuf buf) {
            return new Settings(
                    buf.readString(32767),
                    buf.readString(32767),
                    buf.readString(32767),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readInt());
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String getAuthor() {
            return author;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public float getCameraX() {
            return cameraX;
        }

        public float getCameraY() {
            return cameraY;
        }

        public float getMass() {
            return mass;
        }

        public float getDragCoefficient() {
            return dragCoefficient;
        }

        public float getThrust() {
            return thrust;
        }

        public float getThrustCurve() {
            return thrustCurve;
        }

        public int getCameraAngle() {
            return cameraAngle;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Settings) {
                Settings settings = (Settings) obj;
                return settings.id.equals(id) &&
                        settings.author.equals(author) &&
                        settings.name.equals(name) &&
                        settings.cameraAngle == cameraAngle &&
                        settings.cameraX == cameraX &&
                        settings.cameraY == cameraY &&
                        settings.mass == mass &&
                        settings.width == width &&
                        settings.height == height &&
                        settings.dragCoefficient == dragCoefficient &&
                        settings.thrust == thrust &&
                        settings.thrustCurve == thrustCurve;
            }

            return false;
        }
    }
}
