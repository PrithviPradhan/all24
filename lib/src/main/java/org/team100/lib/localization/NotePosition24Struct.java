package org.team100.lib.localization;

import java.nio.ByteBuffer;

import edu.wpi.first.util.struct.Struct;

public class NotePosition24Struct implements Struct<NotePosition24> {

    @Override
    public Class<NotePosition24> getTypeClass() {
        return NotePosition24.class;
    }

    @Override
    public String getTypeString() {
        return "struct:NotePosition24";
    }

    @Override
    public int getSize() {
        return kSizeInt32 + kSizeInt32;
    }

    @Override
    public String getSchema() {
        return "int x,int y";
    }

    @Override
    public NotePosition24 unpack(ByteBuffer bb) {
        int x = bb.getInt();
        int y = bb.getInt();
        return new NotePosition24(x,y);
    }

    @Override
    public void pack(ByteBuffer bb, NotePosition24 value) {
        bb.putInt(value.getX());
        bb.putInt(value.getY());
    }

}
