package exocr.exocrengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class EXIDCardResult implements Parcelable {
    //是否显示头像		SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_FACEIMG_ID = false;
    //是否显示姓名	SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_NAME_ID = true;
    //是否显示性别		SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_SEX_ID = true;
    // 是否显示民族 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_NATION_ID = true;
    // 是否显示出生 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_BIRTH_ID = true;
    // 是否显示住址 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_ADDRESS_ID = true;
    // 是否显示证件号 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_CARDNUM_ID = true;
    // 是否显示签发机关 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_OFFICE_ID = true;
    // 是否显示有效期限 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_VALID_ID = true;
    // 是否显示正面全图 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_FRONTFULLIMG_ID = true;
    // 是否显示背面全图 SHOW_RESULT_ACTIVITY置true有效
    public static boolean SHOW_BACKFULLIMG_ID = true;

    public static boolean DOUBLE_CHECK = false;
    public final static boolean DISPLAY_LOGO = false;

    public String imgtype;
    //recognition data
    public int type;
    public String cardnum;
    public String name;
    public String sex;
    public String address;
    public String nation;
    public String birth;
    public String office;
    public String validdate;
    public int nColorType;   //1 color, 0 gray

    // public Bitmap stdCardIm = null;
    public String bitmapPath;
    public Rect rtIDNum;
    public Rect rtName;
    public Rect rtSex;
    public Rect rtNation;
    public Rect rtAddress;
    public Rect rtFace;
    public Rect rtOffice;
    public Rect rtValid;

    public EXIDCardResult() {
        type = 0;
        imgtype = "Preview";
    }

    // parcelable
    private EXIDCardResult(Parcel src) {
        type = src.readInt();
        cardnum = src.readString();
        birth = src.readString();
        name = src.readString();
        sex = src.readString();
        address = src.readString();
        nation = src.readString();
        office = src.readString();
        validdate = src.readString();
        bitmapPath = src.readString();
    }


    ////////////////////////////////////////////////////////////

    /**
     * decode from stream
     * return the len of decoded data int the buf
     */
    public static EXIDCardResult decode(byte[] bResultBuf, int reslen) {
        byte code;
        int i, j, rdcount;
        String content = null;

        EXIDCardResult idcard = new EXIDCardResult();

        ////////////////////////////////////////////////////////////
        //type
        rdcount = 0;
        idcard.type = bResultBuf[rdcount++];
        while (rdcount < reslen) {
            code = bResultBuf[rdcount++];
            i = 0;
            j = rdcount;
            while (rdcount < reslen) {
                i++;
                rdcount++;
                if (bResultBuf[rdcount] == 0x20) break;
            }
            try {
                content = new String(bResultBuf, j, i, "GBK");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (code == 0x21) {
                idcard.cardnum = content;
                String year = idcard.cardnum.substring(6, 10);
                String month = idcard.cardnum.substring(10, 12);
                String day = idcard.cardnum.substring(12, 14);
                idcard.birth = year + "-" + month + "-" + day;
            } else if (code == 0x22) {
                idcard.name = content;
            } else if (code == 0x23) {
                idcard.sex = content;
            } else if (code == 0x24) {
                idcard.nation = content;
            } else if (code == 0x25) {
                idcard.address = content;
            } else if (code == 0x26) {
                idcard.office = content;
            } else if (code == 0x27) {
                idcard.validdate = content;
            }
            rdcount++;
        }
        //is it correct, check it!
        if (idcard.type == 1 && (idcard.cardnum == null || idcard.name == null || idcard.nation == null || idcard.sex == null || idcard.address == null) ||
                idcard.type == 2 && (idcard.office == null || idcard.validdate == null) ||
                idcard.type == 0) {
            return null;
        } else {
            if (idcard.type == 1 && (idcard.cardnum.length() != 18 || idcard.name.length() < 2 || idcard.address.length() < 10)) {
                return null;
            }
        }
        return idcard;
    }

    //rects存放各个块的矩形，4个一组，这么做是为了将JNI的接口简单化
    // [0, 1, 2, 3]  idnum			issue
    // [4, 5, 6, 7]	 name			validate
    // [8, 9, 10,11] sex
    // [12,13,14,15] nation
    // [16,17,18,19] address
    // [20,21,22,23] face
    public void setRects(int[] rects) {
        if (type == 1) {
            rtIDNum = new Rect(rects[0], rects[1], rects[2], rects[3]);
            rtName = new Rect(rects[4], rects[5], rects[6], rects[7]);
            rtSex = new Rect(rects[8], rects[9], rects[10], rects[11]);
            rtNation = new Rect(rects[12], rects[13], rects[14], rects[15]);
            rtAddress = new Rect(rects[16], rects[17], rects[18], rects[19]);
            rtFace = new Rect(rects[20], rects[21], rects[22], rects[23]);
        } else if (type == 2) {
            rtOffice = new Rect(rects[0], rects[1], rects[2], rects[3]);
            rtValid = new Rect(rects[4], rects[5], rects[6], rects[7]);
        } else {
            return;
        }
    }

    public void SetViewType(String viewtype) {
        this.imgtype = viewtype;
    }

    public void SetColorType(int aColorType) {
        nColorType = aColorType;
    }

    public void SetBitmap(final Context context, Bitmap bitmap) {

        try {

            final String local;

            if (type == 1) {
                local = context.getFilesDir().getAbsolutePath() + "/card_front.jpg";
            } else if (type == 2) {
                local = context.getFilesDir().getAbsolutePath() + "/card_back.jpg";
            } else {
                return;
            }

            final File file = new File(local);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            this.bitmapPath = local;

            if (null != bitmap) {
                bitmap.recycle();
                bitmap = null;
            }

        } catch (IOException e) {
        }
    }

//    public Bitmap GetIDNumBitmap() {
//        if (stdCardIm == null) return null;
//        Bitmap bmIDNum = Bitmap.createBitmap(stdCardIm, rtIDNum.left, rtIDNum.top, rtIDNum.width(), rtIDNum.height());
//        return bmIDNum;
//    }
//
//    public Bitmap GetNameBitmap() {
//        if (stdCardIm == null) return null;
//        Bitmap bmIDNum = Bitmap.createBitmap(stdCardIm, rtName.left, rtName.top, rtName.width(), rtName.height());
//        return bmIDNum;
//    }
//
//    public Bitmap GetFaceBitmap() {
//        if (stdCardIm == null) return null;
//        Bitmap bmFace = Bitmap.createBitmap(stdCardIm, rtFace.left, rtFace.top, rtFace.width(), rtFace.height());
//        return bmFace;
//    }

    /**
     * @return raw text to show
     */
    public String getText() {
        String text = "\nVeiwType = " + imgtype;
        if (nColorType == 1) {
            text += "  类型:  彩色";
        } else {
            text += "  类型:  扫描";
        }
        if (type == 1) {
            text += "\nname:" + name;
            text += "\nnumber:" + cardnum;
            text += "\nsex:" + sex;
            text += "\nnation:" + nation;
            text += "\nbirth:" + birth;
            text += "\naddress:" + address;
            text += "\nbitmapPath:" + bitmapPath;
        } else if (type == 2) {
            text += "\noffice:" + office;
            text += "\nValDate:" + validdate;
            text += "\nbitmapPath:" + bitmapPath;
        }
        return text;
    }

    public static final Creator<EXIDCardResult> CREATOR = new Creator<EXIDCardResult>() {

        @Override
        public EXIDCardResult createFromParcel(Parcel source) {
            return new EXIDCardResult(source);
        }

        @Override
        public EXIDCardResult[] newArray(int size) {
            return new EXIDCardResult[size];
        }
    };


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        // TODO Auto-generated method stub
        arg0.writeInt(type);
        arg0.writeString(cardnum);
        arg0.writeString(birth);
        arg0.writeString(name);
        arg0.writeString(sex);
        arg0.writeString(address);
        arg0.writeString(nation);
        arg0.writeString(office);
        arg0.writeString(validdate);
        arg0.writeString(bitmapPath);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return name + "\t" + sex + "\t" + nation + "\t" + birth + "\n" + address + "\t" + cardnum + "\n" + office + "\t" + validdate;
    }
}
