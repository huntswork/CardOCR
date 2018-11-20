package exocr.exocrengine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * description: 数据字典
 * create by kalu on 2018/11/19 10:04
 */
public final class EXOCRModel implements Parcelable {

    public String imgtype = "Preview";
    //recognition data
    public int type = 0;
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

    public EXOCRModel() {
    }

    ////////////////////////////////////////////////////////////

    /**
     * decode from stream
     * return the len of decoded data int the buf
     */
    public static final EXOCRModel decode(byte[] bResultBuf, int reslen) {
        byte code;
        int i, j, rdcount;
        String content = null;

        EXOCRModel idcard = new EXOCRModel();

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
            }

        } catch (IOException e) {
        }
    }

    @Override
    public String toString() {
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

    /**********************************************************************************************/

    /**
     * 是否解析成功
     *
     * @return
     */
    public final boolean isDecodeSucc() {
        return type == 1 || type == 2;
    }

    /**
     * 是否正面, 人像
     *
     * @return
     */
    public final boolean isDecodeFront() {
        return type == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imgtype);
        dest.writeInt(this.type);
        dest.writeString(this.cardnum);
        dest.writeString(this.name);
        dest.writeString(this.sex);
        dest.writeString(this.address);
        dest.writeString(this.nation);
        dest.writeString(this.birth);
        dest.writeString(this.office);
        dest.writeString(this.validdate);
        dest.writeInt(this.nColorType);
        dest.writeString(this.bitmapPath);
        dest.writeParcelable(this.rtIDNum, flags);
        dest.writeParcelable(this.rtName, flags);
        dest.writeParcelable(this.rtSex, flags);
        dest.writeParcelable(this.rtNation, flags);
        dest.writeParcelable(this.rtAddress, flags);
        dest.writeParcelable(this.rtFace, flags);
        dest.writeParcelable(this.rtOffice, flags);
        dest.writeParcelable(this.rtValid, flags);
    }

    protected EXOCRModel(Parcel in) {
        this.imgtype = in.readString();
        this.type = in.readInt();
        this.cardnum = in.readString();
        this.name = in.readString();
        this.sex = in.readString();
        this.address = in.readString();
        this.nation = in.readString();
        this.birth = in.readString();
        this.office = in.readString();
        this.validdate = in.readString();
        this.nColorType = in.readInt();
        this.bitmapPath = in.readString();
        this.rtIDNum = in.readParcelable(Rect.class.getClassLoader());
        this.rtName = in.readParcelable(Rect.class.getClassLoader());
        this.rtSex = in.readParcelable(Rect.class.getClassLoader());
        this.rtNation = in.readParcelable(Rect.class.getClassLoader());
        this.rtAddress = in.readParcelable(Rect.class.getClassLoader());
        this.rtFace = in.readParcelable(Rect.class.getClassLoader());
        this.rtOffice = in.readParcelable(Rect.class.getClassLoader());
        this.rtValid = in.readParcelable(Rect.class.getClassLoader());
    }

    public static final Creator<EXOCRModel> CREATOR = new Creator<EXOCRModel>() {
        @Override
        public EXOCRModel createFromParcel(Parcel source) {
            return new EXOCRModel(source);
        }

        @Override
        public EXOCRModel[] newArray(int size) {
            return new EXOCRModel[size];
        }
    };
}
