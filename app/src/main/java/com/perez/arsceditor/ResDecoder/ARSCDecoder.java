/**
 *  Copyright 2011 Ryszard Wi?niewski <brut.alll@gmail.com>
 *  Modified Copyright 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.perez.arsceditor.ResDecoder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import android.util.TypedValue;
import com.perez.arsceditor.ResDecoder.IO.Duo;
import com.perez.arsceditor.ResDecoder.IO.LEDataInputStream;
import com.perez.arsceditor.ResDecoder.IO.LEDataOutputStream;
import com.perez.arsceditor.ResDecoder.data.ResConfigFlags;
import com.perez.arsceditor.ResDecoder.data.ResID;
import com.perez.arsceditor.ResDecoder.data.ResPackage;
import com.perez.arsceditor.ResDecoder.data.ResResSpec;
import com.perez.arsceditor.ResDecoder.data.ResResource;
import com.perez.arsceditor.ResDecoder.data.ResTable;
import com.perez.arsceditor.ResDecoder.data.ResType;
import com.perez.arsceditor.ResDecoder.data.ResTypeSpec;
import com.perez.arsceditor.ResDecoder.data.value.ResBagValue;
import com.perez.arsceditor.ResDecoder.data.value.ResBoolValue;
import com.perez.arsceditor.ResDecoder.data.value.ResFileValue;
import com.perez.arsceditor.ResDecoder.data.value.ResScalarValue;
import com.perez.arsceditor.ResDecoder.data.value.ResStringValue;
import com.perez.arsceditor.ResDecoder.data.value.ResValue;
import com.perez.arsceditor.ResDecoder.data.value.ResValueFactory;

/**
 * @author Ryszard Wi?niewski <brut.alll@gmail.com>
 * @author com.perezHai
 */
public class ARSCDecoder {

    public static class ARSCData {

        private final ResPackage[] mPackages;

        private final ResTable mResTable;

        public ARSCData(ResPackage[] packages, ResTable resTable) {
            mPackages = packages;
            mResTable = resTable;
        }

        public int findPackageWithMostResSpecs() {
            int count = -1;
            int id = 0;
            // set starting point to package id 0.
            count = mPackages[0].getResSpecCount();
            // loop through packages looking for largest
            for(int i = 0; i < mPackages.length; i++) {
                if(mPackages[i].getResSpecCount() >= count) {
                    count = mPackages[i].getResSpecCount();
                    id = i;
                }
            }
            return id;
        }

        public ResPackage getOnePackage() throws IOException {
            if(mPackages.length <= 0)
                throw new IOException("Arsc file contains zero packages");
            else if(mPackages.length != 1) {
                int id = findPackageWithMostResSpecs();
                LOGGER.info("Arsc file contains multiple packages. Using package " + mPackages[id].getName()
                            + " as default.");
                return mPackages[id];
            }
            return mPackages[0];
        }

        public ResPackage[] getPackages() {
            return mPackages;
        }

        public ResTable getResTable() {
            return mResTable;
        }
    }

    public static class Header {
        public final static short TYPE_NONE = -1, TYPE_TABLE = 0x0002, TYPE_PACKAGE = 0x0200, TYPE_TYPE = 0x0201,
                                  TYPE_SPEC_TYPE = 0x0202, TYPE_LIBRARY = 0x0203;

        public static Header read(LEDataInputStream in) throws IOException {
            short type;
            try {
                type = in.readShort();
            } catch(EOFException ex) {
                return new Header(TYPE_NONE, 0, (byte) 0, (byte) 0);
            }
            byte byte1 = in.readByte();
            byte byte2 = in.readByte();
            int chunkSize = in.readInt();
            return new Header(type, chunkSize, byte1, byte2);
        }

        
        public final byte byte1;
        
        public final byte byte2;

        // chunkSize
        public final int chunkSize;

        
        public final short type;

        public Header(short type, int size, byte byte1, byte byte2) {
            this.type = type;
            this.chunkSize = size;
            this.byte1 = byte1;
            this.byte2 = byte2;
        }
    }

    private final static short ENTRY_FLAG_COMPLEX = 0x0001;

    private static final int KNOWN_CONFIG_BYTES = 52;

    private static final Logger LOGGER = Logger.getLogger(ARSCDecoder.class.getName());

    private Header mHeader;

    
    private LEDataInputStream mIn;

    private final boolean mKeepBroken;

    private boolean[] mMissingResSpecs;

    public ResPackage mPkg;

    private int mResId;

    private final ResTable mResTable;

    private HashMap<Byte, ResTypeSpec> mResTypeSpecs = new HashMap<>();

    
    private StringBlock mSpecNames;

    
    public StringBlock mTableStrings;

    
    private ResType mType;

    
    private StringBlock mTypeNames;

    private ResTypeSpec mTypeSpec;

    
    private int packageCount;

    
    private String packageName;

    
    private int size1;

    
    private int size2;

    
    private Header stringsHeader;

    /**
     *  arscStream arsc resTable  keepBroken 
     */
    public ARSCDecoder(InputStream arscStream, ResTable resTable, boolean keepBroken) throws IOException {
        
        mIn = new LEDataInputStream(arscStream);
        mResTable = resTable;
        mKeepBroken = keepBroken;
    }

    private void addMissingResSpecs() throws IOException {
        int resId = mResId & 0xffff0000;
        for(int i = 0; i < mMissingResSpecs.length; i++) {
            if(!mMissingResSpecs[i])
                continue;
            ResResSpec spec = new ResResSpec(new ResID(resId | i), String.format("APKTOOL_DUMMY_%04x", i), mPkg,
                                             mTypeSpec);
            // If we already have this resID dont add it again.
            if(!mPkg.hasResSpec(new ResID(resId | i))) {
                mPkg.addResSpec(spec);
                mTypeSpec.addResSpec(spec);
                if(mType == null)
                    mType = mPkg.getOrCreateConfig(new ResConfigFlags());
                ResValue value = new ResBoolValue(false, 0, null);
                ResResource res = new ResResource(mType, spec, value);
                mPkg.addResource(res);
                mType.addResource(res);
                spec.addResource(res);
            }
        }
    }

    private void addTypeSpec(ResTypeSpec resTypeSpec) {
        mResTypeSpecs.put(resTypeSpec.getId(), resTypeSpec);
    }

    private void checkChunkType(int expectedType) throws IOException {
        if(mHeader.type != expectedType) {
            /*
             * throw new IOException(String.format(
             * "Invalid chunk type: expected=0x%08x, got=0x%08x", expectedType,
             * mHeader.type));
             */
        }
    }

    /**
     * arsc  os  rndChar 
     */
    public void CloneArsc(OutputStream os, String rndChar, boolean close) throws IOException {
        int size1 = mIn.available();
        if(size1 == 1) {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            byte buffer[] = new byte[2048];
            int count;
            while((count = mIn.read(buffer, 0, 2048)) != -1)
                bOut.write(buffer, 0, count);
            bOut.close();
            ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
            mIn = new LEDataInputStream(new BufferedInputStream(bIn));
            size1 = mIn.available();
        }
        mIn.mark(size1);
        
        nextChunk();
        checkChunkType(Header.TYPE_TABLE);
        
        mIn.skipInt();
        
        StringBlock.read(mIn);
        
        nextChunk();
        
        checkChunkType(Header.TYPE_PACKAGE);
        // id
        mIn.skipInt();
        int size2 = mIn.available();
        
        packageName = mIn.readNulEndedString(128, true);
        
        mIn.reset();
        
        LEDataOutputStream lmOut = new LEDataOutputStream(os);
        for(int i = 0; i < size1 - size2; i++)
            lmOut.writeByte(mIn.readByte());
        
        mIn.readNulEndedString(128, true);
        
        lmOut.writeNulEndedString(packageName.substring(0, packageName.length() - 1) + rndChar);
        byte[] buffer = new byte[1024];
        int count;
        
        while((count = mIn.read(buffer, 0, buffer.length)) != -1)
            lmOut.writeFully(buffer, 0, count);
        if(close)
            lmOut.close();
    }

    public ARSCData decode(ARSCDecoder decoder, InputStream arscStream, boolean findFlagsOffsets, boolean keepBroken)
    throws IOException {
        return decode(decoder, arscStream, findFlagsOffsets, keepBroken, new ResTable());
    }

    public ARSCData decode(ARSCDecoder decoder, InputStream arscStream, boolean findFlagsOffsets, boolean keepBroken,
                           ResTable resTable) throws IOException {
        ResPackage[] pkgs = decoder.readTable();
        return new ARSCData(pkgs, resTable);
    }

    /**
     * 
     **/
    public String getPackageName() {
        return packageName;
    }

    private Header nextChunk() throws IOException {
        return mHeader = Header.read(mIn);
    }

    private ResBagValue readComplexEntry() throws IOException {
        int parent = mIn.readInt();
        int count = mIn.readInt();
        ResValueFactory factory = mPkg.getValueFactory();
        @SuppressWarnings("unchecked")
        Duo<Integer, ResScalarValue>[] items = new Duo[count];
        for(int i = 0; i < count; i++)
            items[i] = new Duo<Integer, ResScalarValue>(mIn.readInt(), (ResScalarValue) readValue());
        return factory.bagFactory(parent, items);
    }

    private ResConfigFlags readConfigFlags() throws IOException {
        int size = mIn.readInt();
        int read = 28;
        if(size < 28)
            throw new IOException("Config size < 28");
        boolean isInvalid = false;
        short mcc = mIn.readShort();
        short mnc = mIn.readShort();
        char[] language = this.unpackLanguageOrRegion(mIn.readByte(), mIn.readByte(), 'a');
        char[] country = this.unpackLanguageOrRegion(mIn.readByte(), mIn.readByte(), '0');
        byte orientation = mIn.readByte();
        byte touchscreen = mIn.readByte();
        int density = mIn.readShort();
        byte keyboard = mIn.readByte();
        byte navigation = mIn.readByte();
        byte inputFlags = mIn.readByte();
        /* inputPad0 */mIn.skipBytes(1);
        short screenWidth = mIn.readShort();
        short screenHeight = mIn.readShort();
        short sdkVersion = mIn.readShort();
        /* minorVersion, now must always be 0 */mIn.skipBytes(2);
        byte screenLayout = 0;
        byte uiMode = 0;
        short smallestScreenWidthDp = 0;
        if(size >= 32) {
            screenLayout = mIn.readByte();
            uiMode = mIn.readByte();
            smallestScreenWidthDp = mIn.readShort();
            read = 32;
        }
        short screenWidthDp = 0;
        short screenHeightDp = 0;
        if(size >= 36) {
            screenWidthDp = mIn.readShort();
            screenHeightDp = mIn.readShort();
            read = 36;
        }
        char[] localeScript = null;
        char[] localeVariant = null;
        if(size >= 48) {
            localeScript = readScriptOrVariantChar(4).toCharArray();
            localeVariant = readScriptOrVariantChar(8).toCharArray();
            read = 48;
        }
        byte screenLayout2 = 0;
        if(size >= 52) {
            screenLayout2 = mIn.readByte();
            mIn.skipBytes(3); // reserved padding
            read = 52;
        }
        int exceedingSize = size - KNOWN_CONFIG_BYTES;
        if(exceedingSize > 0) {
            byte[] buf = new byte[exceedingSize];
            read += exceedingSize;
            mIn.readFully(buf);
            BigInteger exceedingBI = new BigInteger(1, buf);
            if(exceedingBI.equals(BigInteger.ZERO)) {
                LOGGER.fine(
                    String.format("Config flags size > %d, but exceeding bytes are all zero, so it should be ok.",
                                  KNOWN_CONFIG_BYTES));
            } else {
                LOGGER.warning(String.format("Config flags size > %d. Exceeding bytes: 0x%X.", KNOWN_CONFIG_BYTES,
                                             exceedingBI));
                isInvalid = true;
            }
        }
        int remainingSize = size - read;
        if(remainingSize > 0)
            mIn.skipBytes(remainingSize);
        return new ResConfigFlags(mcc, mnc, language, country, orientation, touchscreen, density, keyboard, navigation,
                                  inputFlags, screenWidth, screenHeight, sdkVersion, screenLayout, uiMode, smallestScreenWidthDp,
                                  screenWidthDp, screenHeightDp, localeScript, localeVariant, screenLayout2, isInvalid, size);
    }

    private void readEntry() throws IOException {
        /* size */mIn.skipBytes(2);
        short flags = mIn.readShort();
        int specNamesId = mIn.readInt();
        ResValue value = (flags & ENTRY_FLAG_COMPLEX) == 0 ? readValue() : readComplexEntry();
        if(mTypeSpec.isString() && value instanceof ResFileValue)
            value = new ResStringValue(value.toString(), ((ResFileValue) value).getRawIntValue());
        if(mType == null)
            return;
        ResID resId = new ResID(mResId);
        ResResSpec spec;
        if(mPkg.hasResSpec(resId)) {
            spec = mPkg.getResSpec(resId);
            if(spec.isDummyResSpec()) {
                removeResSpec(spec);
                spec = new ResResSpec(resId, mSpecNames.getString(specNamesId), mPkg, mTypeSpec);
                mPkg.addResSpec(spec);
                mTypeSpec.addResSpec(spec);
            }
        } else {
            spec = new ResResSpec(resId, mSpecNames.getString(specNamesId), mPkg, mTypeSpec);
            mPkg.addResSpec(spec);
            mTypeSpec.addResSpec(spec);
        }
        ResResource res = new ResResource(mType, spec, value);
        mType.addResource(res);
        spec.addResource(res);
        mPkg.addResource(res);
    }

    private void readLibraryType() throws IOException {
        checkChunkType(Header.TYPE_LIBRARY);
        int libraryCount = mIn.readInt();
        int packageId;
        String packageName;
        for(int i = 0; i < libraryCount; i++) {
            packageId = mIn.readInt();
            packageName = mIn.readNulEndedString(128, true);
            LOGGER.info(String.format("Decoding Shared Library (%s), pkgId: %d", packageName, packageId));
        }
        while(nextChunk().type == Header.TYPE_TYPE)
            readTableTypeSpec();
    }

    
    private ResPackage readPackage() throws IOException {
        
        checkChunkType(Header.TYPE_PACKAGE);
        // id
        int id = (byte) mIn.readInt();
        if(id == 0) {
            
            
            // APKTOOLpackageid。
            
            id = 2;
            if(mResTable.getPackageOriginal() == null && mResTable.getPackageRenamed() == null)
                mResTable.setSharedLibrary(true);
        }
        
        packageName = mIn.readNulEndedString(128, true);
        /* typeNameStrings */
        mIn.skipInt();
        /* typeNameCount */
        mIn.skipInt();
        /* specNameStrings */
        mIn.skipInt();
        /* specNameCount */
        mIn.skipInt();
        
        mTypeNames = StringBlock.read(mIn);
        
        mSpecNames = StringBlock.read(mIn);
        mResId = id << 24;
        
        mPkg = new ResPackage(mResTable, id, packageName);
        
        nextChunk();
        
        while(mHeader.type == Header.TYPE_LIBRARY) {
            
            readLibraryType();
        }
        //
        while(mHeader.type == Header.TYPE_SPEC_TYPE)
            readTableTypeSpec();
        return mPkg;
    }

    private String readScriptOrVariantChar(int length) throws IOException {
        StringBuilder string = new StringBuilder(16);
        while(length-- != 0) {
            byte ch = mIn.readByte();
            if(ch == 0)
                break;
            string.append((char) ch);
        }
        mIn.skipBytes(length);
        return string.toString();
    }

    private ResTypeSpec readSingleTableTypeSpec() throws IOException {
        checkChunkType(Header.TYPE_TYPE);
        byte id = mIn.readByte();
        mIn.skipBytes(3);
        int entryCount = mIn.readInt();
        /* flags */mIn.skipBytes(entryCount * 4);
        mTypeSpec = new ResTypeSpec(mTypeNames.getString(id - 1), mResTable, mPkg, id, entryCount);
        mPkg.addType(mTypeSpec);
        return mTypeSpec;
    }

    public ResPackage[] readTable() throws IOException {
        
        size1 = mIn.available();
        if(size1 == 1) {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            byte buffer[] = new byte[2048];
            int count;
            while((count = mIn.read(buffer, 0, 2048)) != -1)
                bOut.write(buffer, 0, count);
            bOut.close();
            ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
            mIn = new LEDataInputStream(new BufferedInputStream(bIn));
            size1 = mIn.available();
        }
        System.out.println("size1==" + size1);
        mIn.mark(size1);
        
        stringsHeader = nextChunk();
        
        checkChunkType(Header.TYPE_TABLE);
        
        packageCount = mIn.readInt();
        
        mTableStrings = StringBlock.read(mIn);
        
        size2 = mIn.available();
        // System.out.println("size2==" + size2);
        
        ResPackage[] packages = new ResPackage[packageCount];
        
        nextChunk();
        
        for(int i = 0; i < packageCount; i++)
            packages[i] = readPackage();
        
        return packages;
    }

    private ResType readTableType() throws IOException {
        checkChunkType(Header.TYPE_TYPE);
        byte typeId = mIn.readByte();
        if(mResTypeSpecs.containsKey(typeId)) {
            mResId = (0xff000000 & mResId) | mResTypeSpecs.get(typeId).getId() << 16;
            mTypeSpec = mResTypeSpecs.get(typeId);
        }
        /* res0, res1 */mIn.skipBytes(3);
        int entryCount = mIn.readInt();
        /* entriesStart */mIn.skipInt();
        mMissingResSpecs = new boolean[entryCount];
        Arrays.fill(mMissingResSpecs, true);
        ResConfigFlags flags = readConfigFlags();
        int[] entryOffsets = mIn.readIntArray(entryCount);
        if(flags.isInvalid) {
            String resName = mTypeSpec.getName() + flags.getQualifiers();
            if(mKeepBroken)
                LOGGER.warning("Invalid config flags detected: " + resName);
            else
                LOGGER.warning("Invalid config flags detected. Dropping resources: " + resName);
        }
        mType = flags.isInvalid && !mKeepBroken ? null : mPkg.getOrCreateConfig(flags);
        for(int i = 0; i < entryOffsets.length; i++) {
            if(entryOffsets[i] != -1) {
                mMissingResSpecs[i] = false;
                mResId = (mResId & 0xffff0000) | i;
                readEntry();
            }
        }
        return mType;
    }

    private ResType readTableTypeSpec() throws IOException {
        mTypeSpec = readSingleTableTypeSpec();
        addTypeSpec(mTypeSpec);
        int type = nextChunk().type;
        ResTypeSpec resTypeSpec;
        while(type == Header.TYPE_SPEC_TYPE) {
            resTypeSpec = readSingleTableTypeSpec();
            addTypeSpec(resTypeSpec);
            type = nextChunk().type;
        }
        while(type == Header.TYPE_TYPE) {
            readTableType();
            type = nextChunk().type;
            addMissingResSpecs();
        }
        return mType;
    }

    private ResValue readValue() throws IOException {
        /* size */
        mIn.skipCheckShort((short) 8);
        /* zero */
        mIn.skipCheckByte((byte) 0);
        byte type = mIn.readByte();
        int data = mIn.readInt();
        return type == TypedValue.TYPE_STRING ? mPkg.getValueFactory().factory(mTableStrings.getString(data), data)
               : mPkg.getValueFactory().factory(type, data, null);
    }

    private void removeResSpec(ResResSpec spec) throws IOException {
        if(mPkg.hasResSpec(spec.getId())) {
            mPkg.removeResSpec(spec);
            mTypeSpec.removeResSpec(spec);
        }
    }

    private char[] unpackLanguageOrRegion(byte in0, byte in1, char base) throws IOException {
        // check high bit, if so we have a packed 3 letter code
        if(((in0 >> 7) & 1) == 1) {
            int first = in1 & 0x1F;
            int second = ((in1 & 0xE0) >> 5) + ((in0 & 0x03) << 3);
            int third = (in0 & 0x7C) >> 2;
            // since this function handles languages & regions, we add the
            // value(s) to the base char
            // which is usually 'a' or '0' depending on language or region.
            return new char[] { (char)(first + base), (char)(second + base), (char)(third + base) };
        }
        return new char[] { (char) in0, (char) in1 };
    }

    /***
     * ARSC
     *
     * @throws IOException
     */
    public void write(OutputStream os) throws IOException {
        
        LEDataOutputStream lmOut = new LEDataOutputStream(os);
        
        ByteArrayOutputStream mStrings = mTableStrings.writeString(mTableStrings.getList());
        lmOut.writeShort(stringsHeader.type);
        
        lmOut.writeByte(stringsHeader.byte1);
        lmOut.writeByte(stringsHeader.byte2);
        
        lmOut.writeInt(stringsHeader.chunkSize + (mStrings.size() - mTableStrings.m_strings.length));
        
        lmOut.writeInt(packageCount);
        
        mTableStrings.writeFully(lmOut, mStrings);
        
        mIn.reset();
        mIn.skipBytes(size1 - size2);
        byte[] buffer = new byte[1024];
        int count;
        
        while((count = mIn.read(buffer, 0, buffer.length)) != -1)
            lmOut.writeFully(buffer, 0, count);
        lmOut.close();
    }

    /**
     * arsc os  stringlist_src  stringlist_tar
     * 
     ***/
    public void write(OutputStream os, List<String> stringlist_src, List<String> stringlist_tar) throws IOException {
        int index = 0;
        
        for(String str : stringlist_src) {
            String tar = stringlist_tar.get(index);
            mTableStrings.sortStringBlock(str, tar);
            index++;
        }
        write(os);
    }
}
